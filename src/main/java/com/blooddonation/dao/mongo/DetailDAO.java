package com.blooddonation.dao.mongo;

import com.blooddonation.dao.MongoBaseDAO;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.bson.Document;

/** 访问 MongoDB 库存扩展详情。 */
public class DetailDAO extends MongoBaseDAO {
    /** 创建绑定库存详情集合的 DAO。 */
    public DetailDAO() {
        super("item_details");
    }

    /** 新增或更新指定库存的详情文档。 */
    public void upsertDetail(String itemId, String description, List<String> images, Document metadata) {
        Document document = new Document("item_id", itemId)
            .append("description", description)
            .append("images", images)
            .append("metadata", metadata)
            .append("updated_at", Date.from(Instant.now()));
        collection().replaceOne(Filters.eq("item_id", itemId), document, new ReplaceOptions().upsert(true));
    }

    /** @return 指定库存的详情，不存在时为空 */
    public Optional<Document> findByItemId(String itemId) {
        return Optional.ofNullable(collection().find(Filters.eq("item_id", itemId)).first());
    }

    /** @return 是否删除成功 */
    public boolean deleteByItemId(String itemId) {
        return collection().deleteOne(Filters.eq("item_id", itemId)).getDeletedCount() > 0;
    }
}
