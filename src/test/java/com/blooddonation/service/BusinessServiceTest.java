package com.blooddonation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bson.Document;
import org.junit.jupiter.api.Test;

/** 验证核心业务校验、权限、跨库协调、报表和推荐行为。 */
class BusinessServiceTest {
    /** 验证分类和库存查询返回展示数据。 */
    @Test
    void findCategoriesAndItemsReturnDisplayRows() {
        FakeCategoryDAO categories = new FakeCategoryDAO();
        categories.rows = List.of(Map.of("category_id", 4L, "name", "A型全血"));
        FakeItemDAO items = new FakeItemDAO();
        items.rows = List.of(item(3L, new BigDecimal("10.00"), 1));

        BusinessService service = new BusinessService(items, categories, new FakeDetailDAO(), new FakeOrderDAO());

        assertEquals("A型全血", service.findCategories().get(0).get("name"));
        assertEquals(3L, service.findItems().get(0).get("item_id"));
    }

    /** 验证创建分类时清理名称并保存父分类。 */
    @Test
    void createCategoryTrimsNameAndStoresParent() {
        FakeCategoryDAO categories = new FakeCategoryDAO();
        categories.rows = List.of(Map.of("category_id", 1L, "name", "血型"));

        BusinessService.BusinessResult result = new BusinessService(new FakeItemDAO(), categories, new FakeDetailDAO(), new FakeOrderDAO())
            .createCategory(" A型 ", 1L);

        assertTrue(result.success());
        assertEquals(31L, result.id());
        assertEquals("A型", categories.createdName);
        assertEquals(1L, categories.createdParentId);
    }

    /** 验证分类不能选择自己作为父分类。 */
    @Test
    void updateCategoryRejectsSelfParent() {
        BusinessService.BusinessResult result = new BusinessService(new FakeItemDAO(), new FakeCategoryDAO(), new FakeDetailDAO(), new FakeOrderDAO())
            .updateCategory(3L, "A型", 3L);

        assertFalse(result.success());
        assertEquals("父分类不能选择自己", result.message());
    }

    /** 验证分类不能选择子分类作为父分类。 */
    @Test
    void updateCategoryRejectsChildParent() {
        FakeCategoryDAO categories = new FakeCategoryDAO();
        categories.rows = List.of(
            Map.of("category_id", 1L, "name", "血型"),
            Map.of("category_id", 2L, "name", "A型", "parent_id", 1L)
        );

        BusinessService.BusinessResult result = new BusinessService(new FakeItemDAO(), categories, new FakeDetailDAO(), new FakeOrderDAO())
            .updateCategory(1L, "血型", 2L);

        assertFalse(result.success());
        assertEquals("父分类不能选择子分类", result.message());
    }

    /** 验证可删除未被使用的分类。 */
    @Test
    void deleteCategoryDeletesSelectedCategory() {
        FakeItemDAO items = new FakeItemDAO();
        FakeCategoryDAO categories = new FakeCategoryDAO();
        categories.rows = List.of(Map.of("category_id", 3L, "name", "A型"));

        BusinessService.BusinessResult result = new BusinessService(items, categories, new FakeDetailDAO(), new FakeOrderDAO())
            .deleteCategory(3L);

        assertTrue(result.success());
        assertEquals(3L, categories.deletedCategoryId);
    }

    /** 验证包含子分类的分类不能删除。 */
    @Test
    void deleteCategoryRejectsCategoryWithChildren() {
        FakeCategoryDAO categories = new FakeCategoryDAO();
        categories.rows = List.of(
            Map.of("category_id", 3L, "name", "血型"),
            Map.of("category_id", 4L, "name", "A型", "parent_id", 3L)
        );

        BusinessService.BusinessResult result = new BusinessService(new FakeItemDAO(), categories, new FakeDetailDAO(), new FakeOrderDAO())
            .deleteCategory(3L);

        assertFalse(result.success());
        assertEquals("分类下还有子分类，不能删除", result.message());
        assertEquals(0L, categories.deletedCategoryId);
    }

    /** 验证被库存使用的分类不能删除。 */
    @Test
    void deleteCategoryRejectsCategoryUsedByInventory() {
        FakeItemDAO items = new FakeItemDAO();
        items.rows = List.of(Map.of("item_id", 9L, "category_id", 3L));
        FakeCategoryDAO categories = new FakeCategoryDAO();
        categories.rows = List.of(Map.of("category_id", 3L, "name", "A型"));

        BusinessService.BusinessResult result = new BusinessService(items, categories, new FakeDetailDAO(), new FakeOrderDAO())
            .deleteCategory(3L);

        assertFalse(result.success());
        assertEquals("分类已被库存使用，不能删除", result.message());
        assertEquals(0L, categories.deletedCategoryId);
    }

    /** 验证创建库存时同时写入主数据和详情。 */
    @Test
    void createItemStoresMysqlRowAndMongoDetail() {
        FakeItemDAO items = new FakeItemDAO();
        FakeDetailDAO details = new FakeDetailDAO();

        BusinessService.BusinessResult result = new BusinessService(items, details, new FakeOrderDAO())
            .createItem(" A 型血库存 ", 1L, new BigDecimal("20.00"), " 常规库存 ", List.of("a.png"), new Document("blood_type", "A"));

        assertTrue(result.success());
        assertEquals(11L, result.id());
        assertEquals("A 型血库存", items.createdTitle);
        assertEquals("11", details.itemId);
        assertEquals("常规库存", details.description);
        assertEquals("A", details.metadata.getString("blood_type"));
    }

    /** 验证更新库存时同时更新主数据和详情。 */
    @Test
    void updateItemUpdatesMysqlRowAndMongoDetail() {
        FakeItemDAO items = new FakeItemDAO();
        FakeDetailDAO details = new FakeDetailDAO();

        BusinessService.BusinessResult result = new BusinessService(items, details, new FakeOrderDAO())
            .updateItem(3L, "O 型库存", 7L, new BigDecimal("30.00"), 1, "更新详情", List.of(), new Document("blood_type", "O"));

        assertTrue(result.success());
        assertEquals(3L, items.updatedItemId);
        assertEquals("O 型库存", items.updatedTitle);
        assertEquals("3", details.itemId);
        assertEquals("更新详情", details.description);
    }

    /** 验证删除库存时同时删除主数据和详情。 */
    @Test
    void deleteItemDeletesMysqlRowAndMongoDetail() {
        FakeItemDAO items = new FakeItemDAO();
        FakeDetailDAO details = new FakeDetailDAO();

        BusinessService.BusinessResult result = new BusinessService(items, details, new FakeOrderDAO()).deleteItem(3L);

        assertTrue(result.success());
        assertEquals(3L, items.deletedItemId);
        assertEquals("3", details.deletedItemId);
    }

    /** 验证不存在的库存不能保存详情。 */
    @Test
    void saveItemDetailRejectsMissingItem() {
        BusinessService.BusinessResult result = new BusinessService(new FakeItemDAO(), new FakeDetailDAO(), new FakeOrderDAO())
            .saveItemDetail(99L, "详情", List.of(), new Document());

        assertFalse(result.success());
        assertEquals("业务数据不存在", result.message());
    }

    /** 验证可以读取 MongoDB 库存详情。 */
    @Test
    void findItemDetailReturnsMongoDetail() {
        FakeDetailDAO details = new FakeDetailDAO();
        details.found = new Document("item_id", "3").append("description", "详情");

        Optional<Document> detail = new BusinessService(new FakeItemDAO(), details, new FakeOrderDAO()).findItemDetail(3L);

        assertTrue(detail.isPresent());
        assertEquals("详情", detail.get().getString("description"));
    }

    /** 验证评论要求库存存在且评分合法。 */
    @Test
    void createCommentRequiresExistingItemAndValidRating() {
        FakeItemDAO items = new FakeItemDAO();
        items.item = item(3L, new BigDecimal("10.00"), 1);
        FakeCommentDAO comments = new FakeCommentDAO();
        BusinessService service = new BusinessService(items, new FakeCategoryDAO(), new FakeDetailDAO(), comments, new FakeOrderDAO());

        BusinessService.BusinessResult result = service.createComment(2L, 3L, "  质量稳定  ", 5, List.of("稳定"));

        assertTrue(result.success());
        assertEquals("2", comments.userId);
        assertEquals("3", comments.itemId);
        assertEquals("质量稳定", comments.content);
        assertEquals(5, comments.rating);
        assertEquals(List.of("稳定"), comments.tags);

        BusinessService.BusinessResult invalid = service.createComment(2L, 3L, "评分异常", 6, List.of());

        assertFalse(invalid.success());
        assertEquals("评分必须在 1 到 5 之间", invalid.message());
    }

    /** 验证评论仅允许管理员或作者删除。 */
    @Test
    void deleteCommentAllowsAdminOrOwnerOnly() {
        FakeCommentDAO comments = new FakeCommentDAO();
        comments.deleteSucceeds = true;
        BusinessService service = new BusinessService(new FakeItemDAO(), new FakeCategoryDAO(), new FakeDetailDAO(), comments, new FakeOrderDAO());

        BusinessService.BusinessResult adminResult = service.deleteComment(1L, true, "c1");
        BusinessService.BusinessResult ownerResult = service.deleteComment(2L, false, "c2");

        assertTrue(adminResult.success());
        assertTrue(ownerResult.success());
        assertEquals("c1", comments.deletedId);
        assertEquals("c2", comments.deletedOwnId);
        assertEquals("2", comments.deletedOwnUserId);
    }

    /** 验证用户编号可以转换为显示名称。 */
    @Test
    void findUsernameReturnsUserDisplayName() {
        FakeUserDAO users = new FakeUserDAO();
        users.row = Map.of("user_id", 2L, "username", "user01");
        BusinessService service = new BusinessService(
            new FakeItemDAO(),
            new FakeCategoryDAO(),
            new FakeDetailDAO(),
            new FakeCommentDAO(),
            new FakeOrderDAO(),
            users
        );

        assertEquals("user01", service.findUsername(2L));
        assertEquals("用户 3", service.findUsername(3L));
    }

    /** 验证档案保存需要本人或管理员权限。 */
    @Test
    void userProfileSaveRequiresOwnerOrAdminAndCreatesProfile() {
        FakeUserDAO users = new FakeUserDAO();
        users.row = Map.of("user_id", 2L, "username", "user01");
        FakeProfileDAO profiles = new FakeProfileDAO();
        BusinessService service = new BusinessService(
            new FakeItemDAO(),
            new FakeCategoryDAO(),
            new FakeDetailDAO(),
            new FakeCommentDAO(),
            new FakeOrderDAO(),
            users,
            profiles,
            new FakeLogDAO(),
            new FakeSystemLogDAO()
        );

        BusinessService.BusinessResult denied = service.saveUserProfile(
            3L, false, 2L, "user01@example.test", "19900000002", "USER", 1, "用户一", "TEST-ID-0020", "地址", "备注"
        );
        BusinessService.BusinessResult saved = service.saveUserProfile(
            2L, false, 2L, "user01@example.test", "19900000002", "USER", 0, "用户一", "TEST-ID-0020", "地址", "备注"
        );

        assertFalse(denied.success());
        assertTrue(saved.success());
        assertEquals(2L, users.updatedUserId);
        assertEquals(0, users.updatedStatus);
        assertEquals(2L, profiles.createdUserId);
        assertEquals("用户一", profiles.createdRealName);
    }

    /** 验证只有超级管理员可以修改角色。 */
    @Test
    void onlySuperAdminCanChangeUserRole() {
        FakeUserDAO users = new FakeUserDAO();
        users.row = Map.of("user_id", 2L, "username", "user01", "role", "USER");
        FakeProfileDAO profiles = new FakeProfileDAO();
        BusinessService service = new BusinessService(
            new FakeItemDAO(),
            new FakeCategoryDAO(),
            new FakeDetailDAO(),
            new FakeCommentDAO(),
            new FakeOrderDAO(),
            users,
            profiles,
            new FakeLogDAO(),
            new FakeSystemLogDAO()
        );

        BusinessService.BusinessResult denied = service.saveUserProfile(
            9L, true, 2L, "user01@example.test", "19900000002", "ADMIN", 1, "用户一", "TEST-ID-0020", "地址", "备注"
        );
        BusinessService.BusinessResult saved = service.saveUserProfile(
            1L, true, 2L, "user01@example.test", "19900000002", "ADMIN", 1, "用户一", "TEST-ID-0020", "地址", "备注"
        );

        assertFalse(denied.success());
        assertEquals("只有超级管理员可以修改用户权限", denied.message());
        assertTrue(saved.success());
        assertEquals("ADMIN", users.updatedRole);
    }

    /** 验证不能删除自己或超级管理员。 */
    @Test
    void deleteUserRejectsSelfAndSuperAdmin() {
        FakeUserDAO users = new FakeUserDAO();
        users.row = Map.of("user_id", 2L, "username", "user01", "role", "USER");
        BusinessService service = new BusinessService(
            new FakeItemDAO(),
            new FakeCategoryDAO(),
            new FakeDetailDAO(),
            new FakeCommentDAO(),
            new FakeOrderDAO(),
            users,
            new FakeProfileDAO(),
            new FakeLogDAO(),
            new FakeSystemLogDAO()
        );

        assertEquals("不能删除当前登录用户", service.deleteUser(2L, true, 2L).message());
        assertEquals("不能删除超级管理员", service.deleteUser(2L, true, 1L).message());

        BusinessService.BusinessResult deleted = service.deleteUser(1L, true, 2L);

        assertTrue(deleted.success());
        assertEquals(2L, users.deletedUserId);
    }

    /** 验证创建申请前检查库存状态和数量。 */
    @Test
    void createOrderChecksItemStatusAndAmountBeforeWriting() {
        FakeItemDAO items = new FakeItemDAO();
        items.item = item(3L, new BigDecimal("10.00"), 1);
        FakeOrderDAO orders = new FakeOrderDAO();
        FakeLogDAO logs = new FakeLogDAO();

        BusinessService.BusinessResult result = new BusinessService(items, new FakeCategoryDAO(), new FakeDetailDAO(), new FakeCommentDAO(), orders, new FakeUserDAO(), logs, new FakeSystemLogDAO())
            .createOrder(2L, 3L, new BigDecimal("8.00"));

        assertTrue(result.success());
        assertEquals(21L, result.id());
        assertEquals(2L, orders.userId);
        assertEquals(3L, orders.itemId);
        assertEquals(new BigDecimal("8.00"), orders.amount);
        assertEquals("2", logs.userId);
        assertEquals("3", logs.itemId);
        assertEquals("CREATE_ORDER", logs.actionType);
    }

    /** 验证不可用库存不能创建申请。 */
    @Test
    void createOrderRejectsUnavailableItem() {
        FakeItemDAO items = new FakeItemDAO();
        items.item = item(3L, new BigDecimal("10.00"), 0);
        FakeOrderDAO orders = new FakeOrderDAO();

        BusinessService.BusinessResult result = new BusinessService(items, new FakeDetailDAO(), orders)
            .createOrder(2L, 3L, new BigDecimal("8.00"));

        assertFalse(result.success());
        assertEquals("业务数据不可用", result.message());
        assertEquals(0L, orders.userId);
    }

    /** 验证申请数量不能超过库存。 */
    @Test
    void createOrderRejectsAmountGreaterThanInventory() {
        FakeItemDAO items = new FakeItemDAO();
        items.item = item(3L, new BigDecimal("5.00"), 1);

        BusinessService.BusinessResult result = new BusinessService(items, new FakeDetailDAO(), new FakeOrderDAO())
            .createOrder(2L, 3L, new BigDecimal("8.00"));

        assertFalse(result.success());
        assertEquals("数量不足", result.message());
    }

    /** 验证查询指定用户的申请记录。 */
    @Test
    void findOrdersByUserReturnsUserRecords() {
        FakeOrderDAO orders = new FakeOrderDAO();
        orders.rows = List.of(Map.of("order_id", 21L));

        List<Map<String, Object>> result = new BusinessService(new FakeItemDAO(), new FakeDetailDAO(), orders)
            .findOrdersByUser(2L);

        assertEquals(1, result.size());
        assertEquals(21L, result.get(0).get("order_id"));
    }

    /** 验证管理员可以查询全部申请。 */
    @Test
    void adminFindsAllOrders() {
        FakeOrderDAO orders = new FakeOrderDAO();
        orders.allRows = List.of(Map.of("order_id", 21L), Map.of("order_id", 22L));

        List<Map<String, Object>> result = new BusinessService(new FakeItemDAO(), new FakeDetailDAO(), orders)
            .findOrders(1L, true);

        assertEquals(2, result.size());
    }

    /** 验证用户可以更新自己的待审批申请。 */
    @Test
    void userUpdatesOwnPendingOrder() {
        FakeItemDAO items = new FakeItemDAO();
        items.item = item(3L, new BigDecimal("10.00"), 1);
        FakeOrderDAO orders = new FakeOrderDAO();
        orders.updateOrderSucceeds = true;

        BusinessService.BusinessResult result = new BusinessService(items, new FakeDetailDAO(), orders)
            .updateOwnOrder(2L, 21L, 3L, new BigDecimal("8.00"));

        assertTrue(result.success());
        assertEquals(2L, orders.updatedUserId);
        assertEquals(21L, orders.updatedOrderId);
        assertEquals(3L, orders.updatedItemId);
        assertEquals(new BigDecimal("8.00"), orders.updatedAmount);
    }

    /** 验证申请更新会拒绝不可用库存。 */
    @Test
    void userOrderUpdateRejectsUnavailableInventory() {
        FakeItemDAO items = new FakeItemDAO();
        items.item = item(3L, new BigDecimal("5.00"), 1);

        BusinessService.BusinessResult result = new BusinessService(items, new FakeDetailDAO(), new FakeOrderDAO())
            .updateOwnOrder(2L, 21L, 3L, new BigDecimal("8.00"));

        assertFalse(result.success());
        assertEquals("数量不足", result.message());
    }

    /** 验证更新不存在申请时返回失败。 */
    @Test
    void updateOrderStatusReportsMissingRecord() {
        FakeOrderDAO orders = new FakeOrderDAO();

        BusinessService.BusinessResult result = new BusinessService(new FakeItemDAO(), new FakeDetailDAO(), orders)
            .updateOrderStatus(21L, 1);

        assertFalse(result.success());
        assertEquals("记录不存在", result.message());
        assertEquals(21L, orders.completedOrderId);
        assertEquals(0L, orders.updatedOrderId);
    }

    /** 验证完成申请使用库存扣减事务。 */
    @Test
    void completedOrderUsesInventoryDeductionTransaction() {
        FakeOrderDAO orders = new FakeOrderDAO();
        orders.completeSucceeds = true;

        BusinessService.BusinessResult result = new BusinessService(new FakeItemDAO(), new FakeDetailDAO(), orders)
            .updateOrderStatus(21L, 1);

        assertTrue(result.success());
        assertEquals(21L, orders.completedOrderId);
        assertEquals(0L, orders.updatedOrderId);
    }

    /** 验证取消申请只更新状态。 */
    @Test
    void cancelledOrderOnlyUpdatesStatus() {
        FakeOrderDAO orders = new FakeOrderDAO();
        orders.updateSucceeds = true;

        BusinessService.BusinessResult result = new BusinessService(new FakeItemDAO(), new FakeDetailDAO(), orders)
            .updateOrderStatus(21L, 2);

        assertTrue(result.success());
        assertEquals(0L, orders.completedOrderId);
        assertEquals(21L, orders.updatedOrderId);
        assertEquals(2, orders.updatedStatus);
    }

    /** 验证管理员可以删除申请记录。 */
    @Test
    void deleteOrderDeletesRecord() {
        FakeOrderDAO orders = new FakeOrderDAO();
        orders.deleteSucceeds = true;

        BusinessService.BusinessResult result = new BusinessService(new FakeItemDAO(), new FakeDetailDAO(), orders).deleteOrder(21L);

        assertTrue(result.success());
        assertEquals(21L, orders.deletedOrderId);
    }

    /** 验证用户可以删除自己的待审批申请。 */
    @Test
    void userDeletesOwnPendingOrder() {
        FakeOrderDAO orders = new FakeOrderDAO();
        orders.deleteOwnSucceeds = true;

        BusinessService.BusinessResult result = new BusinessService(new FakeItemDAO(), new FakeDetailDAO(), orders)
            .deleteOwnOrder(2L, 21L);

        assertTrue(result.success());
        assertEquals(2L, orders.deletedUserId);
        assertEquals(21L, orders.deletedOrderId);
    }

    /** 验证统计接口暴露 MongoDB 聚合结果。 */
    @Test
    void statisticsExposeMongoAggregates() {
        FakeLogDAO logs = new FakeLogDAO();
        logs.topItems = List.of(new Document("item_id", "3").append("action_count", 7));
        FakeCommentDAO comments = new FakeCommentDAO();
        comments.ratingSummary = List.of(new Document("item_id", "3").append("comment_count", 2).append("average_rating", 4.5));
        FakeSystemLogDAO systemLogs = new FakeSystemLogDAO();
        systemLogs.auditSummary = List.of(new Document("log_type", "LOGIN").append("log_level", "INFO").append("log_count", 3));

        BusinessService service = new BusinessService(
            new FakeItemDAO(),
            new FakeCategoryDAO(),
            new FakeDetailDAO(),
            comments,
            new FakeOrderDAO(),
            new FakeUserDAO(),
            logs,
            systemLogs
        );

        assertEquals(7, service.topActionItems(5).get(0).getInteger("action_count"));
        assertEquals(4.5, service.commentRatingSummary().get(0).getDouble("average_rating"));
        assertEquals(3, service.auditSummary().get(0).getInteger("log_count"));
    }

    /** 验证个人评分统计只包含自己的评论。 */
    @Test
    void userRatingSummaryUsesOnlyOwnComments() {
        FakeCommentDAO comments = new FakeCommentDAO();
        comments.userRatingSummary = List.of(new Document("item_id", "3").append("comment_count", 1).append("average_rating", 5D));

        List<Document> result = new BusinessService(new FakeItemDAO(), new FakeCategoryDAO(), new FakeDetailDAO(), comments, new FakeOrderDAO())
            .commentRatingSummaryByUser(2L);

        assertEquals("2", comments.ratingUserId);
        assertEquals(5D, result.get(0).getDouble("average_rating"));
    }

    /** 验证月度报表传递年份和月份。 */
    @Test
    void monthlyReportCallsOrderProcedureByYearAndMonth() {
        FakeOrderDAO orders = new FakeOrderDAO();
        orders.monthlyReport = List.of(Map.of("category_name", "A型", "used_amount", new BigDecimal("6.00")));

        List<Map<String, Object>> result = new BusinessService(new FakeItemDAO(), new FakeDetailDAO(), orders)
            .monthlyReport(2026, 7);

        assertEquals("A型", result.get(0).get("category_name"));
        assertEquals(2026, orders.reportYear);
        assertEquals(7, orders.reportMonth);
    }

    /** 验证库存洞察合并 MySQL 与 MongoDB 数据。 */
    @Test
    void itemInsightsJoinMysqlRowsWithMongoStats() {
        FakeItemDAO items = new FakeItemDAO();
        items.rows = List.of(Map.of(
            "item_id", 3L,
            "title", "A型库存",
            "category_id", 4L,
            "amount", new BigDecimal("12.00"),
            "status", 1
        ));
        FakeCategoryDAO categories = new FakeCategoryDAO();
        categories.rows = List.of(Map.of("category_id", 4L, "name", "A型"));
        FakeDetailDAO details = new FakeDetailDAO();
        details.found = new Document("item_id", "3")
            .append("description", "常规库存")
            .append("metadata", new Document("blood_type", "A型"));
        FakeCommentDAO comments = new FakeCommentDAO();
        comments.ratingSummary = List.of(new Document("item_id", "3").append("comment_count", 2).append("average_rating", 4.5));
        FakeLogDAO logs = new FakeLogDAO();
        logs.topItems = List.of(new Document("item_id", "3").append("action_count", 7));
        FakeOrderDAO orders = new FakeOrderDAO();
        orders.itemOrderCounts = List.of(Map.of("item_id", 3L, "order_count", 5L));

        List<ItemInsightDTO> result = new BusinessService(items, categories, details, comments, orders, new FakeUserDAO(), logs, new FakeSystemLogDAO())
            .findItemInsights();

        assertEquals(1, result.size());
        assertEquals("A型库存", result.get(0).title());
        assertEquals("A型", result.get(0).categoryName());
        assertEquals("常规库存", result.get(0).description());
        assertEquals("A型", result.get(0).bloodType());
        assertEquals(2, result.get(0).commentCount());
        assertEquals(4.5, result.get(0).averageRating());
        assertEquals(7, result.get(0).actionCount());
        assertEquals(5, result.get(0).orderCount());
    }

    /** 验证推荐优先相关分类并提供理由。 */
    @Test
    void recommendationsPreferUserRelatedCategoriesAndExplainReason() {
        FakeItemDAO items = new FakeItemDAO();
        items.rows = List.of(
            Map.of("item_id", 3L, "title", "A型库存", "category_id", 4L, "amount", new BigDecimal("12.00"), "status", 1),
            Map.of("item_id", 8L, "title", "O型库存", "category_id", 9L, "amount", new BigDecimal("20.00"), "status", 1)
        );
        items.item = Map.of("item_id", 3L, "title", "A型库存", "category_id", 4L, "amount", new BigDecimal("12.00"), "status", 1);
        FakeCategoryDAO categories = new FakeCategoryDAO();
        categories.rows = List.of(Map.of("category_id", 4L, "name", "A型"), Map.of("category_id", 9L, "name", "O型"));
        FakeOrderDAO orders = new FakeOrderDAO();
        orders.rows = List.of(Map.of("order_id", 21L, "item_id", 3L));

        List<RecommendationDTO> result = new BusinessService(items, categories, new FakeDetailDAO(), new FakeCommentDAO(), orders, new FakeUserDAO(), new FakeLogDAO(), new FakeSystemLogDAO())
            .recommendItems(2L, 5);

        assertEquals(2, result.size());
        assertEquals(3L, result.get(0).item().itemId());
        assertEquals("你最近申请过同类库存", result.get(0).reason());
        assertEquals("可用库存推荐", result.get(1).reason());
    }

    /** @return 测试使用的库存行 */
    private static Map<String, Object> item(long id, BigDecimal amount, int status) {
        Map<String, Object> row = new HashMap<>();
        row.put("item_id", id);
        row.put("amount", amount);
        row.put("status", status);
        return row;
    }

    /** 提供库存数据和操作记录的测试替身。 */
    private static class FakeItemDAO extends ItemDAO {
        private Map<String, Object> item;
        private List<Map<String, Object>> rows = List.of();
        private String createdTitle;
        private long updatedItemId;
        private String updatedTitle;
        private long deletedItemId;

        @Override
        public long create(String title, long categoryId, BigDecimal amount) {
            createdTitle = title;
            item = item(11L, amount, 1);
            return 11L;
        }

        @Override
        public Optional<Map<String, Object>> findById(long itemId) {
            return item != null && ((Number) item.get("item_id")).longValue() == itemId
                ? Optional.of(item)
                : Optional.empty();
        }

        @Override
        public List<Map<String, Object>> findAll() {
            return rows;
        }

        @Override
        public List<Map<String, Object>> findByCategory(long categoryId) {
            return rows.stream()
                .filter(row -> row.get("category_id") != null && ((Number) row.get("category_id")).longValue() == categoryId)
                .toList();
        }

        @Override
        public boolean update(long itemId, String title, long categoryId, BigDecimal amount, int status) {
            updatedItemId = itemId;
            updatedTitle = title;
            return true;
        }

        @Override
        public boolean deleteById(long itemId) {
            deletedItemId = itemId;
            return true;
        }
    }

    /** 提供分类树数据的测试替身。 */
    private static class FakeCategoryDAO extends CategoryDAO {
        private List<Map<String, Object>> rows = List.of();
        private String createdName;
        private Long createdParentId;
        private long deletedCategoryId;

        @Override
        public List<Map<String, Object>> findAll() {
            return rows;
        }

        @Override
        public long create(String name, Long parentId) {
            createdName = name;
            createdParentId = parentId;
            return 31L;
        }

        @Override
        public Optional<Map<String, Object>> findById(long categoryId) {
            return rows.stream()
                .filter(row -> ((Number) row.get("category_id")).longValue() == categoryId)
                .findFirst();
        }

        @Override
        public List<Map<String, Object>> findChildren(Long parentId) {
            return rows.stream()
                .filter(row -> row.get("parent_id") != null && ((Number) row.get("parent_id")).longValue() == parentId)
                .toList();
        }

        @Override
        public boolean update(long categoryId, String name, Long parentId) {
            return true;
        }

        @Override
        public boolean deleteById(long categoryId) {
            deletedCategoryId = categoryId;
            return true;
        }
    }

    /** 提供库存详情数据的测试替身。 */
    private static class FakeDetailDAO extends DetailDAO {
        private String itemId;
        private String description;
        private Document metadata;
        private Document found;
        private String deletedItemId;

        @Override
        public void upsertDetail(String itemId, String description, List<String> images, Document metadata) {
            this.itemId = itemId;
            this.description = description;
            this.metadata = metadata;
        }

        @Override
        public Optional<Document> findByItemId(String itemId) {
            return Optional.ofNullable(found);
        }

        @Override
        public boolean deleteByItemId(String itemId) {
            deletedItemId = itemId;
            return true;
        }
    }

    /** 提供评论和评分数据的测试替身。 */
    private static class FakeCommentDAO extends CommentDAO {
        private String userId;
        private String itemId;
        private String content;
        private int rating;
        private List<String> tags;
        private String deletedId;
        private String deletedOwnId;
        private String deletedOwnUserId;
        private boolean deleteSucceeds;
        private List<Document> ratingSummary = List.of();
        private List<Document> userRatingSummary = List.of();
        private String ratingUserId;

        @Override
        public void createComment(String userId, String itemId, String content, int rating, List<String> tags) {
            this.userId = userId;
            this.itemId = itemId;
            this.content = content;
            this.rating = rating;
            this.tags = tags;
        }

        @Override
        public boolean deleteById(String commentId) {
            deletedId = commentId;
            return deleteSucceeds;
        }

        @Override
        public boolean deleteByIdAndUser(String commentId, String userId) {
            deletedOwnId = commentId;
            deletedOwnUserId = userId;
            return deleteSucceeds;
        }

        @Override
        public List<Document> ratingSummary() {
            return ratingSummary;
        }

        @Override
        public List<Document> ratingSummaryByUser(String userId) {
            ratingUserId = userId;
            return userRatingSummary;
        }
    }

    /** 提供行为日志数据的测试替身。 */
    private static class FakeLogDAO extends LogDAO {
        private String userId;
        private String itemId;
        private String actionType;
        private List<Document> topItems = List.of();
        private List<Document> userLogs = List.of();

        @Override
        public void insertActionLog(String userId, String itemId, String actionType, int durationSeconds, Document clientInfo) {
            this.userId = userId;
            this.itemId = itemId;
            this.actionType = actionType;
        }

        @Override
        public List<Document> topItems(int limit) {
            return topItems;
        }

        @Override
        public List<Document> findByUserId(String userId, int limit) {
            return userLogs;
        }
    }

    /** 提供审计汇总数据的测试替身。 */
    private static class FakeSystemLogDAO extends SystemLogDAO {
        private List<Document> auditSummary = List.of();

        @Override
        public List<Document> auditSummary() {
            return auditSummary;
        }
    }

    /** 提供用户数据和权限操作的测试替身。 */
    private static class FakeUserDAO extends UserDAO {
        private Map<String, Object> row;
        private long updatedUserId;
        private int updatedStatus;
        private String updatedRole;
        private long deletedUserId;

        @Override
        public Optional<Map<String, Object>> findById(long userId) {
            return row != null && ((Number) row.get("user_id")).longValue() == userId
                ? Optional.of(row)
                : Optional.empty();
        }

        @Override
        public boolean updateContact(long userId, String email, String phone) {
            updatedUserId = userId;
            return true;
        }

        @Override
        public boolean updateStatus(long userId, int status) {
            updatedStatus = status;
            return true;
        }

        @Override
        public boolean updateRole(long userId, String role) {
            updatedRole = role;
            return true;
        }

        @Override
        public boolean deleteById(long userId) {
            deletedUserId = userId;
            return true;
        }
    }

    /** 提供用户档案数据的测试替身。 */
    private static class FakeProfileDAO extends ProfileDAO {
        private long createdUserId;
        private String createdRealName;

        @Override
        public Optional<Map<String, Object>> findByUserId(long userId) {
            return Optional.empty();
        }

        @Override
        public long create(long userId, String realName, String idCard, String address, String notes) {
            createdUserId = userId;
            createdRealName = realName;
            return 41L;
        }
    }

    /** 提供申请数据、事务调用和报表参数的测试替身。 */
    private static class FakeOrderDAO extends OrderDAO {
        private long userId;
        private long itemId;
        private BigDecimal amount;
        private List<Map<String, Object>> rows = List.of();
        private List<Map<String, Object>> allRows = List.of();
        private long updatedOrderId;
        private int updatedStatus;
        private long updatedUserId;
        private long updatedItemId;
        private BigDecimal updatedAmount;
        private boolean updateOrderSucceeds;
        private long completedOrderId;
        private boolean completeSucceeds;
        private boolean updateSucceeds;
        private long deletedUserId;
        private boolean deleteOwnSucceeds;
        private long deletedOrderId;
        private boolean deleteSucceeds;
        private List<Map<String, Object>> itemOrderCounts = List.of();
        private List<Map<String, Object>> monthlyReport = List.of();
        private int reportYear;
        private int reportMonth;

        @Override
        public long createOrder(long userId, long itemId, BigDecimal amount) {
            this.userId = userId;
            this.itemId = itemId;
            this.amount = amount;
            return 21L;
        }

        @Override
        public List<Map<String, Object>> findByUser(long userId) {
            return rows;
        }

        @Override
        public List<Map<String, Object>> findAll() {
            return allRows;
        }

        @Override
        public List<Map<String, Object>> countByItem() {
            return itemOrderCounts;
        }

        @Override
        public List<Map<String, Object>> monthlyReport(int year, int month) {
            reportYear = year;
            reportMonth = month;
            return monthlyReport;
        }

        @Override
        public boolean updateStatus(long orderId, int status) {
            updatedOrderId = orderId;
            updatedStatus = status;
            return updateSucceeds;
        }

        @Override
        public boolean updatePendingOrder(long userId, long orderId, long itemId, BigDecimal amount) {
            updatedUserId = userId;
            updatedOrderId = orderId;
            updatedItemId = itemId;
            updatedAmount = amount;
            return updateOrderSucceeds;
        }

        @Override
        public boolean completeOrder(long orderId) {
            completedOrderId = orderId;
            return completeSucceeds;
        }

        @Override
        public boolean deleteById(long orderId) {
            deletedOrderId = orderId;
            return deleteSucceeds;
        }

        @Override
        public boolean deletePendingByUser(long userId, long orderId) {
            deletedUserId = userId;
            deletedOrderId = orderId;
            return deleteOwnSucceeds;
        }
    }
}
