package com.example.dfs.storage;

public class MetadataLogEntry {
    private final String command;
    public MetadataLogEntry(String command) { this.command = command; }
    public String getCommand() { return command; }
}
