package com.ofnull.fastpig.spi.job;

/**
 * 数据模型基础定义
 *
 * @author ofnull
 * @date 2024/6/17
 */
public abstract class BaseEvent {

    /**
     * 是否普通数据事件
     *
     * @return
     */
    public final boolean isRecord() {
        return getClass() == EventRecord.class;
    }

    public final <E> EventRecord<E> asRecord() {
        return (EventRecord<E>) this;
    }
}
