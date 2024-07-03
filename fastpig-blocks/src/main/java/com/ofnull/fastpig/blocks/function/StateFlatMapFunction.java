package com.ofnull.fastpig.blocks.function;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author ofnull
 * @date 2022/2/18 16:18
 */
public class StateFlatMapFunction extends RichFlatMapFunction<Map<String, Object>, Map<String, Object>> {

    private IStateManager stateManager;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        ValueStateDescriptor<Object> valueStateDescriptor = new ValueStateDescriptor<Object>("11", Object.class);
        StateTtlConfig build = StateTtlConfig.newBuilder(Time.of(10, TimeUnit.SECONDS)).build();
//        getRuntimeContext().getState()
    }

    @Override
    public void flatMap(Map<String, Object> stringObjectMap, Collector<Map<String, Object>> collector) throws Exception {

    }
}
