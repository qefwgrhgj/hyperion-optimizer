package com.hyperion.optimizer.core.entity;

public class EntityDepthCuller {
    private final boolean enabled;
    private final double maxDistanceSq;

    public EntityDepthCuller(boolean enabled, double maxDistance) {
        this.enabled = enabled;
        this.maxDistanceSq = maxDistance * maxDistance;
    }

    public boolean shouldCullEntity(double camX, double camY, double camZ,
                                    double entityX, double entityY, double entityZ,
                                    boolean isOccludedBySolidBlock) {
        return shouldCullEntity(camX, camY, camZ, entityX, entityY, entityZ, isOccludedBySolidBlock, false, false);
    }

    // Fix P1-4: Boss and Glowing entity protection against clipping and outline occlusion
    public boolean shouldCullEntity(double camX, double camY, double camZ,
                                    double entityX, double entityY, double entityZ,
                                    boolean isOccludedBySolidBlock,
                                    boolean isGlowing,
                                    boolean isSpecialOrBoss) {
        if (!enabled) return false;
        if (isGlowing || isSpecialOrBoss) return false; // Never cull bosses, players or glowing outlines

        double dx = camX - entityX;
        double dy = camY - entityY;
        double dz = camZ - entityZ;
        double distSq = dx * dx + dy * dy + dz * dz;

        // Fix P2-1: Protect against NaN or Infinity coordinates from corrupt entity data
        if (Double.isNaN(distSq) || Double.isInfinite(distSq)) {
            return false;
        }

        // Cull if beyond max distance
        if (distSq > maxDistanceSq) {
            return true;
        }

        // Cull if occluded by intervening solid terrain (Raycast / Early-Z)
        return isOccludedBySolidBlock;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
