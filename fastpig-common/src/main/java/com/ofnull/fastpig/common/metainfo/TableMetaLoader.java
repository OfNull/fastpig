package com.ofnull.fastpig.common.metainfo;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.ofnull.fastpig.common.jdbc.SqlExecutor;
import com.ofnull.fastpig.spi.jdbc.IJdbcConnection;
import com.ofnull.fastpig.spi.metainfo.ColumnMetaInfo;
import com.ofnull.fastpig.spi.metainfo.IMetaLoader;
import com.ofnull.fastpig.spi.metainfo.TableMetaInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ofnull
 * @date 2024/6/14 16:44
 */
public class TableMetaLoader implements IMetaLoader<TableMetaInfo> {
    private static final Logger LOG = LoggerFactory.getLogger(TableMetaLoader.class);
    private IJdbcConnection connection;
    private TableConfig tableConfig;

    public TableMetaLoader() {
    }

    public TableMetaLoader(IJdbcConnection connection, TableConfig tableConfig) {
        this.connection = connection;
        this.tableConfig = tableConfig;
    }


    private String querySql() {
        String sql = "select b.id, b.`catalog` , b.`schema` , b.`table` , a.`type` as `datasource_type`, a.connect_info as `datasource_connect_info` \n" +
                "from meta_datasource a inner join meta_table b on a.`catalog`  = b.`catalog`  \n" +
                "where b.`catalog` = '%s' and b.`schema` = '%s' and b.`table` = '%s';  ";
        String formatSql = String.format(sql, tableConfig.getCatalog(), tableConfig.getSchema(), tableConfig.getTable());
        return formatSql;
    }

    @Override
    public TableMetaInfo loader() throws Exception {
        TableMetaInfo tableMetaInfo = SqlExecutor.executeQuery(connection, querySql(), TableMetaInfo.class);
        Preconditions.checkArgument(
                tableMetaInfo.getId() != null, "table config db not find.");
        List<ColumnMetaInfo> columnMetaInfo = columnMetaLoader(tableMetaInfo.getId());
        List<String> primaryKeys = columnMetaInfo.stream()
                .filter(v -> v.getPrimaryKey())
                .map(v -> v.getFieldName()).collect(Collectors.toList());
        tableMetaInfo.setPrimaryKeys(primaryKeys);
        tableMetaInfo.setColumnMetadata(columnMetaInfo);
        return tableMetaInfo;
    }

    public List<ColumnMetaInfo> columnMetaLoader(Long tableId) throws Exception {
        String sql = String.format("select * from meta_column where `table_id` = %s order by  orders , id;", tableId);
        List<ColumnMetaInfo> columnMetaList = SqlExecutor.listQuery(connection, sql, ColumnMetaInfo.class);
        Preconditions.checkArgument(
                columnMetaList != null, "table column config db not find.");
        short index = 0;
        for (ColumnMetaInfo info : columnMetaList) {
            info.setIndex(index);
            if (StringUtils.isBlank(info.getFieldName())) {
                String text = StringUtils.lowerCase(info.getColumnName());
                info.setFieldName(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, text));
            }
        }
        return columnMetaList;
    }

    public IJdbcConnection getConnection() {
        return connection;
    }

    public void setConnection(IJdbcConnection connection) {
        this.connection = connection;
    }

    public TableConfig getTableConfig() {
        return tableConfig;
    }

    public void setTableConfig(TableConfig tableConfig) {
        this.tableConfig = tableConfig;
    }
}
