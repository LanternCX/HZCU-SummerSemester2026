package com.blooddonation.dao.mysql;

import com.blooddonation.dao.BaseDAO;
import com.blooddonation.exception.DBException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrderDAO extends BaseDAO {
    public long createOrder(long userId, long itemId, BigDecimal amount) {
        String sql = "INSERT INTO orders (user_id, item_id, amount) VALUES (?, ?, ?)";
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, userId);
                statement.setLong(2, itemId);
                statement.setBigDecimal(3, amount);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    long orderId = keys.next() ? keys.getLong(1) : 0L;
                    connection.commit();
                    return orderId;
                }
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DBException("Failed to create order", e);
        }
    }

    public Optional<Map<String, Object>> findById(long orderId) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        return queryOne(sql, statement -> statement.setLong(1, orderId));
    }

    public List<Map<String, Object>> findByUser(long userId) {
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC";
        return queryList(sql, statement -> statement.setLong(1, userId));
    }

    public boolean updateStatus(long orderId, int status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        return update(sql, statement -> {
            statement.setInt(1, status);
            statement.setLong(2, orderId);
        });
    }
}
