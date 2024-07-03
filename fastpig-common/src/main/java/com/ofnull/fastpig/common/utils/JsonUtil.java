package com.ofnull.fastpig.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ofnull
 * @date 2022/2/14 16:58
 */
public class JsonUtil {
    public static final Logger LOG = LoggerFactory.getLogger(JsonUtil.class);
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String toJsonString(Object obj) {
        String jsonStr = null;
        try {
            jsonStr = mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            LOG.error("json convert Exception Method toJsonString Param: " + obj.getClass() + " Message: " + e);
        }
        return jsonStr;
    }

    public static <T> T convert(Object obj, Class<T> type) {
        return mapper.convertValue(obj, type);
    }

    public static <T> T tryRead(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            LOG.warn("Json tryRead failed, json {} type {}", json, type, e);
            return null;
        }
    }

    public static ObjectMapper objectMapper() {
        return mapper;
    }
}
