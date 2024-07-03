package com.ofnull.fastpig.spi.recordprocessor;

import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

/**
 * @author ofnull
 * @date 2024/6/18
 */
public abstract class BaseRecordProcessEngine<K, I, O> extends KeyedProcessFunction<K, I, O> {

}
