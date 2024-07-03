package com.ofnull.fastpig.blocks.column.type;

import com.ofnull.fastpig.spi.anno.PigType;
import com.ofnull.fastpig.spi.columntype.IColumnType;
import com.ofnull.fastpig.spi.metainfo.ColumnMetaInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ofnull
 * @date 2024/6/18
 */
@PigType("Set")
public class SetColumnType implements IColumnType {
    @Override
    public Object validate(Object value, ColumnMetaInfo columnMetaInfo) throws Exception {

        if (value == null) return null;
        if (value instanceof Set) return value;
        if (value instanceof Collection) return new HashSet<>((Collection) value);
        HashSet<Object> set = new HashSet<>();
        if (value.getClass().isArray()) {
            for (Object v : (Object[]) value) {
                set.add(v);
            }
        }
        return set;
    }
}
