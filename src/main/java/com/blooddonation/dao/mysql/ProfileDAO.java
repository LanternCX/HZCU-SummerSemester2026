package com.blooddonation.dao.mysql;

import com.blooddonation.dao.BaseDAO;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProfileDAO extends BaseDAO {
    public long create(long userId, String realName, String idCard, String address, String notes) {
        String sql = """
            INSERT INTO profiles (user_id, real_name, id_card, address, notes)
            VALUES (?, ?, ?, ?, ?)
            """;
        return insert(sql, statement -> {
            statement.setLong(1, userId);
            statement.setString(2, realName);
            statement.setString(3, idCard);
            statement.setString(4, address);
            statement.setString(5, notes);
        });
    }

    public Optional<Map<String, Object>> findByUserId(long userId) {
        String sql = "SELECT * FROM profiles WHERE user_id = ?";
        return queryOne(sql, statement -> statement.setLong(1, userId));
    }

    public List<Map<String, Object>> findUserProfiles() {
        String sql = """
            SELECT
              u.user_id, u.username, u.email, u.phone, u.role, u.status,
              p.profile_id, p.real_name, p.id_card, p.address, p.notes
            FROM users u
            LEFT JOIN profiles p ON p.user_id = u.user_id
            ORDER BY u.user_id
            """;
        return queryList(sql, statement -> {
        });
    }

    public Optional<Map<String, Object>> findUserProfile(long userId) {
        String sql = """
            SELECT
              u.user_id, u.username, u.email, u.phone, u.role, u.status,
              p.profile_id, p.real_name, p.id_card, p.address, p.notes
            FROM users u
            LEFT JOIN profiles p ON p.user_id = u.user_id
            WHERE u.user_id = ?
            """;
        return queryOne(sql, statement -> statement.setLong(1, userId));
    }

    public boolean update(long profileId, String realName, String idCard, String address, String notes) {
        String sql = "UPDATE profiles SET real_name = ?, id_card = ?, address = ?, notes = ? WHERE profile_id = ?";
        return update(sql, statement -> {
            statement.setString(1, realName);
            statement.setString(2, idCard);
            statement.setString(3, address);
            statement.setString(4, notes);
            statement.setLong(5, profileId);
        });
    }

    public boolean deleteByUserId(long userId) {
        String sql = "DELETE FROM profiles WHERE user_id = ?";
        return update(sql, statement -> statement.setLong(1, userId));
    }
}
