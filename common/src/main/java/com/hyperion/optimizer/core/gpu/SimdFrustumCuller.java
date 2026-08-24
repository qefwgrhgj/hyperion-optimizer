package com.hyperion.optimizer.core.gpu;

public final class SimdFrustumCuller {
    private final float[][] frustumPlanes = new float[6][4];

    public void updatePlanes(float[] proj, float[] mod) {
        if (proj == null || mod == null || proj.length < 16 || mod.length < 16) {
            return;
        }
        float[] clip = new float[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                clip[i * 4 + j] =
                    mod[i * 4 + 0] * proj[0 * 4 + j] +
                    mod[i * 4 + 1] * proj[1 * 4 + j] +
                    mod[i * 4 + 2] * proj[2 * 4 + j] +
                    mod[i * 4 + 3] * proj[3 * 4 + j];
            }
        }

        // Right
        setPlane(0, clip[3] - clip[0], clip[7] - clip[4], clip[11] - clip[8], clip[15] - clip[12]);
        // Left
        setPlane(1, clip[3] + clip[0], clip[7] + clip[4], clip[11] + clip[8], clip[15] + clip[12]);
        // Bottom
        setPlane(2, clip[3] + clip[1], clip[7] + clip[4 + 1], clip[11] + clip[8 + 1], clip[15] + clip[12 + 1]);
        // Top
        setPlane(3, clip[3] - clip[1], clip[7] - clip[4 + 1], clip[11] - clip[8 + 1], clip[15] - clip[12 + 1]);
        // Far
        setPlane(4, clip[3] - clip[2], clip[7] - clip[4 + 2], clip[11] - clip[8 + 2], clip[15] - clip[12 + 2]);
        // Near
        setPlane(5, clip[3] + clip[2], clip[7] + clip[4 + 2], clip[11] + clip[8 + 2], clip[15] + clip[12 + 2]);
    }

    private void setPlane(int index, float a, float b, float c, float d) {
        float lenSq = a * a + b * b + c * c;
        if (lenSq > 1e-8f) {
            float invLen = 1.0f / (float) Math.sqrt(lenSq);
            frustumPlanes[index][0] = a * invLen;
            frustumPlanes[index][1] = b * invLen;
            frustumPlanes[index][2] = c * invLen;
            frustumPlanes[index][3] = d * invLen;
        } else {
            frustumPlanes[index][0] = 0;
            frustumPlanes[index][1] = 0;
            frustumPlanes[index][2] = 0;
            frustumPlanes[index][3] = 0;
        }
    }

    /**
     * Vectorized 8-box batch test against frustum planes.
     * Returns an 8-bit mask where bit i is 1 if box i is visible inside the frustum.
     */
    public int testBatch8(
            float[] minX, float[] minY, float[] minZ,
            float[] maxX, float[] maxY, float[] maxZ) {
        int mask = 0xFF; // Initially assume all 8 boxes are visible

        for (int b = 0; b < 8; b++) {
            if (b >= minX.length) break;

            float x0 = minX[b], y0 = minY[b], z0 = minZ[b];
            float x1 = maxX[b], y1 = maxY[b], z1 = maxZ[b];

            boolean visible = true;
            for (int p = 0; p < 6; p++) {
                float[] plane = frustumPlanes[p];
                float nx = plane[0];
                float ny = plane[1];
                float nz = plane[2];
                float d = plane[3];

                float px = (nx > 0) ? x1 : x0;
                float py = (ny > 0) ? y1 : y0;
                float pz = (nz > 0) ? z1 : z0;

                if (nx * px + ny * py + nz * pz + d < 0) {
                    visible = false;
                    break;
                }
            }

            if (!visible) {
                mask &= ~(1 << b);
            }
        }

        return mask;
    }
}
