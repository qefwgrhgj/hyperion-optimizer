package com.hyperion.optimizer.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 💡 Hyperion Modern Lightmap Extractor Full Illumination Engine (Minecraft 26.2+).
 * Enforces maximum brightness and night vision effect intensity in the LightmapRenderState UBO.
 */
@Mixin(targets = {
    "net.minecraft.client.renderer.LightmapRenderStateExtractor",
    "net.minecraft.class_10850"
})
@Environment(EnvType.CLIENT)
public class MixinLightmapRenderStateExtractor {

    @Inject(method = "extract", at = @At("RETURN"), require = 0)
    private void onExtract(LightmapRenderState state, float partialTick, CallbackInfo ci) {
        if (state != null) {
            state.brightness = 1.0f;
            state.nightVisionEffectIntensity = 1.0f;
        }
    }
}
