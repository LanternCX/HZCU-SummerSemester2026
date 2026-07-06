package com.blooddonation.dao.mysql;

import com.blooddonation.dao.BaseDAO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemDAO extends BaseDAO {
    public long create(String title, long categoryId, BigDecimal amount) {
        String sql = "INSERT INTO items (title, category_id, amount) VALUES (?, ?, ?)";
        return insert(sql, statement -> {
            statement.setString(1, title);
            statement.setLong(2, categoryId);
            statement.setBigDecimal(3, amount);
        });
    }

    public Optional<Map<String, Object>> findById(long itemId) {
        String sql = "SELECT * FROM items WHERE item_id = ?";
        return queryOne(sql, statement -> statement.setLong(1, itemId));
    }

    public List<Map<String, Object>> findAll() {
        return queryList("SELECT * FROM items ORDER BY created_at DESC, item_id DESC", statement -> {
        });
    }

    public List<Map<String, Object>> findByCategory(long categoryId) {
        String sql = "SELECT * FROM items WHERE category_id = ? ORDER BY created_at DESC";
        return queryList(sql, statement -> statement.setLong(1, categoryId));
    }

    public boolean update(long itemId, String title, long categoryId, BigDecimal amount, int status) {
        String sql = "UPDATE items SET title = ?, category_id = ?, amount = ?, status = ? WHERE item_id = ?";
        return update(sql, statement -> {
            statement.setString(1, title);
            statement.setLong(2, categoryId);
            statement.setBigDecimal(3, amount);
            statement.setInt(4, status);
            statement.setLong(5, itemId);
        });
    }

    public boolean updateStatus(long itemId, int status) {
        String sql = "UPDATE items SET status = ? WHERE item_id = ?";
        return update(sql, statement -> {
            statement.setInt(1, status);
            statement.setLong(2, itemId);
        });
    }

    public boolean deleteById(long itemId) {
        String sql = "DELETE FROM items WHERE item_id = ?";
        return update(sql, statement -> statement.setLong(1, itemId));
    }
}
