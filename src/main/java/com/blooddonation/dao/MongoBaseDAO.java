package com.blooddonation.dao;

import com.blooddonation.util.MongoDBUtil;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public abstract class MongoBaseDAO {
    private final String collectionName;

    protected MongoBaseDAO(String collectionName) {
        this.collectionName = collectionName;
    }

    protected MongoCollection<Document> collection() {
        return MongoDBUtil.getCollection(collectionName);
    }
}
