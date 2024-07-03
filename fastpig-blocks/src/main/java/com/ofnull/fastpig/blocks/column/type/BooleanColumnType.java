package com.ofnull.fastpig.blocks.column.type;

import com.ofnull.fastpig.spi.anno.PigType;
import com.ofnull.fastpig.spi.columntype.IColumnType;
import com.ofnull.fastpig.spi.metainfo.ColumnMetaInfo;
import org.apache.commons.lang3.BooleanUtils;

/**
 * @author ofnull
 * @date 2024/6/18
 */
@PigType("Boolean")
public class BooleanColumnType implements IColumnType {
    @Override
    public Object validate(Object value, ColumnMetaInfo columnMetaInfo) throws Exception {
        if (value instanceof Boolean == false) {
            return BooleanUtils.toBoolean(String.valueOf(value));
        }
        return value;
    }
}
