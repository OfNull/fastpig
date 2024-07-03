package com.ofnull.fastpig.common.jdbc;

import java.io.Serializable;

/**
 * @author ofnull
 */
public class JdbcConnectionConfig implements Serializable {
    private String url;
    private String username;
    private String password;
    private String driverName;
    private int connectionCheckTimeoutSeconds = 60;

    public JdbcConnectionConfig() {
    }

    public JdbcConnectionConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public JdbcConnectionConfig(String url, String username, String password, String driverName, int connectionCheckTimeoutSeconds) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverName = driverName;
        this.connectionCheckTimeoutSeconds = connectionCheckTimeoutSeconds;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public int getConnectionCheckTimeoutSeconds() {
        return connectionCheckTimeoutSeconds;
    }

    public void setConnectionCheckTimeoutSeconds(int connectionCheckTimeoutSeconds) {
        this.connectionCheckTimeoutSeconds = connectionCheckTimeoutSeconds;
    }
}
