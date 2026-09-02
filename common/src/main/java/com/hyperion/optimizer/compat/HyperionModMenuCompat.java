package com.hyperion.optimizer.compat;

import com.hyperion.optimizer.gui.HyperionInGameScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import java.util.HashMap;
import java.util.Map;

/**
 * 🛠️ Mod Menu Integration for Hyperion Optimizer across Fabric versions.
 * Directly provides native in-game config screen for both hyperion_optimizer and hyperion-optimizer.
 */
public class HyperionModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return HyperionInGameScreen::new;
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        Map<String, ConfigScreenFactory<?>> factories = new HashMap<>();
        factories.put("hyperion_optimizer", HyperionInGameScreen::new);
        factories.put("hyperion-optimizer", HyperionInGameScreen::new);
        return factories;
    }
}
