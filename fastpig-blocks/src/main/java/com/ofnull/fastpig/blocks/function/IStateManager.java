package com.ofnull.fastpig.blocks.function;

import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.state.State;
import org.apache.flink.api.common.state.StateDescriptor;

import java.io.IOException;
import java.util.Map;

/**
 * @author ofnull
 * @date 2022/2/18 16:21
 */
public interface IStateManager<T extends State, S> {
    StateDescriptor<T, S> buildStateDescriptor(Map<String, Object> cfg);

    StateTtlConfig buildTTL(Map<String, Object> cfg);

    T buildState(RuntimeContext context, Map<String, Object> cfg);

    S getValue() throws IOException;

    void update(S s) throws IOException;
}
