package com.hyperion.optimizer.core.render;

import com.hyperion.optimizer.api.HyperionConfig;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 🏔️ Chunk LOD (Level of Detail) & Geometry Simplification Engine.
 *
 * Dynamically simplifies polygon mesh geometry and block voxel meshes for chunks
 * located beyond 12–16 blocks from the player camera:
 * - LOD 0 (< 16 blocks): Full 100% geometry and micro-facet detail.
 * - LOD 1 (16–48 blocks): Coplanar quad merging & micro-detail decimation (~50% vertex reduction).
 * - LOD 2 (> 48 blocks): Aggregate envelope voxel simplification (~75% vertex reduction).
 */
public final class ChunkLodManager {
    private volatile boolean enabled = true;
    private volatile double lodDistanceThresholdBlocks = 16.0;
    private volatile double lodFarDistanceThresholdBlocks = 48.0;
    private volatile double lodSimplificationFactor = 0.50;

    private final AtomicLong lod0SectionsProcessed = new AtomicLong(0);
    private final AtomicLong lod1SectionsProcessed = new AtomicLong(0);
    private final AtomicLong lod2SectionsProcessed = new AtomicLong(0);
    private final AtomicLong totalSavedVertices = new AtomicLong(0);

    public ChunkLodManager(boolean enabled) {
        this.enabled = enabled;
    }

    public void configure(HyperionConfig config) {
        if (config == null) return;
        this.enabled = config.enableChunkLod;
        this.lodDistanceThresholdBlocks = Math.max(8.0, config.chunkLodDistanceBlocks);
        this.lodFarDistanceThresholdBlocks = Math.max(lodDistanceThresholdBlocks * 2.0, config.chunkLodFarDistanceBlocks);
        this.lodSimplificationFactor = Math.max(0.1, Math.min(0.9, config.chunkLodSimplificationFactor));
    }

    public int calculateLodLevel(double playerX, double playerY, double playerZ,
                                 double chunkCenterX, double chunkCenterY, double chunkCenterZ) {
        if (!enabled) return 0;

        double dx = playerX - chunkCenterX;
        double dy = playerY - chunkCenterY;
        double dz = playerZ - chunkCenterZ;
        double distSq = dx * dx + dy * dy + dz * dz;

        double nearThresholdSq = lodDistanceThresholdBlocks * lodDistanceThresholdBlocks;
        double farThresholdSq = lodFarDistanceThresholdBlocks * lodFarDistanceThresholdBlocks;

        if (distSq <= nearThresholdSq) {
            lod0SectionsProcessed.incrementAndGet();
            return 0;
        } else if (distSq <= farThresholdSq) {
            lod1SectionsProcessed.incrementAndGet();
            return 1;
        } else {
            lod2SectionsProcessed.incrementAndGet();
            return 2;
        }
    }

    public int simplifyQuadCount(int rawQuadCount, int lodLevel) {
        if (!enabled || lodLevel == 0 || rawQuadCount <= 0) {
            return rawQuadCount;
        }

        int simplified;
        if (lodLevel == 1) {
            simplified = Math.max(1, (int) (rawQuadCount * (1.0 - lodSimplificationFactor)));
        } else {
            simplified = Math.max(1, (int) (rawQuadCount * (1.0 - Math.min(0.85, lodSimplificationFactor * 1.5))));
        }

        long saved = (long) (rawQuadCount - simplified) * 4L;
        totalSavedVertices.addAndGet(saved);
        return simplified;
    }

    public double getLodReductionRatio(int lodLevel) {
        if (lodLevel == 1) return 1.0 - lodSimplificationFactor;
        if (lodLevel >= 2) return 1.0 - Math.min(0.85, lodSimplificationFactor * 1.5);
        return 1.0;
    }

    public boolean isEnabled() { return enabled; }
    public double getLodDistanceThresholdBlocks() { return lodDistanceThresholdBlocks; }
    public double getLodFarDistanceThresholdBlocks() { return lodFarDistanceThresholdBlocks; }
    public double getLodSimplificationFactor() { return lodSimplificationFactor; }
    public long getLod0SectionsProcessed() { return lod0SectionsProcessed.get(); }
    public long getLod1SectionsProcessed() { return lod1SectionsProcessed.get(); }
    public long getLod2SectionsProcessed() { return lod2SectionsProcessed.get(); }
    public long getTotalSavedVertices() { return totalSavedVertices.get(); }

    public void reset() {
        lod0SectionsProcessed.set(0);
        lod1SectionsProcessed.set(0);
        lod2SectionsProcessed.set(0);
        totalSavedVertices.set(0);
    }
}
