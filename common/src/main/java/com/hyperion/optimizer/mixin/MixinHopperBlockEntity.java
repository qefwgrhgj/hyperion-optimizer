package com.hyperion.optimizer.mixin;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.physics.SleepingHopperManager;

public class MixinHopperBlockEntity {
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
