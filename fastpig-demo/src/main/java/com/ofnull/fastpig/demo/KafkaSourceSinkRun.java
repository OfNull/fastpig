package com.ofnull.fastpig.demo;

import com.ofnull.fastpig.common.finder.ServiceLoaderHelper;
import com.ofnull.fastpig.common.jdbc.JdbcConnectionConfig;
import com.ofnull.fastpig.common.jdbc.SimpleJdbcConnection;
import com.ofnull.fastpig.common.job.JobConfiguration;
import com.ofnull.fastpig.common.metainfo.TableConfig;
import com.ofnull.fastpig.common.metainfo.TableMetaLoader;
import com.ofnull.fastpig.common.utils.JsonUtil;
import com.ofnull.fastpig.run.SimpleStartSetUp;
import com.ofnull.fastpig.spi.instance.IInstanceGenerate;
import com.ofnull.fastpig.spi.metainfo.TableMetaInfo;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.util.Map;

/**
 * start args: -e qa -n pig.yaml -t local
 *
 * @author ofnull
 * @date 2024/6/6 16:24
 */
public class KafkaSourceSinkRun {
    public static void main(String[] args) throws Exception {
        JobConfiguration entry = SimpleStartSetUp.entry(args);
        DataStream<Map<String, Object>> dataStream = SimpleStartSetUp.addSource(entry);
        dataStream.print();

        Map<String, Object> metaInfoCfg = (Map<String, Object>) entry.getCfgs().get("metaInfo");
        JdbcConnectionConfig jdbcConfig = JsonUtil.convert(metaInfoCfg, JdbcConnectionConfig.class);
        SimpleJdbcConnection jdbcConnection = new SimpleJdbcConnection(jdbcConfig);
        TableConfig tableConfig = JsonUtil.convert(entry.getCfgs().get("tableInfo"), TableConfig.class);
        TableMetaLoader tableMetaLoader = new TableMetaLoader(jdbcConnection, tableConfig);
        TableMetaInfo tableMetaInfo = tableMetaLoader.loader();
        System.out.println("----------");

        entry.getEnv().addSource(new RichSourceFunction<String>() {
            @Override
            public void open(Configuration parameters) throws Exception {
                super.open(parameters);
                JobConfiguration job = (JobConfiguration) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
                System.out.println();
            }

            @Override
            public void run(SourceContext<String> sourceContext) throws Exception {

            }

            @Override
            public void cancel() {

            }
        }).print();


        Map<String, Object> sinkCfg = (Map<String, Object>) entry.getCfgs().get("dataProduct");
        IInstanceGenerate<SinkFunction<Map<String, Object>>> kafkaSink = ServiceLoaderHelper.loadServices(IInstanceGenerate.class, "kafkaSink");
        kafkaSink.open(sinkCfg);
        SinkFunction<Map<String, Object>> kafkaSinkGenerate = kafkaSink.generateInstance();
        dataStream.addSink(kafkaSinkGenerate);
        entry.getEnv().execute("----------------");
    }
}
