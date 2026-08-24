package com.hyperion.optimizer.mixin;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.entity.ExperienceOrbMerger;

public class MixinExperienceOrbEntity {
    public static boolean shouldMergeOrbs(
            double x1, double y1, double z1, int value1,
            double x2, double y2, double z2, int value2) {
        ExperienceOrbMerger merger = HyperionEngine.getInstance().getXpMerger();
        if (merger != null && merger.isEnabled()) {
            return merger.shouldMergeOrbs(x1, y1, z1, value1, x2, y2, z2, value2);
        }
        return false;
    }

    public static int calculateMergedAge(int age1, int age2) {
        ExperienceOrbMerger merger = HyperionEngine.getInstance().getXpMerger();
        if (merger != null) {
            return merger.calculateMergedAge(age1, age2);
        }
        return Math.max(age1, age2);
    }
}
