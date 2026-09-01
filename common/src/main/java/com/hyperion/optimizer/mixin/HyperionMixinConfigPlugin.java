package com.hyperion.optimizer.mixin;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * ⚡ Hyperion Sovereign Cross-Version Mixin Configuration Governor.
 *
 * Dynamically validates target class presence across Minecraft 1.16.5 -> 1.21.11 / 26.2.
 * Prevents MixinTargetValidationException and ClassNotFoundException when packaging
 * or method signatures diverge across loader/version matrix boundaries.
 */
public class HyperionMixinConfigPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        System.out.println("[Hyperion] Mixin Config Plugin loaded for package: " + mixinPackage);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName != null && mixinClassName.endsWith("MixinExplosion")) {
            // MixinExplosion targets legacy Explosion class (<= 1.21.1).
            // In 1.21.2+ and 26.2, net.minecraft.world.level.Explosion was refactored into an interface.
            // Skip MixinExplosion on modern versions to prevent InvalidMixinException: @Mixin target type mismatch.
            try {
                Class<?> explosionClass = Class.forName("net.minecraft.world.level.Explosion", false, Thread.currentThread().getContextClassLoader());
                if (explosionClass.isInterface()) {
                    return false;
                }
            } catch (Throwable ignored) {
                try {
                    Class<?> explosionClass = Class.forName("net.minecraft.world.level.Explosion", false, HyperionMixinConfigPlugin.class.getClassLoader());
                    if (explosionClass.isInterface()) {
                        return false;
                    }
                } catch (Throwable ignored2) {}
            }
        }

        if (mixinClassName != null && mixinClassName.endsWith("MixinServerExplosion")) {
            // MixinServerExplosion targets ServerExplosion class in modern versions (1.21.2+ / 26.2).
            try {
                Class<?> serverExplosionClass = Class.forName("net.minecraft.world.level.ServerExplosion", false, Thread.currentThread().getContextClassLoader());
                return !serverExplosionClass.isInterface();
            } catch (Throwable ignored) {
                try {
                    Class<?> serverExplosionClass = Class.forName("net.minecraft.world.level.ServerExplosion", false, HyperionMixinConfigPlugin.class.getClassLoader());
                    return !serverExplosionClass.isInterface();
                } catch (Throwable ignored2) {
                    return false;
                }
            }
        }

        if (targetClassName == null || targetClassName.isEmpty()) {
            return true;
        }

        try {
            Class<?> clazz = Class.forName(targetClassName, false, Thread.currentThread().getContextClassLoader());
            if (clazz.isInterface()) {
                return false;
            }
            return true;
        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
            try {
                Class<?> clazz = Class.forName(targetClassName, false, HyperionMixinConfigPlugin.class.getClassLoader());
                if (clazz.isInterface()) {
                    return false;
                }
                return true;
            } catch (ClassNotFoundException | NoClassDefFoundError ignored2) {
                return false;
            }
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
