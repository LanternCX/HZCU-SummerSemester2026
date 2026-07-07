package com.blooddonation.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.junit.jupiter.api.Test;

class DashboardFrameTest {
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

    private Object value(Object row, String name) throws Exception {
        Method method = row.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        return method.invoke(row);
    }
}
