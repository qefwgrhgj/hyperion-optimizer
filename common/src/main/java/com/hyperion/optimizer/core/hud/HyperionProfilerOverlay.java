package com.hyperion.optimizer.core.hud;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class HyperionProfilerOverlay {
    private static final HyperionProfilerOverlay INSTANCE = new HyperionProfilerOverlay();

    private final AtomicInteger culledEntitiesCount = new AtomicInteger(0);
    private final AtomicInteger culledChunksCount = new AtomicInteger(0);
    private final AtomicInteger sleepingHoppersCount = new AtomicInteger(0);
    private final AtomicLong savedNetworkBytes = new AtomicLong(0);
    private final AtomicLong savedMemoryAllocations = new AtomicLong(0);

    private float currentFps = 60.0f;
    private float averageFrameTimeMs = 16.6f;

    private HyperionProfilerOverlay() {}

    public static HyperionProfilerOverlay getInstance() {
        return INSTANCE;
    }

    public void recordCulledEntity() {
        culledEntitiesCount.incrementAndGet();
    }

    public void recordCulledChunk() {
        culledChunksCount.incrementAndGet();
    }

    public void setSleepingHoppers(int count) {
        sleepingHoppersCount.set(count);
    }

    public void addSavedNetworkBytes(long bytes) {
        savedNetworkBytes.addAndGet(bytes);
    }

    public void addSavedMemoryBytes(long bytes) {
        savedMemoryAllocations.addAndGet(bytes);
    }

    public void updateMetrics(float fps, float frameTimeMs) {
        this.currentFps = fps;
        this.averageFrameTimeMs = frameTimeMs;
    }

    public int getCulledEntities() {
        return culledEntitiesCount.get();
    }

    public int getCulledChunks() {
        return culledChunksCount.get();
    }

    public int getSleepingHoppers() {
        return sleepingHoppersCount.get();
    }

    public long getSavedNetworkBytes() {
        return savedNetworkBytes.get();
    }

    public long getSavedMemoryAllocations() {
        return savedMemoryAllocations.get();
    }

    public float getCurrentFps() {
        return currentFps;
    }

    public float getAverageFrameTimeMs() {
        return averageFrameTimeMs;
    }

    public String generateTelemetrySummary() {
        return String.format(
            java.util.Locale.ROOT,
            "⚡ [Hyperion Telemetry] FPS: %.1f (%.2f ms) | Culled Chunks: %d | Culled Entities: %d | Sleeping Hoppers: %d | Bandwidth Saved: %.2f KB",
            currentFps,
            averageFrameTimeMs,
            culledChunksCount.get(),
            culledEntitiesCount.get(),
            sleepingHoppersCount.get(),
            savedNetworkBytes.get() / 1024.0
        );
    }

    public void resetFrameCounters() {
        culledEntitiesCount.set(0);
        culledChunksCount.set(0);
    }
}
