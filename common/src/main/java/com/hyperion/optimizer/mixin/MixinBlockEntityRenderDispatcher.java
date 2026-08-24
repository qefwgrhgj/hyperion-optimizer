package com.hyperion.optimizer.mixin;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.entity.StaticChestMeshBaker;

public class MixinBlockEntityRenderDispatcher {
    public static boolean canBakeChestToStaticMesh(long packedPos) {
        StaticChestMeshBaker baker = HyperionEngine.getInstance().getChestBaker();
        if (baker != null && baker.isEnabled()) {
            return baker.shouldRenderAsStaticBlock(packedPos);
        }
        return false;
    }

    public static boolean shouldCullBlockEntity(double camX, double camY, double camZ,
                                                double beX, double beY, double beZ,
                                                boolean isOccluded) {
        com.hyperion.optimizer.core.render.FpsStabilizerEngine stabilizer = HyperionEngine.getInstance().getFpsStabilizer();
        if (stabilizer != null && stabilizer.isEnabled()) {
            return stabilizer.shouldCullBlockEntity(camX, camY, camZ, beX, beY, beZ, isOccluded);
        }
        return false;
    }
}
