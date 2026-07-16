package com.blooddonation.util;

import com.blooddonation.config.DBConfig;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * 管理 MongoDB 客户端并提供数据库与集合访问入口。
 */
public final class MongoDBUtil {
    private static final MongoClient CLIENT = MongoClients.create(DBConfig.get("mongodb.uri"));

    /** 禁止实例化 MongoDB 工具类。 */
    private MongoDBUtil() {
    }

    /**
     * 获取配置指定的 MongoDB 数据库。
     *
     * @return MongoDB 数据库
     */
    public static MongoDatabase getDatabase() {
        return CLIENT.getDatabase(DBConfig.get("mongodb.database"));
    }

    /**
     * 获取指定名称的文档集合。
     *
     * @param collectionName 集合名称
     * @return 文档集合
     */
    public static MongoCollection<Document> getCollection(String collectionName) {
        return getDatabase().getCollection(collectionName);
    }

    /**
     * 关闭 MongoDB 客户端。
     */
    public static void close() {
        CLIENT.close();
    }
}
