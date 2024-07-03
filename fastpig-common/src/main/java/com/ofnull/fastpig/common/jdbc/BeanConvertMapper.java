package com.ofnull.fastpig.common.jdbc;

import com.google.common.base.CaseFormat;
import com.ofnull.fastpig.common.utils.JsonUtil;
import com.ofnull.fastpig.spi.anno.Convert;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author ofnull
 * @date 2022/9/16 17:27
 */
public class BeanConvertMapper<T> {
    private Class<T> mappedClass;
    private Map<String, Field> mappedFields;
    private Set<String> mappedProperties;

    public BeanConvertMapper() {
    }

    public BeanConvertMapper(Class<T> mappedClass) {
        this.init(mappedClass);
    }

    protected void init(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
        this.mappedFields = new HashMap();
        this.mappedProperties = new HashSet();
        Field[] fields = mappedClass.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            mappedFields.put(fieldName, field);
            mappedProperties.add(fieldName);
        }
    }

    public T convertRow(ResultSet rs, int rowNumber) throws SQLException, IllegalAccessException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        T instant = this.instantiate(this.mappedClass);
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            String name = rsmd.getColumnLabel(columnIndex); //别名
            if (name == null || name.length() < 1) {
                name = rsmd.getColumnName(columnIndex);
            }
            String lowerName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name.replaceAll(" ", ""));
            if (mappedFields.containsKey(lowerName)) {
                Field field = mappedFields.get(lowerName);
                field.setAccessible(true);
                Convert convert = field.getAnnotation(Convert.class);
                Object value = getResultSetValue(rs, columnIndex, field.getType());
                if (convert != null && "json".equalsIgnoreCase(convert.type())) {
                    if (field.getType().isEnum()) {
                        field.set(instant, JsonUtil.tryRead(JsonUtil.toJsonString(value), field.getType()));
                    } else {
                        field.set(instant, JsonUtil.tryRead(value.toString(), field.getType()));
                    }
                } else {
                    field.set(instant, value);
                }

            }
        }
        return instant;
    }

    protected T instantiate(Class<T> clazz) {
        if (clazz.isInterface()) {
            throw new RuntimeException(clazz.getName() + " Specified class is an interface");
        }
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(clazz.getName() + "Is it an abstract class?", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(clazz.getName() + "Is the constructor accessible?", e);
        }
    }

    public static Object getResultSetValue(ResultSet rs, int index, Class<?> requiredType) throws SQLException {
        if (String.class.equals(requiredType)) {
            return rs.getString(index);
        } else {
            Object value;
            if (!Boolean.TYPE.equals(requiredType) && !Boolean.class.equals(requiredType)) {
                if (!Byte.TYPE.equals(requiredType) && !Byte.class.equals(requiredType)) {
                    if (!Short.TYPE.equals(requiredType) && !Short.class.equals(requiredType)) {
                        if (!Integer.TYPE.equals(requiredType) && !Integer.class.equals(requiredType)) {
                            if (!Long.TYPE.equals(requiredType) && !Long.class.equals(requiredType)) {
                                if (!Float.TYPE.equals(requiredType) && !Float.class.equals(requiredType)) {
                                    if (!Double.TYPE.equals(requiredType) && !Double.class.equals(requiredType) && !Number.class.equals(requiredType)) {
                                        if (BigDecimal.class.equals(requiredType)) {
                                            return rs.getBigDecimal(index);
                                        }

                                        if (Date.class.equals(requiredType)) {
                                            return rs.getDate(index);
                                        }

                                        if (Time.class.equals(requiredType)) {
                                            return rs.getTime(index);
                                        }

                                        if (!Timestamp.class.equals(requiredType) && !java.util.Date.class.equals(requiredType)) {
                                            if (byte[].class.equals(requiredType)) {
                                                return rs.getBytes(index);
                                            }

                                            if (Blob.class.equals(requiredType)) {
                                                return rs.getBlob(index);
                                            }

                                            if (Clob.class.equals(requiredType)) {
                                                return rs.getClob(index);
                                            }
                                            // ------

                                            return getResultSetValue(rs, index);
                                        }

                                        return rs.getTimestamp(index);
                                    }

                                    value = rs.getDouble(index);
                                } else {
                                    value = rs.getFloat(index);
                                }
                            } else {
                                value = rs.getLong(index);
                            }
                        } else {
                            value = rs.getInt(index);
                        }
                    } else {
                        value = rs.getShort(index);
                    }
                } else {
                    value = rs.getByte(index);
                }
            } else {
                value = rs.getBoolean(index);
            }
            return rs.wasNull() ? null : value;
        }

    }

    public static Object getResultSetValue(ResultSet rs, int index) throws SQLException {
        Object obj = rs.getObject(index);
        String className = null;
        if (obj != null) {
            className = obj.getClass().getName();
        }

        if (obj instanceof Blob) {
            Blob blob = (Blob) obj;
            obj = blob.getBytes(1L, (int) blob.length());
        } else if (obj instanceof Clob) {
            Clob clob = (Clob) obj;
            obj = clob.getSubString(1L, (int) clob.length());
        } else if (!"oracle.sql.TIMESTAMP".equals(className) && !"oracle.sql.TIMESTAMPTZ".equals(className)) {
            if (className != null && className.startsWith("oracle.sql.DATE")) {
                String metaDataClassName = rs.getMetaData().getColumnClassName(index);
                if (!"java.sql.Timestamp".equals(metaDataClassName) && !"oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
                    obj = rs.getDate(index);
                } else {
                    obj = rs.getTimestamp(index);
                }
            } else if (obj != null && obj instanceof Date && "java.sql.Timestamp".equals(rs.getMetaData().getColumnClassName(index))) {
                obj = rs.getTimestamp(index);
            }
        } else {
            obj = rs.getTimestamp(index);
        }

        return obj;
    }
}
