package com.example.dfs.ft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FailureDetector {
    private final Map<String, Long> lastHeartbeat = new ConcurrentHashMap<>();
    private final long timeoutMs;
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private static final Logger logger = LoggerFactory.getLogger(FailureDetector.class);

    public FailureDetector(long timeoutMs) {
        this.timeoutMs = timeoutMs;
        scheduler.initialize();
        scheduler.scheduleAtFixedRate(this::check, 2000);
    }

    public void heartbeat(String nodeId) {
        lastHeartbeat.put(nodeId, Instant.now().toEpochMilli());
    }

    private void check() {
        long now = Instant.now().toEpochMilli();
        lastHeartbeat.forEach((node, ts) -> {
            if (now - ts > timeoutMs) {
                logger.warn("Node {} suspected failed (last hb {} ms ago)", node, (now - ts));
            }
        });
    }

    public void stop() {
        scheduler.shutdown();
    }
}
