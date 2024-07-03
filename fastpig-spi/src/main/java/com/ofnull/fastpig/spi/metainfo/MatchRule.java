package com.ofnull.fastpig.spi.metainfo;

import java.io.Serializable;

/**
 * @author ofnull
 * @date 2024/6/20
 */
public class MatchRule implements Serializable {

    private String key;

    private String operator;

    private Object values;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Object getValues() {
        return values;
    }

    public void setValues(Object values) {
        this.values = values;
    }
}
