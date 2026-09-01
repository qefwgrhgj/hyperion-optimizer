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
    public static final int GLFW_KEY_F8 = 297;
    public static final int GLFW_KEY_F10 = 299;

    // GLFW Modifiers
    public static final int GLFW_MOD_SHIFT = 0x0001;
    public static final int GLFW_MOD_CONTROL = 0x0002;
    public static final int GLFW_MOD_ALT = 0x0004;

    private volatile boolean enabled = true;
    private final AtomicBoolean requestOpenScreen = new AtomicBoolean(false);
    private volatile Runnable screenOpener;

    private final AtomicBoolean pollerStarted = new AtomicBoolean(false);

    private HyperionKeyBindingManager() {
        // Do not eagerly invoke GLFW methods during early initialization
    }

    public static HyperionKeyBindingManager getInstance() {
        return INSTANCE;
    }

    public void setScreenOpener(Runnable opener) {
        this.screenOpener = opener;
    }

    private volatile long cachedWindowHandle = 0L;

    public void setWindowHandle(long handle) {
        if (handle != 0L) {
            this.cachedWindowHandle = handle;
        }
    }

    public static long findMinecraftWindowHandle() {
        String[] classNames = {
            "net.minecraft.client.MinecraftClient",
            "net.minecraft.client.Minecraft",
            "net.minecraft.class_310"
        };
        for (String cName : classNames) {
            try {
                Class<?> mcClass = Class.forName(cName);
                Object mcInstance = null;
                for (String mName : new String[]{"getInstance", "method_1551", "func_71410_x"}) {
                    try {
                        mcInstance = mcClass.getMethod(mName).invoke(null);
                        if (mcInstance != null) break;
                    } catch (Throwable ignored) {}
                }
                if (mcInstance == null) continue;

                // Find window object from fields/methods
                Object windowObj = null;
                for (String mName : new String[]{"getWindow", "method_22683", "func_228018_a_"}) {
                    try {
                        windowObj = mcClass.getMethod(mName).invoke(mcInstance);
                        if (windowObj != null) break;
                    } catch (Throwable ignored) {}
                }
                if (windowObj == null) {
                    for (java.lang.reflect.Field f : mcClass.getDeclaredFields()) {
                        f.setAccessible(true);
                        Object val = f.get(mcInstance);
                        if (val != null && (val.getClass().getSimpleName().contains("Window") || val.getClass().getName().contains("class_1041"))) {
                            windowObj = val;
                            break;
                        }
                    }
                }
                if (windowObj == null) continue;

                // Extract window handle (long)
                for (String mName : new String[]{"getHandle", "handle", "method_4490", "func_227976_a_"}) {
                    try {
                        Object h = windowObj.getClass().getMethod(mName).invoke(windowObj);
                        if (h instanceof Long && (Long) h != 0L) {
                            return (Long) h;
                        }
                    } catch (Throwable ignored) {}
                }
                for (java.lang.reflect.Field f : windowObj.getClass().getDeclaredFields()) {
                    if (f.getType() == long.class) {
                        f.setAccessible(true);
                        long h = f.getLong(windowObj);
                        if (h != 0L) return h;
                    }
                }
            } catch (Throwable ignored) {}
        }
        return 0L;
    }

    public void startGlfwKeyPoller() {
        if (!pollerStarted.compareAndSet(false, true)) return;
        Thread t = new Thread(() -> {
            try {
                // Sleep 2.5 seconds to guarantee Minecraft Blaze3D has initialized GLFW and window
                Thread.sleep(2500);
                Class<?> glfwClass = Class.forName("org.lwjgl.glfw.GLFW");
                java.lang.reflect.Method getKeyMethod = glfwClass.getMethod("glfwGetKey", long.class, int.class);

                boolean wasPressed = false;
                while (enabled) {
                    try {
                        Thread.sleep(50); // 20 Hz check (~0.0001% CPU)
                        long window = cachedWindowHandle;
                        if (window == 0L) {
                            window = findMinecraftWindowHandle();
                            if (window != 0L) {
                                cachedWindowHandle = window;
                            }
                        }
                        if (window != 0L) {
                            int stateRctrl = (int) getKeyMethod.invoke(null, window, GLFW_KEY_RIGHT_CONTROL);
                            int state0 = (int) getKeyMethod.invoke(null, window, GLFW_KEY_0);
                            int stateH = (int) getKeyMethod.invoke(null, window, GLFW_KEY_H);
                            int stateLctrl = (int) getKeyMethod.invoke(null, window, GLFW_KEY_LEFT_CONTROL);
                            int stateLshift = (int) getKeyMethod.invoke(null, window, 340); // GLFW_KEY_LEFT_SHIFT
                            int stateF8 = (int) getKeyMethod.invoke(null, window, GLFW_KEY_F8);
                            int stateF10 = (int) getKeyMethod.invoke(null, window, GLFW_KEY_F10);
                            int stateRshift = (int) getKeyMethod.invoke(null, window, 344); // GLFW_KEY_RIGHT_SHIFT

                            boolean isRctrl = (stateRctrl == 1);
                            boolean isCombo = ((state0 == 1 || stateH == 1) && (stateLctrl == 1 || stateLshift == 1));
                            boolean isFunctionKey = (stateF8 == 1 || stateF10 == 1 || stateRshift == 1);

                            if ((isRctrl || isCombo || isFunctionKey) && !wasPressed) {
                                wasPressed = true;
                                triggerOpenScreen();
                            } else if (!isRctrl && !isCombo && !isFunctionKey) {
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
