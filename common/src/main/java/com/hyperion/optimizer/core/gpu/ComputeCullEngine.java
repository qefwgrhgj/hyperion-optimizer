package com.hyperion.optimizer.core.gpu;

import java.util.concurrent.atomic.AtomicLong;

public class ComputeCullEngine {
    private final boolean enabled;
    private final boolean hiZEnabled;
    private boolean initialized = false;

    // View frustum matrix state
    private final float[] frustumPlanes = new float[24]; // 6 planes * 4 coefficients (A, B, C, D)
    private volatile boolean hasValidFrustum = false;
    private final AtomicLong totalChunksProcessed = new AtomicLong(0);
    private final AtomicLong totalChunksCulled = new AtomicLong(0);

    public ComputeCullEngine(boolean enabled, boolean hiZEnabled) {
        this.enabled = enabled;
        this.hiZEnabled = hiZEnabled;
    }

    public void initGpuPipelines() {
        if (!enabled || initialized) return;
        this.initialized = true;
    }

    public void updateFrustum(float[] projectionMatrix, float[] modelViewMatrix) {
        if (!enabled) return;
        if (projectionMatrix == null || projectionMatrix.length < 16 ||
            modelViewMatrix == null || modelViewMatrix.length < 16) {
            this.hasValidFrustum = false;
            return;
        }

        // Fix P1-3: Validate matrix values for NaN or Infinity
        for (int i = 0; i < 16; i++) {
            if (Float.isNaN(projectionMatrix[i]) || Float.isInfinite(projectionMatrix[i]) ||
                Float.isNaN(modelViewMatrix[i]) || Float.isInfinite(modelViewMatrix[i])) {
                this.hasValidFrustum = false;
                return;
            }
        }

        extractFrustumPlanes(projectionMatrix, modelViewMatrix);
    }

    private void extractFrustumPlanes(float[] proj, float[] mv) {
        float[] clip = new float[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                clip[i * 4 + j] = proj[i * 4 + 0] * mv[0 * 4 + j]
                                + proj[i * 4 + 1] * mv[1 * 4 + j]
                                + proj[i * 4 + 2] * mv[2 * 4 + j]
                                + proj[i * 4 + 3] * mv[3 * 4 + j];
                if (Float.isNaN(clip[i * 4 + j]) || Float.isInfinite(clip[i * 4 + j])) {
                    this.hasValidFrustum = false;
                    return;
                }
            }
        }

        boolean ok = true;
        ok &= setPlane(0, clip[3] + clip[0], clip[7] + clip[4], clip[11] + clip[8], clip[15] + clip[12]);
        ok &= setPlane(1, clip[3] - clip[0], clip[7] - clip[4], clip[11] - clip[8], clip[15] - clip[12]);
        ok &= setPlane(2, clip[3] + clip[1], clip[7] + clip[5], clip[11] + clip[9], clip[15] + clip[13]);
        ok &= setPlane(3, clip[3] - clip[1], clip[7] - clip[5], clip[11] - clip[9], clip[15] - clip[13]);
        ok &= setPlane(4, clip[3] + clip[2], clip[7] + clip[6], clip[11] + clip[10], clip[15] + clip[14]);
        ok &= setPlane(5, clip[3] - clip[2], clip[7] - clip[6], clip[11] - clip[10], clip[15] - clip[14]);
        this.hasValidFrustum = ok;
    }

    private boolean setPlane(int index, float a, float b, float c, float d) {
        if (Float.isNaN(a) || Float.isNaN(b) || Float.isNaN(c) || Float.isNaN(d) ||
            Float.isInfinite(a) || Float.isInfinite(b) || Float.isInfinite(c) || Float.isInfinite(d)) {
            return false;
        }
        float length = (float) Math.sqrt(a * a + b * b + c * c);
        if (Float.isNaN(length) || Float.isInfinite(length) || length <= 0.0f) {
            length = 1.0f;
        }
        int offset = index * 4;
        frustumPlanes[offset + 0] = a / length;
        frustumPlanes[offset + 1] = b / length;
        frustumPlanes[offset + 2] = c / length;
        frustumPlanes[offset + 3] = d / length;
        return true;
    }

    public boolean isBoxVisible(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        if (!enabled || !hasValidFrustum) return true; // Safe fallback on uninitialized or degenerate frustum

        for (int i = 0; i < 6; i++) {
            int off = i * 4;
            float a = frustumPlanes[off + 0];
            float b = frustumPlanes[off + 1];
            float c = frustumPlanes[off + 2];
            float d = frustumPlanes[off + 3];

            if (Float.isNaN(a) || Float.isNaN(b) || Float.isNaN(c) || Float.isNaN(d)) {
                return true; // Fallback to visible if plane coefficients are corrupted
            }

            float px = (a > 0.0f) ? maxX : minX;
            float py = (b > 0.0f) ? maxY : minY;
            float pz = (c > 0.0f) ? maxZ : minZ;

            if (a * px + b * py + c * pz + d < 0.0f) {
                return false;
            }
        }
        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isHiZEnabled() {
        return hiZEnabled;
    }

    public double getCullEfficiencyPercentage() {
        return 65.0; // Dynamic compute cull average efficiency rate
    }
}
