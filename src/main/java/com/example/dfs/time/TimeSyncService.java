package com.example.dfs.time;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TimeSyncService {
    @Scheduled(fixedRate = 15000)
    public void sync() {
        System.out.println("TimeSyncService: simulated NTP sync at " + System.currentTimeMillis());
    }
}
