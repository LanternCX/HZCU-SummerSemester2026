package com.blooddonation.dao.mongo;

import com.blooddonation.dao.MongoBaseDAO;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.bson.Document;

public class DetailDAO extends MongoBaseDAO {
    public DetailDAO() {
        super("item_details");
    }

    public void upsertDetail(String itemId, String description, List<String> images, Document metadata) {
        Document document = new Document("item_id", itemId)
            .append("description", description)
            .append("images", images)
            .append("metadata", metadata)
            .append("updated_at", Date.from(Instant.now()));
        collection().replaceOne(Filters.eq("item_id", itemId), document, new ReplaceOptions().upsert(true));
    }

    public Optional<Document> findByItemId(String itemId) {
        return Optional.ofNullable(collection().find(Filters.eq("item_id", itemId)).first());
    }

    public boolean deleteByItemId(String itemId) {
        return collection().deleteOne(Filters.eq("item_id", itemId)).getDeletedCount() > 0;
    }
}
