package com.example.dfs.model;

import java.io.Serializable;

public class LogEntry implements Serializable {
    private long term;
    private String command;

    public LogEntry() {}
    public LogEntry(long term, String command) { this.term = term; this.command = command; }
    public long getTerm() { return term; }
    public void setTerm(long term) { this.term = term; }
    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
}
