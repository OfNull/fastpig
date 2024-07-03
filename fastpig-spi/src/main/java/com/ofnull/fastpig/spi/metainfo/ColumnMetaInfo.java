package com.ofnull.fastpig.spi.metainfo;

import java.io.Serializable;

/**
 * @author ofnull
 * @date 2024/6/12 15:50
 */
public class ColumnMetaInfo implements Serializable {
    //该字段作为状态唯一判断  ColumnMeta 一旦配置了且已经产生了状态，请按照顺序新增，请勿随意将字段插入已新增位置前
    private short index;
    private String fieldName;
    private String columnName;
    private String columnType;
    private int columnSize;
    private int nullable;
    private int orders;
    private boolean primaryKey;
    private String storageTransform;

    private String updatePolicy;
    private String updatePolicyArgs;
    private Object updatePolicyArgsInstance;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }

    public int getNullable() {
        return nullable;
    }

    public void setNullable(int nullable) {
        this.nullable = nullable;
    }

    public boolean getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getStorageTransform() {
        return storageTransform;
    }

    public void setStorageTransform(String storageTransform) {
        this.storageTransform = storageTransform;
    }

    public int getOrders() {
        return orders;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public String getUpdatePolicy() {
        return updatePolicy;
    }

    public void setUpdatePolicy(String updatePolicy) {
        this.updatePolicy = updatePolicy;
    }

    public String getUpdatePolicyArgs() {
        return updatePolicyArgs;
    }

    public void setUpdatePolicyArgs(String updatePolicyArgs) {
        this.updatePolicyArgs = updatePolicyArgs;
    }

    public Object getUpdatePolicyArgsInstance() {
        return updatePolicyArgsInstance;
    }

    public void setUpdatePolicyArgsInstance(Object updatePolicyArgsInstance) {
        this.updatePolicyArgsInstance = updatePolicyArgsInstance;
    }

    public short getIndex() {
        return index;
    }

    public void setIndex(short index) {
        this.index = index;
    }
}
