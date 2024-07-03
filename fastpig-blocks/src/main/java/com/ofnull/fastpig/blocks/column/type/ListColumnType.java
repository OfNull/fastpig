package com.ofnull.fastpig.blocks.column.type;

import com.ofnull.fastpig.spi.anno.PigType;
import com.ofnull.fastpig.spi.columntype.IColumnType;
import com.ofnull.fastpig.spi.metainfo.ColumnMetaInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author ofnull
 * @date 2024/6/18
 */
@PigType("List")
public class ListColumnType implements IColumnType {
    @Override
    public Object validate(Object value, ColumnMetaInfo columnMetaInfo) throws Exception {
        if (value == null) return null;
        if (value instanceof List) return value;
        if (value instanceof Collection) return new ArrayList<>((Collection) value);
        if (value.getClass().isArray()) {
            ArrayList<Object> arrayList = new ArrayList<>();
            for (Object v : (Object[]) value) {
                arrayList.add(v);
            }
            return arrayList;
        }
        return Arrays.asList(value);
    }
}
