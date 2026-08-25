package com.hyperion.optimizer.gui;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ⌨️ Hyperion Global Keybinding & Shortcut Manager.
 *
 * Handles shortcut combinations to open the Hyperion in-game configuration menu.
 * Default Master Shortcut: Ctrl + Shift + 0 (Control + Shift + Key '0').
 */
public final class HyperionKeyBindingManager {
    private static final HyperionKeyBindingManager INSTANCE = new HyperionKeyBindingManager();

    // Standard GLFW Key Codes
    public static final int GLFW_KEY_0 = 48;
    public static final int GLFW_KEY_H = 72;
    public static final int GLFW_KEY_O = 79;

    // GLFW Modifiers
    public static final int GLFW_MOD_SHIFT = 0x0001;
    public static final int GLFW_MOD_CONTROL = 0x0002;
    public static final int GLFW_MOD_ALT = 0x0004;

    private volatile boolean enabled = true;
    private final AtomicBoolean requestOpenScreen = new AtomicBoolean(false);

    private HyperionKeyBindingManager() {}

    public static HyperionKeyBindingManager getInstance() {
        return INSTANCE;
    }

    /**
     * Checks whether the key event matches the Ctrl + Shift + 0 trigger.
     *
     * @param keyCode The GLFW key code (e.g. 48 for '0')
     * @param scanCode The platform-specific scan code
     * @param action 1 for press, 0 for release, 2 for repeat
     * @param modifiers Bitfield of active modifier keys
     * @return true if the menu should be opened
     */
    public boolean handleKeyInput(int keyCode, int scanCode, int action, int modifiers) {
        if (!enabled || action == 0) { // Ignore on key release
            return false;
        }

        boolean isCtrlDown = (modifiers & GLFW_MOD_CONTROL) != 0;
        boolean isShiftDown = (modifiers & GLFW_MOD_SHIFT) != 0;

        if (isCtrlDown && isShiftDown && keyCode == GLFW_KEY_0) {
            requestOpenScreen.set(true);
            return true;
        }

        return false;
    }

    /**
     * Helper overload for boolean modifier states.
     */
    public boolean shouldOpenConfigScreen(boolean isCtrlDown, boolean isShiftDown, int keyCode) {
        if (!enabled) return false;
        if (isCtrlDown && isShiftDown && keyCode == GLFW_KEY_0) {
            requestOpenScreen.set(true);
            return true;
        }
        return false;
    }

    public boolean consumeOpenScreenRequest() {
        return requestOpenScreen.getAndSet(false);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void reset() {
        requestOpenScreen.set(false);
    }
}
