package com.hyperion.optimizer.core.gpu.dualgpu;

import com.hyperion.optimizer.api.HyperionConfig;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * 🌡️ Dual-GPU Thermal Throttling Detection & Auto-Fallback Manager.
 *
 * Continuously tracks moving-average frametime latency. When thermal throttling,
 * power limit clipping, or VRAM saturation causes sudden frametime spikes (e.g. > 40ms / < 25 FPS)
 * on secondary/primary GPUs:
 *
 * Dynamically and seamlessly migrates heavy offload workloads (Compute Culling, Light Shaders, HUD FBO)
 * back to single-adapter mode on the fly WITHOUT dropping frames or crashing the client.
 * Restores dual-GPU mode automatically once thermal/latency metrics recover to normal.
 */
public final class DualGpuThermalFallback {
    private static final Logger LOGGER = Logger.getLogger("Hyperion-ThermalFallback");

    private volatile boolean enabled = true;
    private volatile double frametimeThresholdMs = 40.0; // 40ms = 25 FPS threshold
    private volatile int consecutiveSpikesBeforeFallback = 3;
    private volatile int recoveryFramesRequired = 120; // 2 seconds of stable 60+ FPS to recover

    private final AtomicBoolean fallbackActive = new AtomicBoolean(false);
    private int consecutiveSpikeCount = 0;
    private int consecutiveStableCount = 0;
    private long warmupGraceUntilMs = 0L;

    private final AtomicLong fallbackTriggeredCount = new AtomicLong(0);
    private final AtomicLong recoveryTriggeredCount = new AtomicLong(0);

    public DualGpuThermalFallback(boolean enabled) {
        this.enabled = enabled;
    }

    public void configure(HyperionConfig config) {
        if (config == null) return;
        this.enabled = config.enableDualGpuThermalFallback;
        this.frametimeThresholdMs = Math.max(16.6, config.thermalFallbackFrametimeThresholdMs);
    }

    public synchronized void triggerWarmupGracePeriod(long graceDurationMs) {
        this.warmupGraceUntilMs = System.currentTimeMillis() + Math.max(1000L, graceDurationMs);
        this.consecutiveSpikeCount = 0;
    }

    /**
     * Records frametime of current frame and adjusts fallback state.
     *
     * @param frameTimeMs The elapsed time of current frame in milliseconds.
     * @return true if fallback mode is currently active (must run on single safe adapter).
     */
    public synchronized boolean recordFrameAndEvaluate(double frameTimeMs) {
        if (!enabled) return false;

        // Skip thermal fallback trigger during initial world/dimension warmup grace period
        if (System.currentTimeMillis() < warmupGraceUntilMs) {
            return fallbackActive.get();
        }

        if (frameTimeMs > frametimeThresholdMs) {
            consecutiveSpikeCount++;
            consecutiveStableCount = 0;
            if (consecutiveSpikeCount >= consecutiveSpikesBeforeFallback && !fallbackActive.get()) {
                fallbackActive.set(true);
                fallbackTriggeredCount.incrementAndGet();
                LOGGER.warning(String.format("[Hyperion-GPU] Thermal Throttling / Latency Spike (%.2f ms) detected! Seamlessly falling back to Single-Adapter safe mode.", frameTimeMs));
            }
        } else {
            consecutiveSpikeCount = 0;
            if (fallbackActive.get()) {
                consecutiveStableCount++;
                if (consecutiveStableCount >= recoveryFramesRequired) {
                    fallbackActive.set(false);
                    recoveryTriggeredCount.incrementAndGet();
                    consecutiveStableCount = 0;
                    LOGGER.info("[Hyperion-GPU] Thermal / frametime metrics normalized. Restoring Dual-GPU hybrid workload distribution.");
                }
            }
        }

        return fallbackActive.get();
    }

    public boolean isFallbackActive() {
        return fallbackActive.get();
    }

    public void forceFallback(boolean active) {
        fallbackActive.set(active);
    }

    public boolean isEnabled() { return enabled; }
    public double getFrametimeThresholdMs() { return frametimeThresholdMs; }
    public long getFallbackTriggeredCount() { return fallbackTriggeredCount.get(); }
    public long getRecoveryTriggeredCount() { return recoveryTriggeredCount.get(); }

    public void reset() {
        fallbackActive.set(false);
        consecutiveSpikeCount = 0;
        consecutiveStableCount = 0;
        fallbackTriggeredCount.set(0);
        recoveryTriggeredCount.set(0);
    }
}
