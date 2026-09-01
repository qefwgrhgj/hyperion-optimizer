package com.hyperion.optimizer.mixin;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.gpu.ComputeCullEngine;
import com.hyperion.optimizer.core.gpu.MultiDrawIndirectManager;
import com.hyperion.optimizer.core.render.ColorCorrectionEngine;
import com.hyperion.optimizer.core.render.FpsStabilizerEngine;
import com.hyperion.optimizer.gui.HyperionGuiLauncher;
import com.hyperion.optimizer.gui.HyperionKeyBindingManager;
import com.hyperion.optimizer.gui.HyperionScreenModel;

/**
 * ⚡ Hyperion Mixin Bridge.
 * Public static entrypoints for subsystem integration and verification tests.
 * Cleanly decoupled from @Mixin classes to strictly satisfy SpongePowered Mixin rules.
 */
public final class HyperionMixinBridge {
    private static final HyperionScreenModel SCREEN_MODEL = new HyperionScreenModel();

    private HyperionMixinBridge() {}

    // Video Options Screen
    public static void onInitVideoOptionsScreen() {
        HyperionEngine.getInstance();
    }

    public static HyperionScreenModel getActiveModel() {
        return SCREEN_MODEL;
    }

    public static void openHyperionSettings() {
        HyperionGuiLauncher.openConfigScreen();
    }

    // Keyboard
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

    // Level Renderer
    public static boolean shouldRenderChunkSection(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        ComputeCullEngine cullEngine = HyperionEngine.getInstance().getComputeCullEngine();
        if (cullEngine != null && cullEngine.isEnabled()) {
            return cullEngine.isBoxVisible(minX, minY, minZ, maxX, maxY, maxZ);
        }
        return true;
    }

    public static void onSetupTerrainFrustum(float[] projectionMatrix, float[] modelViewMatrix) {
        ComputeCullEngine cullEngine = HyperionEngine.getInstance().getComputeCullEngine();
        if (cullEngine != null && cullEngine.isEnabled()) {
            cullEngine.updateFrustum(projectionMatrix, modelViewMatrix);
        }
    }

    public static void onRenderFrameStart() {
        FpsStabilizerEngine stabilizer = HyperionEngine.getInstance().getFpsStabilizer();
        if (stabilizer != null && stabilizer.isEnabled()) {
            stabilizer.onFrameStart();
        }
    }

    public static boolean shouldUploadChunkMesh() {
        FpsStabilizerEngine stabilizer = HyperionEngine.getInstance().getFpsStabilizer();
        if (stabilizer != null && stabilizer.isEnabled()) {
            return stabilizer.canUploadChunkMeshThisFrame();
        }
        return true;
    }

    public static void onFlushTerrainBatch() {
        MultiDrawIndirectManager manager = HyperionEngine.getInstance().getMultiDrawManager();
        if (manager != null) {
            manager.finishBatch();
        }
    }

    // Lightmap Texture & Color
    public static void onProcessLightmap(int[] pixels, int width, int height, float nightFactor) {
        ColorCorrectionEngine engine = HyperionEngine.getInstance().getColorCorrectionEngine();
        if (engine != null && engine.isEnabled()) {
            engine.processLightmapAbgr(pixels, width, height, nightFactor);
        }
    }

    public static void onGradeLightColor(float r, float g, float b, float nightFactor, int ditherX, int ditherY, float[] outRgb) {
        ColorCorrectionEngine engine = HyperionEngine.getInstance().getColorCorrectionEngine();
        if (engine != null && engine.isEnabled()) {
            engine.gradeRgb(r, g, b, nightFactor, ditherX, ditherY, outRgb);
        } else {
            outRgb[0] = r;
            outRgb[1] = g;
            outRgb[2] = b;
        }
    }

    public static void onProcessTexture(int[] pixels, int width, int height) {
        ColorCorrectionEngine engine = HyperionEngine.getInstance().getColorCorrectionEngine();
        if (engine != null && engine.isEnabled()) {
            engine.processTexture(pixels, width, height);
        }
    }

    public static void onProcessTextureAbgr(int[] pixels, int width, int height) {
        ColorCorrectionEngine engine = HyperionEngine.getInstance().getColorCorrectionEngine();
        if (engine != null && engine.isEnabled()) {
            engine.processTextureAbgr(pixels, width, height);
        }
    }

    public static int onGradeBiomeColor(int argb) {
        ColorCorrectionEngine engine = HyperionEngine.getInstance().getColorCorrectionEngine();
        if (engine != null && engine.isEnabled()) {
            return engine.gradeColorRgbInt(argb);
        }
        return argb;
    }
}
