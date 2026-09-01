package com.hyperion.optimizer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.entity.ExperienceOrbMerger;

@Mixin(targets = {"net.minecraft.world.entity.ExperienceOrb", "net.minecraft.class_1303", "net.minecraft.entity.item.ExperienceOrbEntity"})
public class MixinExperienceOrbEntity {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ExperienceOrbMerger merger = HyperionEngine.getInstance().getXpMerger();
        if (merger != null && merger.isEnabled()) {
            // Evaluated during entity tick
        }
    }

    private static boolean shouldMergeOrbs(
            double x1, double y1, double z1, int value1,
            double x2, double y2, double z2, int value2) {
        ExperienceOrbMerger merger = HyperionEngine.getInstance().getXpMerger();
        if (merger != null && merger.isEnabled()) {
            return merger.shouldMergeOrbs(x1, y1, z1, value1, x2, y2, z2, value2);
        }
        return false;
    }

    private static int calculateMergedAge(int age1, int age2) {
        ExperienceOrbMerger merger = HyperionEngine.getInstance().getXpMerger();
        if (merger != null) {
            return merger.calculateMergedAge(age1, age2);
        }
        return Math.max(age1, age2);
    }
}
