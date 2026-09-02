package com.hyperion.optimizer.mixin;

import java.net.URL;
import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * ⚡ Hyperion Sovereign Cross-Version Mixin Configuration Governor.
 *
 * Dynamically validates and filters mixins across Minecraft 1.16.5 -> 1.21.11 / 26.2.
 * Uses zero-classloading resource lookup to ensure NO game classes are loaded prematurely
 * (preventing MixinTargetAlreadyLoadedException).
 */
public class HyperionMixinConfigPlugin implements IMixinConfigPlugin {

    private boolean isModern26 = false;

    @Override
    public void onLoad(String mixinPackage) {
        System.out.println("[Hyperion] Mixin Config Plugin loaded for package: " + mixinPackage);
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = HyperionMixinConfigPlugin.class.getClassLoader();
            }
            URL url = cl.getResource("net/minecraft/world/level/ServerExplosion.class");
            isModern26 = (url != null);
        } catch (Throwable ignored) {
            isModern26 = false;
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName == null) {
            return true;
        }

        // 1. On modern 1.21.2+ / 26.2, Explosion is an interface. Skip legacy MixinExplosion.
        if (mixinClassName.endsWith("MixinExplosion")) {
            return !isModern26;
        }

        // 2. On modern 1.21.2+ / 26.2, ServerExplosion is the concrete class.
        if (mixinClassName.endsWith("MixinServerExplosion")) {
            return isModern26;
        }

        // 3. On modern 1.21.2+ / 26.2, Gui no longer has render(). Skip legacy MixinInGameHud.
        if (mixinClassName.endsWith("MixinInGameHud")) {
            return !isModern26;
        }

        // 4. On modern 1.21.2+ / 26.2, LightmapRenderStateExtractor handles lightmap extraction.
        if (mixinClassName.endsWith("MixinLightmapRenderStateExtractor")) {
            return isModern26;
        }

        // 5. On modern 1.21.2+ / 26.2, LevelRenderer26 handles surface feature culling and anti-stutter.
        if (mixinClassName.endsWith("MixinLevelRenderer26")) {
            return isModern26;
        }

        return true;
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
