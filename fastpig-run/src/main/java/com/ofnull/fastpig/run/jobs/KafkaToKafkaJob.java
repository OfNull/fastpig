package com.ofnull.fastpig.run.jobs;

import com.ofnull.fastpig.common.finder.ServiceLoaderHelper;
import com.ofnull.fastpig.common.jdbc.JdbcConnectionConfig;
import com.ofnull.fastpig.common.jdbc.SimpleJdbcConnection;
import com.ofnull.fastpig.common.job.JobConfiguration;
import com.ofnull.fastpig.common.metainfo.TableConfig;
import com.ofnull.fastpig.common.metainfo.TableMetaLoader;
import com.ofnull.fastpig.run.SimpleStartSetUp;
import com.ofnull.fastpig.run.base.DefaultJobRecordProcessor;
import com.ofnull.fastpig.run.base.SimpleKeyByFields;
import com.ofnull.fastpig.run.base.SimpleKeyByFields.DefaultKeyedConfig;
import com.ofnull.fastpig.spi.instance.IInstanceGenerate;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.util.Map;

import static com.ofnull.fastpig.common.constant.ConfigurationConstant.*;

/**
 * @author ofnull
 * @date 2024/6/25
 */
public class KafkaToKafkaJob {
    private static final String OPERATOR_DEFAULT = "DEFAULT";
    private static final String DATA_PRODUCT = "dataProduct";

    public static void main(String[] args) throws Exception {
        JobConfiguration jobContext = SimpleStartSetUp.entry(args);
        JdbcConnectionConfig jdbcConnConf = jobContext.convertConfigToEntity(META_INFO, JdbcConnectionConfig.class);
        SimpleJdbcConnection simpleJdbcConn = new SimpleJdbcConnection(jdbcConnConf);
        SinkFunction<Map<String, Object>> dataProduct = jobContext.generateInstance(DATA_PRODUCT);
        DefaultKeyedConfig defaultKeyedConfig = jobContext.convertConfigToEntity(DATA_GROUPING, DefaultKeyedConfig.class);
        SimpleKeyByFields simpleKeySelector = SimpleKeyByFields.of(defaultKeyedConfig.getJsonPathSupported(), defaultKeyedConfig.getKeys());
        DataStream<Map<String, Object>> dataStream = SimpleStartSetUp.addSource(jobContext);
        SingleOutputStreamOperator<Map<String, Object>> defaultRecordProcessedStream = dataStream.keyBy(simpleKeySelector)
                .process(new DefaultJobRecordProcessor(null, simpleJdbcConn, OPERATOR_DEFAULT)).uid(OPERATOR_DEFAULT).name(OPERATOR_DEFAULT);
        defaultRecordProcessedStream.addSink(dataProduct).uid(DATA_PRODUCT).name(DATA_PRODUCT);
        SimpleStartSetUp.executorJob(jobContext.getEnv(), jobContext.getJobInfo().getName());
    }
}
