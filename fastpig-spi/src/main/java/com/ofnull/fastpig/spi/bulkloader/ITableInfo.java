package com.ofnull.fastpig.spi.bulkloader;

import java.util.Map;

/**
 * @author ofnull
 * @date 2024/6/12 11:54
 */
public interface ITableInfo {

    String getSchema();

    String getDatasource();

    String getTable();

    String getDbType();

    Map<String, Object> getConnectInfo();

    Map<String, Object> getTableConfig();
}
