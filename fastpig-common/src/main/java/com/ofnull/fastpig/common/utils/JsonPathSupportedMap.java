package com.ofnull.fastpig.common.utils;

import com.jayway.jsonpath.DocumentContext;
import com.ofnull.fastpig.spi.recordprocessor.IJsonPathSupportedMap;

import java.util.Map;

/**
 * @author ofnull
 * @date 2024/6/6 14:23
 */
public class JsonPathSupportedMap implements IJsonPathSupportedMap {
    private boolean jsonPathSupport;

    private Map<String, Object> map;

    private DocumentContext dc;

    public JsonPathSupportedMap(boolean jsonPathSupport, Map<String, Object> map) {
        this.jsonPathSupport = jsonPathSupport;
        this.map = map;
        if (jsonPathSupport) {
            this.dc = JsonPathUtils.parse(map);
        }
    }

    @Override
    public void reset(boolean jsonPathSupport, Map<String, Object> map) {
        this.jsonPathSupport = jsonPathSupport;
        this.map = map;
        if (jsonPathSupport) {
            this.dc = JsonPathUtils.parse(map);
        }
    }

    public JsonPathSupportedMap(Map<String, Object> map) {
        this(true, map);
    }

    @Override
    public <T> T get(String key) {
        return (T) JsonPathUtils.getField(map, dc, key, jsonPathSupport);
    }

    @Override
    public String getString(String key) {
        Object v = get(key);
        if (v == null) return null;
        return v.toString();
    }

    @Override
    public void put(String key, Object value) {
        JsonPathUtils.setField(map, dc, key, value, jsonPathSupport);
    }

    @Override
    public void remove(String key) {
        JsonPathUtils.removeField(map, dc, key, jsonPathSupport);
    }

    @Override
    public Map<String, Object> getMap() {
        if (jsonPathSupport) {
            return dc.json();
        } else {
            return map;
        }
    }

    @Override
    public boolean isJsonPathSupport() {
        return jsonPathSupport;
    }

    @Override
    public String jsonString() {
        return dc.jsonString();
    }
}
