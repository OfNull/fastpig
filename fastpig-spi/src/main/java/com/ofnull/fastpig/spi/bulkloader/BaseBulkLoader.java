package com.ofnull.fastpig.spi.bulkloader;

import com.ofnull.fastpig.spi.columntransform.IKeyTransform;
import com.ofnull.fastpig.spi.metainfo.ColumnMetaInfo;
import com.ofnull.fastpig.spi.metainfo.TableMetaInfo;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.Serializable;
import java.util.*;

/**
 * @author ofnull
 * @date 2024/6/12 11:40
 */
public abstract class BaseBulkLoader<R extends IPreparedWrite> implements Closeable, IWriteOperate<R>, Serializable {
    protected static final String SEPARATOR = ":";

    public abstract void init(int parallelism, TableMetaInfo tableMetaInfo) throws Exception;

    public R prepareWrite(int partition, WriteOperateEnum operateType, Map<String, Object> event, List<ColumnMetaInfo> columnMetaInfos) throws Exception {
        R prepareWrite = innerPrepareWrite(partition, operateType, event, columnMetaInfos);
        return prepareWrite;
    }

    public abstract void doBatchWrite(int partition, ExecBatch execBatch) throws Exception;

    public abstract IKeyTransform finderStorageTransform(String name);

    protected R innerPrepareWrite(int partition, WriteOperateEnum operateEnum, Map<String, Object> event, List<ColumnMetaInfo> columnMetaInfos) throws Exception {
        switch (operateEnum) {
            case INSERT:
                return insert(event, columnMetaInfos);
            case INSERT_IGNORE:
                return insertIgnore(event, columnMetaInfos);
            case UPDATE:
                return update(event, columnMetaInfos);
            case UPSERT:
                return upsert(event, columnMetaInfos);
            case DELETE:
                return delete(event, columnMetaInfos);
            default:
                throw new RuntimeException("Unsupported operation! {}" + operateEnum.name());
        }
    }

    protected EventParseResult dataParse(Map<String, Object> event, List<ColumnMetaInfo> columnMetaInfos) {
        Map<String, Object> source = new HashMap<>(columnMetaInfos.size());
        List<Object> pks = new ArrayList<>();
        int counter = 0;
        for (ColumnMetaInfo metaInfo : columnMetaInfos) {
            String fieldName = metaInfo.getFieldName();
            Object fieldValue = event.get(fieldName);
            if (!event.containsKey(fieldName)) {
                continue;
            }

            source.put(metaInfo.getColumnName(), fieldValue);
            if (!metaInfo.getPrimaryKey()) {
                counter++;
            } else {
                if (StringUtils.isNotEmpty(metaInfo.getStorageTransform())) {
                    IKeyTransform keyTransform = finderStorageTransform(metaInfo.getStorageTransform());
                    if (keyTransform == null) {
                        throw new RuntimeException("No such column storage transform " + metaInfo.getColumnType() + " for column "
                                + metaInfo.getFieldName());
                    }
                    fieldValue = keyTransform.beforeWrite(fieldValue);
                }
                pks.add(Optional.ofNullable(fieldValue).orElse(""));
            }
        }

        return new EventParseResult(source, pks, counter);
    }

    public static class EventParseResult implements Serializable {
        Map<String, Object> source;
        List<Object> pks;
        int counter = 0;

        public EventParseResult(Map<String, Object> source, List<Object> pks, int counter) {
            this.source = source;
            this.pks = pks;
            this.counter = counter;
        }

        public Map<String, Object> getSource() {
            return source;
        }

        public void setSource(Map<String, Object> source) {
            this.source = source;
        }

        public List<Object> getPks() {
            return pks;
        }

        public void setPks(List<Object> pks) {
            this.pks = pks;
        }

        public int getCounter() {
            return counter;
        }

        public void setCounter(int counter) {
            this.counter = counter;
        }
    }

}
