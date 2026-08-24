package com.hyperion.optimizer.mixin;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.physics.PathfindingCircuitBreaker;

public class MixinPathNavigation {
    public static boolean canMobSearchPath(int entityId, long currentTick) {
        PathfindingCircuitBreaker breaker = HyperionEngine.getInstance().getPathCircuitBreaker();
        if (breaker != null && breaker.isEnabled()) {
            return breaker.canEntitySearchPath(entityId, currentTick);
        }
        return true;
    }

    public static void reportPathfindingOutcome(int entityId, boolean success, long currentTick) {
        PathfindingCircuitBreaker breaker = HyperionEngine.getInstance().getPathCircuitBreaker();
        if (breaker != null && breaker.isEnabled()) {
            breaker.recordPathfindingResult(entityId, success, currentTick);
        }
    }
}
