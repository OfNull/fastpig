package com.ofnull.fastpig.run.jobs;

import com.ofnull.fastpig.common.attach.ActionOperateAttached;
import com.ofnull.fastpig.common.jdbc.JdbcConnectionConfig;
import com.ofnull.fastpig.common.jdbc.SimpleJdbcConnection;
import com.ofnull.fastpig.common.job.JobConfiguration;
import com.ofnull.fastpig.common.metainfo.TableConfig;
import com.ofnull.fastpig.common.metainfo.TableMetaLoader;
import com.ofnull.fastpig.run.SimpleStartSetUp;
import com.ofnull.fastpig.run.base.*;
import com.ofnull.fastpig.spi.metainfo.TableMetaInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import java.util.List;
import java.util.Map;

import static com.ofnull.fastpig.common.attach.ActionOperateAttached.OperationType.ADD;
import static com.ofnull.fastpig.common.constant.ConfigurationConstant.*;

/**
 * Kafka 入库
 *
 * @author ofnull
 * @date 2024/7/1
 */
public class KafkaToStoreJob {
    private static final String OPERATOR_DEFAULT = "DEFAULT";
    private static final String ACTION_OPERATE = "ActionOperate";
    public static final String STATE_GROUPING = "stateGrouping";
    public static final String STATE_FIELD_CONVERTOR = "stateFieldConvertor";
    public static final String META_CHECK = "metaCheck";
    public static final String dbWriteConfigKey = "dbWrite";

    public static void main(String[] args) throws Exception {
        JobConfiguration jobContext = SimpleStartSetUp.entry(args);
        JdbcConnectionConfig jdbcConnConf = jobContext.convertConfigToEntity(META_INFO, JdbcConnectionConfig.class);
        SimpleJdbcConnection simpleJdbcConn = new SimpleJdbcConnection(jdbcConnConf);
        TableConfig tableConfig = jobContext.convertConfigToEntity(TABLE_INFO, TableConfig.class);
        TableMetaLoader tableMetaLoader = new TableMetaLoader(simpleJdbcConn, tableConfig);
        TableMetaInfo tableMetaInfo = tableMetaLoader.loader();
        List<String> stateGrouping = jobContext.getRequiredConfig(STATE_GROUPING, tableMetaInfo.getPrimaryKeys());
        SimpleKeyByFields simpleKeySelector = SimpleKeyByFields.of(jobContext.convertConfigToEntity(DATA_GROUPING, SimpleKeyByFields.DefaultKeyedConfig.class));
        DataStream<Map<String, Object>> dataStream = SimpleStartSetUp.addSource(jobContext);
        SingleOutputStreamOperator<Map<String, Object>> defaultRecordProcessedStream = dataStream.keyBy(simpleKeySelector)
                .process(new DefaultJobRecordProcessor(null, simpleJdbcConn, OPERATOR_DEFAULT)).uid(OPERATOR_DEFAULT).name(OPERATOR_DEFAULT);
        //动作标记
        SingleOutputStreamOperator<Map<String, Object>> actionStream = defaultRecordProcessedStream.map(new ActionOperateAttachedMap(new ActionOperateAttached(ADD))).uid(ACTION_OPERATE).name(ACTION_OPERATE);
        //状态处理
        SingleOutputStreamOperator<Map<String, Object>> fieldConvertorStream = actionStream.flatMap(new PartitionMap(stateGrouping))
                .keyBy(new StateFieldConvertor.KeyByPartitionInfo())
                .process(new StateFieldConvertor(simpleJdbcConn, tableMetaLoader))
                .uid(STATE_FIELD_CONVERTOR).name(STATE_FIELD_CONVERTOR);
        //数据验证
        SingleOutputStreamOperator<Map<String, Object>> dataMetaCheckStream = fieldConvertorStream.process(new MetadataCheckProcessor(tableMetaInfo)).uid(META_CHECK).name(META_CHECK);
        //数据入库
        dataMetaCheckStream.process(new ParallelDatabaseLoader(TABLE_INFO, dbWriteConfigKey, simpleJdbcConn)).uid(tableMetaInfo.uuid()).name(tableMetaInfo.uuid());
        SimpleStartSetUp.executorJob(jobContext.getEnv(), jobContext.getJobInfo().getName());
    }
}
