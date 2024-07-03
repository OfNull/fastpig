package com.ofnull.fastpig.blocks.column.type;

import com.ofnull.fastpig.spi.anno.PigType;
import com.ofnull.fastpig.spi.columntype.IColumnType;
import com.ofnull.fastpig.spi.metainfo.ColumnMetaInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * @author ofnull
 * @date 2024/6/18
 */
@PigType("Timestamp")
public class TimestampColumnType implements IColumnType {
    private final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final Pattern TIMESTAMP_PATTERN = Pattern.compile("^[0-9]+$");

    @Override
    public Object validate(Object value, ColumnMetaInfo columnMetaInfo) throws Exception {
        try {
            if (value == null || StringUtils.isBlank(String.valueOf(value))) {
                return null;
            } else if (TIMESTAMP_PATTERN.matcher(String.valueOf(value)).matches()) {
                return SIMPLE_DATE_FORMAT.format(new Date(NumberUtils.toLong(String.valueOf(value))));
            } else {
                SIMPLE_DATE_FORMAT.parse(String.valueOf(value));
            }
        } catch (Exception e) {
            String columnName = columnMetaInfo.getFieldName();
            String reason = String.format("field %s with bad timestamp format, column %s", columnName, columnName);
            throw new IllegalArgumentException(reason);
        }
        return value;
    }
}
