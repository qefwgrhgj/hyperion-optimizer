package com.hyperion.optimizer.core.physics;

import java.util.concurrent.ConcurrentHashMap;

public class SleepingHopperManager {
    private final boolean enabled;
    // Map of packed BlockPos to absolute target wake-up tick
    private final ConcurrentHashMap<Long, Long> sleepingHoppers = new ConcurrentHashMap<>();
    private long lastCleanupTick = 0;
    private static final long CLEANUP_INTERVAL_TICKS = 600L; // Clean every 30 seconds

    public SleepingHopperManager(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isHopperSleeping(long packedPos, long currentServerTick) {
        if (!enabled) return false;

        // Auto-prune expired sleeping hoppers periodically with P2-3 tick rollback defense
        if (currentServerTick < lastCleanupTick || (currentServerTick - lastCleanupTick) > CLEANUP_INTERVAL_TICKS) {
            this.lastCleanupTick = currentServerTick;
            sleepingHoppers.entrySet().removeIf(entry -> currentServerTick >= entry.getValue() || entry.getValue() < currentServerTick);
        }

        Long sleepUntilTick = sleepingHoppers.get(packedPos);
        if (sleepUntilTick == null) return false;

        if (currentServerTick < sleepUntilTick) {
            return true; // Still sleeping
        } else {
            sleepingHoppers.remove(packedPos);
            return false; // Time to wake up
        }
    }

    public void putToSleep(long packedPos, long currentServerTick, int sleepDurationTicks) {
        if (!enabled || sleepDurationTicks <= 0) return;
        // Fix P2-3: Prevent 64-bit integer overflow when server ticks approach Long.MAX_VALUE
        long sleepUntil = (currentServerTick > Long.MAX_VALUE - (long) sleepDurationTicks)
                          ? Long.MAX_VALUE
                          : (currentServerTick + (long) sleepDurationTicks);
        sleepingHoppers.put(packedPos, sleepUntil);
    }

    public void wakeUp(long packedPos) {
        if (!enabled) return;
        sleepingHoppers.remove(packedPos);
    }

    public int getSleepingCount() {
        return sleepingHoppers.size();
    }

    public void clear() {
        sleepingHoppers.clear();
    }

    public boolean isEnabled() {
        return enabled;
    }
}
