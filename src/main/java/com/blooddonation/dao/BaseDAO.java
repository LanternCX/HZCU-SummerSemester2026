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

public abstract class BaseDAO {
    @FunctionalInterface
    protected interface Binder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    protected Connection getConnection() {
        try {
            return MySQLDBUtil.getConnection();
        } catch (SQLException e) {
            throw new DBException("Failed to get MySQL connection", e);
        }
    }

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

    protected boolean update(String sql, Binder binder) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DBException("Failed to update data", e);
        }
    }

    protected Optional<Map<String, Object>> queryOne(String sql, Binder binder) {
        List<Map<String, Object>> rows = queryList(sql, binder);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

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

    private Map<String, Object> toMap(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
        }
        return row;
    }
}
