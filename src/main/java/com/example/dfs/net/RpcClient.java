package com.example.dfs.net;

import org.springframework.web.client.RestTemplate;

public class RpcClient {
    private final RestTemplate rest = new RestTemplate();

    public <T> T post(String url, Object body, Class<T> respType) {
        return rest.postForObject(url, body, respType);
    }
}
