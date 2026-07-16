package com.blooddonation.dao.mysql;

import com.blooddonation.dao.BaseDAO;
import java.util.Map;
import java.util.Optional;

/** 访问 MySQL 用户账号数据。 */
public class UserDAO extends BaseDAO {
    /** @return 新用户编号 */
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

    /** @return 指定编号的用户，不存在时为空 */
    public Optional<Map<String, Object>> findById(long userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        return queryOne(sql, statement -> statement.setLong(1, userId));
    }

    /** @return 指定用户名的用户，不存在时为空 */
    public Optional<Map<String, Object>> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        return queryOne(sql, statement -> statement.setString(1, username));
    }

    /** @return 是否更新成功 */
    public boolean updateContact(long userId, String email, String phone) {
        String sql = "UPDATE users SET email = ?, phone = ? WHERE user_id = ?";
        return update(sql, statement -> {
            statement.setString(1, email);
            statement.setString(2, phone);
            statement.setLong(3, userId);
        });
    }

    /** @return 是否更新成功 */
    public boolean updateStatus(long userId, int status) {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        return update(sql, statement -> {
            statement.setInt(1, status);
            statement.setLong(2, userId);
        });
    }

    /** @return 是否更新成功 */
    public boolean updateRole(long userId, String role) {
        String sql = "UPDATE users SET role = ? WHERE user_id = ?";
        return update(sql, statement -> {
            statement.setString(1, role);
            statement.setLong(2, userId);
        });
    }

    /** @return 是否删除成功 */
    public boolean deleteById(long userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        return update(sql, statement -> statement.setLong(1, userId));
    }
}
