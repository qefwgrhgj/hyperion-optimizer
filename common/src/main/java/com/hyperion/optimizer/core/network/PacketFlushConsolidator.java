package com.hyperion.optimizer.core.network;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PacketFlushConsolidator {
    private final boolean enabled;
    // Fix P0-3: Per-channel isolated pending flush counters instead of global state
    private final ConcurrentHashMap<Object, AtomicInteger> channelPendingFlushes = new ConcurrentHashMap<>();
    private final AtomicInteger defaultPendingFlushes = new AtomicInteger(0);

    public static final int MAX_PENDING_SAFETY_CEILING = 10000;

    public PacketFlushConsolidator(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean shouldConsolidateFlush(int currentPendingCount, int maxBatchSize) {
        if (!enabled) return false;
        // Bounded queue safety: if pending count exceeds safety ceiling, force flush immediately
        if (currentPendingCount >= MAX_PENDING_SAFETY_CEILING) return false;
        return currentPendingCount < maxBatchSize;
    }

    public boolean shouldConsolidateFlush(Object channelKey, int maxBatchSize) {
        if (!enabled) return false;
        int pending = getPending(channelKey);
        if (pending >= MAX_PENDING_SAFETY_CEILING) return false;
        return pending < maxBatchSize;
    }

    public int incrementPending(Object channelKey) {
        return channelPendingFlushes.computeIfAbsent(channelKey, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public void resetPending(Object channelKey) {
        AtomicInteger counter = channelPendingFlushes.get(channelKey);
        if (counter != null) {
            counter.set(0);
        }
    }

    public int getPending(Object channelKey) {
        AtomicInteger counter = channelPendingFlushes.get(channelKey);
        return counter != null ? counter.get() : 0;
    }

    public void removeChannel(Object channelKey) {
        channelPendingFlushes.remove(channelKey);
    }

    public void incrementPending() {
        defaultPendingFlushes.incrementAndGet();
    }

    public void resetPending() {
        defaultPendingFlushes.set(0);
    }

    public boolean isEnabled() {
        return enabled;
    }

    // Fix P3-1: Clear all channel pending flush state on disconnect
    public void clear() {
        channelPendingFlushes.clear();
        defaultPendingFlushes.set(0);
    }
}
