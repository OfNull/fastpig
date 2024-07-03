package com.ofnull.fastpig.demo;

import com.ofnull.fastpig.common.utils.JsonUtil;
import com.starrocks.connector.flink.StarRocksSink;
import com.starrocks.connector.flink.table.sink.StarRocksSinkOptions;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author ofnull
 * @date 2022/4/8 16:58
 */
public class SimpleRun {

    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setMaxParallelism(128);
        env.enableCheckpointing(1000);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        env.getCheckpointConfig().setCheckpointInterval(1000);
        env.getCheckpointConfig().setCheckpointTimeout(1000);
        env.getCheckpointConfig().setExternalizedCheckpointCleanup(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
//        env.getConfig().setGlobalJobParameters(ParameterTool.fromArgs(args));

        DataStreamSource<String> source = env.addSource(new RichSourceFunction<String>() {
            @Override
            public void run(SourceContext<String> ctx) throws Exception {
//                ParameterTool a =(ParameterTool)getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
                while (true) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("name", "current_name");
                    map.put("age", 20);
                    map.put("momery", 100000);
                    map.put("create_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    ctx.collect(JsonUtil.toJsonString(map));
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void cancel() {

            }
        });
        source.print();
//        source.addSink(StarRocksSink.sink(StarRocksSinkOptions.builder()
//                .withProperty("jdbc-url", "jdbc:mysql://10.11.32.63:9030")
//                .withProperty("load-url", "10.11.32.63:8030")
//                .withProperty("username", "ncdp")
//                .withProperty("password", "D#Hi3UmyQnbc")
//                .withProperty("table-name", "test_table")
//                .withProperty("database-name", "ncdp")
//                .withProperty("sink.properties.format", "json")
//                .withProperty("sink.properties.strip_outer_array", "true")
//                .build()));

        try {
            env.execute("sink starRocks");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
