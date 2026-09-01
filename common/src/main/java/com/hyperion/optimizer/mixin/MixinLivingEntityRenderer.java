package com.hyperion.optimizer.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.entity.AnimationLodManager;
import com.hyperion.optimizer.core.entity.EntityDepthCuller;

@Mixin(targets = {"net.minecraft.client.renderer.entity.LivingEntityRenderer", "net.minecraft.class_922", "net.minecraft.client.renderer.entity.LivingRenderer"})
@Environment(EnvType.CLIENT)
public class MixinLivingEntityRenderer {
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderLivingEntity(CallbackInfo ci) {
        // Living entity depth culling & animation LOD
    }

    private static boolean shouldCullEntity(
            double camX, double camY, double camZ,
            double entityX, double entityY, double entityZ,
            boolean isOccludedBySolidBlock,
            boolean isGlowing,
            boolean isBoss) {
        EntityDepthCuller culler = HyperionEngine.getInstance().getEntityCuller();
        if (culler != null && culler.isEnabled()) {
            return culler.shouldCullEntity(camX, camY, camZ, entityX, entityY, entityZ, isOccludedBySolidBlock, isGlowing, isBoss);
        }
        return false;
    }

    private static boolean shouldSkipAnimation(
            double camX, double camY, double camZ,
            double entityX, double entityY, double entityZ,
            long currentFrameIndex,
            int entityId) {
        AnimationLodManager lodManager = HyperionEngine.getInstance().getAnimationLod();
        if (lodManager != null && lodManager.isEnabled()) {
            return lodManager.shouldSkipAnimationTick(camX, camY, camZ, entityX, entityY, entityZ, currentFrameIndex, entityId);
        }
        return false;
    }
}
