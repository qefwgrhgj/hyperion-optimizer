package com.hyperion.optimizer.core.lod.voxel;

import com.hyperion.optimizer.api.HyperionConfig;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 🌲 Voxel Hierarchical Mip Tree & Downsampling Engine.
 *
 * Implements a hierarchical octree/mip-pyramid for voxel LOD rendering:
 * - Level 0: 1x1x1 blocks (Full 16x16x16 chunk section detail, 4096 voxels)
 * - Level 1: 2x2x2 block clusters (8x8x8 downsampled section, 512 voxels)
 * - Level 2: 4x4x4 block clusters (4x4x4 downsampled section, 64 voxels)
 * - Level 3: 8x8x8 block clusters (2x2x2 downsampled section, 8 voxels)
 * - Level 4: 16x16x16 single-envelope section voxel (1 aggregate voxel for extreme 2048+ chunk distances)
 *
 * Allows rendering massive infinite horizons (2048+ chunks / 32,768+ blocks) with constant-time O(1)
 * traversal and minuscule memory footprint.
 */
public final class VoxelHierarchicalMipTree {
    public static final int MAX_MIP_LEVEL = 4;
    public static final int SECTION_SIZE = 16;

    private volatile boolean enabled = true;
    private volatile int maxRenderDistanceChunks = 2048;
    private final AtomicLong totalMipSectionsGenerated = new AtomicLong(0);

    public VoxelHierarchicalMipTree(boolean enabled) {
        this.enabled = enabled;
    }

    public void configure(HyperionConfig config) {
        if (config == null) return;
        this.enabled = config.enableVoxelLodEngine;
        this.maxRenderDistanceChunks = Math.max(32, config.voxelMaxRenderDistanceChunks);
    }

    /**
     * Determines appropriate Mip level (0 to 4) for a chunk section given its Euclidean distance in blocks from the camera.
     */
    public int getMipLevelForDistance(double distanceBlocks) {
        if (!enabled || distanceBlocks <= 256.0) { // < 16 chunks -> Mip 0 (Standard High Res)
            return 0;
        } else if (distanceBlocks <= 512.0) {       // 16 - 32 chunks -> Mip 1 (2x downsampling)
            return 1;
        } else if (distanceBlocks <= 1024.0) {      // 32 - 64 chunks -> Mip 2 (4x downsampling)
            return 2;
        } else if (distanceBlocks <= 2048.0) {      // 64 - 128 chunks -> Mip 3 (8x downsampling)
            return 3;
        } else {                                    // 128 - 2048+ chunks -> Mip 4 (16x downsampling)
            return 4;
        }
    }

    /**
     * Downsamples a 16x16x16 raw voxel section (4096 bytes) into the specified Mip Level representation.
     *
     * @param rawVoxelData 4096 byte array (16x16x16) of block state IDs
     * @param targetMipLevel Mip Level (0 to 4)
     * @return Compact downsampled voxel array
     */
    public byte[] downsampleSection(byte[] rawVoxelData, int targetMipLevel) {
        if (rawVoxelData == null || rawVoxelData.length < 4096 || targetMipLevel <= 0) {
            return rawVoxelData;
        }

        int step = 1 << targetMipLevel; // 2, 4, 8, 16
        int dim = SECTION_SIZE / step;  // 8, 4, 2, 1
        int targetLength = dim * dim * dim;
        byte[] downsampled = new byte[targetLength];

        for (int y = 0; y < dim; y++) {
            for (int z = 0; z < dim; z++) {
                for (int x = 0; x < dim; x++) {
                    int dominantBlock = findDominantVoxel(rawVoxelData, x * step, y * step, z * step, step);
                    downsampled[(y * dim + z) * dim + x] = (byte) dominantBlock;
                }
            }
        }

        totalMipSectionsGenerated.incrementAndGet();
        return downsampled;
    }

    private static final ThreadLocal<int[]> HISTOGRAM_BUFFER = ThreadLocal.withInitial(() -> new int[256]);

    /**
     * Finds the dominant non-air voxel inside a sub-cube of size (step x step x step).
     * Zero-allocation routine using reusable thread-local histogram buffer.
     */
    private int findDominantVoxel(byte[] data, int startX, int startY, int startZ, int step) {
        int[] counts = HISTOGRAM_BUFFER.get();
        Arrays.fill(counts, 0);
        int maxCount = 0;
        int dominantId = 0;

        for (int dy = 0; dy < step; dy++) {
            int y = startY + dy;
            if (y >= SECTION_SIZE) break;
            for (int dz = 0; dz < step; dz++) {
                int z = startZ + dz;
                if (z >= SECTION_SIZE) break;
                for (int dx = 0; dx < step; dx++) {
                    int x = startX + dx;
                    if (x >= SECTION_SIZE) break;

                    int idx = (y * SECTION_SIZE + z) * SECTION_SIZE + x;
                    int id = data[idx] & 0xFF;
                    if (id != 0) { // Ignore Air
                        counts[id]++;
                        if (counts[id] > maxCount) {
                            maxCount = counts[id];
                            dominantId = id;
                        }
                    }
                }
            }
        }

        return dominantId;
    }

    public boolean isEnabled() { return enabled; }
    public int getMaxRenderDistanceChunks() { return maxRenderDistanceChunks; }
    public long getTotalMipSectionsGenerated() { return totalMipSectionsGenerated.get(); }

    public void reset() {
        totalMipSectionsGenerated.set(0);
    }
}
