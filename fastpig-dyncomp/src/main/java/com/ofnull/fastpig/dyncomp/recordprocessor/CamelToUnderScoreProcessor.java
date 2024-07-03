package com.ofnull.fastpig.dyncomp.recordprocessor;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableSet;
import com.ofnull.fastpig.spi.anno.PigType;
import com.ofnull.fastpig.spi.recordprocessor.IJsonPathSupportedMap;
import com.ofnull.fastpig.spi.recordprocessor.IRecordProcessor;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Map key 驼峰转下划线
 * config field 可以配置 需要转换 的Map
 * <p>
 * {
 * “key_one”: "value"
 * “key_two”: {
 * "simple_name": "xx",
 * "simple_age": "yy"
 * }
 * }
 * <p>
 * field = null
 * {
 * “keyOne”: "value"
 * “keyTwo”: {
 * "simple_name": "xx",
 * "simple_age": "yy"
 * }
 * }
 * <p>
 * field = key_two
 * {
 * “key_one”: "value"
 * “key_two”: {
 * "simpleName": "xx",
 * "simpleAge": "yy"
 * }
 * }
 *
 * @author ofnull
 * @date 2024/6/21
 */
@PigType("CamelToUnderScore")
public class CamelToUnderScoreProcessor implements IRecordProcessor<CamelToUnderScoreProcessor.Config> {

    @Override
    public Class<Config> argsType() {
        return Config.class;
    }

    @Override
    public void doTransform(IJsonPathSupportedMap jpsMap, Config config, ProcessContext context) throws Exception {
        String field = config.getField();
        if (StringUtils.isNotBlank(field)) {
            Object value = jpsMap.get(field);
            if (value instanceof Map) {
                convert((Map<String, Object>) value);
            }
        } else {
            convert(jpsMap.getMap());
        }
    }

    private void convert(Map<String, Object> map) {

        for (String key : ImmutableSet.copyOf(map.keySet())) {
            String ckey = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, key);
            if (StringUtils.equalsIgnoreCase(key, ckey) == false) {
                map.put(ckey, map.remove(key));
            }
        }
    }

    public static class Config {
        private String field;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }
    }
}
