package com.hyperion.optimizer.mixin;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.render.ColorCorrectionEngine;

/**
 * Mixin hook for Minecraft Lightmap Texture Manager & Color Pipeline.
 * Injects Hyperion's HDR ACES Tonemapping, Anti-Black-Crush, and Vibrance.
 */
public class MixinLightmapTexture {
    public static void onProcessLightmap(int[] pixels, int width, int height, float nightFactor) {
        ColorCorrectionEngine engine = HyperionEngine.getInstance().getColorCorrectionEngine();
        if (engine != null && engine.isEnabled()) {
            engine.processLightmap(pixels, width, height, nightFactor);
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
}
