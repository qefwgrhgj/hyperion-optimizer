package com.hyperion.optimizer.gui;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ⌨️ Hyperion Global Keybinding & Shortcut Manager.
 *
 * Handles shortcut keys to open the Hyperion in-game configuration menu.
 * Primary Master Key: Right Control (Правый Ctrl / GLFW_KEY_RIGHT_CONTROL = 345).
 * Secondary Fallback Combination: Ctrl + Shift + 0.
 */
public final class HyperionKeyBindingManager {
    private static final HyperionKeyBindingManager INSTANCE = new HyperionKeyBindingManager();

    // Standard GLFW Key Codes
    public static final int GLFW_KEY_RIGHT_CONTROL = 345;
    public static final int GLFW_KEY_LEFT_CONTROL = 341;
    public static final int GLFW_KEY_0 = 48;
    public static final int GLFW_KEY_H = 72;
    public static final int GLFW_KEY_O = 79;

    // GLFW Modifiers
    public static final int GLFW_MOD_SHIFT = 0x0001;
    public static final int GLFW_MOD_CONTROL = 0x0002;
    public static final int GLFW_MOD_ALT = 0x0004;

    private volatile boolean enabled = true;
    private final AtomicBoolean requestOpenScreen = new AtomicBoolean(false);
    private volatile Runnable screenOpener;

    private final AtomicBoolean pollerStarted = new AtomicBoolean(false);

    private HyperionKeyBindingManager() {
        startGlfwKeyPoller();
    }

    public static HyperionKeyBindingManager getInstance() {
        return INSTANCE;
    }

    public void setScreenOpener(Runnable opener) {
        this.screenOpener = opener;
    }

    public void startGlfwKeyPoller() {
        if (!pollerStarted.compareAndSet(false, true)) return;
        Thread t = new Thread(() -> {
            try {
                Class<?> glfwClass = Class.forName("org.lwjgl.glfw.GLFW");
                java.lang.reflect.Method getCurrentContextMethod = glfwClass.getMethod("glfwGetCurrentContext");
                java.lang.reflect.Method getKeyMethod = glfwClass.getMethod("glfwGetKey", long.class, int.class);

                boolean wasPressed = false;
                while (enabled) {
                    try {
                        Thread.sleep(60); // 16 Hz check (~0.0001% CPU)
                        long window = (long) getCurrentContextMethod.invoke(null);
                        if (window != 0) {
                            int stateRctrl = (int) getKeyMethod.invoke(null, window, GLFW_KEY_RIGHT_CONTROL);
                            int state0 = (int) getKeyMethod.invoke(null, window, GLFW_KEY_0);
                            int stateH = (int) getKeyMethod.invoke(null, window, GLFW_KEY_H);
                            int stateLctrl = (int) getKeyMethod.invoke(null, window, GLFW_KEY_LEFT_CONTROL);
                            int stateLshift = (int) getKeyMethod.invoke(null, window, 340); // GLFW_KEY_LEFT_SHIFT

                            boolean isRctrl = (stateRctrl == 1);
                            boolean isCombo = ((state0 == 1 || stateH == 1) && (stateLctrl == 1 || stateLshift == 1));

                            if ((isRctrl || isCombo) && !wasPressed) {
                                wasPressed = true;
                                triggerOpenScreen();
                            } else if (!isRctrl && !isCombo) {
                                wasPressed = false;
                            }
                        }
                    } catch (Throwable t2) {
                        Thread.sleep(500);
                    }
                }
            } catch (Throwable ignored) {}
        }, "Hyperion-KeyPoller");
        t.setDaemon(true);
        t.start();
    }

    public void triggerOpenScreen() {
        requestOpenScreen.set(true);
        if (screenOpener != null) {
            try {
                screenOpener.run();
            } catch (Throwable ignored) {}
        } else {
            HyperionGuiLauncher.openConfigScreen();
        }
    }

    /**
     * Checks whether the key event matches the Right Control (or Ctrl + Shift + 0) trigger.
     *
     * @param keyCode The GLFW key code (345 for Right Control, 48 for '0')
     * @param scanCode The platform-specific scan code
     * @param action 1 for press, 0 for release, 2 for repeat
     * @param modifiers Bitfield of active modifier keys
     * @return true if the menu should be opened
     */
    public boolean handleKeyInput(int keyCode, int scanCode, int action, int modifiers) {
        if (!enabled || action == 0) { // Ignore on key release
            return false;
        }

        // 1. Primary trigger: Right Control (GLFW_KEY_RIGHT_CONTROL = 345)
        if (keyCode == GLFW_KEY_RIGHT_CONTROL) {
            triggerOpenScreen();
            return true;
        }

        // 2. Secondary fallback combinations: Ctrl + Shift + 0, or Ctrl + Shift + H
        boolean isCtrlDown = (modifiers & GLFW_MOD_CONTROL) != 0;
        boolean isShiftDown = (modifiers & GLFW_MOD_SHIFT) != 0;

        if (isCtrlDown && isShiftDown && (keyCode == GLFW_KEY_0 || keyCode == GLFW_KEY_H || keyCode == GLFW_KEY_O)) {
            triggerOpenScreen();
            return true;
        }

        return false;
    }

    /**
     * Helper overload for direct key code check.
     */
    public boolean shouldOpenConfigScreen(int keyCode) {
        if (!enabled) return false;
        if (keyCode == GLFW_KEY_RIGHT_CONTROL) {
            requestOpenScreen.set(true);
            return true;
        }
        return false;
    }

    /**
     * Helper overload for boolean modifier states and key codes.
     */
    public boolean shouldOpenConfigScreen(boolean isCtrlDown, boolean isShiftDown, int keyCode) {
        if (!enabled) return false;
        if (keyCode == GLFW_KEY_RIGHT_CONTROL || (isCtrlDown && isShiftDown && keyCode == GLFW_KEY_0)) {
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
