package com.ofnull.fastpig.blocks.base;

import com.ofnull.fastpig.common.attach.ActionOperateAttached;
import com.ofnull.fastpig.common.attach.ServerProcessAttached;
import com.ofnull.fastpig.common.attach.WriteOperateAttached;
import com.ofnull.fastpig.common.finder.LoadedServices;
import com.ofnull.fastpig.common.metainfo.FieldUpdateImplLoader;
import com.ofnull.fastpig.common.metainfo.TableMetaLoader;
import com.ofnull.fastpig.common.utils.JsonUtil;
import com.ofnull.fastpig.common.utils.ValidatorUtil;
import com.ofnull.fastpig.spi.bulkloader.WriteOperateEnum;
import com.ofnull.fastpig.spi.fieldupdater.IFieldUpdater;
import com.ofnull.fastpig.spi.fieldupdater.IFieldUpdater.IRecordState;
import com.ofnull.fastpig.spi.jdbc.IJdbcConnection;
import com.ofnull.fastpig.spi.metainfo.ColumnMetaInfo;
import com.ofnull.fastpig.spi.metainfo.SourceClassDefinition;
import com.ofnull.fastpig.spi.metainfo.TableMetaInfo;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ofnull.fastpig.common.attach.ServerProcessAttached.SERVER_PROCESS_ATTACHED;

/**
 * 字段状态处理
 *
 * @author ofnull
 * @date 2024/6/25
 */
public class StateFieldConvertor extends KeyedProcessFunction<Integer, Map<String, Object>, Map<String, Object>> {
    private static final Logger LOG = LoggerFactory.getLogger(StateFieldConvertor.class);
    private static final String UPDATE_TIME = "_updateTime_";
    private static final WriteOperateEnum defaultWriteOperate = WriteOperateEnum.UPSERT;

    private TableMetaInfo tableMetaInfo;
    private TableMetaLoader tableMetaLoader;
    private IJdbcConnection jdbcConnection;
    private transient LoadedServices<IFieldUpdater> fieldUpdaterLoadedServices;
    private transient IRecordState recordState;

    public StateFieldConvertor(IJdbcConnection jdbcConnection, TableMetaLoader tableMetaLoader) {
        this.jdbcConnection = jdbcConnection;
        this.tableMetaLoader = tableMetaLoader;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        this.refreshTableMetaInfo();
        this.refreshFieldUpdateLoadedServices();

        MapStateDescriptor<String, Object> dbValueStateDescriptor = new MapStateDescriptor<>("dbValueState", TypeInformation.of(String.class), new TypeHint<Object>() {
        }.getTypeInfo());
        dbValueStateDescriptor.setQueryable("dbValueState");
        MapState<String, Object> dbValueState = getRuntimeContext().getMapState(dbValueStateDescriptor);

        MapStateDescriptor<String, Long> dbTimeStateDescriptor = new MapStateDescriptor<>("dbTimeState", TypeInformation.of(String.class), new TypeHint<Long>() {
        }.getTypeInfo());
        dbTimeStateDescriptor.setQueryable("dbTimeState");
        MapState<String, Long> dbTimeState = getRuntimeContext().getMapState(dbTimeStateDescriptor);
        this.recordState = new RecordStateWarp(dbValueState, dbTimeState);
    }


    @Override
    public void processElement(Map<String, Object> event, KeyedProcessFunction<Integer, Map<String, Object>, Map<String, Object>>.Context context, Collector<Map<String, Object>> out) throws Exception {
        ServerProcessAttached processAttached = JsonUtil.convert(event.get(SERVER_PROCESS_ATTACHED), ServerProcessAttached.class);
        ActionOperateAttached actionAttached = processAttached.getActionOperateAttached();
        ActionOperateAttached.OperationType actionType = actionAttached.getOperationType();
        if (actionType == null) {
            LOG.error("Unknown action, event:{}", JsonUtil.toJsonString(event));
            return;
        }
        String recordKey = getRecordKey(event);
        this.recordState.updateRecordKey(recordKey);
        final Long updateTime = MapUtils.getLong(event, UPDATE_TIME);
        switch (actionType) {
            case INIT:
                for (ColumnMetaInfo columnMeta : tableMetaInfo.getColumnMetadata()) {
                    IFieldUpdater service = fieldUpdaterLoadedServices.findService(columnMeta.getUpdatePolicy(), IFieldUpdater.class);
                    service.init(event, columnMeta, tableMetaInfo, updateTime, recordState);
                }
                processAttached.setWriteOperateAttached(WriteOperateAttached.of(defaultWriteOperate));
                event.put(ServerProcessAttached.SERVER_PROCESS_ATTACHED, processAttached.toMap());
                out.collect(event);
                return;
            case ADD:
                for (ColumnMetaInfo columnMeta : tableMetaInfo.getColumnMetadata()) {
                    IFieldUpdater service = fieldUpdaterLoadedServices.findService(columnMeta.getUpdatePolicy(), IFieldUpdater.class);
                    service.add(event, columnMeta, tableMetaInfo, updateTime, recordState);
                }
                processAttached.setWriteOperateAttached(WriteOperateAttached.of(defaultWriteOperate));
                event.put(ServerProcessAttached.SERVER_PROCESS_ATTACHED, processAttached.toMap());
                out.collect(event);
                return;

            case REMOVE:
                recordState.removeTimeState(tableMetaInfo.getColumnMetadata());
                recordState.removeValueState(tableMetaInfo.getColumnMetadata());
                processAttached.setWriteOperateAttached(WriteOperateAttached.of(WriteOperateEnum.DELETE));
                event.put(ServerProcessAttached.SERVER_PROCESS_ATTACHED, processAttached.toMap());
                out.collect(event);
                return;
            case MERGE:
                for (ColumnMetaInfo columnMeta : tableMetaInfo.getColumnMetadata()) {
                    IFieldUpdater service = fieldUpdaterLoadedServices.findService(columnMeta.getUpdatePolicy(), IFieldUpdater.class);
                    service.merge(event, columnMeta, tableMetaInfo, updateTime, recordState);
                }
                processAttached.setWriteOperateAttached(WriteOperateAttached.of(defaultWriteOperate));
                event.put(ServerProcessAttached.SERVER_PROCESS_ATTACHED, processAttached.toMap());
                out.collect(event);
                return;
            default:
                throw new IllegalArgumentException(" Action OperationType No Match!");
        }
    }

    private String getRecordKey(Map<String, Object> event) {
        List<Object> primaryValues = new ArrayList<>();
        for (String primaryKey : tableMetaInfo.getPrimaryKeys()) {
            Object value = event.get(primaryKey);
            primaryValues.add(value == null ? "" : value);
        }
        return StringUtils.join(primaryValues, ":");
    }

    private void refreshTableMetaInfo() throws Exception {
        this.tableMetaInfo = tableMetaLoader.loader();
    }

    private void refreshFieldUpdateLoadedServices() throws Exception {
        List<SourceClassDefinition> fieldUpdaterClasses = new FieldUpdateImplLoader(jdbcConnection).loader();
        LoadedServices<IFieldUpdater> oldLoadedServices = this.fieldUpdaterLoadedServices;
        this.fieldUpdaterLoadedServices = LoadedServices.of(fieldUpdaterClasses);
        initFieldUpdaterConfig(tableMetaInfo);
        if (oldLoadedServices != null) {
            oldLoadedServices.close();
        }
    }

    private void initFieldUpdaterConfig(TableMetaInfo tableMetaInfo) throws Exception {
        for (ColumnMetaInfo columnMeta : tableMetaInfo.getColumnMetadata()) {
            String updatePolicy = columnMeta.getUpdatePolicy();
            String updatePolicyArgs = columnMeta.getUpdatePolicyArgs();
            IFieldUpdater service = this.fieldUpdaterLoadedServices.findService(updatePolicy, IFieldUpdater.class);
            Class<?> argsType = service.argsType();
            if (StringUtils.isNotBlank(updatePolicyArgs)) {
                if (argsType == null) {
                    LOG.warn("FiledUpdater {} updatePolicyArgs nonNull but argsType null, column:{} table:{}", updatePolicy, columnMeta.getColumnName(), tableMetaInfo.getTable());
                    continue;
                }
                Object policyArgsInstance = JsonUtil.objectMapper().readValue(updatePolicyArgs, argsType);
                ValidatorUtil.validate(policyArgsInstance);
                columnMeta.setUpdatePolicyArgsInstance(policyArgsInstance);
            } else if (argsType != null) {
                Object noArgsInstance = argsType.newInstance();
                ValidatorUtil.validate(noArgsInstance);
                columnMeta.setUpdatePolicyArgsInstance(noArgsInstance);
            }
        }
    }

    public static class RecordStateWarp implements IRecordState {
        private final MapState<String, Object> dbValueState;
        private final MapState<String, Long> dbTimeState;
        private String recordKey;

        public RecordStateWarp(MapState<String, Object> dbValueState, MapState<String, Long> dbTimeState) {
            this.dbValueState = dbValueState;
            this.dbTimeState = dbTimeState;
        }

        @Override
        public void updateRecordKey(String recordKey) {
            this.recordKey = recordKey;
        }

        @Override
        public Object getValueState(Short index) throws Exception {
            return dbValueState.get(columnKey(index));
        }

        @Override
        public Long getTimeState(Short index) throws Exception {
            return dbTimeState.get(columnKey(index));
        }

        @Override
        public void updateValueState(Short index, Object state) throws Exception {
            if (state != null) {
                dbValueState.put(columnKey(index), state);
            }
        }

        @Override
        public void updateTimeState(Short index, Long state) throws Exception {
            if (state != null) {
                dbTimeState.put(columnKey(index), state);
            }
        }

        @Override
        public void removeValueState(Short index) throws Exception {
            dbValueState.remove(columnKey(index));
        }

        @Override
        public void removeTimeState(Short index) throws Exception {
            dbTimeState.remove(columnKey(index));
        }

        @Override
        public void removeValueState(List<ColumnMetaInfo> metadataList) throws Exception {
            for (ColumnMetaInfo columnMetaInfo : metadataList) {
                removeValueState(columnMetaInfo.getIndex());
            }
        }

        @Override
        public void removeTimeState(List<ColumnMetaInfo> metadataList) throws Exception {
            for (ColumnMetaInfo columnMetaInfo : metadataList) {
                removeTimeState(columnMetaInfo.getIndex());
            }
        }

        @Override
        public void appendStateField(Map<String, Object> event, String fieldName, Object fieldValue) throws Exception {
            event.put(fieldName, fieldValue);
        }

        private String columnKey(Short index) {
            return recordKey + ":" + index;
        }
    }

    public static class KeyByPartitionInfo implements KeySelector<Map<String, Object>, Integer> {
        @Override
        public Integer getKey(Map<String, Object> event) throws Exception {
            ServerProcessAttached processAttached = JsonUtil.convert(event.get(SERVER_PROCESS_ATTACHED), ServerProcessAttached.class);
            return processAttached.getPartitionAttached().getPartition();
        }
    }

}
