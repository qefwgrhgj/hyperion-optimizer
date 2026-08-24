package com.hyperion.optimizer.core.gpu.amd;

import java.util.concurrent.atomic.AtomicLong;

public final class AmdVramBudgetGuard {
    private final boolean enabled;
    private final long totalVramBytes;
    private final long warningThresholdBytes;
    private final AtomicLong currentlyAllocatedBytes = new AtomicLong(0);
    private boolean compressionActive = false;

    public AmdVramBudgetGuard(boolean enabled, long totalVramMb) {
        this.enabled = enabled;
        this.totalVramBytes = Math.max(512L, totalVramMb) * 1024L * 1024L;
        // Trigger compression & LOD throttling when VRAM reaches 75% capacity
        this.warningThresholdBytes = (long) (this.totalVramBytes * 0.75);
    }

    public boolean allocateChunkGeometry(long bytes) {
        if (!enabled) return true;

        long current = currentlyAllocatedBytes.addAndGet(bytes);
        if (current >= warningThresholdBytes) {
            compressionActive = true;
        }
        return current <= totalVramBytes;
    }

    public void releaseChunkGeometry(long bytes) {
        if (!enabled) return;

        long current = currentlyAllocatedBytes.updateAndGet(val -> Math.max(0L, val - bytes));
        if (current < (long) (this.totalVramBytes * 0.60)) {
            compressionActive = false;
        }
    }

    public boolean isCompressionActive() {
        return enabled && compressionActive;
    }

    public double getVramUsagePercentage() {
        if (totalVramBytes == 0) return 0.0;
        return (currentlyAllocatedBytes.get() / (double) totalVramBytes) * 100.0;
    }

    public long getCurrentlyAllocatedMb() {
        return currentlyAllocatedBytes.get() / (1024L * 1024L);
    }

    public long getTotalVramMb() {
        return totalVramBytes / (1024L * 1024L);
    }

    public void reset() {
        currentlyAllocatedBytes.set(0);
        compressionActive = false;
    }
}
