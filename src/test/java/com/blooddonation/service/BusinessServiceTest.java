package com.blooddonation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.blooddonation.dao.mongo.DetailDAO;
import com.blooddonation.dao.mongo.CommentDAO;
import com.blooddonation.dao.mysql.CategoryDAO;
import com.blooddonation.dao.mysql.ItemDAO;
import com.blooddonation.dao.mysql.OrderDAO;
import com.blooddonation.dao.mysql.UserDAO;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bson.Document;
import org.junit.jupiter.api.Test;

class BusinessServiceTest {
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

    @Test
    void updateCategoryRejectsSelfParent() {
        BusinessService.BusinessResult result = new BusinessService(new FakeItemDAO(), new FakeCategoryDAO(), new FakeDetailDAO(), new FakeOrderDAO())
            .updateCategory(3L, "A型", 3L);

        assertFalse(result.success());
        assertEquals("父分类不能选择自己", result.message());
    }

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

    @Test
    void deleteItemDeletesMysqlRowAndMongoDetail() {
        FakeItemDAO items = new FakeItemDAO();
        FakeDetailDAO details = new FakeDetailDAO();

        BusinessService.BusinessResult result = new BusinessService(items, details, new FakeOrderDAO()).deleteItem(3L);

        assertTrue(result.success());
        assertEquals(3L, items.deletedItemId);
        assertEquals("3", details.deletedItemId);
    }

    @Test
    void saveItemDetailRejectsMissingItem() {
        BusinessService.BusinessResult result = new BusinessService(new FakeItemDAO(), new FakeDetailDAO(), new FakeOrderDAO())
            .saveItemDetail(99L, "详情", List.of(), new Document());

        assertFalse(result.success());
        assertEquals("业务数据不存在", result.message());
    }

    @Test
    void findItemDetailReturnsMongoDetail() {
        FakeDetailDAO details = new FakeDetailDAO();
        details.found = new Document("item_id", "3").append("description", "详情");

        Optional<Document> detail = new BusinessService(new FakeItemDAO(), details, new FakeOrderDAO()).findItemDetail(3L);

        assertTrue(detail.isPresent());
        assertEquals("详情", detail.get().getString("description"));
    }

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

    @Test
    void createOrderChecksItemStatusAndAmountBeforeWriting() {
        FakeItemDAO items = new FakeItemDAO();
        items.item = item(3L, new BigDecimal("10.00"), 1);
        FakeOrderDAO orders = new FakeOrderDAO();

        BusinessService.BusinessResult result = new BusinessService(items, new FakeDetailDAO(), orders)
            .createOrder(2L, 3L, new BigDecimal("8.00"));

        assertTrue(result.success());
        assertEquals(21L, result.id());
        assertEquals(2L, orders.userId);
        assertEquals(3L, orders.itemId);
        assertEquals(new BigDecimal("8.00"), orders.amount);
    }

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

    @Test
    void createOrderRejectsAmountGreaterThanInventory() {
        FakeItemDAO items = new FakeItemDAO();
        items.item = item(3L, new BigDecimal("5.00"), 1);

        BusinessService.BusinessResult result = new BusinessService(items, new FakeDetailDAO(), new FakeOrderDAO())
            .createOrder(2L, 3L, new BigDecimal("8.00"));

        assertFalse(result.success());
        assertEquals("数量不足", result.message());
    }

    @Test
    void findOrdersByUserReturnsUserRecords() {
        FakeOrderDAO orders = new FakeOrderDAO();
        orders.rows = List.of(Map.of("order_id", 21L));

        List<Map<String, Object>> result = new BusinessService(new FakeItemDAO(), new FakeDetailDAO(), orders)
            .findOrdersByUser(2L);

        assertEquals(1, result.size());
        assertEquals(21L, result.get(0).get("order_id"));
    }

    @Test
    void adminFindsAllOrders() {
        FakeOrderDAO orders = new FakeOrderDAO();
        orders.allRows = List.of(Map.of("order_id", 21L), Map.of("order_id", 22L));

        List<Map<String, Object>> result = new BusinessService(new FakeItemDAO(), new FakeDetailDAO(), orders)
            .findOrders(1L, true);

        assertEquals(2, result.size());
    }

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

    @Test
    void userOrderUpdateRejectsUnavailableInventory() {
        FakeItemDAO items = new FakeItemDAO();
        items.item = item(3L, new BigDecimal("5.00"), 1);

        BusinessService.BusinessResult result = new BusinessService(items, new FakeDetailDAO(), new FakeOrderDAO())
            .updateOwnOrder(2L, 21L, 3L, new BigDecimal("8.00"));

        assertFalse(result.success());
        assertEquals("数量不足", result.message());
    }

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

    @Test
    void deleteOrderDeletesRecord() {
        FakeOrderDAO orders = new FakeOrderDAO();
        orders.deleteSucceeds = true;

        BusinessService.BusinessResult result = new BusinessService(new FakeItemDAO(), new FakeDetailDAO(), orders).deleteOrder(21L);

        assertTrue(result.success());
        assertEquals(21L, orders.deletedOrderId);
    }

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

    private static Map<String, Object> item(long id, BigDecimal amount, int status) {
        Map<String, Object> row = new HashMap<>();
        row.put("item_id", id);
        row.put("amount", amount);
        row.put("status", status);
        return row;
    }

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
    }

    private static class FakeUserDAO extends UserDAO {
        private Map<String, Object> row;

        @Override
        public Optional<Map<String, Object>> findById(long userId) {
            return row != null && ((Number) row.get("user_id")).longValue() == userId
                ? Optional.of(row)
                : Optional.empty();
        }
    }

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
