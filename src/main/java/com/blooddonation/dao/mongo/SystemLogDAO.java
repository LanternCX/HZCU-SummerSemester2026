package com.blooddonation.dao.mongo;

import com.blooddonation.dao.MongoBaseDAO;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.Document;

public class SystemLogDAO extends MongoBaseDAO {
    public SystemLogDAO() {
        super("system_logs");
    }

    public void insertLog(String userId, String logType, String logLevel, String message, Document actionDetail) {
        Document document = new Document("user_id", userId)
            .append("log_type", logType)
            .append("log_level", logLevel)
            .append("message", message)
            .append("action_detail", actionDetail)
            .append("timestamp", Date.from(Instant.now()));
        collection().insertOne(document);
    }

    public List<Document> findByType(String logType, int limit) {
        return collection()
            .find(Filters.eq("log_type", logType))
            .sort(Sorts.descending("timestamp"))
            .limit(limit)
            .into(new ArrayList<>());
    }

    public List<Document> auditSummary() {
        List<Document> pipeline = List.of(
            new Document("$group", new Document("_id", new Document("log_type", "$log_type").append("log_level", "$log_level"))
                .append("log_count", new Document("$sum", 1))
                .append("last_log_at", new Document("$max", "$timestamp"))),
            new Document("$project", new Document("_id", 0)
                .append("log_type", "$_id.log_type")
                .append("log_level", "$_id.log_level")
                .append("log_count", 1)
                .append("last_log_at", 1)),
            new Document("$sort", new Document("log_count", -1))
        );
        return collection().aggregate(pipeline).into(new ArrayList<>());
    }
}
