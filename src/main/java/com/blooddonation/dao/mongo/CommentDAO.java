package com.blooddonation.dao.mongo;

import com.blooddonation.dao.MongoBaseDAO;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.Document;

public class CommentDAO extends MongoBaseDAO {
    public CommentDAO() {
        super("comments");
    }

    public void createComment(String userId, String itemId, String content, int rating, List<String> tags) {
        Document document = new Document("user_id", userId)
            .append("item_id", itemId)
            .append("content", content)
            .append("rating", rating)
            .append("tags", tags)
            .append("created_at", Date.from(Instant.now()));
        collection().insertOne(document);
    }

    public List<Document> findByItemId(String itemId, int limit) {
        return collection()
            .find(Filters.eq("item_id", itemId))
            .sort(Sorts.descending("created_at"))
            .limit(limit)
            .into(new ArrayList<>());
    }

    public List<Document> ratingSummary() {
        List<Document> pipeline = List.of(
            new Document("$group", new Document("_id", "$item_id")
                .append("comment_count", new Document("$sum", 1))
                .append("average_rating", new Document("$avg", "$rating"))),
            new Document("$project", new Document("_id", 0)
                .append("item_id", "$_id")
                .append("comment_count", 1)
                .append("average_rating", 1)),
            new Document("$sort", new Document("average_rating", -1))
        );
        return collection().aggregate(pipeline).into(new ArrayList<>());
    }
}
