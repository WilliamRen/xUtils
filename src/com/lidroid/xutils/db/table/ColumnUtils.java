/*
 * Copyright (c) 2013. wyouflf (wyouflf@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lidroid.xutils.db.table;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Foreign;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Transient;
import com.lidroid.xutils.util.LogUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;

public class ColumnUtils {

    private ColumnUtils() {
    }

    public static Method getColumnGetMethod(Class<?> entityType, Field field) {
        String fieldName = field.getName();
        Method getMethod = null;
        if (field.getType() == boolean.class) {
            getMethod = getBooleanColumnGetMethod(entityType, fieldName);
        }
        if (getMethod == null) {
            String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            try {
                getMethod = entityType.getDeclaredMethod(methodName);
            } catch (NoSuchMethodException e) {
                LogUtils.d(methodName + " not exist");
            }
        }
        return getMethod;
    }

    public static Method getColumnSetMethod(Class<?> entityType, Field field) {
        String fieldName = field.getName();
        Method setMethod = null;
        if (field.getType() == boolean.class) {
            setMethod = getBooleanColumnSetMethod(entityType, field);
        }
        if (setMethod == null) {
            String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            try {
                setMethod = entityType.getDeclaredMethod(methodName, field.getType());
            } catch (NoSuchMethodException e) {
                LogUtils.d(methodName + " not exist");
            }
        }
        return setMethod;
    }


    public static String getColumnNameByField(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && column.column().trim().length() > 0) {
            return column.column();
        }

        Id id = field.getAnnotation(Id.class);
        if (id != null && id.column().trim().length() != 0) {
            return id.column();
        }

        Foreign foreign = field.getAnnotation(Foreign.class);
        if (foreign != null && foreign.column().trim().length() > 0) {
            return foreign.column();
        }

        return field.getName();
    }

    public static String getForeignColumnNameByField(Field field) {

        Foreign foreign = field.getAnnotation(Foreign.class);
        if (foreign != null && foreign.column().trim().length() > 0) {
            return foreign.foreign();
        }

        return field.getName();
    }

    public static String getColumnDefaultValue(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && column.defaultValue() != null && column.defaultValue().trim().length() > 0) {
            return column.defaultValue();
        }
        return null;
    }

    public static boolean isTransient(Field field) {
        return field.getAnnotation(Transient.class) != null;
    }

    public static boolean isForeign(Field field) {
        return field.getAnnotation(Foreign.class) != null;
    }

    public static boolean isSimpleColumnType(Field field) {
        Class<?> clazz = field.getType();
        return isSimpleColumnType(clazz);
    }

    public static boolean isSimpleColumnType(Class columnType) {
        return columnType.isPrimitive() ||
                columnType.equals(String.class) ||
                columnType.equals(Integer.class) ||
                columnType.equals(Long.class) ||
                columnType.equals(Date.class) ||
                columnType.equals(java.sql.Date.class) ||
                columnType.equals(Boolean.class) ||
                columnType.equals(Float.class) ||
                columnType.equals(Double.class) ||
                columnType.equals(Byte.class) ||
                columnType.equals(Short.class) ||
                columnType.equals(CharSequence.class) ||
                columnType.equals(Character.class);
    }

    public static Object valueStr2SimpleColumnValue(Class columnType, String valueStr) {
        Object value = null;
        if (isSimpleColumnType(columnType) && valueStr != null) {
            if (columnType.equals(String.class) || columnType.equals(CharSequence.class)) {
                value = valueStr;
            } else if (columnType.equals(int.class) || columnType.equals(Integer.class)) {
                value = Integer.valueOf(valueStr);
            } else if (columnType.equals(long.class) || columnType.equals(Long.class)) {
                value = Long.valueOf(valueStr);
            } else if (columnType.equals(java.sql.Date.class)) {
                value = new java.sql.Date(Long.valueOf(valueStr));
            } else if (columnType.equals(Date.class)) {
                value = new Date(Long.valueOf(valueStr));
            } else if (columnType.equals(boolean.class) || columnType.equals(Boolean.class)) {
                value = ColumnUtils.convert2Boolean(valueStr);
            } else if (columnType.equals(float.class) || columnType.equals(Float.class)) {
                value = Float.valueOf(valueStr);
            } else if (columnType.equals(double.class) || columnType.equals(Double.class)) {
                value = Double.valueOf(valueStr);
            } else if (columnType.equals(byte.class) || columnType.equals(Byte.class)) {
                value = Byte.valueOf(valueStr);
            } else if (columnType.equals(short.class) || columnType.equals(Short.class)) {
                value = Short.valueOf(valueStr);
            } else if (columnType.equals(char.class) || columnType.equals(Character.class)) {
                value = valueStr.charAt(0);
            }
        }
        return value;
    }

    public static Boolean convert2Boolean(final Object value) {
        if (value != null) {
            String valueStr = value.toString();
            return valueStr.length() == 1 ? "1".equals(valueStr) : Boolean.valueOf(valueStr);
        }
        return false;
    }

    public static Object convertIfNeeded(final Object value) {
        if (value != null) {
            if (value instanceof Boolean) {
                return ((Boolean) value) ? 1 : 0;
            } else if (value instanceof java.sql.Date) {
                return ((java.sql.Date) value).getTime();
            } else if (value instanceof Date) {
                return ((Date) value).getTime();
            }
        }
        return value;
    }


    private static boolean isStartWithIs(String fieldName) {
        return fieldName != null && fieldName.startsWith("is");
    }

    private static Method getBooleanColumnGetMethod(Class<?> entityType, String fieldName) {
        String methodName = "is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        if (isStartWithIs(fieldName)) {
            methodName = fieldName;
        }
        try {
            return entityType.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            LogUtils.d(methodName + " not exist");
        }
        return null;
    }

    private static Method getBooleanColumnSetMethod(Class<?> entityType, Field field) {
        String fieldName = field.getName();
        String methodName = null;
        if (isStartWithIs(field.getName())) {
            methodName = "set" + fieldName.substring(2, 3).toUpperCase() + fieldName.substring(3);
        } else {
            methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        }
        try {
            return entityType.getDeclaredMethod(methodName, field.getType());
        } catch (NoSuchMethodException e) {
            LogUtils.d(methodName + " not exist");
        }
        return null;
    }

}
