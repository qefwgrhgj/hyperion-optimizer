package com.hyperion.optimizer.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.entity.StaticChestMeshBaker;

@Mixin(targets = {"net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher", "net.minecraft.class_824", "net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher"})
@Environment(EnvType.CLIENT)
public class MixinBlockEntityRenderDispatcher {
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderBlockEntity(CallbackInfo ci) {
        // Fast block entity culling check
    }

    private static boolean canBakeChestToStaticMesh(long packedPos) {
        StaticChestMeshBaker baker = HyperionEngine.getInstance().getChestBaker();
        if (baker != null && baker.isEnabled()) {
            return baker.shouldRenderAsStaticBlock(packedPos);
        }
        return false;
    }

    private static boolean shouldCullBlockEntity(double camX, double camY, double camZ,
                                                double beX, double beY, double beZ,
                                                boolean isOccluded) {
        com.hyperion.optimizer.core.render.FpsStabilizerEngine stabilizer = HyperionEngine.getInstance().getFpsStabilizer();
        if (stabilizer != null && stabilizer.isEnabled()) {
            return stabilizer.shouldCullBlockEntity(camX, camY, camZ, beX, beY, beZ, isOccluded);
        }
        return false;
    }
}
