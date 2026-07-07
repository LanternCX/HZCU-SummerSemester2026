package com.blooddonation.dao.mysql;

import com.blooddonation.dao.BaseDAO;
import com.blooddonation.exception.DBException;
import java.math.BigDecimal;
import java.sql.CallableStatement;
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

    public List<Map<String, Object>> findAll() {
        return queryList("SELECT * FROM orders ORDER BY created_at DESC, order_id DESC", statement -> {
        });
    }

    public List<Map<String, Object>> countByItem() {
        String sql = """
            SELECT item_id, COUNT(*) AS order_count
            FROM orders
            GROUP BY item_id
            """;
        return queryList(sql, statement -> {
        });
    }

    public List<Map<String, Object>> monthlyReport(int year, int month) {
        try (Connection connection = getConnection();
             CallableStatement statement = connection.prepareCall("{CALL sp_monthly_report(?, ?)}")) {
            statement.setInt(1, year);
            statement.setInt(2, month);
            try (ResultSet rows = statement.executeQuery()) {
                ResultSetMetaData metaData = rows.getMetaData();
                List<Map<String, Object>> result = new ArrayList<>();
                while (rows.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        row.put(metaData.getColumnLabel(i), rows.getObject(i));
                    }
                    result.add(row);
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DBException("Failed to load monthly report", e);
        }
    }

    public boolean updateStatus(long orderId, int status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ? AND status = 0";
        return update(sql, statement -> {
            statement.setInt(1, status);
            statement.setLong(2, orderId);
        });
    }

    public boolean updatePendingOrder(long userId, long orderId, long itemId, BigDecimal amount) {
        String sql = "UPDATE orders SET item_id = ?, amount = ? WHERE order_id = ? AND user_id = ? AND status = 0";
        return update(sql, statement -> {
            statement.setLong(1, itemId);
            statement.setBigDecimal(2, amount);
            statement.setLong(3, orderId);
            statement.setLong(4, userId);
        });
    }

    public boolean deletePendingByUser(long userId, long orderId) {
        String sql = "DELETE FROM orders WHERE order_id = ? AND user_id = ? AND status = 0";
        return update(sql, statement -> {
            statement.setLong(1, orderId);
            statement.setLong(2, userId);
        });
    }

    public boolean completeOrder(long orderId) {
        String selectSql = """
            SELECT o.item_id, o.amount, o.status, i.amount AS item_amount, i.status AS item_status
            FROM orders o
            JOIN items i ON i.item_id = o.item_id
            WHERE o.order_id = ?
            FOR UPDATE
            """;
        String orderSql = "UPDATE orders SET status = 1 WHERE order_id = ?";
        String itemSql = """
            UPDATE items
            SET amount = amount - ?, status = CASE WHEN amount - ? = 0 THEN 0 ELSE status END
            WHERE item_id = ?
            """;

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement select = connection.prepareStatement(selectSql)) {
                select.setLong(1, orderId);
                try (ResultSet rows = select.executeQuery()) {
                    if (!rows.next()) {
                        connection.rollback();
                        return false;
                    }

                    BigDecimal orderAmount = rows.getBigDecimal("amount");
                    BigDecimal itemAmount = rows.getBigDecimal("item_amount");
                    int orderStatus = rows.getInt("status");
                    int itemStatus = rows.getInt("item_status");
                    long itemId = rows.getLong("item_id");
                    if (orderStatus != 0 || itemStatus != 1 || itemAmount.compareTo(orderAmount) < 0) {
                        connection.rollback();
                        return false;
                    }

                    try (PreparedStatement updateOrder = connection.prepareStatement(orderSql);
                         PreparedStatement updateItem = connection.prepareStatement(itemSql)) {
                        updateOrder.setLong(1, orderId);
                        updateOrder.executeUpdate();

                        updateItem.setBigDecimal(1, orderAmount);
                        updateItem.setBigDecimal(2, orderAmount);
                        updateItem.setLong(3, itemId);
                        updateItem.executeUpdate();
                    }
                    connection.commit();
                    return true;
                }
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DBException("Failed to complete order", e);
        }
    }

    public boolean deleteById(long orderId) {
        String selectSql = "SELECT item_id, amount, status FROM orders WHERE order_id = ? FOR UPDATE";
        String restoreSql = "UPDATE items SET amount = amount + ?, status = 1 WHERE item_id = ?";
        String deleteSql = "DELETE FROM orders WHERE order_id = ?";

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement select = connection.prepareStatement(selectSql)) {
                select.setLong(1, orderId);
                try (ResultSet rows = select.executeQuery()) {
                    if (!rows.next()) {
                        connection.rollback();
                        return false;
                    }

                    if (rows.getInt("status") == 1) {
                        try (PreparedStatement restore = connection.prepareStatement(restoreSql)) {
                            restore.setBigDecimal(1, rows.getBigDecimal("amount"));
                            restore.setLong(2, rows.getLong("item_id"));
                            restore.executeUpdate();
                        }
                    }

                    try (PreparedStatement delete = connection.prepareStatement(deleteSql)) {
                        delete.setLong(1, orderId);
                        delete.executeUpdate();
                    }
                    connection.commit();
                    return true;
                }
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DBException("Failed to delete order", e);
        }
    }
}
