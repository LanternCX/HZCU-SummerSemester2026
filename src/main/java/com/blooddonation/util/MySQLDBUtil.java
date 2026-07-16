package com.blooddonation.util;

import com.blooddonation.config.DBConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 管理 MySQL 连接池并提供数据库连接。
 */
public final class MySQLDBUtil {
    private static final HikariDataSource DATA_SOURCE = createDataSource();

    /** 禁止实例化 MySQL 工具类。 */
    private MySQLDBUtil() {
    }

    /**
     * 从连接池获取一个 MySQL 连接。
     *
     * @return 可用数据库连接
     * @throws SQLException 获取连接失败
     */
    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

    /**
     * 关闭 MySQL 连接池。
     */
    public static void close() {
        DATA_SOURCE.close();
    }

    /** @return 根据项目配置创建的连接池 */
    private static HikariDataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DBConfig.get("mysql.url"));
        config.setUsername(DBConfig.get("mysql.username"));
        config.setPassword(DBConfig.get("mysql.password"));
        config.setDriverClassName(DBConfig.get("mysql.driver"));
        return new HikariDataSource(config);
    }
}
