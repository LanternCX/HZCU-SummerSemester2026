package com.blooddonation.dao.mysql;

import com.blooddonation.dao.BaseDAO;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CategoryDAO extends BaseDAO {
    public long create(String name, Long parentId) {
        String sql = "INSERT INTO categories (name, parent_id) VALUES (?, ?)";
        return insert(sql, statement -> {
            statement.setString(1, name);
            if (parentId == null) {
                statement.setNull(2, Types.BIGINT);
            } else {
                statement.setLong(2, parentId);
            }
        });
    }

    public Optional<Map<String, Object>> findById(long categoryId) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";
        return queryOne(sql, statement -> statement.setLong(1, categoryId));
    }

    public List<Map<String, Object>> findAll() {
        return queryList("SELECT * FROM categories ORDER BY parent_id IS NULL DESC, parent_id, category_id", statement -> {
        });
    }

    public List<Map<String, Object>> findChildren(Long parentId) {
        if (parentId == null) {
            return queryList("SELECT * FROM categories WHERE parent_id IS NULL", statement -> {
            });
        }
        String sql = "SELECT * FROM categories WHERE parent_id = ?";
        return queryList(sql, statement -> statement.setLong(1, parentId));
    }

    public boolean updateName(long categoryId, String name) {
        String sql = "UPDATE categories SET name = ? WHERE category_id = ?";
        return update(sql, statement -> {
            statement.setString(1, name);
            statement.setLong(2, categoryId);
        });
    }

    public boolean update(long categoryId, String name, Long parentId) {
        String sql = "UPDATE categories SET name = ?, parent_id = ? WHERE category_id = ?";
        return update(sql, statement -> {
            statement.setString(1, name);
            if (parentId == null) {
                statement.setNull(2, Types.BIGINT);
            } else {
                statement.setLong(2, parentId);
            }
            statement.setLong(3, categoryId);
        });
    }

    public boolean deleteById(long categoryId) {
        String sql = "DELETE FROM categories WHERE category_id = ?";
        return update(sql, statement -> statement.setLong(1, categoryId));
    }
}
