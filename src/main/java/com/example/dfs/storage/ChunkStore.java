package com.example.dfs.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ChunkStore {
    private final Path baseDir;

    public ChunkStore(String baseDir) {
        this.baseDir = Path.of(baseDir);
        try { Files.createDirectories(this.baseDir); }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    public void saveChunk(String fileId, String chunkId, byte[] data) throws IOException {
        Path fdir = baseDir.resolve(fileId);
        Files.createDirectories(fdir);
        Path chunkFile = fdir.resolve(chunkId);
        Files.write(chunkFile, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public byte[] readChunk(String fileId, String chunkId) throws IOException {
        Path chunkFile = baseDir.resolve(fileId).resolve(chunkId);
        return Files.readAllBytes(chunkFile);
    }

    public boolean chunkExists(String fileId, String chunkId) {
        return Files.exists(baseDir.resolve(fileId).resolve(chunkId));
    }
}
