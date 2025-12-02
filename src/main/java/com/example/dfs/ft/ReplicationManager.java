package com.example.dfs.ft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import java.util.List;

public class ReplicationManager {
    private final List<String> peers;
    private final RestTemplate rest = new RestTemplate();
    private static final Logger logger = LoggerFactory.getLogger(ReplicationManager.class);

    public ReplicationManager(List<String> peers) {
        this.peers = peers;
    }

    // Prototype: POST chunk bytes to peer's storage endpoint
    public void replicateChunk(String fileId, String chunkId, byte[] data) {
        for (String p : peers) {
            try {
                String url = p + "/files/internal/replicate/" + fileId + "/chunks/" + chunkId;
                rest.postForObject(url, data, String.class);
            } catch (Exception e) {
                logger.warn("Replication to {} failed: {}", p, e.getMessage());
            }
        }
    }
}
