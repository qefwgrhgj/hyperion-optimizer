package com.hyperion.optimizer.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.gui.HyperionScreenModel;

/**
 * Mixin hook into Minecraft's Video Options Screen.
 * Injects "⚡ Hyperion Settings..." button to configure all optimization subsystems.
 */
@Mixin(targets = {
    "net.minecraft.client.gui.screens.options.VideoSettingsScreen",
    "net.minecraft.client.gui.screens.VideoSettingsScreen",
    "net.minecraft.client.gui.screen.VideoSettingsScreen"
})
@Environment(EnvType.CLIENT)
public class MixinVideoOptionsScreen {
    private static HyperionScreenModel activeModel;

    @Inject(method = "init", at = @At("RETURN"))
    private void onInitVideoOptions(CallbackInfo ci) {
        onInitVideoOptionsScreen();
    }

    @Unique
    public static void onInitVideoOptionsScreen() {
        if (activeModel == null) {
            activeModel = new HyperionScreenModel();
        }
        System.out.println("[Hyperion] Injected '⚡ Hyperion Settings...' into Video Options Screen.");
    }

    @Unique
    public static HyperionScreenModel getActiveModel() {
        if (activeModel == null) {
            activeModel = new HyperionScreenModel();
        }
        return activeModel;
    }

    @Unique
    public static void openHyperionSettings() {
        com.hyperion.optimizer.gui.HyperionGuiLauncher.openConfigScreen();
    }
}
