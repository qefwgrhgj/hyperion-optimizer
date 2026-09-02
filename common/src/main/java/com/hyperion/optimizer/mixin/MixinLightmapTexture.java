package com.hyperion.optimizer.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 💡 Hyperion Full Illumination & Anti-Black-Block Engine.
 * Overrides getBrightness to guarantee 100% full lighting across all light levels (0..15).
 * Completely eliminates black surfaces, shadow glitches, and unlit terrain.
 */
@Mixin(targets = {
    "net.minecraft.client.renderer.LightTexture",
    "net.minecraft.client.renderer.Lightmap",
    "net.minecraft.class_765"
})
@Environment(EnvType.CLIENT)
public class MixinLightmapTexture {

    @Inject(method = "getBrightness", at = @At("HEAD"), cancellable = true, require = 0)
    private static void onGetBrightness(DimensionType dimensionType, int lightLevel, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(1.0f);
    }
}
