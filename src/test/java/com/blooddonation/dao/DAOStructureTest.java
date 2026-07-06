package com.blooddonation.dao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DAOStructureTest {
    @Test
    void reqDaoClassesExistInExpectedPackages() {
        String[] classNames = {
            "com.blooddonation.dao.BaseDAO",
            "com.blooddonation.dao.MongoBaseDAO",
            "com.blooddonation.dao.mysql.UserDAO",
            "com.blooddonation.dao.mysql.CategoryDAO",
            "com.blooddonation.dao.mysql.ItemDAO",
            "com.blooddonation.dao.mysql.OrderDAO",
            "com.blooddonation.dao.mysql.ProfileDAO",
            "com.blooddonation.dao.mongo.LogDAO",
            "com.blooddonation.dao.mongo.CommentDAO",
            "com.blooddonation.dao.mongo.DetailDAO",
            "com.blooddonation.dao.mongo.SystemLogDAO"
        };

        for (String className : classNames) {
            assertDoesNotThrow(() -> Class.forName(className), className);
        }
    }

    @Test
    void mysqlDaoClassesExtendBaseDao() throws Exception {
        Class<?> baseDao = Class.forName("com.blooddonation.dao.BaseDAO");
        String[] classNames = {
            "com.blooddonation.dao.mysql.UserDAO",
            "com.blooddonation.dao.mysql.CategoryDAO",
            "com.blooddonation.dao.mysql.ItemDAO",
            "com.blooddonation.dao.mysql.OrderDAO",
            "com.blooddonation.dao.mysql.ProfileDAO"
        };

        for (String className : classNames) {
            assertTrue(baseDao.isAssignableFrom(Class.forName(className)), className);
        }
    }

    @Test
    void mongoDaoClassesExtendMongoBaseDao() throws Exception {
        Class<?> baseDao = Class.forName("com.blooddonation.dao.MongoBaseDAO");
        String[] classNames = {
            "com.blooddonation.dao.mongo.LogDAO",
            "com.blooddonation.dao.mongo.CommentDAO",
            "com.blooddonation.dao.mongo.DetailDAO",
            "com.blooddonation.dao.mongo.SystemLogDAO"
        };

        for (String className : classNames) {
            assertTrue(baseDao.isAssignableFrom(Class.forName(className)), className);
        }
    }

    @Test
    void orderDaoExposesExplicitTransactionalCreateOrderMethod() throws Exception {
        Class<?> orderDao = Class.forName("com.blooddonation.dao.mysql.OrderDAO");
        Method method = orderDao.getDeclaredMethod("createOrder", long.class, long.class, BigDecimal.class);

        assertEquals(long.class, method.getReturnType());
        assertTrue(Modifier.isPublic(method.getModifiers()));
    }
}
