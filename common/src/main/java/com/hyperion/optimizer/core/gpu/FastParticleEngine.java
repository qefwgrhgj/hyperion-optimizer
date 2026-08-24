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

    private static class ParticleCounter {
        long currentSecond = 0;
        int count = 0;
    }

    private static final int MAX_COUNTER_MAP_SIZE = 4096;
    private final ConcurrentHashMap<Long, ParticleCounter> blockParticleCounters = new ConcurrentHashMap<>();

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

        // Fix P1-3: Periodic pruning of stale block particle counters
        if (blockParticleCounters.size() > MAX_COUNTER_MAP_SIZE) {
            blockParticleCounters.entrySet().removeIf(entry -> (currentEpochSecond - entry.getValue().currentSecond) > 2L);
        }

        // 2. Per-Block Rate Limiter
        int bx = (int) Math.floor(partX);
        int by = (int) Math.floor(partY);
        int bz = (int) Math.floor(partZ);
        long packed = PrimitiveVectorPool.packBlockPos(bx, by, bz);

        ParticleCounter counter = blockParticleCounters.computeIfAbsent(packed, k -> new ParticleCounter());
        synchronized (counter) {
            if (counter.currentSecond != currentEpochSecond) {
                counter.currentSecond = currentEpochSecond;
                counter.count = 1;
                return true;
            }
            if (counter.count >= maxParticlesPerBlockPerSecond) {
                return false; // Rate limit exceeded for this block
            }
            counter.count++;
            return true;
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
