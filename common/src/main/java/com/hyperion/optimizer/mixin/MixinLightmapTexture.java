package com.hyperion.optimizer.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.render.ColorCorrectionEngine;

/**
 * Mixin hook for Minecraft Lightmap Texture Manager & Color Pipeline.
 * Injects Hyperion's HDR ACES Tonemapping, Anti-Black-Crush, and Vibrance.
 */
@Mixin(targets = {"net.minecraft.client.renderer.LightTexture", "net.minecraft.class_765", "net.minecraft.client.renderer.GameRenderer"})
@Environment(EnvType.CLIENT)
public class MixinLightmapTexture {
    @Inject(method = "updateLightTexture", at = @At("HEAD"))
    private void onUpdateLightmap(CallbackInfo ci) {
        // Lightmap cache and color correction
    }

    private static void onProcessLightmap(int[] pixels, int width, int height, float nightFactor) {
        ColorCorrectionEngine engine = HyperionEngine.getInstance().getColorCorrectionEngine();
        if (engine != null && engine.isEnabled()) {
            engine.processLightmapAbgr(pixels, width, height, nightFactor);
        }
    }

    private static void onGradeLightColor(float r, float g, float b, float nightFactor, int ditherX, int ditherY, float[] outRgb) {
        ColorCorrectionEngine engine = HyperionEngine.getInstance().getColorCorrectionEngine();
        if (engine != null && engine.isEnabled()) {
            engine.gradeRgb(r, g, b, nightFactor, ditherX, ditherY, outRgb);
        } else {
            outRgb[0] = r;
            outRgb[1] = g;
            outRgb[2] = b;
        }
    }

    private static void onProcessTexture(int[] pixels, int width, int height) {
        ColorCorrectionEngine engine = HyperionEngine.getInstance().getColorCorrectionEngine();
        if (engine != null && engine.isEnabled()) {
            engine.processTexture(pixels, width, height);
        }
    }

    private static void onProcessTextureAbgr(int[] pixels, int width, int height) {
        ColorCorrectionEngine engine = HyperionEngine.getInstance().getColorCorrectionEngine();
        if (engine != null && engine.isEnabled()) {
            engine.processTextureAbgr(pixels, width, height);
        }
    }

    private static int onGradeBiomeColor(int argb) {
        ColorCorrectionEngine engine = HyperionEngine.getInstance().getColorCorrectionEngine();
        if (engine != null && engine.isEnabled()) {
            return engine.gradeColorRgbInt(argb);
        }
        return argb;
    }
}
