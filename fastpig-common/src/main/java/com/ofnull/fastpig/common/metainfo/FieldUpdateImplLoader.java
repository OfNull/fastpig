package com.ofnull.fastpig.common.metainfo;

import com.ofnull.fastpig.common.jdbc.SqlExecutor;
import com.ofnull.fastpig.spi.jdbc.IJdbcConnection;
import com.ofnull.fastpig.spi.metainfo.IMetaLoader;
import com.ofnull.fastpig.spi.metainfo.SourceClassDefinition;

import java.util.List;

/**
 * @author ofnull
 * @date 2024/6/21
 */
public class FieldUpdateImplLoader implements IMetaLoader<List<SourceClassDefinition>> {
    private IJdbcConnection connection;

    public FieldUpdateImplLoader(IJdbcConnection connection) {
        this.connection = connection;
    }

    @Override
    public List<SourceClassDefinition> loader() throws Exception {
        String sql = "select `name`, `definition` from `meta_field_update_impl`;";
        List<SourceClassDefinition> fieldUpdateImps = SqlExecutor.listQuery(connection, sql, SourceClassDefinition.class);
        return fieldUpdateImps;
    }
}
