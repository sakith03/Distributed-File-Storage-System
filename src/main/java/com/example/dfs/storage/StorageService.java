package com.example.dfs.storage;

import com.example.dfs.raft.RaftNode;
import com.example.dfs.ft.ReplicationManager;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StorageService {
    private final ChunkStore chunkStore;
    private final ConcurrentHashMap<String, MetadataLogEntry> metadata = new ConcurrentHashMap<>();
    private final RaftNode raft;
    private final ReplicationManager replicationManager;

    public StorageService(Environment env, RaftNode raft, ReplicationManager replicationManager) {
        String dir = env.getProperty("node.data.dir", "./data/node1");
        this.chunkStore = new ChunkStore(dir);
        this.raft = raft;
        this.replicationManager = replicationManager;
    }

    public boolean storeChunkAndReplicate(String fileId, String chunkId, byte[] data) throws IOException {
        chunkStore.saveChunk(fileId, chunkId, data);

        String command = "WRITE:" + fileId + ":" + chunkId + ":" + data.length;
        boolean appended = raft.appendCommand(command);
        if (!appended) {
            return false;
        }

        new Thread(() -> replicationManager.replicateChunk(fileId, chunkId, data)).start();
        metadata.put(fileId + ":" + chunkId, new MetadataLogEntry(command));
        return true;
    }

    public byte[] readChunk(String fileId, String chunkId) throws IOException {
        return chunkStore.readChunk(fileId, chunkId);
    }

    public boolean hasChunk(String fileId, String chunkId) {
        return chunkStore.chunkExists(fileId, chunkId);
    }
}
