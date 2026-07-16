package com.blooddonation.dao.mysql;

import com.blooddonation.dao.BaseDAO;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 访问 MySQL 分类及父子层级数据。 */
public class CategoryDAO extends BaseDAO {
    /** @return 新分类编号 */
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

    /** @return 指定分类，不存在时为空 */
    public Optional<Map<String, Object>> findById(long categoryId) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";
        return queryOne(sql, statement -> statement.setLong(1, categoryId));
    }

    /** @return 按层级顺序排列的全部分类 */
    public List<Map<String, Object>> findAll() {
        return queryList("SELECT * FROM categories ORDER BY parent_id IS NULL DESC, parent_id, category_id", statement -> {
        });
    }

    /** @return 指定父分类的直接子分类 */
    public List<Map<String, Object>> findChildren(Long parentId) {
        if (parentId == null) {
            return queryList("SELECT * FROM categories WHERE parent_id IS NULL", statement -> {
            });
        }
        String sql = "SELECT * FROM categories WHERE parent_id = ?";
        return queryList(sql, statement -> statement.setLong(1, parentId));
    }

    /** @return 是否更新成功 */
    public boolean updateName(long categoryId, String name) {
        String sql = "UPDATE categories SET name = ? WHERE category_id = ?";
        return update(sql, statement -> {
            statement.setString(1, name);
            statement.setLong(2, categoryId);
        });
    }

    /** @return 是否更新成功 */
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

    /** @return 是否删除成功 */
    public boolean deleteById(long categoryId) {
        String sql = "DELETE FROM categories WHERE category_id = ?";
        return update(sql, statement -> statement.setLong(1, categoryId));
    }
}
