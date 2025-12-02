package com.example.dfs.model;

import java.util.List;

public class AppendEntriesRequest {
    public long term;
    public String leaderId;
    public long prevLogIndex;
    public long prevLogTerm;
    public List<LogEntry> entries;
    public long leaderCommit;
}
