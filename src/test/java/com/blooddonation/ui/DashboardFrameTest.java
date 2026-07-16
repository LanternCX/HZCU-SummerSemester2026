package com.blooddonation.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.junit.jupiter.api.Test;

/** 验证主界面统计数据转换和日期显示。 */
class DashboardFrameTest {
    /** 验证热度统计忽略已删除库存。 */
    @Test
    void statisticsRowsIgnoreDeletedItems() throws Exception {
        Method method = DashboardFrame.class.getDeclaredMethod("actionChartRows", Map.class, List.class);
        method.setAccessible(true);

        List<?> rows = (List<?>) method.invoke(null, Map.of(1L, "A型全血"), List.of(
            new Document("item_id", "1").append("action_count", 8),
            new Document("item_id", "99").append("action_count", 5)
        ));

        assertEquals(1, rows.size());
        assertEquals("A型全血", value(rows.get(0), "label"));
        assertEquals(8.0, value(rows.get(0), "value"));
    }

    /** 验证 MySQL 日期时间可以转换为年月文本。 */
    @Test
    void monthTextSupportsMysqlDateTimeValues() {
        assertEquals("2026-07", DashboardFrame.monthText(LocalDateTime.of(2026, 7, 19, 9, 30)));
    }

    /** @return 通过反射读取的记录组件值 */
    private Object value(Object row, String name) throws Exception {
        Method method = row.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        return method.invoke(row);
    }
}
