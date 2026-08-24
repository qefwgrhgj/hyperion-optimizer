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
        double dy = camY - entityY;
        double dz = camZ - entityZ;
        double distSq = dx * dx + dy * dy + dz * dz;

        if (distSq <= nearDistSq) {
            return false; // Full framerate animation (144+ FPS)
        } else if (distSq <= farDistSq) {
            // Half framerate animation (30-60 FPS) with phase shift
            return ((currentFrameIndex + entityId) & 1) != 0;
        } else {
            // Quarter framerate animation (15-20 FPS) with phase shift
            return ((currentFrameIndex + entityId) & 3) != 0;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
