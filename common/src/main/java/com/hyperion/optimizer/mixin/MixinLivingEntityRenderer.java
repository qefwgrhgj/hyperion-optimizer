package com.hyperion.optimizer.mixin;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.entity.AnimationLodManager;
import com.hyperion.optimizer.core.entity.EntityDepthCuller;

public class MixinLivingEntityRenderer {
    public static boolean shouldCullEntity(
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

    public static boolean shouldSkipAnimation(
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
