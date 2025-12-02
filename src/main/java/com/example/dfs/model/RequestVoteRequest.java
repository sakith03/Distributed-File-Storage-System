package com.example.dfs.model;

public class RequestVoteRequest {
    public long term;
    public String candidateId;
    public long lastLogIndex;
    public long lastLogTerm;
}
