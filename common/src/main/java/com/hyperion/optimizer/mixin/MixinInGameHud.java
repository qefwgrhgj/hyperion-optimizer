package com.hyperion.optimizer.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.hud.DecoupledHudManager;

@Mixin(targets = {
    "net.minecraft.client.gui.Gui",
    "net.minecraft.client.gui.IngameGui"
})
@Environment(EnvType.CLIENT)
public class MixinInGameHud {
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHud(CallbackInfo ci) {
        shouldRepaintHud(System.nanoTime());
    }

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
            hudManager.getDirtyTracker().updateHealth(health, foodLevel, armor);
        }
    }
}
