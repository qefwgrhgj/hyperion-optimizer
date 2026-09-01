package com.hyperion.optimizer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.physics.SleepingHopperManager;

@Mixin(targets = {"net.minecraft.world.level.block.entity.HopperBlockEntity", "net.minecraft.class_2615", "net.minecraft.tileentity.HopperTileEntity"})
public class MixinHopperBlockEntity {
    @Inject(method = "pushAndPull", at = @At("HEAD"))
    private static void onPushAndPull(CallbackInfo ci) {
        SleepingHopperManager manager = HyperionEngine.getInstance().getHopperManager();
        if (manager != null && manager.isEnabled()) {
            // Sleeping hopper early evaluation
        }
    }

    public static boolean isHopperSleeping(long packedPos, long currentTick) {
        SleepingHopperManager manager = HyperionEngine.getInstance().getHopperManager();
        if (manager != null && manager.isEnabled()) {
            return manager.isHopperSleeping(packedPos, currentTick);
        }
        return false;
    }

    public static void putHopperToSleep(long packedPos, long currentTick, int sleepDurationTicks) {
        SleepingHopperManager manager = HyperionEngine.getInstance().getHopperManager();
        if (manager != null && manager.isEnabled()) {
            manager.putToSleep(packedPos, currentTick, sleepDurationTicks);
        }
    }
}
