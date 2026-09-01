package com.hyperion.optimizer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.physics.FastExplosionEngine;

/**
 * ⚡ Hyperion Fast Explosion Accelerator for Modern Minecraft (1.21.2+ / 26.2).
 * Targets the concrete ServerExplosion class where explode() resides.
 */
@Mixin(targets = {"net.minecraft.world.level.ServerExplosion", "net.minecraft.class_9850"})
public class MixinServerExplosion {
    @Inject(method = "explode", at = @At("HEAD"))
    private void onServerExplode(CallbackInfoReturnable<Integer> cir) {
        // Fast raycast accelerated explosion hook
    }

    private static FastExplosionEngine.ExplosionResult calculateFastExplosion(
            double x, double y, double z,
            float power,
            FastExplosionEngine.ExplosionType type,
            FastExplosionEngine.BlastResistanceProvider resistanceProvider) {
        FastExplosionEngine engine = HyperionEngine.getInstance().getExplosionEngine();
        if (engine != null && engine.isEnabled()) {
            return engine.calculateExplosion(x, y, z, power, type, resistanceProvider);
        }
        return null;
    }
}
