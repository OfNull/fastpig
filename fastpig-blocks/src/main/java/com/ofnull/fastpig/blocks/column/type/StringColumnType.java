package com.ofnull.fastpig.blocks.column.type;

import cn.hutool.core.date.DateTime;
import com.ofnull.fastpig.common.utils.JsonUtil;
import com.ofnull.fastpig.spi.anno.PigType;
import com.ofnull.fastpig.spi.columntype.IColumnType;
import com.ofnull.fastpig.spi.metainfo.ColumnMetaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author ofnull
 * @date 2024/6/18
 */
@PigType("String")
public class StringColumnType implements IColumnType {
    private static final Logger LOG = LoggerFactory.getLogger(StringColumnType.class);

    @Override
    public Object validate(Object value, ColumnMetaInfo columnMetaInfo) throws Exception {
        if (value == null) return null;
        String strValue = castString(value);
        int dataSize = strValue.getBytes(StandardCharsets.UTF_8).length;
        if (columnMetaInfo.getColumnSize() != -1 && dataSize > columnMetaInfo.getColumnSize()) {
            String columnName = columnMetaInfo.getFieldName();
            String reason = String.format("field %s over size, column %s type %s size %d", columnName, columnName, "VARCHAR", columnMetaInfo.getColumnSize());
            throw new IllegalArgumentException(reason);
        }
        return strValue;
    }

    private String castString(Object value) {
        if (value instanceof String) return (String) value;
        if (value instanceof Date) {
            Date date = (Date) value;
            return new DateTime(date).toString("yyyy-MM-dd HH:mm:ss");
        }
        try {
            return JsonUtil.toJsonString(value);
        } catch (Exception e) {
            LOG.warn("Json format failed, value {} reason {}:{}", value, e.getClass().getName(), e.getCause());
            return String.valueOf(value);
        }
    }
}
