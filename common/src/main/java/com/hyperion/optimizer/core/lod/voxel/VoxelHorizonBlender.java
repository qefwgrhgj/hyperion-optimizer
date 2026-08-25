package com.hyperion.optimizer.core.lod.voxel;

import com.hyperion.optimizer.api.HyperionConfig;

/**
 * 🌅 Voxel Horizon Blender & Atmospheric Distance Fog Integrator (Inspired by Voxy).
 *
 * Implements smooth visual blending between close-range vanilla/Sodium full-detail chunks
 * and far-distance voxel LOD terrain out to 2048+ chunks.
 *
 * Prevents harsh render distance edges, popping chunk borders, and color mismatches
 * by calculating continuous depth-weighted fade factors and atmospheric horizon fog envelopes.
 */
public final class VoxelHorizonBlender {
    private volatile boolean enabled = true;
    private volatile double blendStartDistanceChunks = 12.0;
    private volatile double blendEndDistanceChunks = 24.0;
    private volatile boolean enableAtmosphericHorizonFog = true;

    public VoxelHorizonBlender(boolean enabled) {
        this.enabled = enabled;
    }

    public void configure(HyperionConfig config) {
        if (config == null) return;
        this.enabled = config.enableVoxelHorizonBlending;
        this.blendStartDistanceChunks = Math.max(4.0, config.voxelBlendStartChunks);
        this.blendEndDistanceChunks = Math.max(blendStartDistanceChunks + 4.0, config.voxelBlendEndChunks);
        this.enableAtmosphericHorizonFog = config.enableVoxelAtmosphericFog;
    }

    /**
     * Calculates the alpha blend factor (0.0 = 100% close-range terrain, 1.0 = 100% distant voxel LOD).
     *
     * @param distanceChunks Euclidean distance in chunks from player camera.
     * @return Alpha factor between 0.0 and 1.0 using smooth-step interpolation.
     */
    public float calculateLodBlendFactor(double distanceChunks) {
        if (!enabled || distanceChunks <= blendStartDistanceChunks) {
            return 0.0f; // Pure vanilla/Sodium terrain
        }
        if (distanceChunks >= blendEndDistanceChunks) {
            return 1.0f; // Pure voxel LOD
        }

        // Hermite smooth-step: 3x^2 - 2x^3
        double t = (distanceChunks - blendStartDistanceChunks) / (blendEndDistanceChunks - blendStartDistanceChunks);
        t = Math.max(0.0, Math.min(1.0, t));
        return (float) (t * t * (3.0 - 2.0 * t));
    }

    /**
     * Calculates atmospheric fog density factor for extreme distances (>1000 chunks).
     */
    public float calculateAtmosphericFogFactor(double distanceBlocks, double maxDistanceBlocks) {
        if (!enableAtmosphericHorizonFog || maxDistanceBlocks <= 0) return 0.0f;

        double normDist = distanceBlocks / maxDistanceBlocks;
        if (normDist < 0.70) return 0.0f;

        double fogProgress = (normDist - 0.70) / 0.30;
        return (float) Math.min(1.0, Math.max(0.0, fogProgress * fogProgress));
    }

    public boolean isEnabled() { return enabled; }
    public double getBlendStartDistanceChunks() { return blendStartDistanceChunks; }
    public double getBlendEndDistanceChunks() { return blendEndDistanceChunks; }
    public boolean isAtmosphericHorizonFogEnabled() { return enableAtmosphericHorizonFog; }
}
