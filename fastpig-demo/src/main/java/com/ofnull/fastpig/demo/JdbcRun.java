package com.ofnull.fastpig.demo;

import com.ofnull.fastpig.common.jdbc.JdbcConnectionConfig;
import com.ofnull.fastpig.common.jdbc.SimpleJdbcConnection;
import com.ofnull.fastpig.spi.jdbc.IJdbcConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author ofnull
 * @date 2024/6/19
 */
public class JdbcRun {
    public static void main(String[] args) throws SQLException {
        JdbcConnectionConfig config = new JdbcConnectionConfig();
        config.setUrl("jdbc:mysql://restore.saas-db-publicqa.mysql.internal.weimob.com:3306/db_saas_cdp_tag_main?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&serverTimezone=Asia/Shanghai&useLocalSessionState=true");
        config.setUsername("cdp_qa_w");
        config.setPassword("w5oHfrPTKd!oO8BJlxZA");

        IJdbcConnection jdbcConnection = new SimpleJdbcConnection(config);
        Connection connection = jdbcConnection.getOrEstablishConnection();



        jdbcConnection.closeConnection();

    }
}
