package com.blooddonation.dao.mongo;

import com.blooddonation.dao.MongoBaseDAO;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.Document;

/** 访问 MongoDB 系统日志和审计统计。 */
public class SystemLogDAO extends MongoBaseDAO {
    /** 创建绑定系统日志集合的 DAO。 */
    public SystemLogDAO() {
        super("system_logs");
    }

    /** 写入一条系统日志。 */
    public void insertLog(String userId, String logType, String logLevel, String message, Document actionDetail) {
        Document document = new Document("user_id", userId)
            .append("log_type", logType)
            .append("log_level", logLevel)
            .append("message", message)
            .append("action_detail", actionDetail)
            .append("timestamp", Date.from(Instant.now()));
        collection().insertOne(document);
    }

    /** @return 指定类型或全部类型的最近系统日志 */
    public List<Document> findByType(String logType, int limit) {
        return collection()
            .find(Filters.eq("log_type", logType))
            .sort(Sorts.descending("timestamp"))
            .limit(limit)
            .into(new ArrayList<>());
    }

    /** @return 按日志类型和级别汇总的审计数据 */
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
