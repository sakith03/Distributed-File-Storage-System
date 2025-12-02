package com.example.dfs.time;

public class HybridLogicalClock {
    private long lastPhysical = 0;
    private long counter = 0;

    public static record HLCStamp(long physical, long counter) {}

    public synchronized HLCStamp now() {               //This generates a current timestamp for an event
        long phys = System.currentTimeMillis();
        if (phys > lastPhysical) {
            lastPhysical = phys;
            counter = 0;
        } else {
            counter++;
        }
        return new HLCStamp(lastPhysical, counter);
    }

    public synchronized HLCStamp update(HLCStamp remote) {          //Merges a remote timestamp to maintain causal order
        long phys = System.currentTimeMillis();
        long maxPhys = Math.max(phys, Math.max(lastPhysical, remote.physical()));
        if (maxPhys == lastPhysical && maxPhys == remote.physical()) {
            counter = Math.max(counter, remote.counter()) + 1;
        } else if (maxPhys == lastPhysical) {
            counter++;
        } else if (maxPhys == remote.physical()) {
            counter = remote.counter() + 1;
        } else {
            counter = 0;
        }
        lastPhysical = maxPhys;
        return new HLCStamp(lastPhysical, counter);
    }
}
