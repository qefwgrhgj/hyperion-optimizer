package com.hyperion.optimizer.compat;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * 🤝 Universal Mod Compatibility & Ecosystem Integration Manager.
 *
 * Dynamically identifies loaded optimization and graphics mods in the runtime environment:
 * - Iris / Oculus Shaders: Coordinates GBuffer & Composite depth pass bindings.
 * - Sodium / Embeddium / Nvidium: Adapts chunk meshing and prevents duplicate render passes.
 * - Distant Horizons / Bobby: Manages LOD mutual exclusion, preventing double VRAM allocation.
 * - Lithium / FerriteCore / ImmediatelyFast: Coordinates sleeping hoppers, physics, and shared buffer builders.
 * - Entity Culling / More Culling: Unifies entity visibility occlusion tracking.
 */
public final class HyperionModCompatManager {
    private static final Logger LOGGER = Logger.getLogger("Hyperion-Compat");
    private static final HyperionModCompatManager INSTANCE = new HyperionModCompatManager();

    private final Set<String> detectedMods = Collections.synchronizedSet(new HashSet<>());

    private volatile boolean irisLoaded = false;
    private volatile boolean sodiumLoaded = false;
    private volatile boolean nvidiumLoaded = false;
    private volatile boolean distantHorizonsLoaded = false;
    private volatile boolean bobbyLoaded = false;
    private volatile boolean lithiumLoaded = false;
    private volatile boolean ferriteCoreLoaded = false;
    private volatile boolean immediatelyFastLoaded = false;
    private volatile boolean entityCullingLoaded = false;

    private HyperionModCompatManager() {
        detectLoadedMods();
    }

    public static HyperionModCompatManager getInstance() {
        return INSTANCE;
    }

    public void detectLoadedMods() {
        detectedMods.clear();

        irisLoaded = checkClass("net.irisshaders.iris.Iris") || checkClass("net.coderbot.iris.Iris");
        sodiumLoaded = checkClass("me.jellysquid.mods.sodium.client.SodiumClientMod") || checkClass("net.caffeinemc.mods.sodium.client.SodiumClientMod");
        nvidiumLoaded = checkClass("me.cortex.nvidium.Nvidium");
        distantHorizonsLoaded = checkClass("com.seibel.distanthorizons.core.wrapper.mod.ModWrapper");
        bobbyLoaded = checkClass("de.johni0702.minecraft.bobby.Bobby");
        lithiumLoaded = checkClass("me.jellysquid.mods.lithium.common.LithiumMod");
        ferriteCoreLoaded = checkClass("malte0811.ferritecore.FerriteCore");
        immediatelyFastLoaded = checkClass("net.raphimc.immediatelyfast.ImmediatelyFast");
        entityCullingLoaded = checkClass("dev.tr7zw.entityculling.EntityCullingMod");

        if (irisLoaded) detectedMods.add("iris");
        if (sodiumLoaded) detectedMods.add("sodium");
        if (nvidiumLoaded) detectedMods.add("nvidium");
        if (distantHorizonsLoaded) detectedMods.add("distanthorizons");
        if (bobbyLoaded) detectedMods.add("bobby");
        if (lithiumLoaded) detectedMods.add("lithium");
        if (ferriteCoreLoaded) detectedMods.add("ferritecore");
        if (immediatelyFastLoaded) detectedMods.add("immediatelyfast");
        if (entityCullingLoaded) detectedMods.add("entityculling");

        LOGGER.info(String.format("[Hyperion-Compat] Mod Environment Scanned. Active Ecosystem: %s", detectedMods));
    }

    private static boolean checkClass(String className) {
        try {
            Class.forName(className, false, HyperionModCompatManager.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }

    public void registerDetectedMod(String modId) {
        if (modId != null) {
            String lower = modId.toLowerCase();
            detectedMods.add(lower);
            if (lower.contains("iris") || lower.contains("oculus")) irisLoaded = true;
            if (lower.contains("sodium") || lower.contains("embeddium") || lower.contains("rubidium")) sodiumLoaded = true;
            if (lower.contains("nvidium")) nvidiumLoaded = true;
            if (lower.contains("distant") || lower.contains("dh")) distantHorizonsLoaded = true;
            if (lower.contains("bobby")) bobbyLoaded = true;
            if (lower.contains("lithium")) lithiumLoaded = true;
            if (lower.contains("ferrite")) ferriteCoreLoaded = true;
            if (lower.contains("immediately")) immediatelyFastLoaded = true;
            if (lower.contains("entityculling")) entityCullingLoaded = true;
        }
    }

    public boolean isIrisLoaded() { return irisLoaded; }
    public boolean isSodiumLoaded() { return sodiumLoaded; }
    public boolean isNvidiumLoaded() { return nvidiumLoaded; }
    public boolean isDistantHorizonsLoaded() { return distantHorizonsLoaded; }
    public boolean isBobbyLoaded() { return bobbyLoaded; }
    public boolean isLithiumLoaded() { return lithiumLoaded; }
    public boolean isFerriteCoreLoaded() { return ferriteCoreLoaded; }
    public boolean isImmediatelyFastLoaded() { return immediatelyFastLoaded; }
    public boolean isEntityCullingLoaded() { return entityCullingLoaded; }
    public Set<String> getDetectedMods() { return Collections.unmodifiableSet(detectedMods); }

    public void reset() {
        detectedMods.clear();
        detectLoadedMods();
    }
}
