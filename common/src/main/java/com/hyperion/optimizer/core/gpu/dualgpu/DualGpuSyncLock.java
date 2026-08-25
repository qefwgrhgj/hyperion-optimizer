package com.hyperion.optimizer.core.gpu.dualgpu;

import com.hyperion.optimizer.api.HyperionConfig;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * 🔒 Dual-GPU Desynchronization & Wait-Loop Suppressor (Sync Lock).
 *
 * Prevents faster GPU execution contexts (or waiting threads) from entering
 * aggressive busy-wait spin-loops when discrete GPU and integrated GPU exhibit
 * divergent execution latencies.
 *
 * Replaces 100% CPU core spinning with bounded, adaptive micro-parks and strict
 * synchronization timeout barriers (default 5ms), preventing CPU heating and pipeline deadlocks.
 */
public final class DualGpuSyncLock {
    private volatile boolean enabled = true;
    private volatile long maxSyncTimeoutNanos = 5_000_000L; // 5 ms timeout
    private volatile long spinYieldThresholdNanos = 50_000L; // 50 microseconds before park

    private final AtomicLong successfulSyncs = new AtomicLong(0);
    private final AtomicLong timedOutSyncs = new AtomicLong(0);
    private final AtomicLong totalWaitTimeNanos = new AtomicLong(0);

    public DualGpuSyncLock(boolean enabled) {
        this.enabled = enabled;
    }

    public void configure(HyperionConfig config) {
        if (config == null) return;
        this.enabled = config.enableDualGpuSyncLock;
        this.maxSyncTimeoutNanos = Math.max(500_000L, config.dualGpuSyncTimeoutMs * 1_000_000L);
    }

    /**
     * Waits for an inter-GPU synchronization condition with bounded micro-parks instead of busy-spinning.
     *
     * @param syncCondition A functional condition checker returning true when GPU task is ready.
     * @return true if synchronized successfully, false if timed out.
     */
    public boolean awaitSync(SyncCondition syncCondition) {
        if (!enabled || syncCondition == null || syncCondition.isReady()) {
            successfulSyncs.incrementAndGet();
            return true;
        }

        long start = System.nanoTime();
        long elapsed = 0;

        while (!syncCondition.isReady()) {
            elapsed = System.nanoTime() - start;
            if (elapsed >= maxSyncTimeoutNanos) {
                timedOutSyncs.incrementAndGet();
                totalWaitTimeNanos.addAndGet(elapsed);
                return false; // Timed out to prevent infinite hang
            }

            // Adaptive backoff: short spins first, then micro-parks to yield CPU
            if (elapsed < spinYieldThresholdNanos) {
                Thread.onSpinWait();
            } else {
                LockSupport.parkNanos(100_000L); // 100 microsecond gentle sleep
            }
        }

        successfulSyncs.incrementAndGet();
        totalWaitTimeNanos.addAndGet(System.nanoTime() - start);
        return true;
    }

    @FunctionalInterface
    public interface SyncCondition {
        boolean isReady();
    }

    public boolean isEnabled() { return enabled; }
    public long getMaxSyncTimeoutNanos() { return maxSyncTimeoutNanos; }
    public long getSuccessfulSyncs() { return successfulSyncs.get(); }
    public long getTimedOutSyncs() { return timedOutSyncs.get(); }
    public long getTotalWaitTimeNanos() { return totalWaitTimeNanos.get(); }

    public void reset() {
        successfulSyncs.set(0);
        timedOutSyncs.set(0);
        totalWaitTimeNanos.set(0);
    }
}
