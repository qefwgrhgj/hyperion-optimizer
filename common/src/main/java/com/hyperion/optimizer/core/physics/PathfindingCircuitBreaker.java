package com.hyperion.optimizer.core.physics;

import java.util.concurrent.ConcurrentHashMap;

public class PathfindingCircuitBreaker {
    private final boolean enabled;
    private final int maxFailuresBeforeBackoff;
    private long lastCleanupTick = 0;
    private static final long CLEANUP_INTERVAL_TICKS = 1200L; // Clean every 1 minute
    private static final int MAX_TRACKER_CAPACITY = 8192;

    private static class FailureTracker {
        volatile int failureCount = 0;
        volatile long backoffUntilTick = 0;
        volatile long lastAccessTick = 0;
    }

    private final ConcurrentHashMap<Integer, FailureTracker> entityTrackers = new ConcurrentHashMap<>();

    public PathfindingCircuitBreaker(boolean enabled, int maxFailuresBeforeBackoff) {
        this.enabled = enabled;
        this.maxFailuresBeforeBackoff = maxFailuresBeforeBackoff;
    }

    public boolean canEntitySearchPath(int entityId, long currentTick) {
        return canEntitySearchPath(entityId, false, currentTick);
    }

    // Fix P0-3: Boss, Iron Golem & Pet exemption to prevent malicious AI freeze exploit
    public boolean canEntitySearchPath(int entityId, boolean isExemptOrBoss, long currentTick) {
        if (!enabled || isExemptOrBoss) return true;
        
        // Fix P2-3: Auto-prune with server tick rollback defense
        if (currentTick < lastCleanupTick || (currentTick - lastCleanupTick) > CLEANUP_INTERVAL_TICKS || entityTrackers.size() > MAX_TRACKER_CAPACITY) {
            cleanupStaleEntries(currentTick);
        }

        FailureTracker tracker = entityTrackers.get(entityId);
        if (tracker == null) return true;

        tracker.lastAccessTick = currentTick;
        return currentTick >= tracker.backoffUntilTick;
    }

    public void recordPathfindingResult(int entityId, boolean success, long currentTick) {
        if (!enabled) return;

        if (success) {
            entityTrackers.remove(entityId);
        } else {
            FailureTracker tracker = entityTrackers.computeIfAbsent(entityId, k -> new FailureTracker());
            synchronized (tracker) {
                tracker.failureCount++;
                tracker.lastAccessTick = currentTick;

                if (tracker.failureCount >= maxFailuresBeforeBackoff) {
                    int backoffTicks = Math.min(100, (1 << Math.min(6, tracker.failureCount - maxFailuresBeforeBackoff)) * 20);
                    tracker.backoffUntilTick = currentTick + backoffTicks;
                }
            }
        }
    }

    private void cleanupStaleEntries(long currentTick) {
        this.lastCleanupTick = currentTick;
        com.hyperion.optimizer.core.threading.HyperionThreadPoolManager pool = com.hyperion.optimizer.core.threading.HyperionThreadPoolManager.getInstance();
        if (pool != null && pool.getAsyncScheduler() != null && !pool.getAsyncScheduler().isShutdown()) {
            pool.getAsyncScheduler().execute(() -> {
                entityTrackers.entrySet().removeIf(entry -> currentTick < entry.getValue().lastAccessTick || (currentTick - entry.getValue().lastAccessTick) > CLEANUP_INTERVAL_TICKS);
            });
        } else {
            entityTrackers.entrySet().removeIf(entry -> currentTick < entry.getValue().lastAccessTick || (currentTick - entry.getValue().lastAccessTick) > CLEANUP_INTERVAL_TICKS);
        }
    }

    public void removeEntity(int entityId) {
        entityTrackers.remove(entityId);
    }

    public int getTrackedCount() {
        return entityTrackers.size();
    }

    public boolean isEnabled() {
        return enabled;
    }

    // Fix P3-1: Clear all tracked entities on world change / disconnect
    public void clear() {
        entityTrackers.clear();
        lastCleanupTick = 0;
    }
}
