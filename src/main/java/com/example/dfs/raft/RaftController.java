package com.example.dfs.raft;

import com.example.dfs.model.AppendEntriesRequest;
import com.example.dfs.model.AppendEntriesResponse;
import com.example.dfs.model.RequestVoteRequest;
import com.example.dfs.model.RequestVoteResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/raft")
public class RaftController {
    private final RaftNode raft;

    public RaftController(RaftNode raft) { this.raft = raft; }

    @PostMapping("/appendEntries")
    public AppendEntriesResponse appendEntries(@RequestBody AppendEntriesRequest req) {
        return raft.handleAppendEntries(req);
    }

    @PostMapping("/requestVote")
    public RequestVoteResponse requestVote(@RequestBody RequestVoteRequest req) {
        return raft.handleRequestVote(req);
    }

    @GetMapping("/role")
    public String role() { return raft.getRole().name(); }
}
