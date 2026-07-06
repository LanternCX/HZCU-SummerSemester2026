package com.blooddonation.config;

import com.blooddonation.exception.DBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DBConfig {
    private static final Properties PROPERTIES = load();

    private DBConfig() {
    }

    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }

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
