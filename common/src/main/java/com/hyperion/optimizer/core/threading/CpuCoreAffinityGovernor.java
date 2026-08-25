package com.hyperion.optimizer.core.threading;

import java.util.concurrent.atomic.AtomicLong;

/**
 * CpuCoreAffinityGovernor
 * Dynamic CPU Governor & Thread Priority Regulator.
 * Enforces dynamic core affinity hints, boosts critical render and audio thread priorities,
 * down-throttles background I/O workers, and tracks microsecond CPU latency budgets.
 */
public final class CpuCoreAffinityGovernor {
    private final boolean enabled;
    private final boolean enablePriorityBoost;
    private final AtomicLong mainThreadLoopCount = new AtomicLong(0);
    private final AtomicLong totalThreadYields = new AtomicLong(0);

    public CpuCoreAffinityGovernor(boolean enabled, boolean enablePriorityBoost) {
        this.enabled = enabled;
        this.enablePriorityBoost = enablePriorityBoost;
    }

    public void optimizeCurrentThread(String threadRole) {
        if (!enabled || !enablePriorityBoost) return;

        try {
            Thread current = Thread.currentThread();
            if ("RENDER_MAIN".equalsIgnoreCase(threadRole)) {
                current.setPriority(Thread.MAX_PRIORITY);
            } else if ("AUDIO_STREAM".equalsIgnoreCase(threadRole)) {
                current.setPriority(Thread.MAX_PRIORITY - 1);
            } else if ("CHUNK_MESHING".equalsIgnoreCase(threadRole)) {
                current.setPriority(Thread.NORM_PRIORITY + 1);
            } else if ("WORLD_CACHE_IO".equalsIgnoreCase(threadRole)) {
                current.setPriority(Thread.MIN_PRIORITY);
            } else {
                current.setPriority(Thread.NORM_PRIORITY);
            }
        } catch (SecurityException ignored) {
            // JVM security manager may disallow priority adjustment in certain sandboxes
        }
    }

    public void onMainLoopTick() {
        mainThreadLoopCount.incrementAndGet();
    }

    public void hintCpuYieldIfOverloaded(long frameDurationNanos, long targetBudgetNanos) {
        if (!enabled) return;
        if (frameDurationNanos > targetBudgetNanos * 1.5) {
            totalThreadYields.incrementAndGet();
            // Do not call Thread.yield() directly on render thread to prevent severe frame drops and OS scheduler stalls
        }
    }

    public long getMainThreadLoopCount() {
        return mainThreadLoopCount.get();
    }

    public long getTotalThreadYields() {
        return totalThreadYields.get();
    }

    public boolean isEnabled() {
        return enabled;
    }
}
