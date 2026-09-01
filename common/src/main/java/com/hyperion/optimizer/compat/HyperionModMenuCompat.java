package com.hyperion.optimizer.compat;

import com.hyperion.optimizer.gui.HyperionGuiLauncher;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Constructor;

/**
 * 🛠️ Mod Menu Integration for Hyperion Optimizer across Fabric versions.
 *
 * Employs safe reflective construction to prevent classloading leaks or NoClassDefFoundError
 * when running across divergent mappings (Intermediary / Mojang Mappings / Legacy).
 */
public class HyperionModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (Screen parent) -> {
            try {
                Class<?> screenClass = Class.forName("net.minecraft.client.gui.screens.Screen");
                Class<?> inGameScreenClass = Class.forName("com.hyperion.optimizer.gui.HyperionInGameScreen");
                Constructor<?> ctor = inGameScreenClass.getConstructor(screenClass);
                return (Screen) ctor.newInstance(parent);
            } catch (Throwable t) {
                // Fallback for legacy environments or alternative mappings:
                // Opens the robust standalone GUI dashboard and returns parent screen
                HyperionGuiLauncher.openConfigScreen();
                return parent;
            }
        };
    }
}
