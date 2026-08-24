package com.hyperion.optimizer.core.physics;

public class VoxelShapeFastCache {
    private final boolean enabled;

    // Fast-path bitmasks for standard solid full-cube vs empty shape
    public static final int SHAPE_TYPE_EMPTY = 0;
    public static final int SHAPE_TYPE_FULL_CUBE = 1;
    public static final int SHAPE_TYPE_COMPLEX = 2;

    public VoxelShapeFastCache(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean canFastPassCubeCollision(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
                                           int blockX, int blockY, int blockZ, int shapeType) {
        if (!enabled) return false;

        // Fix P2-2: Reject inverted or degenerate AABBs
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            return false;
        }

        if (shapeType == SHAPE_TYPE_EMPTY) {
            return false; // No collision with air/passable block
        }

        if (shapeType == SHAPE_TYPE_FULL_CUBE) {
            // Direct AABB test with 1x1x1 cube without creating VoxelShape objects
            return minX < blockX + 1.0 && maxX > blockX &&
                   minY < blockY + 1.0 && maxY > blockY &&
                   minZ < blockZ + 1.0 && maxZ > blockZ;
        }

        return false; // Fall back to vanilla complex voxel shape
    }

    public boolean isEnabled() {
        return enabled;
    }
}
