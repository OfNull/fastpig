package com.ofnull.fastpig.blocks.base;

import com.ofnull.fastpig.common.finder.LoadedServices;
import com.ofnull.fastpig.common.job.JobConfiguration;
import com.ofnull.fastpig.common.job.JobInfo;
import com.ofnull.fastpig.common.metainfo.JobRecordProcessMetaLoader;
import com.ofnull.fastpig.common.metainfo.MatchOperatorImplLoader;
import com.ofnull.fastpig.common.metainfo.RecordProcessorImplLoader;
import com.ofnull.fastpig.common.metainfo.TableMetaLoader;
import com.ofnull.fastpig.common.utils.JsonPathSupportedMap;
import com.ofnull.fastpig.common.utils.JsonUtil;
import com.ofnull.fastpig.common.utils.MatchRuleUtils;
import com.ofnull.fastpig.common.utils.ValidatorUtil;
import com.ofnull.fastpig.spi.anno.PigType;
import com.ofnull.fastpig.spi.jdbc.IJdbcConnection;
import com.ofnull.fastpig.spi.job.IJobConfiguration;
import com.ofnull.fastpig.spi.matchoperate.IMatchOperator;
import com.ofnull.fastpig.spi.metainfo.*;
import com.ofnull.fastpig.spi.recordprocessor.BaseRecordProcessEngine;
import com.ofnull.fastpig.spi.recordprocessor.IJsonPathSupportedMap;
import com.ofnull.fastpig.spi.recordprocessor.IRecordProcessor;
import com.ofnull.fastpig.spi.recordprocessor.IRecordProcessor.ProcessContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 本地实现的数据处理器
 *
 * @author ofnull
 * @date 2024/6/18
 */
public class DefaultJobRecordProcessor extends BaseRecordProcessEngine<String, Map<String, Object>, Map<String, Object>> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultJobRecordProcessor.class);

    private String operator;
    private IJdbcConnection jdbcConnection;

    private ProcessContext processContext;
    private IJobConfiguration jobConfiguration;
    private TableMetaLoader tableMetaLoader;
    private TableMetaInfo tableMetaInfo;
    private List<JobRecordProcessMetaInfo> jobRecordProcessMetaInfos;
    private transient LoadedServices<IRecordProcessor> recordProcessorLoadedServices;
    private transient LoadedServices<IMatchOperator> matchOperatorLoadedServices;
    private transient List<ReadyJobRecordProcess> readyJobRecordProcesses;

    public DefaultJobRecordProcessor(TableMetaLoader tableMetaLoader, IJdbcConnection jdbcConnection, String operator) {
        this.tableMetaLoader = tableMetaLoader;
        this.jdbcConnection = jdbcConnection;
        this.operator = operator;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        this.jobConfiguration = (JobConfiguration) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        refreshTableMetaInfo();
        JobInfo jobInfo = jobConfiguration.getJobInfo();
        this.refreshJobRecordProcessorMeta(jobInfo.getName(), operator);
        refreshMatchOperatorLoadedServices();
        refreshRecordProcessorLoadedServices();
        this.processContext = new ProcessContext();
        processContext.setJobConfiguration(jobConfiguration);

        //TODO 指标
    }

    @Override
    public void processElement(Map<String, Object> event, KeyedProcessFunction<String, Map<String, Object>, Map<String, Object>>.Context context, Collector<Map<String, Object>> out) throws Exception {
        processContext.setAppendEvents(new CopyOnWriteArrayList<>());
        processContext.getAppendEvents().add(new JsonPathSupportedMap(true, event));
        HashSet<String> failedSubFlow = new HashSet<>();
        for (ReadyJobRecordProcess jobProcess : readyJobRecordProcesses) {
            if (failedSubFlow.contains(jobProcess.getSubFlow())) {
                continue; // 跳出子流程
            }
            processContext.setDiscardIndies(new ArrayList<>());
            processContext.setCurrentEventIndex(-1);
            //循环处理数据
            for (IJsonPathSupportedMap jpsEventMap : processContext.getAppendEvents()) {
                processContext.incrCurrentIndex();
                boolean matches = MatchRuleUtils.matches(matchOperatorLoadedServices, jpsEventMap, jobProcess.getMatchRules());
                IRecordProcessor processorInstance = matches ? jobProcess.getYesProcessorInstance() : jobProcess.getNoProcessorInstance();
                Object processorConfig = matches ? jobProcess.getYesConfig() : jobProcess.getNoConfig();
                if (processorInstance == null) {
                    continue;
                }

                try {
                    processorInstance.doTransform(jpsEventMap, processorConfig, processContext);
                } catch (Exception e) {
                    this.handlerFailed(e, jobProcess, processorInstance, processorConfig, failedSubFlow);
                }

            }
            //根据策略丢弃数据
            List<Integer> discardIndies = processContext.getDiscardIndies();
            if (CollectionUtils.isNotEmpty(discardIndies)) {
                for (int index = discardIndies.size() - 1; index >= 0; index--) {
                    Integer discardIndex = discardIndies.get(index);
                    processContext.getAppendEvents().remove(discardIndex);
                }
            }
        }

        for (IJsonPathSupportedMap appendEvent : processContext.getAppendEvents()) {
            out.collect(appendEvent.getMap());
        }
    }

    private void handlerFailed(Exception failedException, ReadyJobRecordProcess jobProcess, IRecordProcessor processorInstance, Object processorConfig, HashSet<String> failedSubFlow) {
        if (Objects.isNull(failedException)) {
            return;
        }
        String processorName = processorInstance.getClass().getAnnotation(PigType.class).value();
        switch (jobProcess.getFailPolicy()) {
            case IGNORE_SILENT:
                LOG.debug("Ignored failed, line {} processor {} config {} reason {}", jobProcess.getId(), processorName, JsonUtil.toJsonString(processorConfig), failedException.getMessage(), failedException);
                break;
            case IGNORE:
                LOG.warn("Ignored failed, line {} processor {} config {} reason {}", jobProcess.getId(), processorName, JsonUtil.toJsonString(processorConfig), failedException.getMessage(), failedException);
                break;
            case DISCARD:    //TODO 侧数据流 记录异常？
            case DISCARD_SILENT:
                processContext.markCurrentEventDiscard();
                break;
            case BREAK_SUB_FLOW:
                failedSubFlow.add(jobProcess.subFlow);
                break;
            default:
                throw new IllegalArgumentException("FailPolicy " + jobProcess.getFailPolicy() + " Undefined");
        }
    }

    private void refreshTableMetaInfo() throws Exception {
        if (tableMetaLoader != null) {
            this.tableMetaInfo = tableMetaLoader.loader();
        }

    }

    private void refreshJobRecordProcessorMeta(String jobName, String operator) throws Exception {
        List<JobRecordProcessMetaInfo> jobRecordProcessMetaInfos = new JobRecordProcessMetaLoader(jdbcConnection, jobName, operator).loader();
        this.jobRecordProcessMetaInfos = jobRecordProcessMetaInfos;
    }

    private void refreshMatchOperatorLoadedServices() throws Exception {
        List<SourceClassDefinition> matchOperatorSourceClasses = new MatchOperatorImplLoader(jdbcConnection).loader();
        LoadedServices<IMatchOperator> oldLoadedServices = this.matchOperatorLoadedServices;
        this.matchOperatorLoadedServices = LoadedServices.of(matchOperatorSourceClasses);
        if (oldLoadedServices != null) {
            oldLoadedServices.close();
        }
    }

    private void refreshRecordProcessorLoadedServices() throws Exception {
        List<SourceClassDefinition> recordProcessorSourceClasses = new RecordProcessorImplLoader(jdbcConnection).loader();
        LoadedServices<IRecordProcessor> oldLoadedServices = this.recordProcessorLoadedServices;
        this.recordProcessorLoadedServices = LoadedServices.of(recordProcessorSourceClasses);
        this.initJobRecordProcessConfig(jobRecordProcessMetaInfos);
        if (oldLoadedServices != null) {
            oldLoadedServices.close();
        }
    }

    private void initJobRecordProcessConfig(List<JobRecordProcessMetaInfo> recordConfigs) throws Exception {
        List<ReadyJobRecordProcess> readyJobRecordProcesses = new ArrayList<>();
        for (JobRecordProcessMetaInfo recordConfig : recordConfigs) {
            Pair<IRecordProcessor, Object> yesProcessorPair = callInitProcessor(recordConfig.getYesProcessor(), recordConfig.getYesConfig());
            ReadyJobRecordProcess readyJobRecord = new ReadyJobRecordProcess();
            readyJobRecord.setYesProcessorInstance(yesProcessorPair.getLeft());
            readyJobRecord.setYesConfig(yesProcessorPair.getRight());
            Pair<IRecordProcessor, Object> noProcessorPair = callInitProcessor(recordConfig.getNoProcessor(), recordConfig.getNoConfig());
            readyJobRecord.setNoProcessorInstance(noProcessorPair.getLeft());
            readyJobRecord.setNoConfig(noProcessorPair.getRight());
            readyJobRecord.setId(recordConfig.getId());
            readyJobRecord.setMatchRules(recordConfig.getMatchRules());
            readyJobRecord.setFailPolicy(recordConfig.getFailPolicy());
            readyJobRecord.setSubFlow(recordConfig.getSubFlow());
            readyJobRecord.setOrders(recordConfig.getOrders());
            readyJobRecordProcesses.add(readyJobRecord);
        }
        List<ReadyJobRecordProcess> oldReadyJobRecordProcesses = this.readyJobRecordProcesses;
        this.readyJobRecordProcesses = readyJobRecordProcesses;
        this.close(oldReadyJobRecordProcesses);
    }

    private Pair<IRecordProcessor, Object> callInitProcessor(String processor, String config) throws Exception {
        if (StringUtils.isBlank(processor)) {
            return Pair.of(null, null);
        }

        IRecordProcessor recordProcessor = recordProcessorLoadedServices.createNewService(processor, IRecordProcessor.class);
        Class argsType = recordProcessor.argsType();

        Object configObj = null;
        if (!(Void.class.equals(argsType))) {
            configObj = JsonUtil.objectMapper().readValue(config, argsType);
            ValidatorUtil.validate(configObj);
        }
        recordProcessor.init(configObj);
        recordProcessor.setJobConfiguration(jobConfiguration);
        //todo 考虑支持 支持执行脚本引擎
        return Pair.of(recordProcessor, configObj);
    }

    private void close(List<ReadyJobRecordProcess> processes) {
        if (CollectionUtils.isNotEmpty(processes)) {
            for (ReadyJobRecordProcess process : processes) {
                IRecordProcessor processor = process.getYesProcessorInstance();
                if (processor != null) {
                    try {
                        processor.close();
                    } catch (IOException e) {
                        LOG.warn("close YesProcessor fail, ", e);
                    }
                }
                processor = process.getNoProcessorInstance();
                if (processor != null) {
                    try {
                        processor.close();
                    } catch (IOException e) {
                        LOG.warn("close NoProcessor fail, ", e);
                    }
                }
            }
        }
    }

    public static class ReadyJobRecordProcess {
        private Long id;

        private MatchRules matchRules;

        private IRecordProcessor yesProcessorInstance;
        private Object yesConfig;

        private IRecordProcessor noProcessorInstance;
        private Object noConfig;

        private FailPolicy failPolicy;

        private String subFlow;

        private Integer orders;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public MatchRules getMatchRules() {
            return matchRules;
        }

        public void setMatchRules(MatchRules matchRules) {
            this.matchRules = matchRules;
        }

        public IRecordProcessor getYesProcessorInstance() {
            return yesProcessorInstance;
        }

        public void setYesProcessorInstance(IRecordProcessor yesProcessorInstance) {
            this.yesProcessorInstance = yesProcessorInstance;
        }

        public Object getYesConfig() {
            return yesConfig;
        }

        public void setYesConfig(Object yesConfig) {
            this.yesConfig = yesConfig;
        }

        public IRecordProcessor getNoProcessorInstance() {
            return noProcessorInstance;
        }

        public void setNoProcessorInstance(IRecordProcessor noProcessorInstance) {
            this.noProcessorInstance = noProcessorInstance;
        }

        public Object getNoConfig() {
            return noConfig;
        }

        public void setNoConfig(Object noConfig) {
            this.noConfig = noConfig;
        }


        public FailPolicy getFailPolicy() {
            return failPolicy;
        }

        public void setFailPolicy(FailPolicy failPolicy) {
            this.failPolicy = failPolicy;
        }

        public String getSubFlow() {
            return subFlow;
        }

        public void setSubFlow(String subFlow) {
            this.subFlow = subFlow;
        }

        public Integer getOrders() {
            return orders;
        }

        public void setOrders(Integer orders) {
            this.orders = orders;
        }
    }
}
