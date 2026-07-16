package com.blooddonation.dao;

import com.blooddonation.exception.DBException;
import com.blooddonation.util.MySQLDBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 封装 MySQL DAO 共用的连接、参数绑定和结果映射逻辑。
 */
public abstract class BaseDAO {
    /**
     * 为预编译语句绑定参数。
     */
    @FunctionalInterface
    protected interface Binder {
        /**
         * 绑定 SQL 参数。
         *
         * @param statement 待绑定的预编译语句
         * @throws SQLException 参数绑定失败
         */
        void bind(PreparedStatement statement) throws SQLException;
    }

    /**
     * 获取 MySQL 连接，并统一转换连接异常。
     *
     * @return 数据库连接
     */
    protected Connection getConnection() {
        try {
            return MySQLDBUtil.getConnection();
        } catch (SQLException e) {
            throw new DBException("Failed to get MySQL connection", e);
        }
    }

    /**
     * 执行插入并返回数据库生成的主键。
     *
     * @param sql 插入语句
     * @param binder 参数绑定器
     * @return 生成的主键；未生成时返回 0
     */
    protected long insert(String sql, Binder binder) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            binder.bind(statement);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new DBException("Failed to insert data", e);
        }
    }

    /**
     * 执行更新或删除语句。
     *
     * @param sql 更新或删除语句
     * @param binder 参数绑定器
     * @return 是否影响至少一条记录
     */
    protected boolean update(String sql, Binder binder) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DBException("Failed to update data", e);
        }
    }

    /**
     * 查询第一条记录。
     *
     * @param sql 查询语句
     * @param binder 参数绑定器
     * @return 第一条记录，不存在时为空
     */
    protected Optional<Map<String, Object>> queryOne(String sql, Binder binder) {
        List<Map<String, Object>> rows = queryList(sql, binder);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /**
     * 查询记录列表，并按列名映射为键值对。
     *
     * @param sql 查询语句
     * @param binder 参数绑定器
     * @return 查询结果列表
     */
    protected List<Map<String, Object>> queryList(String sql, Binder binder) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                while (resultSet.next()) {
                    rows.add(toMap(resultSet));
                }
                return rows;
            }
        } catch (SQLException e) {
            throw new DBException("Failed to query data", e);
        }
    }

    /** @return 将当前结果集行转换为按列名索引的映射 */
    private Map<String, Object> toMap(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
        }
        return row;
    }
}
