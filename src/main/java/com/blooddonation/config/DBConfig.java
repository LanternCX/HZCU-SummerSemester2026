package com.blooddonation.config;

import com.blooddonation.exception.DBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 读取并提供数据库连接配置。
 */
public final class DBConfig {
    private static final Properties PROPERTIES = load();

    /** 禁止实例化配置工具类。 */
    private DBConfig() {
    }

    /**
     * 获取指定配置项。
     *
     * @param key 配置键
     * @return 对应的配置值，不存在时返回 {@code null}
     */
    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }

    /** @return 从类路径加载的数据库配置 */
    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream input = DBConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new DBException("db.properties not found", null);
            }
            properties.load(input);
            return properties;
        } catch (IOException e) {
            throw new DBException("Failed to load db.properties", e);
        }
    }
}
