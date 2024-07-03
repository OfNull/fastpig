package com.ofnull.fastpig.common.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * @author ofnull
 * @date 2024/6/6 11:36
 */
public class PropertiesUtil {
    public static Properties toProperties(Map map) {
        Properties answer = new Properties();
        if (map != null) {
            Iterator iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();
                Object value = entry.getValue();
                answer.put(key, value);
            }
        }
        return answer;
    }

    public static Map<String, String> toMaps(Properties properties) {
        Map<String, String> map = new HashMap<>();
        if (properties != null && !properties.isEmpty()) {
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                map.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return map;
    }
}
