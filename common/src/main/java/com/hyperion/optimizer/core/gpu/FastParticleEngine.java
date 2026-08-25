package com.hyperion.optimizer.core.gpu;

import com.hyperion.optimizer.core.memory.PrimitiveVectorPool;

import java.util.concurrent.ConcurrentHashMap;

/**
 * High-Performance GPU/Client Particle Engine.
 * Implements per-block particle spawn limiting (anti-lag cap) and distance-based culling.
 */
public class FastParticleEngine {
    private final boolean enabled;
    private final int maxParticlesPerBlockPerSecond;
    private final double maxParticleDistanceSq;

    private static final int MAX_COUNTER_MAP_SIZE = 4096;
    private final ConcurrentHashMap<Long, java.util.concurrent.atomic.AtomicLong> blockParticleCounters = new ConcurrentHashMap<>();

    public FastParticleEngine(boolean enabled, int maxParticlesPerBlockPerSecond, double maxParticleDistance) {
        this.enabled = enabled;
        this.maxParticlesPerBlockPerSecond = Math.max(1, maxParticlesPerBlockPerSecond);
        this.maxParticleDistanceSq = maxParticleDistance * maxParticleDistance;
    }

    /**
     * Determines whether a particle can spawn at target position or should be culled/rate-limited.
     */
    public boolean shouldSpawnParticle(double camX, double camY, double camZ,
                                       double partX, double partY, double partZ,
                                       long currentEpochSecond) {
        if (!enabled) return true;

        // 1. Distance Culling
        double dx = camX - partX;
        double dy = camY - partY;
        double dz = camZ - partZ;
        if ((dx * dx + dy * dy + dz * dz) > maxParticleDistanceSq) {
            return false; // Beyond max view distance
        }

        // Periodic pruning of stale block particle counters in background
        if (blockParticleCounters.size() > MAX_COUNTER_MAP_SIZE) {
            com.hyperion.optimizer.core.threading.HyperionThreadPoolManager pool = com.hyperion.optimizer.core.threading.HyperionThreadPoolManager.getInstance();
            if (pool != null && pool.getAsyncScheduler() != null && !pool.getAsyncScheduler().isShutdown()) {
                pool.getAsyncScheduler().execute(() -> {
                    blockParticleCounters.entrySet().removeIf(entry -> (currentEpochSecond - (entry.getValue().get() >>> 32)) > 2L);
                });
            } else {
                blockParticleCounters.entrySet().removeIf(entry -> (currentEpochSecond - (entry.getValue().get() >>> 32)) > 2L);
            }
        }

        // 2. Lock-Free Per-Block Rate Limiter
        int bx = (int) Math.floor(partX);
        int by = (int) Math.floor(partY);
        int bz = (int) Math.floor(partZ);
        long packed = PrimitiveVectorPool.packBlockPos(bx, by, bz);

        java.util.concurrent.atomic.AtomicLong counter = blockParticleCounters.computeIfAbsent(packed, k -> new java.util.concurrent.atomic.AtomicLong(0));
        while (true) {
            long currentVal = counter.get();
            long sec = currentVal >>> 32;
            int count = (int) (currentVal & 0xFFFFFFFFL);

            if (sec != currentEpochSecond) {
                long newVal = (currentEpochSecond << 32) | 1L;
                if (counter.compareAndSet(currentVal, newVal)) {
                    return true;
                }
            } else {
                if (count >= maxParticlesPerBlockPerSecond) {
                    return false; // Rate limit exceeded for this block
                }
                long newVal = (currentEpochSecond << 32) | ((long) (count + 1) & 0xFFFFFFFFL);
                if (counter.compareAndSet(currentVal, newVal)) {
                    return true;
                }
            }
        }
    }

    public void clear() {
        blockParticleCounters.clear();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxParticlesPerBlockPerSecond() {
        return maxParticlesPerBlockPerSecond;
    }
}
