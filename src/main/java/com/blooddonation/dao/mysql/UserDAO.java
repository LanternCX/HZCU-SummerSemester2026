package com.blooddonation.dao.mysql;

import com.blooddonation.dao.BaseDAO;
import java.util.Map;
import java.util.Optional;

public class UserDAO extends BaseDAO {
    public long create(String username, String passwordHash, String email, String phone, String role) {
        String sql = """
            INSERT INTO users (username, password_hash, email, phone, role)
            VALUES (?, ?, ?, ?, ?)
            """;
        return insert(sql, statement -> {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, email);
            statement.setString(4, phone);
            statement.setString(5, role);
        });
    }

    public Optional<Map<String, Object>> findById(long userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        return queryOne(sql, statement -> statement.setLong(1, userId));
    }

    public Optional<Map<String, Object>> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        return queryOne(sql, statement -> statement.setString(1, username));
    }

    public boolean updateContact(long userId, String email, String phone) {
        String sql = "UPDATE users SET email = ?, phone = ? WHERE user_id = ?";
        return update(sql, statement -> {
            statement.setString(1, email);
            statement.setString(2, phone);
            statement.setLong(3, userId);
        });
    }

    public boolean updateStatus(long userId, int status) {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        return update(sql, statement -> {
            statement.setInt(1, status);
            statement.setLong(2, userId);
        });
    }

    public boolean deleteById(long userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        return update(sql, statement -> statement.setLong(1, userId));
    }
}
