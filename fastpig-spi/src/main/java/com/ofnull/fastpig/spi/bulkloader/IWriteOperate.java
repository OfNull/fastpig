package com.ofnull.fastpig.spi.bulkloader;

import com.ofnull.fastpig.spi.metainfo.ColumnMetaInfo;

import java.util.List;
import java.util.Map;

/**
 * @author ofnull
 * @date 2024/6/12 15:16
 */
public interface IWriteOperate<R extends IPreparedWrite> {
     R insert(Map<String, Object> event, List<ColumnMetaInfo> columnMetaInfos);

    R insertIgnore(Map<String, Object> event, List<ColumnMetaInfo> columnMetaInfos);

    R update(Map<String, Object> event, List<ColumnMetaInfo> columnMetaInfos);

    R delete(Map<String, Object> event, List<ColumnMetaInfo> columnMetaInfos);

    R upsert(Map<String, Object> event, List<ColumnMetaInfo> columnMetaInfos);
}
