package com.hyperion.optimizer.core.hud;

public class DecoupledHudManager {
    private final boolean enabled;
    private final int targetFps;
    private final boolean dynamicRefresh;
    private final HudDirtyTracker dirtyTracker;

    private long lastRenderTimeNanos = 0;
    private final long frameIntervalNanos;
    private boolean isFramebufferValid = false;

    public DecoupledHudManager(boolean enabled, int targetFps, boolean dynamicRefresh) {
        this.enabled = enabled;
        this.targetFps = Math.max(15, Math.min(360, targetFps));
        this.dynamicRefresh = dynamicRefresh;
        this.dirtyTracker = new HudDirtyTracker();
        this.frameIntervalNanos = 1_000_000_000L / this.targetFps;
    }

    public void onResolutionChanged() {
        this.isFramebufferValid = false;
        if (dirtyTracker != null) {
            dirtyTracker.markDirty();
        }
    }

    public void onPlayerRespawn() {
        this.isFramebufferValid = false;
        this.lastRenderTimeNanos = 0L;
        if (dirtyTracker != null) {
            dirtyTracker.clearDirty();
        }
    }

    public boolean shouldRepaintHud(long currentTimeNanos) {
        if (!enabled) return true;

        if (!isFramebufferValid) {
            isFramebufferValid = true;
            lastRenderTimeNanos = currentTimeNanos;
            dirtyTracker.clearDirty();
            return true;
        }

        // Check if game state changed (health, hunger, chat, hotbar, armor)
        if (dynamicRefresh && dirtyTracker.isDirty()) {
            dirtyTracker.clearDirty();
            lastRenderTimeNanos = currentTimeNanos;
            return true;
        }

        // Framerate pacing gate with P1-2 rollback defense (NTP / manual clock adjustments)
        if (currentTimeNanos < lastRenderTimeNanos || (currentTimeNanos - lastRenderTimeNanos) >= frameIntervalNanos) {
            lastRenderTimeNanos = currentTimeNanos;
            return true;
        }

        return false; // Skip redundant HUD immediate mode redraw
    }

    public void invalidateBuffer() {
        this.isFramebufferValid = false;
    }

    public HudDirtyTracker getDirtyTracker() {
        return dirtyTracker;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
