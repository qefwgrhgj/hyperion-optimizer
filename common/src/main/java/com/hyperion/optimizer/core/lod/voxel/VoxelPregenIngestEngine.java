package com.hyperion.optimizer.core.lod.voxel;

import com.hyperion.optimizer.api.HyperionConfig;
import com.hyperion.optimizer.core.threading.HyperionThreadPoolManager;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ⚙️ Asynchronous Voxel Ingestion & Pre-Generation Engine (Inspired by Voxy).
 *
 * Captures loaded chunk voxel data in the background (as the player travels or through
 * pre-generation utilities like Chunky / DH Importers) and offloads Mip generation
 * to CPU worker threads, populating the compressed voxel LOD storage with zero main thread stutter.
 */
public final class VoxelPregenIngestEngine {
    private volatile boolean enabled = true;
    private final VoxelHierarchicalMipTree mipTree;
    private final VoxelSectionStorage storage;

    private final AtomicLong totalIngestedChunks = new AtomicLong(0);
    private final AtomicLong totalQueuedTasks = new AtomicLong(0);

    public VoxelPregenIngestEngine(boolean enabled, VoxelHierarchicalMipTree mipTree, VoxelSectionStorage storage) {
        this.enabled = enabled;
        this.mipTree = mipTree;
        this.storage = storage;
    }

    public void configure(HyperionConfig config) {
        if (config == null) return;
        this.enabled = config.enableVoxelLodEngine;
    }

    /**
     * Ingests a raw 16x16x16 chunk section and builds all Mip levels (0 to 4) asynchronously.
     *
     * @param chunkX Chunk X coordinate
     * @param sectionY Section Y index
     * @param chunkZ Chunk Z coordinate
     * @param rawVoxelData 4096-byte section block data
     */
    public CompletableFuture<Void> ingestSectionAsync(int chunkX, int sectionY, int chunkZ, byte[] rawVoxelData) {
        if (!enabled || rawVoxelData == null || mipTree == null || storage == null) {
            return CompletableFuture.completedFuture(null);
        }

        totalQueuedTasks.incrementAndGet();
        ForkJoinPool pool = HyperionThreadPoolManager.getInstance().getChunkMeshingPool();

        return CompletableFuture.runAsync(() -> {
            try {
                // Store Mip 0 (Full res)
                storage.storeSection(chunkX, sectionY, chunkZ, 0, rawVoxelData);

                // Build downsampled Mips 1 through 4
                for (int mip = 1; mip <= VoxelHierarchicalMipTree.MAX_MIP_LEVEL; mip++) {
                    byte[] downsampled = mipTree.downsampleSection(rawVoxelData, mip);
                    storage.storeSection(chunkX, sectionY, chunkZ, mip, downsampled);
                }

                totalIngestedChunks.incrementAndGet();
            } finally {
                totalQueuedTasks.decrementAndGet();
            }
        }, pool);
    }

    public boolean isEnabled() { return enabled; }
    public long getTotalIngestedChunks() { return totalIngestedChunks.get(); }
    public long getTotalQueuedTasks() { return totalQueuedTasks.get(); }

    public void reset() {
        totalIngestedChunks.set(0);
        totalQueuedTasks.set(0);
    }
}
