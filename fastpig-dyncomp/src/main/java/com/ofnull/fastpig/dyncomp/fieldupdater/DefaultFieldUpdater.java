package com.ofnull.fastpig.dyncomp.fieldupdater;

import com.ofnull.fastpig.spi.anno.PigType;
import com.ofnull.fastpig.spi.fieldupdater.IFieldUpdater;
import com.ofnull.fastpig.spi.metainfo.ColumnMetaInfo;
import com.ofnull.fastpig.spi.metainfo.TableMetaInfo;

import java.util.Map;

/**
 * @author ofnull
 * @date 2024/6/25
 */
@PigType("Default")
public class DefaultFieldUpdater implements IFieldUpdater {

    @Override
    public void init(Map<String, Object> event, ColumnMetaInfo columnMeta, TableMetaInfo tableMeta, Long eventTime, IRecordState recordState) throws Exception {
        add(event, columnMeta, tableMeta, eventTime, recordState);
    }

    @Override
    public void add(Map<String, Object> event, ColumnMetaInfo columnMeta, TableMetaInfo tableMeta, Long eventTime, IRecordState recordState) throws Exception {

    }

    @Override
    public void merge(Map<String, Object> event, ColumnMetaInfo columnMeta, TableMetaInfo tableMeta, Long eventTime, IRecordState recordState) throws Exception {

    }
}
