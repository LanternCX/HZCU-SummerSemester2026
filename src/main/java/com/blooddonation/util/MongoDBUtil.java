package com.blooddonation.util;

import com.blooddonation.config.DBConfig;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public final class MongoDBUtil {
    private static final MongoClient CLIENT = MongoClients.create(DBConfig.get("mongodb.uri"));

    private MongoDBUtil() {
    }

    public static MongoDatabase getDatabase() {
        return CLIENT.getDatabase(DBConfig.get("mongodb.database"));
    }

    public static MongoCollection<Document> getCollection(String collectionName) {
        return getDatabase().getCollection(collectionName);
    }

    public static void close() {
        CLIENT.close();
    }
}
