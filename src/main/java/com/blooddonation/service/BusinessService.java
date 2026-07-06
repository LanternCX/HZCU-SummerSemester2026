package com.blooddonation.service;

import com.blooddonation.dao.mongo.DetailDAO;
import com.blooddonation.dao.mysql.CategoryDAO;
import com.blooddonation.dao.mysql.ItemDAO;
import com.blooddonation.dao.mysql.OrderDAO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bson.Document;

public class BusinessService {
    private final ItemDAO itemDAO;
    private final CategoryDAO categoryDAO;
    private final DetailDAO detailDAO;
    private final OrderDAO orderDAO;

    public BusinessService() {
        this(new ItemDAO(), new CategoryDAO(), new DetailDAO(), new OrderDAO());
    }

    BusinessService(ItemDAO itemDAO, DetailDAO detailDAO, OrderDAO orderDAO) {
        this(itemDAO, new CategoryDAO(), detailDAO, orderDAO);
    }

    BusinessService(ItemDAO itemDAO, CategoryDAO categoryDAO, DetailDAO detailDAO, OrderDAO orderDAO) {
        this.itemDAO = itemDAO;
        this.categoryDAO = categoryDAO;
        this.detailDAO = detailDAO;
        this.orderDAO = orderDAO;
    }

    public List<Map<String, Object>> findCategories() {
        return categoryDAO.findAll();
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

        return orderDAO.updatePendingOrder(userId, orderId, itemId, amount)
            ? BusinessResult.ok(orderId, "保存成功")
            : BusinessResult.fail("记录不存在或不可编辑");
    }

    public BusinessResult updateOrderStatus(long orderId, int status) {
        if (orderId <= 0 || status < 0 || status > 2) {
            return BusinessResult.fail("请选择有效记录状态");
        }
        boolean updated = status == 1 ? orderDAO.completeOrder(orderId) : orderDAO.updateStatus(orderId, status);
        return updated
            ? BusinessResult.ok(orderId, "状态更新成功")
            : BusinessResult.fail("记录不存在");
    }

    public BusinessResult deleteOrder(long orderId) {
        if (orderId <= 0) {
            return BusinessResult.fail("请选择有效记录");
        }
        return orderDAO.deleteById(orderId)
            ? BusinessResult.ok(orderId, "删除成功")
            : BusinessResult.fail("记录不存在");
    }

    public BusinessResult deleteOwnOrder(long userId, long orderId) {
        if (userId <= 0 || orderId <= 0) {
            return BusinessResult.fail("请选择有效用户和记录");
        }
        return orderDAO.deletePendingByUser(userId, orderId)
            ? BusinessResult.ok(orderId, "删除成功")
            : BusinessResult.fail("记录不存在或不可删除");
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private Document safe(Document metadata) {
        return metadata == null ? new Document() : metadata;
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
