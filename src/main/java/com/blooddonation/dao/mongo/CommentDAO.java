package com.blooddonation.dao.mongo;

import com.blooddonation.dao.MongoBaseDAO;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;

/** 访问 MongoDB 评论、评分和标签数据。 */
public class CommentDAO extends MongoBaseDAO {
    /** 创建绑定评论集合的 DAO。 */
    public CommentDAO() {
        super("comments");
    }

    /** 创建库存评论。 */
    public void createComment(String userId, String itemId, String content, int rating, List<String> tags) {
        Document document = new Document("user_id", userId)
            .append("item_id", itemId)
            .append("content", content)
            .append("rating", rating)
            .append("tags", tags)
            .append("created_at", Date.from(Instant.now()));
        collection().insertOne(document);
    }

    /** @return 指定库存的最新评论 */
    public List<Document> findByItemId(String itemId, int limit) {
        return collection()
            .find(Filters.eq("item_id", itemId))
            .sort(Sorts.descending("created_at"))
            .limit(limit)
            .into(new ArrayList<>());
    }

    /** @return 是否删除成功 */
    public boolean deleteById(String commentId) {
        if (!ObjectId.isValid(commentId)) {
            return false;
        }
        return collection().deleteOne(Filters.eq("_id", new ObjectId(commentId))).getDeletedCount() == 1;
    }

    /** @return 是否删除属于指定用户的评论 */
    public boolean deleteByIdAndUser(String commentId, String userId) {
        if (!ObjectId.isValid(commentId)) {
            return false;
        }
        return collection().deleteOne(Filters.and(
            Filters.eq("_id", new ObjectId(commentId)),
            Filters.eq("user_id", userId)
        )).getDeletedCount() == 1;
    }

    /** @return 按库存汇总的评论数量和平均评分 */
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

    /** @return 指定用户按库存汇总的评论数量和平均评分 */
    public List<Document> ratingSummaryByUser(String userId) {
        List<Document> pipeline = List.of(
            new Document("$match", Filters.eq("user_id", userId)),
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
