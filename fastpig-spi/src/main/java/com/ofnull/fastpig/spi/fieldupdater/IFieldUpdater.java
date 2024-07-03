package com.ofnull.fastpig.spi.fieldupdater;

import com.ofnull.fastpig.spi.metainfo.ColumnMetaInfo;
import com.ofnull.fastpig.spi.metainfo.TableMetaInfo;

import java.util.List;
import java.util.Map;

/**
 * @author ofnull
 * @date 2024/6/25
 */
public interface IFieldUpdater {
    default Class<?> argsType() {
        return null;
    }


    void init(Map<String, Object> event, ColumnMetaInfo columnMeta, TableMetaInfo tableMeta, Long eventTime, IRecordState recordState) throws Exception;

    void add(Map<String, Object> event, ColumnMetaInfo columnMeta, TableMetaInfo tableMeta, Long eventTime, IRecordState recordState) throws Exception;

    void merge(Map<String, Object> event, ColumnMetaInfo columnMeta, TableMetaInfo tableMeta, Long eventTime, IRecordState recordState) throws Exception;


    interface IRecordState {
        void updateRecordKey(String recordKey);

        Object getValueState(Short index) throws Exception;

        Long getTimeState(Short index) throws Exception;

        void updateValueState(Short index, Object state) throws Exception;

        void updateTimeState(Short index, Long state) throws Exception;

        void removeValueState(Short index) throws Exception;

        void removeTimeState(Short index) throws Exception;

        void removeValueState(List<ColumnMetaInfo> metadataList) throws Exception;

        void removeTimeState(List<ColumnMetaInfo> metadataList) throws Exception;

        void appendStateField(Map<String, Object> event, String fieldName, Object fieldValue) throws Exception;

    }
}
