package com.hyperion.optimizer.mixin;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.gpu.ComputeCullEngine;
import com.hyperion.optimizer.core.gpu.MultiDrawIndirectManager;

public class MixinLevelRenderer {
    public static boolean shouldRenderChunkSection(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        ComputeCullEngine cullEngine = HyperionEngine.getInstance().getComputeCullEngine();
        if (cullEngine != null && cullEngine.isEnabled()) {
            return cullEngine.isBoxVisible(minX, minY, minZ, maxX, maxY, maxZ);
        }
        return true;
    }

    public static void onSetupTerrainFrustum(float[] projectionMatrix, float[] modelViewMatrix) {
        ComputeCullEngine cullEngine = HyperionEngine.getInstance().getComputeCullEngine();
        if (cullEngine != null && cullEngine.isEnabled()) {
            cullEngine.updateFrustum(projectionMatrix, modelViewMatrix);
        }
    }

    public static void onRenderFrameStart() {
        com.hyperion.optimizer.core.render.FpsStabilizerEngine stabilizer = HyperionEngine.getInstance().getFpsStabilizer();
        if (stabilizer != null && stabilizer.isEnabled()) {
            stabilizer.onFrameStart();
        }
    }

    public static boolean shouldUploadChunkMesh() {
        com.hyperion.optimizer.core.render.FpsStabilizerEngine stabilizer = HyperionEngine.getInstance().getFpsStabilizer();
        if (stabilizer != null && stabilizer.isEnabled()) {
            return stabilizer.canUploadChunkMeshThisFrame();
        }
        return true;
    }

    public static void onFlushTerrainBatch() {
        MultiDrawIndirectManager manager = HyperionEngine.getInstance().getMultiDrawManager();
        if (manager != null) {
            manager.finishBatch();
        }
    }
}
