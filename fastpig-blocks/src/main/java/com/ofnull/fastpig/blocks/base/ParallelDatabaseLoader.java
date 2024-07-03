package com.ofnull.fastpig.blocks.base;

import com.google.common.hash.Hashing;
import com.ofnull.fastpig.common.attach.PartitionAttached;
import com.ofnull.fastpig.common.attach.ServerProcessAttached;
import com.ofnull.fastpig.common.attach.WriteOperateAttached;
import com.ofnull.fastpig.common.finder.ServiceLoaderHelper;
import com.ofnull.fastpig.common.job.JobConfiguration;
import com.ofnull.fastpig.common.metainfo.TableConfig;
import com.ofnull.fastpig.common.metainfo.TableMetaLoader;
import com.ofnull.fastpig.common.utils.JsonUtil;
import com.ofnull.fastpig.spi.bulkloader.BaseBulkLoader;
import com.ofnull.fastpig.spi.bulkloader.ExecBatch;
import com.ofnull.fastpig.spi.bulkloader.IPreparedWrite;
import com.ofnull.fastpig.spi.jdbc.IJdbcConnection;
import com.ofnull.fastpig.spi.metainfo.TableMetaInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.concurrent.ExecutorThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.ofnull.fastpig.common.attach.ServerProcessAttached.SERVER_PROCESS_ATTACHED;

/**
 * @author ofnull
 * @date 2024/6/17
 */
public class ParallelDatabaseLoader extends ProcessFunction<Map<String, Object>, Map<String, Object>> implements CheckpointedFunction {
    public static final Logger LOG = LoggerFactory.getLogger(ParallelDatabaseLoader.class);
    private StopWatch stopWatch;
    private final String tableConfigKey;
    private final String dbWriteConfigKey;
    private IJdbcConnection jdbcConnection;
    private TableConfig tableConfig;
    private Config dbWriteConfig;
    private TableMetaInfo tableMetaInfo;

    private transient BaseBulkLoader bulkLoader;
    private transient Map<Integer, ExecBatch> tablePartitionedBatch;
    private transient ThreadPoolExecutor executorService;

    private final AtomicLong recordCounter = new AtomicLong(0);
    private final AtomicLong lastFlushTime = new AtomicLong(0);

    public ParallelDatabaseLoader(String tableConfigKey, String dbWriteConfigKey, IJdbcConnection jdbcConnection) {
        this.tableConfigKey = tableConfigKey;
        this.dbWriteConfigKey = dbWriteConfigKey;
        this.jdbcConnection = jdbcConnection;

    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        tablePartitionedBatch = new HashMap<>();
        JobConfiguration job = (JobConfiguration) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        this.tableConfig = job.convertConfigToEntity(tableConfigKey, TableConfig.class);
        this.dbWriteConfig = job.convertConfigToEntity(dbWriteConfigKey, Config.class);
        this.tableMetaInfo = new TableMetaLoader(jdbcConnection, tableConfig).loader();
        this.executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(dbWriteConfig.getParallelism(), new ExecutorThreadFactory("parallelDatabaseLoader-poll"));
        this.stopWatch = new StopWatch();
        refresh();
    }

    private void refresh() throws Exception {
        BaseBulkLoader tempBulkLoader = this.bulkLoader;
        this.bulkLoader = ServiceLoaderHelper.loadServices(BaseBulkLoader.class, tableMetaInfo.getDatasourceType());
        this.bulkLoader.init(getRuntimeContext().getIndexOfThisSubtask(), tableMetaInfo);
        if (tempBulkLoader != null) {
            tempBulkLoader.close();
        }
    }


    @Override
    public void processElement(Map<String, Object> event, ProcessFunction<Map<String, Object>, Map<String, Object>>.Context context, Collector<Map<String, Object>> collector) throws Exception {
        ServerProcessAttached processAttached = JsonUtil.convert(event.get(SERVER_PROCESS_ATTACHED), ServerProcessAttached.class);
        if (processAttached == null) {
            throw new RuntimeException("processAttached missing!");
        }
        WriteOperateAttached operateAttached = processAttached.getWriteOperateAttached();
        if (operateAttached == null || operateAttached.getOperateEnum() == null) {
            throw new RuntimeException("operateAttached missing!");
        }
        int partition = partition(processAttached.getPartitionAttached(), dbWriteConfig.getParallelism());
        IPreparedWrite preparedWrite = bulkLoader.prepareWrite(partition, operateAttached.getOperateEnum(), event, tableMetaInfo.getColumnMetadata());
        if (Objects.nonNull(preparedWrite) && preparedWrite.isNotEmpty()) {
            ExecBatch execBatch = tablePartitionedBatch.get(partition);
            if (execBatch == null) {
                execBatch = new ExecBatch();
                tablePartitionedBatch.put(partition, execBatch);
            }
            execBatch.getDataList().add(preparedWrite.getData());
        }
        recordCounter.incrementAndGet();
        checkFlush();
    }

    private int partition(PartitionAttached partitionAttached, int maxPartition) {
        if (partitionAttached != null) {
            return Math.abs(Hashing.murmur3_128().newHasher().putInt(partitionAttached.getPartition()).hash().asInt() % maxPartition);
        } else {
            return RandomUtils.nextInt(0, maxPartition);
        }
    }


    private void checkFlush() throws Exception {
        if (recordCounter.get() >= dbWriteConfig.getMaxBatchSize() || System.currentTimeMillis() - lastFlushTime.get() > dbWriteConfig.getMaxBlockTime()) {
            doFlushWithRetry();
        }
    }

    private void doFlushWithRetry() throws Exception {
        for (Integer i = 0; i < dbWriteConfig.getFlushRetry(); i++) {
            try {
                doFlush();
                break;
            } catch (Exception e) {
                if (i >= dbWriteConfig.getFlushRetry()) {
                    throw new IOException(e);
                }
                LOG.warn("Flush Database failed, catalog:{}, table:{} retry:{}", tableMetaInfo.getCatalog(), tableMetaInfo.getTable(), i);
                TimeUnit.MILLISECONDS.sleep(10 * i);
                refresh();
            }
        }
    }

    private void doFlush() throws Exception {
        stopWatch.reset();
        if (MapUtils.isEmpty(tablePartitionedBatch)) {
            return;
        }
        List<FlushTask> flushTasks = new ArrayList<>();
        for (Map.Entry<Integer, ExecBatch> entry : tablePartitionedBatch.entrySet()) {
            flushTasks.add(new FlushTask(entry.getKey(), entry.getValue(), bulkLoader));
        }
        stopWatch.start();
        List<Future<List<Map<String, Object>>>> futures = executorService.invokeAll(flushTasks, dbWriteConfig.getBatchTimeout(), TimeUnit.MILLISECONDS);
        for (Future<List<Map<String, Object>>> future : futures) {
            future.get(dbWriteConfig.getBatchTimeout() * 2, TimeUnit.MILLISECONDS);
        }
        stopWatch.stop();
        LOG.info("[{}.{}] Flush {} records, using: {}ms", tableMetaInfo.getCatalog(), tableMetaInfo.getTable(), recordCounter.get(), stopWatch.getTime(TimeUnit.MILLISECONDS));
        lastFlushTime.set(System.currentTimeMillis());
        recordCounter.set(0);
        this.tablePartitionedBatch = new HashMap<>();
        this.tablePartitionedBatch.clear();
    }


    @Override
    public void snapshotState(FunctionSnapshotContext functionSnapshotContext) throws Exception {
        doFlushWithRetry();
    }

    @Override
    public void initializeState(FunctionInitializationContext functionInitializationContext) throws Exception {

    }

    @Override
    public void close() throws Exception {
        super.close();
        if (bulkLoader != null) {
            bulkLoader.close();
        }
    }

    class FlushTask implements Callable<List<Map<String, Object>>> {
        private final int partition;
        private final ExecBatch execBatch;
        private final BaseBulkLoader bulkLoader;

        public FlushTask(int partition, ExecBatch execBatch, BaseBulkLoader bulkLoader) {
            this.partition = partition;
            this.execBatch = execBatch;
            this.bulkLoader = bulkLoader;
        }

        @Override
        public List<Map<String, Object>> call() throws Exception {
            if (CollectionUtils.isEmpty(execBatch.getDataList())) {
                return Collections.emptyList();
            }
            try {
                bulkLoader.doBatchWrite(partition, execBatch);
            } catch (Exception e) {
                LOG.error("FlushTask Executor failed!", e);
                throw e;
            }
            return Collections.emptyList();
        }
    }

    public static class Config {
        @NotNull(message = "parallelism > 0 ")
        private Integer parallelism = 2;
        @NotNull(message = "parallelism > 0 ")
        private Integer maxBatchSize = 1000;
        private Integer maxBlockTime = 10000;
        private Long batchTimeout = 60_000L;
        private Integer flushRetry = 3;
        private Boolean flushOnSnapshot = true;

        public Integer getParallelism() {
            return parallelism;
        }

        public void setParallelism(Integer parallelism) {
            this.parallelism = parallelism;
        }

        public Integer getMaxBatchSize() {
            return maxBatchSize;
        }

        public void setMaxBatchSize(Integer maxBatchSize) {
            this.maxBatchSize = maxBatchSize;
        }

        public Integer getMaxBlockTime() {
            return maxBlockTime;
        }

        public void setMaxBlockTime(Integer maxBlockTime) {
            this.maxBlockTime = maxBlockTime;
        }

        public Long getBatchTimeout() {
            return batchTimeout;
        }

        public void setBatchTimeout(Long batchTimeout) {
            this.batchTimeout = batchTimeout;
        }

        public Boolean getFlushOnSnapshot() {
            return flushOnSnapshot;
        }

        public void setFlushOnSnapshot(Boolean flushOnSnapshot) {
            this.flushOnSnapshot = flushOnSnapshot;
        }

        public Integer getFlushRetry() {
            return flushRetry;
        }

        public void setFlushRetry(Integer flushRetry) {
            this.flushRetry = flushRetry;
        }
    }
}
