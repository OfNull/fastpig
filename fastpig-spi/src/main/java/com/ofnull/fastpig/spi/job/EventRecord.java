package com.ofnull.fastpig.spi.job;

import java.io.Serializable;
import java.util.Map;

/**
 * 普通处理数据
 *
 * @author ofnull
 * @date 2024/6/17
 */
public class EventRecord<E> extends BaseEvent implements Serializable {
    private Map<String, Object> event;

    private Map<String, Object> meta;

    private Map<String, Object> dataProcessAttached;

    public EventRecord() {
    }

    public EventRecord(Map<String, Object> event) {
        this.event = event;
    }

    public Map<String, Object> getEvent() {
        return event;
    }

    public void setEvent(Map<String, Object> event) {
        this.event = event;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    public Map<String, Object> getDataProcessAttached() {
        return dataProcessAttached;
    }

    public void setDataProcessAttached(Map<String, Object> dataProcessAttached) {
        this.dataProcessAttached = dataProcessAttached;
    }
}
