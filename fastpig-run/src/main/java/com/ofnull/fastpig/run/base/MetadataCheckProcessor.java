package com.ofnull.fastpig.run.base;

import com.ofnull.fastpig.common.finder.ServiceLoaderHelper;
import com.ofnull.fastpig.common.utils.JsonUtil;
import com.ofnull.fastpig.spi.columntype.IColumnType;
import com.ofnull.fastpig.spi.metainfo.ColumnMetaInfo;
import com.ofnull.fastpig.spi.metainfo.TableMetaInfo;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author ofnull
 * @date 2024/6/18
 */
public class MetadataCheckProcessor extends ProcessFunction<Map<String, Object>, Map<String, Object>> {
    private static final Logger LOG = LoggerFactory.getLogger(MetadataCheckProcessor.class);
    private final TableMetaInfo tableMetaInfo;
    private Map<String, IColumnType> columnTypes;

    public MetadataCheckProcessor(TableMetaInfo tableMetaInfo) {
        this.tableMetaInfo = tableMetaInfo;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        HashMap columnTypes = new HashMap<>();
        for (ColumnMetaInfo columnMetaInfo : tableMetaInfo.getColumnMetadata()) {
            IColumnType columnType = ServiceLoaderHelper.loadServices(IColumnType.class, columnMetaInfo.getColumnType());
            columnTypes.put(columnMetaInfo.getColumnType(), columnType);
        }
        this.columnTypes = columnTypes;
    }

    @Override
    public void processElement(Map<String, Object> event, ProcessFunction<Map<String, Object>, Map<String, Object>>.Context context, Collector<Map<String, Object>> collector) throws Exception {
        try {
            for (ColumnMetaInfo columnMetaInfo : tableMetaInfo.getColumnMetadata()) {
                String fieldName = columnMetaInfo.getFieldName();
                Object filedValue = event.get(fieldName);
                if (Objects.isNull(filedValue)) {
                    if (columnMetaInfo.getNullable() == 0) {
                        String reason = String.format("field %s is null but column %s require not null", fieldName, fieldName);
                        throw new IllegalArgumentException(reason);
                    }
                    continue;
                }
                IColumnType columnType = columnTypes.get(columnMetaInfo.getColumnType());
                if (Objects.isNull(columnType)) {
                    throw new RuntimeException("No such column type " + columnMetaInfo.getColumnType());
                }
                Object validate = columnType.validate(filedValue, columnMetaInfo);
                event.put(fieldName, validate);
            }
            collector.collect(event);
        } catch (Exception e) {
            //TODO 侧输出流收集异常？
            LOG.warn("Metadata check Exception Discard data, event:{}", JsonUtil.toJsonString(event), e);
        }
    }

}
