package com.ofnull.fastpig.blocks.function;

import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.typeutils.MapTypeInfo;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author ofnull
 * @date 2022/2/18 16:33
 */
public class ValueStateManager<T extends Object> implements IStateManager<ValueState<T>, T>, Serializable {
    private ValueState<T> valueState;

    public Class getType(Map<String, Object> cfg) {
        return Integer.class;
    }

    public TypeInformation getFinkType(String name) {
        switch (name) {
            case "STRING":
                return Types.STRING;
            case "INT":
                return Types.INT;
            case "MAP":
                return new MapTypeInfo(Types.STRING, TypeInformation.of(Object.class));
            default:
                throw new RuntimeException("Type Not Found");
        }
    }

    @Override
    public ValueStateDescriptor<T> buildStateDescriptor(Map<String, Object> cfg) {
//        ValueStateDescriptor<T> descriptor = new ValueStateDescriptor<T>("valueState", TypeInformation.of(new TypeHint<T>() {
//        }));

//        ValueStateDescriptor<T> descriptor = new ValueStateDescriptor<T>("valueState", TypeInformation.of(getType(cfg)));
        ValueStateDescriptor<T> descriptor = new ValueStateDescriptor<T>("valueState", getFinkType("INT"));

        return descriptor;
    }

    @Override
    public StateTtlConfig buildTTL(Map<String, Object> cfg) {
        StateTtlConfig.newBuilder(Time.of(10, TimeUnit.SECONDS))
                .neverReturnExpired()
                .updateTtlOnCreateAndWrite()
                .cleanupFullSnapshot()
                .build();
        return null;
    }

    @Override
    public ValueState<T> buildState(RuntimeContext context, Map<String, Object> cfg) {
        ValueStateDescriptor<T> descriptor = buildStateDescriptor(cfg);
        Optional.ofNullable(buildTTL(cfg)).ifPresent(ttlCfg -> descriptor.enableTimeToLive(ttlCfg));
        valueState = context.getState(descriptor);
        return valueState;
    }

    @Override
    public T getValue() throws IOException {
        return valueState.value();
    }

    @Override
    public void update(T t) throws IOException {
        valueState.update(t);


    }
}
