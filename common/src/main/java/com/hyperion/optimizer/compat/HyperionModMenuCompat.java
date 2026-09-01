package com.hyperion.optimizer.compat;

import com.hyperion.optimizer.gui.HyperionInGameScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * 🛠️ Mod Menu Integration for Hyperion Optimizer across all Fabric versions.
 *
 * Integrates the "⚙ Settings" button in Mod Menu's in-game interface directly into the native in-game screen.
 */
public class HyperionModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return HyperionInGameScreen::new;
    }
}
