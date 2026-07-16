package com.blooddonation.dao;

import com.blooddonation.util.MongoDBUtil;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

/**
 * 保存 MongoDB 集合名称并提供统一的集合访问入口。
 */
public abstract class MongoBaseDAO {
    private final String collectionName;

    /**
     * 创建绑定到指定集合的 DAO。
     *
     * @param collectionName 集合名称
     */
    protected MongoBaseDAO(String collectionName) {
        this.collectionName = collectionName;
    }

    /**
     * 获取当前 DAO 对应的 MongoDB 集合。
     *
     * @return 文档集合
     */
    protected MongoCollection<Document> collection() {
        return MongoDBUtil.getCollection(collectionName);
    }
}
