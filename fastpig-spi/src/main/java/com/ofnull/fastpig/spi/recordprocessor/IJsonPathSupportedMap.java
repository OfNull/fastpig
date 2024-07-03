package com.ofnull.fastpig.spi.recordprocessor;

import java.util.Map;

/**
 * @author ofnull
 * @date 2024/6/6 14:22
 */
public interface IJsonPathSupportedMap {
    void reset(boolean jsonPathSupport, Map<String, Object> map);

    <T> T get(String key);

    String getString(String key);

    void put(String key, Object value);

    Map<String, Object> getMap();

    boolean isJsonPathSupport();

    String jsonString();

    void remove(String key);
}
