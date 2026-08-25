package com.hyperion.optimizer.core.entity;

public class AnimationLodManager {
    private final boolean enabled;
    private final double nearDistSq;
    private final double farDistSq;

    public AnimationLodManager(boolean enabled, double nearDist, double farDist) {
        this.enabled = enabled;
        this.nearDistSq = nearDist * nearDist;
        this.farDistSq = farDist * farDist;
    }

    public boolean shouldSkipAnimationTick(double camX, double camY, double camZ,
                                           double entityX, double entityY, double entityZ,
                                           long currentFrameIndex) {
        return shouldSkipAnimationTick(camX, camY, camZ, entityX, entityY, entityZ, currentFrameIndex, 0);
    }

    // Fix P2-4: Interleaved entity phase offset to eliminate synchronized crowd animation jitter
    public boolean shouldSkipAnimationTick(double camX, double camY, double camZ,
                                           double entityX, double entityY, double entityZ,
                                           long currentFrameIndex,
                                           int entityId) {
        if (!enabled) return false;

        double dx = camX - entityX;
        double dxSq = dx * dx;
        if (dxSq > farDistSq) {
            return ((currentFrameIndex + entityId) & 3) != 0;
        }

        double dz = camZ - entityZ;
        double dzSq = dz * dz;
        if (dxSq + dzSq > farDistSq) {
            return ((currentFrameIndex + entityId) & 3) != 0;
        }

        double dy = camY - entityY;
        double distSq = dxSq + dzSq + dy * dy;

        if (distSq <= nearDistSq) {
            return false; // Full framerate animation (144+ FPS)
        } else if (distSq <= farDistSq) {
            // Half framerate animation (60-72 FPS) with phase shift
            return ((currentFrameIndex + entityId) & 1) != 0;
        } else if (distSq <= 4096.0) { // 32 - 64 blocks
            // Quarter framerate animation (20-30 FPS) with phase shift
            return ((currentFrameIndex + entityId) & 3) != 0;
        } else if (distSq <= 16384.0) { // 64 - 128 blocks
            // 1/8th framerate animation (~10 FPS)
            return ((currentFrameIndex + entityId) & 7) != 0;
        } else { // 128+ blocks (32+ chunks render distance)
            // 1/16th framerate animation (~5 FPS for distant background mobs)
            return ((currentFrameIndex + entityId) & 15) != 0;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
