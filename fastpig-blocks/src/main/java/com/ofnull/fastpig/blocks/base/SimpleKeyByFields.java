package com.ofnull.fastpig.blocks.base;

import com.ofnull.fastpig.common.utils.JsonPathSupportedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.shaded.guava30.com.google.common.collect.Lists;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * @author ofnull
 * @date 2024/6/18
 */
public class SimpleKeyByFields implements KeySelector<Map<String, Object>, String> {
    private boolean jsonPathSupported = false;
    private String[] fields;
    private long counter = 0L;

    public static SimpleKeyByFields of(DefaultKeyedConfig config) {
        return new SimpleKeyByFields(config.getJsonPathSupported(), config.getKeys());
    }

    public static SimpleKeyByFields of(boolean jsonPathSupported, String... fields) {
        return new SimpleKeyByFields(jsonPathSupported, fields);
    }

    public SimpleKeyByFields(boolean jsonPathSupported, String[] fields) {
        this.jsonPathSupported = jsonPathSupported;
        this.fields = fields;
    }

    @Override
    public String getKey(Map<String, Object> event) throws Exception {
        List<Object> list = Lists.newArrayList();
        boolean hasData = false;
        if (jsonPathSupported == true) {
            JsonPathSupportedMap map = new JsonPathSupportedMap(event);
            for (String field : fields) {
                Object v = map.get(field);
                if (v != null) {
                    list.add(v);
                    hasData = true;
                } else {
                    list.add(EMPTY);
                }
            }

        } else {
            for (String field : fields) {
                Object v = event.get(field);
                if (v != null) {
                    list.add(v);
                    hasData = true;
                } else {
                    list.add(EMPTY);
                }
            }
        }

        return hasData ? StringUtils.join(list, ":") : String.valueOf(event.hashCode());
    }

    public static class DeduplicationKeyedConfig {
        @NotNull(message = "keys not null")
        private List<String> keys;
        private Boolean jsonPathSupported = false;
        // seconds
        private Long ttl;

        public List<String> getKeys() {
            return keys;
        }

        public void setKeys(List<String> keys) {
            this.keys = keys;
        }

        public Boolean getJsonPathSupported() {
            return jsonPathSupported;
        }

        public void setJsonPathSupported(Boolean jsonPathSupported) {
            this.jsonPathSupported = jsonPathSupported;
        }

        public Long getTtl() {
            return ttl;
        }

        public void setTtl(Long ttl) {
            this.ttl = ttl;
        }
    }

    public static class DefaultKeyedConfig {
        private String[] keys;
        private Boolean jsonPathSupported = false;

        public String[] getKeys() {
            return keys;
        }

        public void setKeys(String[] keys) {
            this.keys = keys;
        }

        public Boolean getJsonPathSupported() {
            return jsonPathSupported;
        }

        public void setJsonPathSupported(Boolean jsonPathSupported) {
            this.jsonPathSupported = jsonPathSupported;
        }
    }
}
