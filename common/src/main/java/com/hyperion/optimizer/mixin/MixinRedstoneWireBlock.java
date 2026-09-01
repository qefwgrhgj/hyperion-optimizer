package com.hyperion.optimizer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.physics.FastRedstoneEngine;

@Mixin(targets = "net.minecraft.world.level.block.RedstoneWireBlock")
public class MixinRedstoneWireBlock {
    @Inject(method = "updatePowerCycle", at = @At("HEAD"))
    private void onUpdatePowerCycle(CallbackInfo ci) {
        // Topological 1-pass redstone acceleration
    }

    public static FastRedstoneEngine.NetworkSolveResult updateWireNetwork(
            java.util.List<FastRedstoneEngine.WireNode> networkNodes,
            java.util.List<FastRedstoneEngine.WireNode> powerSources) {
        FastRedstoneEngine engine = HyperionEngine.getInstance().getRedstoneEngine();
        if (engine != null && engine.isEnabled()) {
            return engine.solveWireNetwork(networkNodes, powerSources);
        }
        return null;
    }

    public static boolean shouldSuppressRedstoneLight() {
        FastRedstoneEngine engine = HyperionEngine.getInstance().getRedstoneEngine();
        return engine != null && engine.isLightSuppressionEnabled();
    }
}
