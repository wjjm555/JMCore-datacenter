package com.wjjm.vehicle.datacentercore.util;

import android.content.ContentValues;
import android.text.TextUtils;

import java.util.Set;
import java.util.regex.Pattern;

public class SQLiteQueryUtil {

    private static final Pattern sLimitPattern = Pattern.compile("\\s*\\d+\\s*(,\\s*\\d+\\s*)?");

    public static final String[] CONFLICT_VALUES = new String[]
            {"", " OR ROLLBACK ", " OR ABORT ", " OR FAIL ", " OR IGNORE ", " OR REPLACE "};

    public static String query(String tables, String[] projection, String where, String[] selectionArgs, String orderBy) {

        StringBuilder query = new StringBuilder(120);

        query.append("SELECT ");

        if (projection != null && projection.length != 0) {
            appendColumns(query, projection);
        } else {
            query.append("* ");
        }
        query.append("FROM ");
        query.append(tables);
        appendClause(query, " WHERE ", where);
        query.append(" IN (");
        if (selectionArgs != null && selectionArgs.length != 0) {
            appendColumns(query, selectionArgs);
        }
        query.append(')');
        appendClause(query, " ORDER BY ", orderBy);
        return query.toString();
    }

    public static String insert(String table, ContentValues initialValues) {

        int conflictAlgorithm = 5;
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT");
        sql.append(CONFLICT_VALUES[conflictAlgorithm]);
        sql.append(" INTO ");
        sql.append(table);
        sql.append('(');

        if (initialValues != null) {
            Set<String> keys = initialValues.keySet();
            if (keys.size() > 0) {
                String[] values = new String[keys.size()];
                int i = 0;
                for (String key : keys) {
                    values[i] = initialValues.getAsString(key);
                    sql.append((i++ > 0) ? "," : "");
                    sql.append(key);
                }
                sql.append(')');
                sql.append(" VALUES (");
                for (i = 0; i < values.length; ++i) {
                    sql.append((i > 0) ? "," : "");
                    sql.append(values[i]);
                }
            } else {
                sql.append("null ) VALUES (NULL");
            }
        }
        sql.append(')');

        return sql.toString();

    }

    public static String delete(String table, String whereClause, String[] whereArgs) {

        StringBuilder sql = new StringBuilder();

        sql.append("DELETE FROM ");
        sql.append(table);

        if (!TextUtils.isEmpty(whereClause)) {
            sql.append(" WHERE ");
            sql.append(whereClause);
            sql.append(" IN (");
            appendColumns(sql, whereArgs);
            sql.append(')');
        }

        return sql.toString();
    }

    public static String update(String table, ContentValues values, String whereClause, String[] whereArgs) {

        int conflictAlgorithm = 0;

        StringBuilder sql = new StringBuilder(120);
        sql.append("UPDATE ");
        sql.append(CONFLICT_VALUES[conflictAlgorithm]);
        sql.append(table);
        sql.append(" SET ");

        // move all bind args to one array
        int i = 0;
        for (String colName : values.keySet()) {
            sql.append((i++ > 0) ? "," : "");
            sql.append(colName);
            sql.append("=");
            sql.append(values.get(colName));
        }
//        if (whereArgs != null) {
//            for (i = setValuesSize; i < bindArgsSize; i++) {
//                bindArgs[i] = whereArgs[i - setValuesSize];
//            }
//        }
        if (!TextUtils.isEmpty(whereClause)) {
            sql.append(" WHERE ");
            sql.append(whereClause);
            sql.append(" IN (");
            if (whereArgs != null && whereArgs.length != 0) {
                appendColumns(sql, whereArgs);
            }
            sql.append(')');
        }

        return sql.toString();

    }

    public static void appendColumns(StringBuilder s, String[] columns) {
        int n = columns.length;

        for (int i = 0; i < n; i++) {
            String column = columns[i];

            if (column != null) {
                if (i > 0) {
                    s.append(", ");
                }
                s.append(column);
            }
        }
        s.append(' ');
    }

    private static void appendClause(StringBuilder s, String name, String clause) {
        if (!TextUtils.isEmpty(clause)) {
            s.append(name);
            s.append(clause);
        }
    }

}
