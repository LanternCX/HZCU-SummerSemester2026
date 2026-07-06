package com.blooddonation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.blooddonation.dao.mongo.SystemLogDAO;
import com.blooddonation.dao.mysql.UserDAO;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.bson.Document;
import org.junit.jupiter.api.Test;

class AuthServiceTest {
    @Test
    void loginAcceptsActiveUserAndWritesLog() {
        FakeUserDAO users = new FakeUserDAO();
        users.user = user(1L, "admin", AuthService.hashPassword("admin123"), "ADMIN", 1);
        FakeSystemLogDAO logs = new FakeSystemLogDAO();

        AuthService.LoginResult result = new AuthService(users, logs).login("admin", "admin123", "127.0.0.1");

        assertTrue(result.success());
        assertEquals("admin", result.session().username());
        assertEquals("LOGIN", logs.logType);
    }

    @Test
    void loginRejectsDisabledUser() {
        FakeUserDAO users = new FakeUserDAO();
        users.user = user(8L, "auditor02", AuthService.hashPassword("admin123"), "USER", 0);

        AuthService.LoginResult result = new AuthService(users, new FakeSystemLogDAO())
            .login("auditor02", "admin123", "127.0.0.1");

        assertFalse(result.success());
        assertEquals("账号已停用", result.message());
    }

    @Test
    void loginWritesWarnLogForBadPassword() {
        FakeUserDAO users = new FakeUserDAO();
        users.user = user(1L, "admin", AuthService.hashPassword("admin123"), "ADMIN", 1);
        FakeSystemLogDAO logs = new FakeSystemLogDAO();

        AuthService.LoginResult result = new AuthService(users, logs).login("admin", "bad", "127.0.0.1");

        assertFalse(result.success());
        assertEquals("WARN", logs.logLevel);
        assertEquals("登录失败", logs.message);
    }

    @Test
    void registerCreatesActiveUserWithHashedPassword() {
        FakeUserDAO users = new FakeUserDAO();

        AuthService.RegisterResult result = new AuthService(users, new FakeSystemLogDAO())
            .register("user99", "user123", "user99@example.test", "19900000999");

        assertTrue(result.success());
        assertEquals(99L, result.userId());
        assertEquals("user99", users.createdUsername);
        assertEquals(AuthService.hashPassword("user123"), users.createdPasswordHash);
        assertEquals("USER", users.createdRole);
    }

    @Test
    void registerRejectsDuplicateUsername() {
        FakeUserDAO users = new FakeUserDAO();
        users.user = user(2L, "user01", AuthService.hashPassword("user123"), "USER", 1);

        AuthService.RegisterResult result = new AuthService(users, new FakeSystemLogDAO())
            .register("user01", "user123", "new@example.test", "19900000999");

        assertFalse(result.success());
        assertEquals("用户名已存在", result.message());
    }

    @Test
    void registerRejectsInvalidEmail() {
        AuthService.RegisterResult result = new AuthService(new FakeUserDAO(), new FakeSystemLogDAO())
            .register("user99", "user123", "bad-email", "19900000999");

        assertFalse(result.success());
        assertEquals("邮箱格式不正确", result.message());
    }

    @Test
    void registerRejectsInvalidPhone() {
        AuthService.RegisterResult result = new AuthService(new FakeUserDAO(), new FakeSystemLogDAO())
            .register("user99", "user123", "user99@example.test", "12345");

        assertFalse(result.success());
        assertEquals("手机号格式不正确", result.message());
    }

    private static Map<String, Object> user(long id, String username, String passwordHash, String role, int status) {
        Map<String, Object> row = new HashMap<>();
        row.put("user_id", id);
        row.put("username", username);
        row.put("password_hash", passwordHash);
        row.put("role", role);
        row.put("status", status);
        return row;
    }

    private static class FakeUserDAO extends UserDAO {
        private Map<String, Object> user;
        private String createdUsername;
        private String createdPasswordHash;
        private String createdRole;

        @Override
        public Optional<Map<String, Object>> findByUsername(String username) {
            return user != null && username.equals(user.get("username")) ? Optional.of(user) : Optional.empty();
        }

        @Override
        public long create(String username, String passwordHash, String email, String phone, String role) {
            createdUsername = username;
            createdPasswordHash = passwordHash;
            createdRole = role;
            return 99L;
        }
    }

    private static class FakeSystemLogDAO extends SystemLogDAO {
        private String logType;
        private String logLevel;
        private String message;

        @Override
        public void insertLog(String userId, String logType, String logLevel, String message, Document actionDetail) {
            this.logType = logType;
            this.logLevel = logLevel;
            this.message = message;
        }
    }
}
