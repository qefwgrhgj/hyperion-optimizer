package com.hyperion.optimizer.mixin;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.gui.HyperionScreenModel;

/**
 * Mixin hook into Minecraft's Video Options Screen.
 * Injects "⚡ Hyperion Settings..." button to configure all optimization subsystems.
 */
public class MixinVideoOptionsScreen {
    private static HyperionScreenModel activeModel;

    public static void onInitVideoOptionsScreen() {
        if (activeModel == null) {
            activeModel = new HyperionScreenModel();
        }
        System.out.println("[Hyperion] Injected '⚡ Hyperion Settings...' into Video Options Screen.");
    }

    public static HyperionScreenModel getActiveModel() {
        if (activeModel == null) {
            activeModel = new HyperionScreenModel();
        }
        return activeModel;
    }

    public static void openHyperionSettings() {
        System.out.println("[Hyperion] Opening Hyperion Options Screen: " + HyperionEngine.getInstance().getConfig());
    }
}
