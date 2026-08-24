package com.hyperion.optimizer.mixin;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.physics.FastRedstoneEngine;

public class MixinRedstoneWireBlock {
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
