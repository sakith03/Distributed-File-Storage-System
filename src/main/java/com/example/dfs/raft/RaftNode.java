package com.example.dfs.raft;

import com.example.dfs.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import jakarta.annotation.PostConstruct;


@Component
public class RaftNode {
    private static final Logger logger = LoggerFactory.getLogger(RaftNode.class);

    private final String nodeId;
    private final List<String> peers;
    private final RestTemplate rest = new RestTemplate();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private volatile Role role = Role.FOLLOWER;
    private final AtomicLong currentTerm = new AtomicLong(0);
    private volatile String votedFor = null;
    private final List<LogEntry> log = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong commitIndex = new AtomicLong(0);
    private final Random rand = new Random();

    private ScheduledFuture<?> electionTimeoutTask;
    private ScheduledFuture<?> heartbeatTask;

    public RaftNode(Environment env) {
        this.nodeId = env.getProperty("node.id", "node1");
        String peersCSV = env.getProperty("node.peers", "");
        if (peersCSV.trim().isEmpty()) this.peers = new ArrayList<>();
        else this.peers = Arrays.asList(peersCSV.split(","));
        logger.info("RaftNode {} created with peers {}", nodeId, peers);
    }

    @PostConstruct
    public void start() {
        resetElectionTimeout();
    }

    private void resetElectionTimeout() {
        if (electionTimeoutTask != null) electionTimeoutTask.cancel(true);
        int timeout = 300 + rand.nextInt(400); // 300-700ms
        electionTimeoutTask = scheduler.schedule(this::onElectionTimeout, timeout, TimeUnit.MILLISECONDS);
    }

    private synchronized void onElectionTimeout() {
        logger.info("{} election timeout; starting election", nodeId);
        startElection();
    }

    private void startElection() {
        long term = currentTerm.incrementAndGet();
        votedFor = nodeId;
        int votes = 1;
        RequestVoteRequest req = new RequestVoteRequest();
        req.term = term;
        req.candidateId = nodeId;
        req.lastLogIndex = log.size() - 1;
        req.lastLogTerm = (log.isEmpty() ? 0 : log.get(log.size()-1).getTerm());

        CountDownLatch latch = new CountDownLatch(peers.size());
        AtomicLong voteCount = new AtomicLong(votes);

        for (String p : peers) {
            scheduler.submit(() -> {
                try {
                    String url = p + "/raft/requestVote";
                    RequestVoteResponse resp = rest.postForObject(url, req, RequestVoteResponse.class);
                    if (resp != null && resp.voteGranted) voteCount.incrementAndGet();
                    if (resp != null && resp.term > currentTerm.get()) currentTerm.set(resp.term);
                } catch (Exception e) {
                    logger.debug("RequestVote to {} failed: {}", p, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        try { latch.await(500, TimeUnit.MILLISECONDS); } catch (InterruptedException ignored) {}
        long vc = voteCount.get();
        if (vc > (peers.size() + 1) / 2) becomeLeader();
        else resetElectionTimeout();
    }

    private void becomeLeader() {
        role = Role.LEADER;
        logger.info("{} became LEADER for term {}", nodeId, currentTerm.get());
        startHeartbeat();
    }

    private void startHeartbeat() {
        if (heartbeatTask != null) heartbeatTask.cancel(true);
        heartbeatTask = scheduler.scheduleAtFixedRate(this::sendHeartbeats, 0, 150, TimeUnit.MILLISECONDS);
    }

    private void sendHeartbeats() {
        AppendEntriesRequest req = new AppendEntriesRequest();
        req.term = currentTerm.get();
        req.leaderId = nodeId;
        req.prevLogIndex = log.size() - 1;
        req.prevLogTerm = (log.isEmpty() ? 0 : log.get(log.size()-1).getTerm());
        req.entries = Collections.emptyList();
        req.leaderCommit = commitIndex.get();
        for (String p : peers) {
            scheduler.submit(() -> {
                try {
                    rest.postForObject(p + "/raft/appendEntries", req, AppendEntriesResponse.class);
                } catch (Exception e) {
                    logger.debug("heartbeat to {} failed: {}", p, e.getMessage());
                }
            });
        }
    }

    public synchronized AppendEntriesResponse handleAppendEntries(AppendEntriesRequest req) {
        AppendEntriesResponse resp = new AppendEntriesResponse();
        if (req.term < currentTerm.get()) {
            resp.term = currentTerm.get();
            resp.success = false;
            return resp;
        }
        currentTerm.set(req.term);
        role = Role.FOLLOWER;
        resetElectionTimeout();
        resp.term = currentTerm.get();
        resp.success = true;
        return resp;
    }

    public synchronized RequestVoteResponse handleRequestVote(RequestVoteRequest req) {
        RequestVoteResponse resp = new RequestVoteResponse();
        if (req.term < currentTerm.get()) {
            resp.term = currentTerm.get();
            resp.voteGranted = false;
            return resp;
        }
        // Reset votedFor when moving to a new term
        if (req.term > currentTerm.get()) {
            currentTerm.set(req.term);
            votedFor = null;
            role = Role.FOLLOWER;
        }
        if (votedFor == null || votedFor.equals(req.candidateId)) {
            votedFor = req.candidateId;
            resp.voteGranted = true;
            resp.term = currentTerm.get();
            resetElectionTimeout();
            return resp;
        }
        resp.voteGranted = false;
        resp.term = currentTerm.get();
        return resp;
    }

    public synchronized boolean appendCommand(String command) {
        if (role != Role.LEADER) return false;
        LogEntry entry = new LogEntry(currentTerm.get(), command);
        log.add(entry);
        commitIndex.incrementAndGet(); // naive
        return true;
    }

    public Role getRole() { return role; }
    public String getNodeId() { return nodeId; }

    public enum Role { LEADER, FOLLOWER, CANDIDATE }
}
