package com.example.dfs.storage;

import com.example.dfs.raft.RaftNode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/files")
public class StorageController {
    private final StorageService storageService;
    private final RaftNode raft;

    public StorageController(StorageService storageService, RaftNode raft) {
        this.storageService = storageService;
        this.raft = raft;
    }

    @PostMapping(path = "/{fileId}/chunks/{chunkId}", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<String> uploadChunk(@PathVariable String fileId, @PathVariable String chunkId, @RequestBody byte[] body) {
        if (raft.getRole() != RaftNode.Role.LEADER) {
            return ResponseEntity.status(307).body("Not leader; forward to leader (prototype)");
        }
        try {
            boolean ok = storageService.storeChunkAndReplicate(fileId, chunkId, body);
            if (ok) return ResponseEntity.ok("OK");
            else return ResponseEntity.status(500).body("Failed to append metadata");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    // internal endpoint used by peers for raw replication
    @PostMapping(path = "/internal/replicate/{fileId}/chunks/{chunkId}", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<String> replicateChunk(@PathVariable String fileId, @PathVariable String chunkId, @RequestBody byte[] body) {
        try {
            storageService.storeChunkAndReplicate(fileId, chunkId, body);
            return ResponseEntity.ok("replicated");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("replication failed");
        }
    }

    @GetMapping("/{fileId}/chunks/{chunkId}")
    public ResponseEntity<byte[]> getChunk(@PathVariable String fileId, @PathVariable String chunkId) {
        try {
            if (!storageService.hasChunk(fileId, chunkId)) return ResponseEntity.notFound().build();
            byte[] data = storageService.readChunk(fileId, chunkId);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
