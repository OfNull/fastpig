package com.ofnull.fastpig.blocks.column.type;

import com.ofnull.fastpig.spi.anno.PigType;
import com.ofnull.fastpig.spi.columntype.IColumnType;
import com.ofnull.fastpig.spi.metainfo.ColumnMetaInfo;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ofnull
 * @date 2024/7/1
 */
@PigType("Long")
public class LongColumnType implements IColumnType {

    @Override
    public Object validate(Object value, ColumnMetaInfo columnMetaInfo) throws Exception {
        if (value instanceof Date) {
            return ((Date) value).getTime();
        }
        if (value instanceof Long == false) {
            if (value instanceof String) {
                try {
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(String.valueOf(value)).getTime();
                } catch (Exception e) {
                }
            }
            return NumberUtils.toLong(String.valueOf(value));
        }
        return value;
    }
}
