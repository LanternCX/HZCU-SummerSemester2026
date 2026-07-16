package com.blooddonation.dao.mongo;

import com.blooddonation.dao.MongoBaseDAO;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.Document;

/** 访问 MongoDB 用户行为日志与热度统计。 */
public class LogDAO extends MongoBaseDAO {
    /** 创建绑定用户行为日志集合的 DAO。 */
    public LogDAO() {
        super("action_logs");
    }

    /** 写入一条用户行为日志。 */
    public void insertActionLog(String userId, String itemId, String actionType, int durationSeconds, Document clientInfo) {
        Document document = new Document("user_id", userId)
            .append("item_id", itemId)
            .append("action_type", actionType)
            .append("duration_seconds", durationSeconds)
            .append("client_info", clientInfo)
            .append("created_at", Date.from(Instant.now()));
        collection().insertOne(document);
    }

    /** @return 指定用户的最近行为日志 */
    public List<Document> findByUserId(String userId, int limit) {
        return collection()
            .find(Filters.eq("user_id", userId))
            .sort(Sorts.descending("created_at"))
            .limit(limit)
            .into(new ArrayList<>());
    }

    /** @return 全局最近行为日志 */
    public List<Document> findRecent(int limit) {
        return collection()
            .find()
            .sort(Sorts.descending("created_at"))
            .limit(limit)
            .into(new ArrayList<>());
    }

    /** @return 按操作次数排序的库存热度统计 */
    public List<Document> topItems(int limit) {
        List<Document> pipeline = List.of(
            new Document("$group", new Document("_id", "$item_id").append("action_count", new Document("$sum", 1))),
            new Document("$sort", new Document("action_count", -1)),
            new Document("$limit", limit),
            new Document("$project", new Document("_id", 0).append("item_id", "$_id").append("action_count", 1))
        );
        return collection().aggregate(pipeline).into(new ArrayList<>());
    }
}
