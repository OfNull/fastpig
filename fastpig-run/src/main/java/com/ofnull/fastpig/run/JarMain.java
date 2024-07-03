package com.ofnull.fastpig.run;

import com.ofnull.fastpig.run.function.ValueStateManager;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;

import java.util.concurrent.TimeUnit;

/**
 * @author ofnull
 * @date 2022/2/10 17:29
 */
public class JarMain {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setMaxParallelism(128);
        env.enableCheckpointing(1000);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        env.getCheckpointConfig().setCheckpointInterval(1000);
        env.getCheckpointConfig().setCheckpointTimeout(1000);
        env.getCheckpointConfig().setExternalizedCheckpointCleanup(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        DataStreamSource<Integer> mapDataStreamSource = env.addSource(new SourceFunction<Integer>() {
            @Override
            public void run(SourceContext<Integer> ctx) throws Exception {
                Integer in = 0;
                while (true) {
                    ctx.collect(in++);
                    TimeUnit.SECONDS.sleep(10);
                }
            }

            @Override
            public void cancel() {

            }
        });
        ValueStateManager<Integer> valueStateManager = new ValueStateManager<>();
        SingleOutputStreamOperator<String> stringSingleOutputStreamOperator = mapDataStreamSource.keyBy(v-> "1").flatMap(new RichFlatMapFunction<Integer, String>() {

            @Override
            public void open(Configuration parameters) throws Exception {
                super.open(parameters);
                ValueState<Integer> valueState = valueStateManager.buildState(getRuntimeContext(), null);
            }

            @Override
            public void flatMap(Integer integer, Collector<String> collector) throws Exception {
                Integer value = valueStateManager.getValue();
                if (value == null) {
                    value = integer;
                }
                System.out.println("当前状态数据 " + value);
                valueStateManager.update(value);
                collector.collect(String.valueOf(integer));
            }
        });
        stringSingleOutputStreamOperator.print();
//        mapDataStreamSource.addSink()
        env.execute("sssss");
    }
}
