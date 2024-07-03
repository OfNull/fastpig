package com.ofnull.fastpig.blocks.function;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.io.Serializable;
import java.util.Map;

/**
 * @author ofnull
 * @date 2022/2/18 17:17
 */
public class TypesUtils implements Serializable {
    private static Map<String, Class> classMap;

    static {
        classMap.put("STRING", String.class);
        classMap.put("LONG", Long.class);
        classMap.put("INT", Integer.class);
        classMap.put("BYTE", Byte.class);
        classMap.put("SHORT", Short.class);
        classMap.put("DOUBLE", Double.class);
        classMap.put("FLOAT", Float.class);
        classMap.put("BOOLEAN", Boolean.class);
    }

    public static TypeInformation getFinkType(String name) {
        switch (name) {
            case "STRING":
                return Types.STRING;
            case "INT":
                return Types.INT;
            default:
                throw new RuntimeException("Type Not Found");
        }
    }

    ;

}
