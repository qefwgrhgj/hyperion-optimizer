package com.hyperion.optimizer.mixin;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.physics.FastExplosionEngine;

public class MixinExplosion {
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
