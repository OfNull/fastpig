package com.ofnull.fastpig.common.jdbc;

import com.ofnull.fastpig.spi.jdbc.IJdbcConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ofnull
 * @date 2022/9/16 16:15
 */
public class SqlExecutor {
    public SqlExecutor() {
    }

    public static <T> T executeQuery(Connection connection, String sql, Class<T> clazz) throws SQLException, IllegalAccessException, InstantiationException {
        BeanConvertMapper<T> convertMapper = new BeanConvertMapper(clazz);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);) {
            ResultSet rs = preparedStatement.executeQuery();
            int index = 1;
            while (rs.next()) {
                T t = convertMapper.convertRow(rs, index++);
                return t;
            }
            return null;
        }
    }

    public static <T> List<T> listQuery(Connection connection, String sql, Class<T> clazz) throws SQLException, IllegalAccessException, InstantiationException {
        List<T> result = new ArrayList<>();
        BeanConvertMapper<T> convertMapper = new BeanConvertMapper(clazz);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);) {
            ResultSet rs = preparedStatement.executeQuery();
            int index = 1;
            while (rs.next()) {
                T t = convertMapper.convertRow(rs, index++);
                result.add(t);
            }

        }
        return result;
    }


    public static <T> T executeQuery(IJdbcConnection connection, String sql, Class<T> clazz) throws SQLException, IllegalAccessException, InstantiationException {
        BeanConvertMapper<T> convertMapper = new BeanConvertMapper(clazz);
        try (PreparedStatement preparedStatement = connection.reestablishConnection().prepareStatement(sql);) {
            ResultSet rs = preparedStatement.executeQuery();
            int index = 1;
            while (rs.next()) {
                T t = convertMapper.convertRow(rs, index++);
                return t;
            }
            return null;
        }
    }

    public static <T> List<T> listQuery(IJdbcConnection connection, String sql, Class<T> clazz) throws SQLException, IllegalAccessException, InstantiationException {
        List<T> result = new ArrayList<>();
        BeanConvertMapper<T> convertMapper = new BeanConvertMapper(clazz);
        try (PreparedStatement preparedStatement = connection.getOrEstablishConnection().prepareStatement(sql);) {
            ResultSet rs = preparedStatement.executeQuery();
            int index = 1;
            while (rs.next()) {
                T t = convertMapper.convertRow(rs, index++);
                result.add(t);
            }

        }
        return result;
    }

}
