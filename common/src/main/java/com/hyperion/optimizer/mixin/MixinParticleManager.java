package com.hyperion.optimizer.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.gpu.FastParticleEngine;

@Mixin(targets = {"net.minecraft.client.particle.ParticleEngine", "net.minecraft.class_702", "net.minecraft.client.particle.ParticleManager"})
@Environment(EnvType.CLIENT)
public class MixinParticleManager {
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderParticles(CallbackInfo ci) {
        FastParticleEngine engine = HyperionEngine.getInstance().getParticleEngine();
        if (engine != null && engine.isEnabled()) {
            // Accelerated particle rendering
        }
    }

    private static boolean canSpawnParticle(int blockX, int blockY, int blockZ, double cameraX, double cameraY, double cameraZ, long currentTick) {
        FastParticleEngine engine = HyperionEngine.getInstance().getParticleEngine();
        if (engine != null && engine.isEnabled()) {
            return engine.shouldSpawnParticle(blockX, blockY, blockZ, cameraX, cameraY, cameraZ, currentTick);
        }
        return true;
    }
}
