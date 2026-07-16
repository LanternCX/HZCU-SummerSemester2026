package com.blooddonation.service;

import com.blooddonation.dao.mongo.DetailDAO;
import com.blooddonation.dao.mongo.CommentDAO;
import com.blooddonation.dao.mongo.LogDAO;
import com.blooddonation.dao.mysql.CategoryDAO;
import com.blooddonation.dao.mysql.ItemDAO;
import com.blooddonation.dao.mysql.OrderDAO;
import com.blooddonation.dto.ItemInsightDTO;
import com.blooddonation.dto.RecommendationDTO;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bson.Document;

/**
 * 合并 MySQL 主数据与 MongoDB 评分、详情和热度，生成库存洞察与推荐。
 */
public class RecommendService {
    private final ItemDAO itemDAO;
    private final CategoryDAO categoryDAO;
    private final DetailDAO detailDAO;
    private final CommentDAO commentDAO;
    private final OrderDAO orderDAO;
    private final LogDAO logDAO;

    /** 使用默认 DAO 创建推荐服务。 */
    public RecommendService() {
        this(new ItemDAO(), new CategoryDAO(), new DetailDAO(), new CommentDAO(), new OrderDAO(), new LogDAO());
    }

    /** 使用指定 DAO 创建可测试的推荐服务。 */
    RecommendService(ItemDAO itemDAO, CategoryDAO categoryDAO, DetailDAO detailDAO, CommentDAO commentDAO, OrderDAO orderDAO, LogDAO logDAO) {
        this.itemDAO = itemDAO;
        this.categoryDAO = categoryDAO;
        this.detailDAO = detailDAO;
        this.commentDAO = commentDAO;
        this.orderDAO = orderDAO;
        this.logDAO = logDAO;
    }

    /**
     * 汇总全部库存的分类、详情、评分、热度和申请数量。
     *
     * @return 库存综合信息
     */
    public List<ItemInsightDTO> findItemInsights() {
        Map<Long, String> categories = categoryNames();
        Map<Long, Document> ratings = documentsByItem(commentDAO.ratingSummary());
        Map<Long, Document> actions = documentsByItem(logDAO.topItems(100));
        Map<Long, Integer> orderCounts = orderCounts();

        return itemDAO.findAll().stream()
            .map(row -> insight(row, categories, ratings, actions, orderCounts))
            .toList();
    }

    /**
     * 按用户相关分类、行为热度和平均评分生成推荐。
     *
     * @param userId 用户编号
     * @param limit 最大返回数量
     * @return 推荐列表
     */
    public List<RecommendationDTO> recommendItems(long userId, int limit) {
        if (userId <= 0 || limit <= 0) {
            return List.of();
        }
        Set<Long> relatedCategories = relatedCategories(userId);
        return findItemInsights().stream()
            .filter(item -> item.status() == 1)
            .sorted(Comparator
                .comparing((ItemInsightDTO item) -> !relatedCategories.contains(categoryId(item.itemId())))
                .thenComparing(ItemInsightDTO::actionCount, Comparator.reverseOrder())
                .thenComparing(ItemInsightDTO::averageRating, Comparator.reverseOrder()))
            .limit(limit)
            .map(item -> new RecommendationDTO(item, relatedCategories.contains(categoryId(item.itemId()))
                ? "你最近申请过同类库存"
                : item.actionCount() > 0 ? "近期关注度较高" : "可用库存推荐"))
            .toList();
    }

    /** @return 合并单条库存及其跨库统计后的综合信息 */
    private ItemInsightDTO insight(
        Map<String, Object> row,
        Map<Long, String> categories,
        Map<Long, Document> ratings,
        Map<Long, Document> actions,
        Map<Long, Integer> orderCounts
    ) {
        long itemId = ((Number) row.get("item_id")).longValue();
        long categoryId = ((Number) row.get("category_id")).longValue();
        Document detail = detailDAO.findByItemId(String.valueOf(itemId)).orElse(new Document());
        Document metadata = detail.get("metadata", Document.class);
        Document rating = ratings.getOrDefault(itemId, new Document());
        Document action = actions.getOrDefault(itemId, new Document());
        return new ItemInsightDTO(
            itemId,
            String.valueOf(row.get("title")),
            categories.getOrDefault(categoryId, "未分类"),
            (BigDecimal) row.get("amount"),
            ((Number) row.get("status")).intValue(),
            detail.getString("description") == null ? "" : detail.getString("description"),
            metadata == null ? "" : String.valueOf(metadata.getOrDefault("blood_type", "")),
            number(rating.get("comment_count")),
            doubleNumber(rating.get("average_rating")),
            number(action.get("action_count")),
            orderCounts.getOrDefault(itemId, 0)
        );
    }

    /** @return 按分类编号索引的分类名称 */
    private Map<Long, String> categoryNames() {
        Map<Long, String> names = new HashMap<>();
        for (Map<String, Object> row : categoryDAO.findAll()) {
            names.put(((Number) row.get("category_id")).longValue(), String.valueOf(row.get("name")));
        }
        return names;
    }

    /** @return 按库存编号索引的 MongoDB 统计文档 */
    private Map<Long, Document> documentsByItem(List<Document> documents) {
        Map<Long, Document> rows = new HashMap<>();
        for (Document document : documents) {
            Long itemId = parseId(document.get("item_id"));
            if (itemId != null) {
                rows.put(itemId, document);
            }
        }
        return rows;
    }

    /** @return 按库存编号索引的申请次数 */
    private Map<Long, Integer> orderCounts() {
        Map<Long, Integer> counts = new HashMap<>();
        for (Map<String, Object> row : orderDAO.countByItem()) {
            counts.put(((Number) row.get("item_id")).longValue(), ((Number) row.get("order_count")).intValue());
        }
        return counts;
    }

    /** @return 用户申请或访问过的相关分类 */
    private Set<Long> relatedCategories(long userId) {
        Set<Long> categories = new HashSet<>();
        for (Map<String, Object> order : orderDAO.findByUser(userId)) {
            Long itemId = parseId(order.get("item_id"));
            if (itemId != null) {
                itemDAO.findById(itemId).ifPresent(item -> categories.add(((Number) item.get("category_id")).longValue()));
            }
        }
        for (Document log : logDAO.findByUserId(String.valueOf(userId), 50)) {
            Long itemId = parseId(log.get("item_id"));
            if (itemId != null) {
                itemDAO.findById(itemId).ifPresent(item -> categories.add(((Number) item.get("category_id")).longValue()));
            }
        }
        return categories;
    }

    /** @return 指定库存的分类编号 */
    private long categoryId(long itemId) {
        return itemDAO.findById(itemId)
            .map(row -> ((Number) row.get("category_id")).longValue())
            .orElse(0L);
    }

    /** @return 可解析的编号，无效值返回 {@code null} */
    private Long parseId(Object value) {
        if (value == null || "NONE".equals(String.valueOf(value))) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /** @return 数值的整数形式，非数值返回 0 */
    private int number(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    /** @return 数值的双精度形式，非数值返回 0 */
    private double doubleNumber(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0D;
    }
}
