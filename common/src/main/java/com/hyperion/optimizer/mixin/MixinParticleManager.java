package com.hyperion.optimizer.mixin;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.gpu.FastParticleEngine;

public class MixinParticleManager {
    public static boolean canSpawnParticle(int blockX, int blockY, int blockZ, double cameraX, double cameraY, double cameraZ, long currentTick) {
        FastParticleEngine engine = HyperionEngine.getInstance().getParticleEngine();
        if (engine != null && engine.isEnabled()) {
            return engine.shouldSpawnParticle(blockX, blockY, blockZ, cameraX, cameraY, cameraZ, currentTick);
        }
        return true;
    }
}
