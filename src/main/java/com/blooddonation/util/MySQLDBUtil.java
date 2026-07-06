package com.blooddonation.util;

import com.blooddonation.config.DBConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class MySQLDBUtil {
    private static final HikariDataSource DATA_SOURCE = createDataSource();

    private MySQLDBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

    public static void close() {
        DATA_SOURCE.close();
    }

    private static HikariDataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DBConfig.get("mysql.url"));
        config.setUsername(DBConfig.get("mysql.username"));
        config.setPassword(DBConfig.get("mysql.password"));
        config.setDriverClassName(DBConfig.get("mysql.driver"));
        return new HikariDataSource(config);
    }
}
