package com.hyperion.optimizer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.physics.FastExplosionEngine;

@Mixin(targets = {"net.minecraft.world.level.Explosion", "net.minecraft.class_1927", "net.minecraft.world.Explosion"})
public class MixinExplosion {
    @Inject(method = "explode", at = @At("HEAD"))
    private void onExplode(CallbackInfo ci) {
        // Fast raycast accelerated explosion
    }

    public static FastExplosionEngine.ExplosionResult calculateFastExplosion(
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
