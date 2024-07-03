package com.ofnull.fastpig.spi.jdbc;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author ofnull
 * @date 2024/6/13 16:35
 */
public interface IJdbcConnection {
    @Nullable
    Connection getConnection();

    boolean isConnectionValid() throws SQLException;

    Connection getOrEstablishConnection() throws SQLException;

    void closeConnection();

    Connection reestablishConnection() throws SQLException;
}
