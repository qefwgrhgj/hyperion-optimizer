package com.hyperion.optimizer.mixin;

import com.hyperion.optimizer.gui.HyperionKeyBindingManager;

/**
 * Mixin hook into Minecraft Keyboard Input Handler.
 * Intercepts Right Control key (GLFW_KEY_RIGHT_CONTROL = 345) to toggle Hyperion In-Game Settings.
 */
public class MixinKeyboard {
    public static boolean onKey(long window, int key, int scancode, int action, int modifiers) {
        HyperionKeyBindingManager manager = HyperionKeyBindingManager.getInstance();
        if (manager != null && manager.isEnabled()) {
            return manager.handleKeyInput(key, scancode, action, modifiers);
        }
        return false;
    }

    public static boolean shouldInterceptKey(int key, int action) {
        if (action == 0) return false;
        HyperionKeyBindingManager manager = HyperionKeyBindingManager.getInstance();
        return manager != null && manager.isEnabled() && manager.shouldOpenConfigScreen(key);
    }
}
