package com.example.dfs.config;

import com.example.dfs.ft.FailureDetector;
import com.example.dfs.ft.ReplicationManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class BeanConfig {

    @Value("${node.peers:}")
    private String peersCsv;

    @Bean
    public FailureDetector failureDetector() {
        return new FailureDetector(5000); // 5s timeout
    }

    @Bean
    public ReplicationManager replicationManager() {
        List<String> peers = peersCsv.trim().isEmpty() ? List.of() : Arrays.asList(peersCsv.split(","));
        return new ReplicationManager(peers);
    }
}
