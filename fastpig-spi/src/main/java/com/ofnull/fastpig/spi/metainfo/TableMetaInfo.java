package com.ofnull.fastpig.spi.metainfo;

import java.io.Serializable;
import java.util.List;


/**
 * @author ofnull
 * @date 2024/6/12 11:53
 */
public class TableMetaInfo implements Serializable {
    private Long id;

    private String catalog;

    private String schema = "";

    private String table;

    private String datasourceType;
    private String datasourceConnectInfo;

    private List<String> primaryKeys;
    private List<ColumnMetaInfo> columnMetadata;

    public String uuid() {
        return catalog + ":" + schema + ":" + table;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getDatasourceType() {
        return datasourceType;
    }

    public void setDatasourceType(String datasourceType) {
        this.datasourceType = datasourceType;
    }

    public String getDatasourceConnectInfo() {
        return datasourceConnectInfo;
    }

    public void setDatasourceConnectInfo(String datasourceConnectInfo) {
        this.datasourceConnectInfo = datasourceConnectInfo;
    }

    public List<String> getPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(List<String> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    public List<ColumnMetaInfo> getColumnMetadata() {
        return columnMetadata;
    }

    public void setColumnMetadata(List<ColumnMetaInfo> columnMetadata) {
        this.columnMetadata = columnMetadata;
    }
}
