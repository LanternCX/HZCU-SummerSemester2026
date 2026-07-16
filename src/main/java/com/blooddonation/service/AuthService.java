package com.blooddonation.service;

import com.blooddonation.dao.mongo.SystemLogDAO;
import com.blooddonation.dao.mysql.UserDAO;
import java.util.Map;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

/**
 * 处理用户注册、登录、密码哈希和认证日志。
 */
public class AuthService {
    private final UserDAO userDAO;
    private final SystemLogDAO systemLogDAO;

    /** 使用默认 DAO 创建认证服务。 */
    public AuthService() {
        this(new UserDAO(), new SystemLogDAO());
    }

    /** 使用指定 DAO 创建可测试的认证服务。 */
    AuthService(UserDAO userDAO, SystemLogDAO systemLogDAO) {
        this.userDAO = userDAO;
        this.systemLogDAO = systemLogDAO;
    }

    /**
     * 校验账号状态和密码，并记录登录结果。
     *
     * @param username 用户名
     * @param password 明文密码
     * @param ip 客户端地址
     * @return 登录结果
     */
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

    /**
     * 校验注册信息并创建普通用户账号。
     *
     * @return 注册结果
     */
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

    /**
     * 使用 BCrypt 生成不可逆密码哈希。
     *
     * @param password 明文密码
     * @return BCrypt 哈希
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /** @return 账号状态和密码校验后的登录结果 */
    private LoginResult authenticate(Map<String, Object> user, String password, String ip) {
        if (((Number) user.get("status")).intValue() != 1) {
            logFailure(String.valueOf(user.get("user_id")), ip, "账号已停用");
            return LoginResult.fail("账号已停用");
        }
        if (!BCrypt.checkpw(password, String.valueOf(user.get("password_hash")))) {
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

    /** 记录登录失败及原因。 */
    private void logFailure(String userId, String ip, String message) {
        systemLogDAO.insertLog(
            userId == null || userId.isBlank() ? "UNKNOWN" : userId,
            "LOGIN",
            "WARN",
            "登录失败",
            new Document("ip", ip).append("operation", "LOGIN").append("reason", message)
        );
    }

    /**
     * 表示已认证用户的会话身份。
     *
     * @param userId 用户编号
     * @param username 用户名
     * @param role 用户角色
     */
    public record UserSession(long userId, String username, String role) {
    }

    /**
     * 表示登录是否成功及对应会话。
     */
    public record LoginResult(boolean success, String message, UserSession session) {
        /** @return 成功登录结果 */
        static LoginResult ok(UserSession session) {
            return new LoginResult(true, "登录成功", session);
        }

        /** @return 失败登录结果 */
        static LoginResult fail(String message) {
            return new LoginResult(false, message, null);
        }
    }

    /**
     * 表示注册是否成功及新用户编号。
     */
    public record RegisterResult(boolean success, String message, long userId) {
        /** @return 成功注册结果 */
        static RegisterResult ok(long userId) {
            return new RegisterResult(true, "注册成功", userId);
        }

        /** @return 失败注册结果 */
        static RegisterResult fail(String message) {
            return new RegisterResult(false, message, 0L);
        }
    }
}
