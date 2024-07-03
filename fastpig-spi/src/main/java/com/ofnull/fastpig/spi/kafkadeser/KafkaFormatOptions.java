package com.ofnull.fastpig.spi.kafkadeser;

import java.io.Serializable;

/**
 * @author ofnull
 */
public class KafkaFormatOptions implements Serializable {

    private Boolean jsonPathSupport = false;

    private String topicField;

    private String keyField;

    public Boolean getJsonPathSupport() {
        return jsonPathSupport;
    }

    public void setJsonPathSupport(Boolean jsonPathSupport) {
        this.jsonPathSupport = jsonPathSupport;
    }

    public String getTopicField() {
        return topicField;
    }

    public void setTopicField(String topicField) {
        this.topicField = topicField;
    }

    public String getKeyField() {
        return keyField;
    }

    public void setKeyField(String keyField) {
        this.keyField = keyField;
    }
}
