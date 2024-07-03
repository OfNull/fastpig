package com.ofnull.fastpig.spi.columntype;

import com.ofnull.fastpig.spi.metainfo.ColumnMetaInfo;

/**
 * @author ofnull
 * @date 2024/6/18
 */

public interface IColumnType {
    Object validate(Object value, ColumnMetaInfo columnMetaInfo) throws Exception;
}
