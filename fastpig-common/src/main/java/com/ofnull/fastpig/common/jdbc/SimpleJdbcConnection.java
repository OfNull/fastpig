package com.ofnull.fastpig.common.jdbc;

import com.ofnull.fastpig.spi.jdbc.IJdbcConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

/**
 * @author ofnull
 */
public class SimpleJdbcConnection implements IJdbcConnection, Serializable {
    public static final Logger LOG = LoggerFactory.getLogger(SimpleJdbcConnection.class);
    private final JdbcConnectionConfig jdbcConfig;

    private transient Connection connection;

    static {
        DriverManager.getDrivers();
    }

    public SimpleJdbcConnection(JdbcConnectionConfig jdbcConfig) {
        this.jdbcConfig = jdbcConfig;
    }

    @Nullable
    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public boolean isConnectionValid() throws SQLException {
        return connection != null
                && connection.isValid(jdbcConfig.getConnectionCheckTimeoutSeconds());
    }

    @Override
    public Connection getOrEstablishConnection() throws SQLException {
        if (isConnectionValid()) {
            return connection;
        }
        connection = DriverManager.getConnection(
                jdbcConfig.getUrl(),
                Optional.ofNullable(jdbcConfig.getUsername()).orElse(null),
                Optional.ofNullable(jdbcConfig.getPassword()).orElse(null));
        return connection;
    }

    @Override
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOG.warn("JDBC connection close failed.", e);
            } finally {
                connection = null;
            }
        }
    }

    @Override
    public Connection reestablishConnection() throws SQLException {
        closeConnection();
        return getOrEstablishConnection();
    }
}
