package com.blooddonation.service;

import com.blooddonation.dao.mongo.DetailDAO;
import com.blooddonation.dao.mongo.CommentDAO;
import com.blooddonation.dao.mongo.LogDAO;
import com.blooddonation.dao.mongo.SystemLogDAO;
import com.blooddonation.dao.mysql.CategoryDAO;
import com.blooddonation.dao.mysql.ItemDAO;
import com.blooddonation.dao.mysql.OrderDAO;
import com.blooddonation.dao.mysql.ProfileDAO;
import com.blooddonation.dao.mysql.UserDAO;
import com.blooddonation.dto.ItemInsightDTO;
import com.blooddonation.dto.RecommendationDTO;
import com.blooddonation.exception.DBException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bson.Document;

public class BusinessService {
    private static final long SUPER_ADMIN_ID = 1L;

    private final ItemDAO itemDAO;
    private final CategoryDAO categoryDAO;
    private final DetailDAO detailDAO;
    private final CommentDAO commentDAO;
    private final OrderDAO orderDAO;
    private final UserDAO userDAO;
    private final ProfileDAO profileDAO;
    private final LogDAO logDAO;
    private final SystemLogDAO systemLogDAO;
    private final RecommendService recommendService;

    public BusinessService() {
        this(new ItemDAO(), new CategoryDAO(), new DetailDAO(), new CommentDAO(), new OrderDAO(), new UserDAO(), new ProfileDAO(), new LogDAO(), new SystemLogDAO());
    }

    BusinessService(ItemDAO itemDAO, DetailDAO detailDAO, OrderDAO orderDAO) {
        this(itemDAO, new CategoryDAO(), detailDAO, new CommentDAO(), orderDAO, new UserDAO());
    }

    BusinessService(ItemDAO itemDAO, CategoryDAO categoryDAO, DetailDAO detailDAO, OrderDAO orderDAO) {
        this(itemDAO, categoryDAO, detailDAO, new CommentDAO(), orderDAO, new UserDAO());
    }

    BusinessService(ItemDAO itemDAO, CategoryDAO categoryDAO, DetailDAO detailDAO, CommentDAO commentDAO, OrderDAO orderDAO) {
        this(itemDAO, categoryDAO, detailDAO, commentDAO, orderDAO, new UserDAO());
    }

    BusinessService(ItemDAO itemDAO, CategoryDAO categoryDAO, DetailDAO detailDAO, CommentDAO commentDAO, OrderDAO orderDAO, UserDAO userDAO) {
        this(itemDAO, categoryDAO, detailDAO, commentDAO, orderDAO, userDAO, new ProfileDAO(), silentLogDAO(), silentSystemLogDAO());
    }

    BusinessService(
        ItemDAO itemDAO,
        CategoryDAO categoryDAO,
        DetailDAO detailDAO,
        CommentDAO commentDAO,
        OrderDAO orderDAO,
        UserDAO userDAO,
        LogDAO logDAO,
        SystemLogDAO systemLogDAO
    ) {
        this(itemDAO, categoryDAO, detailDAO, commentDAO, orderDAO, userDAO, new ProfileDAO(), logDAO, systemLogDAO);
    }

    BusinessService(
        ItemDAO itemDAO,
        CategoryDAO categoryDAO,
        DetailDAO detailDAO,
        CommentDAO commentDAO,
        OrderDAO orderDAO,
        UserDAO userDAO,
        ProfileDAO profileDAO,
        LogDAO logDAO,
        SystemLogDAO systemLogDAO
    ) {
        this.itemDAO = itemDAO;
        this.categoryDAO = categoryDAO;
        this.detailDAO = detailDAO;
        this.commentDAO = commentDAO;
        this.orderDAO = orderDAO;
        this.userDAO = userDAO;
        this.profileDAO = profileDAO;
        this.logDAO = logDAO;
        this.systemLogDAO = systemLogDAO;
        this.recommendService = new RecommendService(itemDAO, categoryDAO, detailDAO, commentDAO, orderDAO, logDAO);
    }

    public List<Map<String, Object>> findUserProfiles(long userId, boolean admin) {
        if (admin) {
            return profileDAO.findUserProfiles();
        }
        return userId <= 0 ? List.of() : profileDAO.findUserProfile(userId).stream().toList();
    }

    public BusinessResult saveUserProfile(
        long actorUserId,
        boolean admin,
        long userId,
        String email,
        String phone,
        String role,
        int status,
        String realName,
        String idCard,
        String address,
        String notes
    ) {
        if (userId <= 0 || (!admin && actorUserId != userId)) {
            return BusinessResult.fail("无权维护该用户档案");
        }
        Optional<Map<String, Object>> user = userDAO.findById(userId);
        if (user.isEmpty()) {
            return BusinessResult.fail("用户不存在");
        }
        String cleanEmail = clean(email);
        String cleanPhone = clean(phone);
        String cleanRealName = clean(realName);
        String cleanIdCard = clean(idCard);
        if (!cleanEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return BusinessResult.fail("请输入有效邮箱");
        }
        if (!cleanPhone.isEmpty() && !cleanPhone.matches("^1\\d{10}$")) {
            return BusinessResult.fail("请输入有效手机号");
        }
        if (cleanRealName.isEmpty() || cleanIdCard.isEmpty()) {
            return BusinessResult.fail("请输入姓名和证件号");
        }
        if (status < 0 || status > 1) {
            return BusinessResult.fail("请选择有效用户状态");
        }
        String cleanRole = clean(role);
        if (!"ADMIN".equals(cleanRole) && !"USER".equals(cleanRole)) {
            return BusinessResult.fail("请选择有效用户权限");
        }
        if (isSuperAdmin(userId) && (!admin || actorUserId != userId)) {
            return BusinessResult.fail("不能修改超级管理员权限");
        }
        if (admin && !isSuperAdmin(actorUserId) && !cleanRole.equals(String.valueOf(user.get().get("role")))) {
            return BusinessResult.fail("只有超级管理员可以修改用户权限");
        }

        userDAO.updateContact(userId, cleanEmail, cleanPhone);
        if (admin) {
            userDAO.updateStatus(userId, status);
            if (!isSuperAdmin(userId)) {
                userDAO.updateRole(userId, cleanRole);
            }
        }
        Optional<Map<String, Object>> profile = profileDAO.findByUserId(userId);
        if (profile.isPresent()) {
            profileDAO.update(((Number) profile.get().get("profile_id")).longValue(), cleanRealName, cleanIdCard, clean(address), clean(notes));
        } else {
            profileDAO.create(userId, cleanRealName, cleanIdCard, clean(address), clean(notes));
        }
        return BusinessResult.ok(userId, "保存成功");
    }

    public BusinessResult deleteUser(long actorUserId, boolean admin, long userId) {
        if (!admin || userId <= 0) {
            return BusinessResult.fail("无权删除该用户");
        }
        if (actorUserId == userId) {
            return BusinessResult.fail("不能删除当前登录用户");
        }
        if (isSuperAdmin(userId)) {
            return BusinessResult.fail("不能删除超级管理员");
        }
        if (userDAO.findById(userId).isEmpty()) {
            return BusinessResult.fail("用户不存在");
        }
        try {
            return userDAO.deleteById(userId)
                ? BusinessResult.ok(userId, "删除成功")
                : BusinessResult.fail("删除失败，请刷新后重试");
        } catch (DBException ex) {
            return BusinessResult.fail("用户已有记录，不能删除");
        }
    }

    public List<Map<String, Object>> findCategories() {
        return categoryDAO.findAll();
    }

    public BusinessResult createCategory(String name, Long parentId) {
        if (name == null || name.trim().isEmpty()) {
            return BusinessResult.fail("请输入分类名称");
        }
        if (parentId != null && categoryDAO.findById(parentId).isEmpty()) {
            return BusinessResult.fail("父分类不存在");
        }

        long categoryId = categoryDAO.create(name.trim(), parentId);
        return BusinessResult.ok(categoryId, "保存成功");
    }

    public BusinessResult updateCategory(long categoryId, String name, Long parentId) {
        if (categoryId <= 0) {
            return BusinessResult.fail("请选择有效分类");
        }
        if (name == null || name.trim().isEmpty()) {
            return BusinessResult.fail("请输入分类名称");
        }
        if (parentId != null && parentId == categoryId) {
            return BusinessResult.fail("父分类不能选择自己");
        }
        if (parentId != null && categoryDAO.findById(parentId).isEmpty()) {
            return BusinessResult.fail("父分类不存在");
        }
        if (parentId != null && isCategoryDescendant(parentId, categoryId, categoryDAO.findAll())) {
            return BusinessResult.fail("父分类不能选择子分类");
        }
        if (!categoryDAO.update(categoryId, name.trim(), parentId)) {
            return BusinessResult.fail("分类不存在");
        }
        return BusinessResult.ok(categoryId, "保存成功");
    }

    public BusinessResult deleteCategory(long categoryId) {
        if (categoryId <= 0) {
            return BusinessResult.fail("请选择有效分类");
        }
        if (categoryDAO.findById(categoryId).isEmpty()) {
            return BusinessResult.fail("分类不存在");
        }
        if (!categoryDAO.findChildren(categoryId).isEmpty()) {
            return BusinessResult.fail("分类下还有子分类，不能删除");
        }
        if (!itemDAO.findByCategory(categoryId).isEmpty()) {
            return BusinessResult.fail("分类已被库存使用，不能删除");
        }
        try {
            return categoryDAO.deleteById(categoryId)
                ? BusinessResult.ok(categoryId, "删除成功")
                : BusinessResult.fail("删除失败，请刷新后重试");
        } catch (DBException ex) {
            return BusinessResult.fail("分类已被使用，不能删除");
        }
    }

    public List<Map<String, Object>> findItems() {
        return itemDAO.findAll();
    }

    public BusinessResult createItem(
        String title,
        long categoryId,
        BigDecimal amount,
        String description,
        List<String> images,
        Document metadata
    ) {
        if (title == null || title.trim().isEmpty()) {
            return BusinessResult.fail("请输入业务数据名称");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BusinessResult.fail("数量必须大于 0");
        }

        long itemId = itemDAO.create(title.trim(), categoryId, amount);
        detailDAO.upsertDetail(String.valueOf(itemId), clean(description), images == null ? List.of() : images, safe(metadata));
        return BusinessResult.ok(itemId, "保存成功");
    }

    public BusinessResult updateItem(
        long itemId,
        String title,
        long categoryId,
        BigDecimal amount,
        int status,
        String description,
        List<String> images,
        Document metadata
    ) {
        if (itemId <= 0) {
            return BusinessResult.fail("请选择有效业务数据");
        }
        if (title == null || title.trim().isEmpty()) {
            return BusinessResult.fail("请输入业务数据名称");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BusinessResult.fail("数量必须大于 0");
        }
        if (status < 0 || status > 1) {
            return BusinessResult.fail("请选择有效业务状态");
        }
        if (!itemDAO.update(itemId, title.trim(), categoryId, amount, status)) {
            return BusinessResult.fail("业务数据不存在");
        }

        detailDAO.upsertDetail(String.valueOf(itemId), clean(description), images == null ? List.of() : images, safe(metadata));
        return BusinessResult.ok(itemId, "保存成功");
    }

    public BusinessResult deleteItem(long itemId) {
        if (itemId <= 0) {
            return BusinessResult.fail("请选择有效业务数据");
        }
        if (!itemDAO.deleteById(itemId)) {
            return BusinessResult.fail("业务数据不存在或已有记录");
        }
        detailDAO.deleteByItemId(String.valueOf(itemId));
        return BusinessResult.ok(itemId, "删除成功");
    }

    public BusinessResult saveItemDetail(long itemId, String description, List<String> images, Document metadata) {
        if (itemId <= 0) {
            return BusinessResult.fail("请选择有效业务数据");
        }
        if (itemDAO.findById(itemId).isEmpty()) {
            return BusinessResult.fail("业务数据不存在");
        }

        detailDAO.upsertDetail(String.valueOf(itemId), clean(description), images == null ? List.of() : images, safe(metadata));
        return BusinessResult.ok(itemId, "保存成功");
    }

    public Optional<Document> findItemDetail(long itemId) {
        return itemId <= 0 ? Optional.empty() : detailDAO.findByItemId(String.valueOf(itemId));
    }

    public List<Document> findItemComments(long itemId) {
        return itemId <= 0 ? List.of() : commentDAO.findByItemId(String.valueOf(itemId), 30);
    }

    public BusinessResult createComment(long userId, long itemId, String content, int rating, List<String> tags) {
        if (userId <= 0 || itemId <= 0) {
            return BusinessResult.fail("请选择有效用户和库存批次");
        }
        if (itemDAO.findById(itemId).isEmpty()) {
            return BusinessResult.fail("库存批次不存在");
        }
        if (content == null || content.trim().isEmpty()) {
            return BusinessResult.fail("请输入评论内容");
        }
        if (rating < 1 || rating > 5) {
            return BusinessResult.fail("评分必须在 1 到 5 之间");
        }

        commentDAO.createComment(String.valueOf(userId), String.valueOf(itemId), content.trim(), rating, tags == null ? List.of() : tags);
        logAction(userId, itemId, "CREATE_COMMENT");
        return BusinessResult.ok(itemId, "评论已发布");
    }

    public BusinessResult deleteComment(long userId, boolean admin, String commentId) {
        if (userId <= 0 || commentId == null || commentId.isBlank()) {
            return BusinessResult.fail("请选择有效评论");
        }
        boolean deleted = admin
            ? commentDAO.deleteById(commentId)
            : commentDAO.deleteByIdAndUser(commentId, String.valueOf(userId));
        if (deleted) {
            logAction(userId, 0L, "DELETE_COMMENT");
        }
        return deleted
            ? BusinessResult.ok(0L, "评论已删除")
            : BusinessResult.fail("评论不存在或无权删除");
    }

    public String findUsername(long userId) {
        if (userId <= 0) {
            return "未知用户";
        }
        return userDAO.findById(userId)
            .map(row -> String.valueOf(row.get("username")))
            .orElse("用户 " + userId);
    }

    public BusinessResult createOrder(long userId, long itemId, BigDecimal amount) {
        if (userId <= 0 || itemId <= 0) {
            return BusinessResult.fail("请选择有效用户和业务数据");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BusinessResult.fail("数量必须大于 0");
        }

        Optional<Map<String, Object>> item = itemDAO.findById(itemId);
        if (item.isEmpty()) {
            return BusinessResult.fail("业务数据不存在");
        }
        if (((Number) item.get().get("status")).intValue() != 1) {
            return BusinessResult.fail("业务数据不可用");
        }
        if (((BigDecimal) item.get().get("amount")).compareTo(amount) < 0) {
            return BusinessResult.fail("数量不足");
        }

        long orderId = orderDAO.createOrder(userId, itemId, amount);
        logAction(userId, itemId, "CREATE_ORDER");
        return BusinessResult.ok(orderId, "记录创建成功");
    }

    public List<Map<String, Object>> findOrdersByUser(long userId) {
        return userId <= 0 ? List.of() : orderDAO.findByUser(userId);
    }

    public List<Map<String, Object>> findOrders(long userId, boolean admin) {
        if (admin) {
            return orderDAO.findAll();
        }
        return findOrdersByUser(userId);
    }

    public BusinessResult updateOwnOrder(long userId, long orderId, long itemId, BigDecimal amount) {
        if (userId <= 0 || orderId <= 0 || itemId <= 0) {
            return BusinessResult.fail("请选择有效用户、记录和业务数据");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BusinessResult.fail("数量必须大于 0");
        }

        Optional<Map<String, Object>> item = itemDAO.findById(itemId);
        if (item.isEmpty()) {
            return BusinessResult.fail("业务数据不存在");
        }
        if (((Number) item.get().get("status")).intValue() != 1) {
            return BusinessResult.fail("业务数据不可用");
        }
        if (((BigDecimal) item.get().get("amount")).compareTo(amount) < 0) {
            return BusinessResult.fail("数量不足");
        }

        if (!orderDAO.updatePendingOrder(userId, orderId, itemId, amount)) {
            return BusinessResult.fail("记录不存在或不可编辑");
        }
        logAction(userId, itemId, "UPDATE_ORDER");
        return BusinessResult.ok(orderId, "保存成功");
    }

    public BusinessResult updateOrderStatus(long orderId, int status) {
        if (orderId <= 0 || status < 0 || status > 2) {
            return BusinessResult.fail("请选择有效记录状态");
        }
        boolean updated = status == 1 ? orderDAO.completeOrder(orderId) : orderDAO.updateStatus(orderId, status);
        if (!updated) {
            return BusinessResult.fail("记录不存在");
        }
        logAction(0L, 0L, "UPDATE_ORDER_STATUS");
        return BusinessResult.ok(orderId, "状态更新成功");
    }

    public BusinessResult deleteOrder(long orderId) {
        if (orderId <= 0) {
            return BusinessResult.fail("请选择有效记录");
        }
        if (!orderDAO.deleteById(orderId)) {
            return BusinessResult.fail("记录不存在");
        }
        logAction(0L, 0L, "DELETE_ORDER");
        return BusinessResult.ok(orderId, "删除成功");
    }

    public BusinessResult deleteOwnOrder(long userId, long orderId) {
        if (userId <= 0 || orderId <= 0) {
            return BusinessResult.fail("请选择有效用户和记录");
        }
        if (!orderDAO.deletePendingByUser(userId, orderId)) {
            return BusinessResult.fail("记录不存在或不可删除");
        }
        logAction(userId, 0L, "DELETE_ORDER");
        return BusinessResult.ok(orderId, "删除成功");
    }

    public List<Document> topActionItems(int limit) {
        return logDAO.topItems(limit);
    }

    public List<Document> commentRatingSummary() {
        return commentDAO.ratingSummary();
    }

    public List<Document> commentRatingSummaryByUser(long userId) {
        return userId <= 0 ? List.of() : commentDAO.ratingSummaryByUser(String.valueOf(userId));
    }

    public List<Map<String, Object>> monthlyReport(int year, int month) {
        if (year < 2000 || month < 1 || month > 12) {
            return List.of();
        }
        return orderDAO.monthlyReport(year, month);
    }

    public List<Document> auditSummary() {
        return systemLogDAO.auditSummary();
    }

    public List<ItemInsightDTO> findItemInsights() {
        return recommendService.findItemInsights();
    }

    public List<RecommendationDTO> recommendItems(long userId, int limit) {
        return recommendService.recommendItems(userId, limit);
    }

    public List<Document> findSystemLogs(String logType, int limit) {
        return systemLogDAO.findByType(logType, limit);
    }

    public List<Document> findUserActionLogs(long userId, int limit) {
        return userId <= 0 ? List.of() : logDAO.findByUserId(String.valueOf(userId), limit);
    }

    public List<Document> findActionLogs(long userId, boolean admin, int limit) {
        return admin ? logDAO.findRecent(limit) : findUserActionLogs(userId, limit);
    }

    private void logAction(long userId, long itemId, String actionType) {
        logDAO.insertActionLog(
            userId <= 0 ? "SYSTEM" : String.valueOf(userId),
            itemId <= 0 ? "NONE" : String.valueOf(itemId),
            actionType,
            0,
            new Document()
        );
    }

    private static LogDAO silentLogDAO() {
        return new LogDAO() {
            @Override
            public void insertActionLog(String userId, String itemId, String actionType, int durationSeconds, Document clientInfo) {
            }
        };
    }

    private static SystemLogDAO silentSystemLogDAO() {
        return new SystemLogDAO();
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private Document safe(Document metadata) {
        return metadata == null ? new Document() : metadata;
    }

    private boolean isCategoryDescendant(long categoryId, long parentId, List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            Object rowParentId = row.get("parent_id");
            long rowId = ((Number) row.get("category_id")).longValue();
            if (rowParentId != null && ((Number) rowParentId).longValue() == parentId) {
                if (rowId == categoryId || isCategoryDescendant(categoryId, rowId, rows)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSuperAdmin(long userId) {
        return userId == SUPER_ADMIN_ID;
    }

    public record BusinessResult(boolean success, String message, long id) {
        static BusinessResult ok(long id, String message) {
            return new BusinessResult(true, message, id);
        }

        static BusinessResult fail(String message) {
            return new BusinessResult(false, message, 0L);
        }
    }
}
