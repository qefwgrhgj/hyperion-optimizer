package com.hyperion.optimizer.core.ai;

import com.hyperion.optimizer.api.HyperionConfig;
import java.util.concurrent.atomic.LongAdder;

/**
 * 🧟 Mob AI & Pathfinding Throttle Optimizer (Inspired by Mobtimizations - Corosus).
 *
 * Drastically reduces server/client CPU ticks consumed by entity artificial intelligence:
 * 1. Pathfinding Gate: Suppresses redundant A* path recalculations if entity is actively moving along a valid path.
 * 2. Hostile Hazard Bypass: Disables 3x3x3 continuous lava/cliff scanning for monsters (Zombies, Creepers, Skeletons).
 * 3. Distance-Adaptive Target Acquisition: Paces target checks (distant mobs scan once every 10–20 ticks instead of 1).
 * 4. Heavy Goal Stripping: Completely removes zombie turtle egg searches and throttles village raid scanning by 66%.
 */
public final class MobAiOptimizer {
    private volatile boolean enabled = true;
    private volatile boolean enableHostileHazardBypass = true;
    private volatile boolean enableTargetScanPacing = true;
    private volatile boolean enableZombieEggTaskBypass = true;
    private volatile boolean enableVillageRaidScanThrottle = true;

    private final LongAdder totalPathRecalculationsSuppressed = new LongAdder();
    private final LongAdder totalHazardScansBypassed = new LongAdder();
    private final LongAdder totalTargetChecksPaced = new LongAdder();

    public MobAiOptimizer(boolean enabled) {
        this.enabled = enabled;
    }

    public void configure(HyperionConfig config) {
        if (config == null) return;
        this.enabled = config.enablePathfindingCircuitBreaker || config.enableAsyncWorldTickDispatcher;
    }

    /**
     * Determines whether an entity should recalculate its pathfinding goal.
     *
     * @param hasExistingPath true if entity already has a path object
     * @param isPathStuck true if entity has made zero progress for >20 ticks
     * @param distanceToTarget distance to current target position
     * @return true if path recalculation should execute, false if it can be safely suppressed.
     */
    public boolean shouldRecalculatePath(boolean hasExistingPath, boolean isPathStuck, double distanceToTarget) {
        if (!enabled) return true;

        if (hasExistingPath && !isPathStuck && distanceToTarget > 2.0) {
            totalPathRecalculationsSuppressed.increment();
            return false; // Suppress redundant path recalculation
        }

        return true;
    }

    /**
     * Checks if hazard scanning (3x3x3 lava/drop check) should be evaluated for this entity.
     *
     * @param isHostileMonster true for Zombies, Skeletons, Creepers, etc.
     * @param isTamedPet true for dogs, cats, horses
     * @return true if hazard scanning should run, false if it can be skipped.
     */
    public boolean shouldPerformHazardScanning(boolean isHostileMonster, boolean isTamedPet) {
        if (!enabled || !enableHostileHazardBypass) return true;

        // Never disable hazard checks for player's pets
        if (isTamedPet) return true;

        if (isHostileMonster) {
            totalHazardScansBypassed.increment();
            return false; // Hostile mobs don't need expensive 3x3x3 hazard ray-tracing
        }

        return true;
    }

    /**
     * Calculates the tick interval for target acquisition based on distance to nearest player.
     */
    public int getTargetAcquisitionInterval(double distToNearestPlayerBlocks) {
        if (!enabled || !enableTargetScanPacing) return 1; // Vanilla every-tick check

        totalTargetChecksPaced.increment();
        if (distToNearestPlayerBlocks > 48.0) {
            return 20; // 1 check per second for distant mobs
        } else if (distToNearestPlayerBlocks > 24.0) {
            return 10; // 2 checks per second for medium range mobs
        } else if (distToNearestPlayerBlocks > 12.0) {
            return 4;  // 5 checks per second for near mobs
        } else {
            return 1;  // Immediate check for close-combat mobs
        }
    }

    /**
     * Checks if zombie turtle egg scanning is permitted.
     */
    public boolean isTurtleEggSearchPermitted() {
        if (!enabled || !enableZombieEggTaskBypass) return true;
        return false; // Fully disable heavy turtle egg scans
    }

    /**
     * Checks if a village raid scan tick should execute (throttled to 1/3 rate).
     */
    public boolean shouldExecuteVillageRaidScan(int tickCount) {
        if (!enabled || !enableVillageRaidScanThrottle) return true;
        return (tickCount % 3) == 0; // Throttle to 33% frequency
    }

    public boolean isEnabled() { return enabled; }
    public boolean isHostileHazardBypassEnabled() { return enableHostileHazardBypass; }
    public boolean isTargetScanPacingEnabled() { return enableTargetScanPacing; }
    public boolean isZombieEggTaskBypassEnabled() { return enableZombieEggTaskBypass; }

    public long getTotalPathRecalculationsSuppressed() { return totalPathRecalculationsSuppressed.sum(); }
    public long getTotalHazardScansBypassed() { return totalHazardScansBypassed.sum(); }
    public long getTotalTargetChecksPaced() { return totalTargetChecksPaced.sum(); }

    public void reset() {
        totalPathRecalculationsSuppressed.reset();
        totalHazardScansBypassed.reset();
        totalTargetChecksPaced.reset();
    }
}
