package com.blooddonation.service;

import com.blooddonation.dao.mongo.SystemLogDAO;
import com.blooddonation.dao.mysql.UserDAO;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import org.bson.Document;

public class AuthService {
    private final UserDAO userDAO;
    private final SystemLogDAO systemLogDAO;

    public AuthService() {
        this(new UserDAO(), new SystemLogDAO());
    }

    AuthService(UserDAO userDAO, SystemLogDAO systemLogDAO) {
        this.userDAO = userDAO;
        this.systemLogDAO = systemLogDAO;
    }

    public LoginResult login(String username, String password, String ip) {
        String cleanUsername = username == null ? "" : username.trim();
        if (cleanUsername.isEmpty() || password == null || password.isEmpty()) {
            logFailure(cleanUsername, ip, "请输入用户名和密码");
            return LoginResult.fail("请输入用户名和密码");
        }

        return userDAO.findByUsername(cleanUsername)
            .map(user -> authenticate(user, password, ip))
            .orElseGet(() -> {
                logFailure(cleanUsername, ip, "用户名或密码错误");
                return LoginResult.fail("用户名或密码错误");
            });
    }

    public RegisterResult register(String username, String password, String email, String phone) {
        String cleanUsername = username == null ? "" : username.trim();
        String cleanEmail = email == null ? "" : email.trim();
        String cleanPhone = phone == null ? "" : phone.trim();
        if (cleanUsername.isEmpty() || password == null || password.isEmpty() || cleanEmail.isEmpty()) {
            return RegisterResult.fail("请输入用户名、密码和邮箱");
        }
        if (!cleanEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return RegisterResult.fail("邮箱格式不正确");
        }
        if (!cleanPhone.isEmpty() && !cleanPhone.matches("^\\d{11}$")) {
            return RegisterResult.fail("手机号格式不正确");
        }
        if (userDAO.findByUsername(cleanUsername).isPresent()) {
            return RegisterResult.fail("用户名已存在");
        }

        long userId = userDAO.create(cleanUsername, hashPassword(password), cleanEmail, cleanPhone, "USER");
        return RegisterResult.ok(userId);
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private LoginResult authenticate(Map<String, Object> user, String password, String ip) {
        if (((Number) user.get("status")).intValue() != 1) {
            logFailure(String.valueOf(user.get("user_id")), ip, "账号已停用");
            return LoginResult.fail("账号已停用");
        }
        if (!hashPassword(password).equals(user.get("password_hash"))) {
            logFailure(String.valueOf(user.get("user_id")), ip, "用户名或密码错误");
            return LoginResult.fail("用户名或密码错误");
        }

        UserSession session = new UserSession(
            ((Number) user.get("user_id")).longValue(),
            String.valueOf(user.get("username")),
            String.valueOf(user.get("role"))
        );
        systemLogDAO.insertLog(
            String.valueOf(session.userId()),
            "LOGIN",
            "INFO",
            "登录成功",
            new Document("ip", ip).append("operation", "LOGIN")
        );
        return LoginResult.ok(session);
    }

    private void logFailure(String userId, String ip, String message) {
        systemLogDAO.insertLog(
            userId == null || userId.isBlank() ? "UNKNOWN" : userId,
            "LOGIN",
            "WARN",
            "登录失败",
            new Document("ip", ip).append("operation", "LOGIN").append("reason", message)
        );
    }

    public record UserSession(long userId, String username, String role) {
    }

    public record LoginResult(boolean success, String message, UserSession session) {
        static LoginResult ok(UserSession session) {
            return new LoginResult(true, "登录成功", session);
        }

        static LoginResult fail(String message) {
            return new LoginResult(false, message, null);
        }
    }

    public record RegisterResult(boolean success, String message, long userId) {
        static RegisterResult ok(long userId) {
            return new RegisterResult(true, "注册成功", userId);
        }

        static RegisterResult fail(String message) {
            return new RegisterResult(false, message, 0L);
        }
    }
}
