package com.hyperion.optimizer.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hyperion.optimizer.gui.HyperionKeyBindingManager;

/**
 * Mixin hook into Minecraft Keyboard Input Handler.
 * Intercepts Right Control key (GLFW_KEY_RIGHT_CONTROL = 345) to toggle Hyperion In-Game Settings.
 */
@Mixin(targets = {"net.minecraft.client.KeyboardHandler", "net.minecraft.class_309", "net.minecraft.client.KeyboardListener"})
@Environment(EnvType.CLIENT)
public class MixinKeyboard {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void onKeyPress(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (onKey(window, key, scancode, action, modifiers)) {
            ci.cancel();
        }
    }

    @Unique
    public static boolean onKey(long window, int key, int scancode, int action, int modifiers) {
        HyperionKeyBindingManager manager = HyperionKeyBindingManager.getInstance();
        if (manager != null && manager.isEnabled()) {
            return manager.handleKeyInput(key, scancode, action, modifiers);
        }
        return false;
    }

    @Unique
    public static boolean shouldInterceptKey(int key, int action) {
        if (action == 0) return false;
        HyperionKeyBindingManager manager = HyperionKeyBindingManager.getInstance();
        return manager != null && manager.isEnabled() && manager.shouldOpenConfigScreen(key);
    }
}
