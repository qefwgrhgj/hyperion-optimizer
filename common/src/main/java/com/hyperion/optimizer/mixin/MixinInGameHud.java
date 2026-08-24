package com.hyperion.optimizer.mixin;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.hud.DecoupledHudManager;

public class MixinInGameHud {
    public static boolean shouldRepaintHud(long nanoTime) {
        DecoupledHudManager hudManager = HyperionEngine.getInstance().getHudManager();
        if (hudManager != null && hudManager.isEnabled()) {
            return hudManager.shouldRepaintHud(nanoTime);
        }
        return true;
    }

    public static void onPlayerDamage(float health, int foodLevel, int armor) {
        DecoupledHudManager hudManager = HyperionEngine.getInstance().getHudManager();
        if (hudManager != null && hudManager.isEnabled()) {
            hudManager.getDirtyTracker().updateState(health, foodLevel, armor, 0, 0, 0f, 0, System.currentTimeMillis());
        }
    }
}
