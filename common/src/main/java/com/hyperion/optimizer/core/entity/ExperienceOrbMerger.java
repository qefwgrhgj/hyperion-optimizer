package com.hyperion.optimizer.core.entity;

public class ExperienceOrbMerger {
    private final boolean enabled;
    private final double mergeRadiusSq;
    private final int maxOrbCapacity;

    public ExperienceOrbMerger(boolean enabled, double mergeRadius, int maxOrbCapacity) {
        this.enabled = enabled;
        this.mergeRadiusSq = mergeRadius * mergeRadius;
        this.maxOrbCapacity = maxOrbCapacity;
    }

    public boolean shouldMergeOrbs(double orb1X, double orb1Y, double orb1Z, int orb1Value,
                                   double orb2X, double orb2Y, double orb2Z, int orb2Value) {
        if (!enabled) return false;

        // Fix P0-1: Prevent 32-bit integer overflow exploit by using safe 64-bit addition and sanity check
        if (orb1Value <= 0 || orb2Value <= 0 || ((long) orb1Value + (long) orb2Value) > (long) maxOrbCapacity) {
            return false;
        }

        double dx = orb1X - orb2X;
        double dy = orb1Y - orb2Y;
        double dz = orb1Z - orb2Z;
        return (dx * dx + dy * dy + dz * dz) <= mergeRadiusSq;
    }

    // Fix P1-3: Non-negative age clamp prevents entity timer corruption
    public int calculateMergedAge(int age1, int age2) {
        return Math.max(0, Math.max(age1, age2));
    }

    public boolean isEnabled() {
        return enabled;
    }
}
