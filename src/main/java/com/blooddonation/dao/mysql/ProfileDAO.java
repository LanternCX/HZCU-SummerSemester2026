package com.blooddonation.dao.mysql;

import com.blooddonation.dao.BaseDAO;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 访问 MySQL 用户实名档案数据。 */
public class ProfileDAO extends BaseDAO {
    /** @return 新档案编号 */
    public long create(long userId, String realName, String idCard, String bloodType, String address, String notes) {
        String sql = """
            INSERT INTO profiles (user_id, real_name, id_card, blood_type, address, notes)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        return insert(sql, statement -> {
            statement.setLong(1, userId);
            statement.setString(2, realName);
            statement.setString(3, idCard);
            statement.setString(4, bloodType);
            statement.setString(5, address);
            statement.setString(6, notes);
        });
    }

    /** @return 指定用户的档案，不存在时为空 */
    public Optional<Map<String, Object>> findByUserId(long userId) {
        String sql = "SELECT * FROM profiles WHERE user_id = ?";
        return queryOne(sql, statement -> statement.setLong(1, userId));
    }

    /** @return 合并用户账号信息的全部档案 */
    public List<Map<String, Object>> findUserProfiles() {
        String sql = """
            SELECT
              u.user_id, u.username, u.email, u.phone, u.role, u.status,
              p.profile_id, p.real_name, p.id_card, p.blood_type, p.address, p.notes
            FROM users u
            LEFT JOIN profiles p ON p.user_id = u.user_id
            ORDER BY u.user_id
            """;
        return queryList(sql, statement -> {
        });
    }

    /** @return 合并账号信息的指定用户档案 */
    public Optional<Map<String, Object>> findUserProfile(long userId) {
        String sql = """
            SELECT
              u.user_id, u.username, u.email, u.phone, u.role, u.status,
              p.profile_id, p.real_name, p.id_card, p.blood_type, p.address, p.notes
            FROM users u
            LEFT JOIN profiles p ON p.user_id = u.user_id
            WHERE u.user_id = ?
            """;
        return queryOne(sql, statement -> statement.setLong(1, userId));
    }

    /** @return 是否更新成功 */
    public boolean update(long profileId, String realName, String idCard, String bloodType, String address, String notes) {
        String sql = "UPDATE profiles SET real_name = ?, id_card = ?, blood_type = ?, address = ?, notes = ? WHERE profile_id = ?";
        return update(sql, statement -> {
            statement.setString(1, realName);
            statement.setString(2, idCard);
            statement.setString(3, bloodType);
            statement.setString(4, address);
            statement.setString(5, notes);
            statement.setLong(6, profileId);
        });
    }

    /** @return 是否删除成功 */
    public boolean deleteByUserId(long userId) {
        String sql = "DELETE FROM profiles WHERE user_id = ?";
        return update(sql, statement -> statement.setLong(1, userId));
    }
}
