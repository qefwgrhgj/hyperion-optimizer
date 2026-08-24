package com.hyperion.optimizer.core.physics;

import com.hyperion.optimizer.core.memory.PrimitiveVectorPool;

import java.util.concurrent.ConcurrentHashMap;

/**
 * High-Performance Fluid Dynamics Engine (Lithium Physics).
 * Caches computed 3D fluid flow vectors for water and lava, eliminating expensive recursive
 * path checks on static fluid streams until neighboring blocks change.
 */
public class FastFluidEngine {
    public static class FluidFlowVector {
        public final double vx;
        public final double vy;
        public final double vz;

        public FluidFlowVector(double vx, double vy, double vz) {
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
        }
    }

    private static final int MAX_CACHE_CAPACITY = 32768;
    private final boolean enabled;
    private final ConcurrentHashMap<Long, FluidFlowVector> flowVectorCache = new ConcurrentHashMap<>();

    public FastFluidEngine(boolean enabled) {
        this.enabled = enabled;
    }

    public FluidFlowVector getCachedFlowVector(int x, int y, int z) {
        if (!enabled) return null;
        long packed = PrimitiveVectorPool.packBlockPos(x, y, z);
        return flowVectorCache.get(packed);
    }

    public void cacheFlowVector(int x, int y, int z, double vx, double vy, double vz) {
        if (!enabled) return;
        if (flowVectorCache.size() >= MAX_CACHE_CAPACITY) {
            flowVectorCache.clear();
        }
        long packed = PrimitiveVectorPool.packBlockPos(x, y, z);
        flowVectorCache.put(packed, new FluidFlowVector(vx, vy, vz));
    }

    public void invalidateBlock(int x, int y, int z) {
        if (!enabled) return;
        // Invalidate block and 6 immediate neighbors
        long packed = PrimitiveVectorPool.packBlockPos(x, y, z);
        flowVectorCache.remove(packed);

        int[][] offsets = {{1,0,0}, {-1,0,0}, {0,1,0}, {0,-1,0}, {0,0,1}, {0,0,-1}};
        for (int[] off : offsets) {
            flowVectorCache.remove(PrimitiveVectorPool.packBlockPos(x + off[0], y + off[1], z + off[2]));
        }
    }

    // Fix P1-2: Prune chunk fluid cache on chunk unload
    public void invalidateChunk(int chunkX, int chunkZ) {
        if (!enabled) return;
        int minBlockX = chunkX << 4;
        int maxBlockX = minBlockX + 15;
        int minBlockZ = chunkZ << 4;
        int maxBlockZ = minBlockZ + 15;
        flowVectorCache.keySet().removeIf(pos -> {
            int x = PrimitiveVectorPool.unpackX(pos);
            int z = PrimitiveVectorPool.unpackZ(pos);
            return x >= minBlockX && x <= maxBlockX && z >= minBlockZ && z <= maxBlockZ;
        });
    }

    public void clear() {
        flowVectorCache.clear();
    }

    public int getCachedFlowCount() {
        return flowVectorCache.size();
    }

    public boolean isEnabled() {
        return enabled;
    }
}
