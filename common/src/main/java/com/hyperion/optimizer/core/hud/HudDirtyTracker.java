package com.hyperion.optimizer.core.hud;

public class HudDirtyTracker {
    // Fix P3-1: Volatile fields for cross-thread visibility between network and render threads
    private volatile boolean dirty = true;

    // Tracked player telemetry
    private volatile float lastHealth = -1.0f;
    private volatile int lastHunger = -1;
    private volatile int lastArmor = -1;
    private volatile int lastAir = -1;
    private volatile int lastExperienceLevel = -1;
    private volatile float lastExperienceProgress = -1.0f;
    private volatile int lastSelectedSlot = -1;
    private volatile long lastChatUpdateTick = -1L;

    public synchronized boolean updateState(float health, int hunger, int armor, int air, int expLevel, float expProgress, int selectedSlot, long chatTick) {
        if (health != lastHealth || hunger != lastHunger || armor != lastArmor ||
            air != lastAir || expLevel != lastExperienceLevel || expProgress != lastExperienceProgress ||
            selectedSlot != lastSelectedSlot || chatTick != lastChatUpdateTick) {
            
            this.lastHealth = health;
            this.lastHunger = hunger;
            this.lastArmor = armor;
            this.lastAir = air;
            this.lastExperienceLevel = expLevel;
            this.lastExperienceProgress = expProgress;
            this.lastSelectedSlot = selectedSlot;
            this.lastChatUpdateTick = chatTick;
            this.dirty = true;
            return true;
        }
        return false;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clearDirty() {
        this.dirty = false;
    }
}
