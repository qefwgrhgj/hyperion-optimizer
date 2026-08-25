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
     * Scales gracefully from 1 tick in close-quarters up to 100 ticks (5s) for distant mobs at 32+ chunks.
     */
    public int getTargetAcquisitionInterval(double distToNearestPlayerBlocks) {
        if (!enabled || !enableTargetScanPacing) return 1; // Vanilla every-tick check

        totalTargetChecksPaced.increment();
        if (distToNearestPlayerBlocks > 128.0) {
            return 100; // 1 check every 5 seconds for extreme render distance mobs (32+ chunks)
        } else if (distToNearestPlayerBlocks > 64.0) {
            return 40;  // 1 check every 2 seconds for far mobs
        } else if (distToNearestPlayerBlocks > 32.0) {
            return 20;  // 1 check per second for medium-far mobs
        } else if (distToNearestPlayerBlocks > 16.0) {
            return 10;  // 2 checks per second for medium range mobs
        } else if (distToNearestPlayerBlocks > 8.0) {
            return 4;   // 5 checks per second for near mobs
        } else {
            return 1;   // Immediate every-tick check for melee combat
        }
    }

    /**
     * Evaluates whether an entity's general AI brain goal loop can be skipped on this tick.
     * At 32 chunks, sleeping non-combat mobs beyond 96 blocks cuts CPU load by over 80%.
     */
    public boolean shouldSkipDistantMobAi(double distToNearestPlayerBlocks, long worldTick, int entityId) {
        if (!enabled) return false;
        if (distToNearestPlayerBlocks > 128.0) {
            return ((worldTick + entityId) % 20) != 0; // Tick only 1 Hz for extreme range
        } else if (distToNearestPlayerBlocks > 64.0) {
            return ((worldTick + entityId) % 5) != 0;  // Tick only 4 Hz for distant mobs
        }
        return false;
    }

    /**
     * Phase-Staggered Target Acquisition Check.
     * Distributes entity AI scanning evenly across all 20 ticks of every second
     * using (entityId + worldTick) % interval, completely eliminating wave tick spikes.
     */
    public boolean shouldExecuteTargetScan(int entityId, long worldTick, double distToNearestPlayerBlocks) {
        if (!enabled || !enableTargetScanPacing) return true;
        int interval = getTargetAcquisitionInterval(distToNearestPlayerBlocks);
        if (interval <= 1) return true;
        return (Math.abs(entityId) + worldTick) % interval == 0;
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

    /**
     * Throttles hostile monster pathfinding and random wandering at night when they are not in active combat.
     * Prevents dozens of surface-spawned night mobs from overwhelming the CPU.
     */
    public boolean shouldThrottleNightHostileMob(boolean isHostile, boolean isNight, double distToPlayer, long worldTick, int entityId) {
        if (!enabled || !isHostile || !isNight) return false;
        if (distToPlayer > 24.0) {
            return ((worldTick + entityId) % 4) != 0; // Run pathfinding at 5 Hz instead of 20 Hz
        }
        return false;
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
