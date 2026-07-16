package com.blooddonation.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.blooddonation.dao.mongo.SystemLogDAO;
import com.blooddonation.util.MongoDBUtil;
import com.mongodb.client.model.Filters;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@Tag("stress")
@EnabledIfSystemProperty(named = "stressTests", matches = "true")
class SystemLogDAOStressTest {
    private static final int LOG_COUNT = 10_000;
    private static final int CONCURRENCY = 50;

    @Test
    void writesTenThousandLogsWithFiftyConcurrentWorkers() throws Exception {
        String runId = UUID.randomUUID().toString();
        SystemLogDAO logs = new SystemLogDAO();
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> tasks = new ArrayList<>(CONCURRENCY);
        long startedAt = System.nanoTime();

        try {
            for (int worker = 0; worker < CONCURRENCY; worker++) {
                int workerId = worker;
                tasks.add(executor.submit(() -> {
                    start.await();
                    for (int i = workerId; i < LOG_COUNT; i += CONCURRENCY) {
                        logs.insertLog(
                            "stress-user-" + workerId,
                            "STRESS_TEST",
                            "INFO",
                            "并发日志 " + i,
                            new Document("run_id", runId).append("sequence", i)
                        );
                    }
                    return null;
                }));
            }

            start.countDown();
            for (Future<?> task : tasks) {
                task.get(2, TimeUnit.MINUTES);
            }

            Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);
            long inserted = MongoDBUtil.getCollection("system_logs")
                .countDocuments(Filters.eq("action_detail.run_id", runId));

            assertEquals(LOG_COUNT, inserted);
            System.out.printf(
                "压力测试完成：%,d 条日志，%d 并发，耗时 %.3f 秒，吞吐量 %.2f 条/秒%n",
                inserted,
                CONCURRENCY,
                elapsed.toMillis() / 1_000.0,
                inserted / Math.max(elapsed.toMillis() / 1_000.0, 0.001)
            );
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
            MongoDBUtil.getCollection("system_logs")
                .deleteMany(Filters.eq("action_detail.run_id", runId));
        }
    }
}
