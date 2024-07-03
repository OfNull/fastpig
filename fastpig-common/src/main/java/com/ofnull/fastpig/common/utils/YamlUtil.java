package com.ofnull.fastpig.common.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author ofnull
 * @date 2022/2/10 11:00
 */
public class YamlUtil {
    public static final Logger LOG = LoggerFactory.getLogger(YamlUtil.class);
    private static final String PRFIX = "./";

    private YamlUtil() {
    }

    public static Map<String, Object> parseYaml(String yamStr) {
        Map<String, Object> cfg = new Yaml().loadAs(yamStr, Map.class);
        return cfg;
    }

    public static Map<String, Object> parseYaml(InputStream inStream) {
        return parseYaml(new InputStreamReader(inStream));
    }

    public static Map<String, Object> parseYaml(InputStreamReader isReader) {
        Map<String, Object> cfg = new Yaml().loadAs(isReader, Map.class);
        return cfg;
    }
}
