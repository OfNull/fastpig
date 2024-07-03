package com.ofnull.fastpig.common.utils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.internal.DefaultsImpl;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.jayway.jsonpath.Option.DEFAULT_PATH_LEAF_TO_NULL;

/**
 * @author ofnull
 * @date 2024/6/6 14:06
 */
public class JsonPathUtils {
    public static final Logger LOG = LoggerFactory.getLogger(JsonPathUtils.class);

    static final Configuration configuration =
            Configuration.builder().options(DefaultsImpl.INSTANCE.options())
                    .jsonProvider(new JacksonJsonProvider())
                    .build().addOptions(DEFAULT_PATH_LEAF_TO_NULL);

    public static DocumentContext parse(Object data) {
        return JsonPath.using(configuration).parse(data);
    }

    public static Object getField(Map<String, Object> map, DocumentContext dc, String field, boolean enableJsonPathSupport) {
        if (enableJsonPathSupport == false || !isJsonPath(field)) {
            return map.get(field);
        }
        return tryGet(dc, field);
    }

    public static void setField(Map<String, Object> map, DocumentContext dc, String field, Object value, boolean enableJsonPathSupport) {
        if (enableJsonPathSupport == false || !isJsonPath(field)) {
            map.put(field, value);
            return;
        }
        trySet(dc, field, value);
    }

    public static void removeField(Map<String, Object> map, DocumentContext dc, String field, boolean enableJsonPathSupport) {
        if (enableJsonPathSupport == false || !isJsonPath(field)) {
            map.remove(field);
            return;
        }
        tryRemove(dc, field);
    }

    private static <T> T tryGet(DocumentContext dc, String field) {
        try {
            return dc.read(field);
        } catch (PathNotFoundException e) {
            return null;
        } catch (Exception e) {
            LOG.debug("Json path get failed", e);
            return null;
        }
    }

    private static void trySet(DocumentContext dc, String field, Object value) {
        try {
            dc.set(field, value);
        } catch (Exception e) {
            LOG.debug("Json path set failed", e);
        }
    }

    private static void tryRemove(DocumentContext dc, String field) {
        try {
            dc.delete(field);
        } catch (Exception e) {
            LOG.debug("Json path remove failed", e);
        }
    }

    private static boolean isJsonPath(String path) {
        return path.startsWith("$");
    }


}
