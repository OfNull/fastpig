package com.ofnull.fastpig.connector.jdbc.bulkloader;

import cn.hutool.db.sql.SqlExecutor;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.ofnull.fastpig.common.utils.JsonUtil;
import com.ofnull.fastpig.common.utils.ValidatorUtil;
import com.ofnull.fastpig.spi.anno.PigType;
import com.ofnull.fastpig.spi.bulkloader.BaseBulkLoader;
import com.ofnull.fastpig.spi.bulkloader.ExecBatch;
import com.ofnull.fastpig.spi.bulkloader.IPreparedWrite;
import com.ofnull.fastpig.spi.columntransform.IKeyTransform;
import com.ofnull.fastpig.spi.metainfo.ColumnMetaInfo;
import com.ofnull.fastpig.spi.metainfo.TableMetaInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.join;

/**
 * @author kun.zhou
 * @date 2024/7/16
 */
@PigType("jdbc")
public class MysqlBulkLoader extends BaseBulkLoader<MysqlBulkLoader.SqlWrap> {
    private static final Logger LOG = LoggerFactory.getLogger(MysqlBulkLoader.class);
    private DruidDataSource dataSource;
    private TableMetaInfo tableMetaInfo;

    @Override
    public void init(int parallelism, TableMetaInfo tableMetaInfo) throws Exception {
        this.tableMetaInfo = tableMetaInfo;
        JdbcConfig jdbcConfig = JsonUtil.tryRead(tableMetaInfo.getDatasourceConnectInfo(), JdbcConfig.class);
        LOG.debug("JDBC Config 【{}】", tableMetaInfo.getDatasourceConnectInfo());
        ValidatorUtil.validate(jdbcConfig);
        this.dataSource = jdbcConfig.toDruidDataSource();
    }

    @Override
    public void doBatchWrite(int partition, ExecBatch execBatch) throws Exception {
        String sql = execBatch.getBatchFlag();
        if (!StringUtils.isNotBlank(sql)) {
            throw new IllegalArgumentException("sql is null");
        }
        List<Object[]> valuesList = (List<Object[]>) execBatch.getDataList();
        try (DruidPooledConnection connection = this.dataSource.getConnection();) {
            SqlExecutor.execute(connection, sql, valuesList);
        }
    }

    @Override
    public IKeyTransform finderStorageTransform(String name) {
        throw new UnsupportedOperationException("jdbc UnsupportedOperation");
    }

    @Override
    public SqlWrap insert(Map<String, Object> event, List<ColumnMetaInfo> columnMetaInfos) {
        List<String> fields = new ArrayList(columnMetaInfos.size());
        List<Object> values = new ArrayList(columnMetaInfos.size());
        for (ColumnMetaInfo metaInfo : columnMetaInfos) {
            Object value = event.get(metaInfo.getFieldName());
            fields.add(String.format("`%s`", metaInfo.getColumnName()));
            values.add(value);
        }
        String sql = String.format("INSERT INTO %s(%s) VALUES (%s)",
                tableMetaInfo.getTable(), StringUtils.join(fields, ","), StringUtils.repeat("?", ",", fields.size()));
        return new SqlWrap(sql, values.toArray());
    }

    @Override
    public SqlWrap insertIgnore(Map<String, Object> event, List<ColumnMetaInfo> columnMetaInfos) {
        List<String> fields = new ArrayList(columnMetaInfos.size());
        List<Object> values = new ArrayList(columnMetaInfos.size());
        for (ColumnMetaInfo metaInfo : columnMetaInfos) {
            Object value = event.get(metaInfo.getFieldName());
            fields.add(String.format("`%s`", metaInfo.getColumnName()));
            values.add(value);
        }
        String sql = String.format("INSERT IGNORE %s(%s) VALUES (%s)",
                tableMetaInfo.getTable(), StringUtils.join(fields, ","), StringUtils.repeat("?", ",", fields.size()));
        return new SqlWrap(sql, values.toArray());
    }

    @Override
    public SqlWrap update(Map<String, Object> event, List<ColumnMetaInfo> columnMetaInfos) {
        List<String> fields = new ArrayList();
        List<Object> values = new ArrayList();
        List<String> whereFields = new ArrayList();
        List<Object> whereValues = new ArrayList();
        for (ColumnMetaInfo metaInfo : columnMetaInfos) {
            Object value = event.get(metaInfo.getFieldName());
            if (metaInfo.getPrimaryKey()) {
                whereFields.add(String.format("`%s`=?", metaInfo.getColumnName()));
                whereValues.add(value);
            } else {
                fields.add(String.format("`%s`=?", metaInfo.getColumnName()));
                values.add(value);
            }
        }
        values.addAll(whereValues);
        String sql = String.format("UPDATE %s SET %s WHERE %s", tableMetaInfo.getTable(),
                String.join(",", fields), String.join(",", whereFields));
        return new SqlWrap(sql, values.toArray());
    }

    @Override
    public SqlWrap delete(Map<String, Object> event, List<ColumnMetaInfo> columnMetaInfos) {
        List<String> whereFields = new ArrayList();
        List<Object> whereArgs = new ArrayList();
        for (ColumnMetaInfo metaInfo : columnMetaInfos) {
            String field = metaInfo.getColumnName();
            Object value = event.get(metaInfo.getFieldName());
            if (metaInfo.getPrimaryKey()) {
                whereFields.add(String.format("`%s`=?", field));
                whereArgs.add(value);
            }
        }
        String sql = String.format("DELETE FROM %s WHERE %s", tableMetaInfo.getTable(), join(whereFields, " and "));
        return new SqlWrap(sql, whereArgs.toArray());
    }

    @Override
    public SqlWrap upsert(Map<String, Object> event, List<ColumnMetaInfo> columnMetaInfos) {
        List<String> fields = new ArrayList();
        List<String> duplicateFields = new ArrayList();
        List<Object> values = new ArrayList();
        for (ColumnMetaInfo metaInfo : columnMetaInfos) {
            Object value = event.get(metaInfo.getFieldName());
            fields.add(String.format("`%s`", metaInfo.getColumnName()));
            values.add(value);
            if (!metaInfo.getPrimaryKey()) {
                duplicateFields.add(String.format("`%s`=values(`%s`)", metaInfo.getColumnName(), metaInfo.getColumnName()));
            }
        }
        String sql = String.format("INSERT IGNORE %s(%s) VALUES (%s) ON DUPLICATE KEY UPDATE %s", tableMetaInfo.getTable(),
                StringUtils.join(fields, ","), StringUtils.repeat("?", ",", fields.size()),
                StringUtils.join(duplicateFields, ","));

        return new SqlWrap(sql, values.toArray());
    }

    @Override
    public void close() throws IOException {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public static class SqlWrap implements IPreparedWrite {
        private String sql;
        private Object[] values;

        public SqlWrap(String sql, Object[] values) {
            this.sql = sql;
            this.values = values;
        }

        @Override
        public boolean isNotEmpty() {
            return values != null && values.length > 0;
        }

        @Override
        public Object[] getData() {
            return values;
        }

        @Override
        public String batchFlag() {
            return sql;
        }

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        public Object[] getValues() {
            return values;
        }

        public void setValues(Object[] values) {
            this.values = values;
        }
    }
}
