package com.blooddonation.dao.mysql;

import com.blooddonation.dao.BaseDAO;
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

    public boolean update(long profileId, String realName, String address, String notes) {
        String sql = "UPDATE profiles SET real_name = ?, address = ?, notes = ? WHERE profile_id = ?";
        return update(sql, statement -> {
            statement.setString(1, realName);
            statement.setString(2, address);
            statement.setString(3, notes);
            statement.setLong(4, profileId);
        });
    }

    public boolean deleteByUserId(long userId) {
        String sql = "DELETE FROM profiles WHERE user_id = ?";
        return update(sql, statement -> statement.setLong(1, userId));
    }
}
