package com.ofnull.fastpig.blocks.column.type;

import com.ofnull.fastpig.spi.anno.PigType;
import com.ofnull.fastpig.spi.columntype.IColumnType;
import com.ofnull.fastpig.spi.metainfo.ColumnMetaInfo;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * @author ofnull
 * @date 2024/6/18
 */
@PigType("Double")
public class DoubleColumnType implements IColumnType {
    @Override
    public Object validate(Object value, ColumnMetaInfo columnMetaInfo) throws Exception {
        if (value instanceof Double == false) {
            return NumberUtils.toDouble(String.valueOf(value));
        }
        return value;
    }
}