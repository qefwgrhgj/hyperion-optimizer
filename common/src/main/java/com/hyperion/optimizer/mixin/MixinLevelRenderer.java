package com.hyperion.optimizer.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.gpu.ComputeCullEngine;
import com.hyperion.optimizer.core.gpu.MultiDrawIndirectManager;

@Mixin(targets = {
    "net.minecraft.client.renderer.LevelRenderer",
    "net.minecraft.client.renderer.WorldRenderer"
})
@Environment(EnvType.CLIENT)
public class MixinLevelRenderer {
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void onRenderLevelStart(CallbackInfo ci) {
        onRenderFrameStart();
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void onRenderLevelEnd(CallbackInfo ci) {
        onFlushTerrainBatch();
    }

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
