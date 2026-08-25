package com.hyperion.optimizer.core.animation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * ⚡ Entity Capability & Animation Matrix Cache Engine (Inspired by ThreeTAG/Palladium).
 *
 * Provides high-speed caching for dynamic entity capabilities, superpower states,
 * and custom multi-bone animation matrices:
 * 1. Capability Primitive Cache: Avoids repeated reflection and NBT parsing during frame renders.
 * 2. Recycled Matrix Stack Pool: Eliminates object allocation when interpolating custom limb rotations and superpower auras.
 */
public final class PalladiumCapabilityCache {
    private volatile boolean enabled = true;

    // Cache: entityId -> packed capability attribute bits
    private final Map<Integer, Long> entityCapabilityMap = new ConcurrentHashMap<>(256);
    // Cache: entityId -> cached transform matrix (16 floats)
    private final Map<Integer, float[]> animationMatrixMap = new ConcurrentHashMap<>(256);

    private final LongAdder totalCapabilityLookupsCached = new LongAdder();
    private final LongAdder totalMatrixTransformationsReused = new LongAdder();

    public PalladiumCapabilityCache(boolean enabled) {
        this.enabled = enabled;
    }

    public void setCapability(int entityId, long packedCapabilityBits) {
        if (!enabled) return;
        entityCapabilityMap.put(entityId, packedCapabilityBits);
    }

    public long getCapability(int entityId, long defaultBits) {
        if (!enabled) return defaultBits;
        Long val = entityCapabilityMap.get(entityId);
        if (val != null) {
            totalCapabilityLookupsCached.increment();
            return val;
        }
        return defaultBits;
    }

    public void storeAnimationMatrix(int entityId, float[] matrix16) {
        if (!enabled || matrix16 == null || matrix16.length < 16) return;
        float[] dest = animationMatrixMap.computeIfAbsent(entityId, k -> new float[16]);
        System.arraycopy(matrix16, 0, dest, 0, 16);
    }

    public boolean getAnimationMatrix(int entityId, float[] outMatrix16) {
        if (!enabled || outMatrix16 == null || outMatrix16.length < 16) return false;
        float[] cached = animationMatrixMap.get(entityId);
        if (cached != null) {
            System.arraycopy(cached, 0, outMatrix16, 0, 16);
            totalMatrixTransformationsReused.increment();
            return true;
        }
        return false;
    }

    public void invalidateEntity(int entityId) {
        entityCapabilityMap.remove(entityId);
        animationMatrixMap.remove(entityId);
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public long getTotalCapabilityLookupsCached() { return totalCapabilityLookupsCached.sum(); }
    public long getTotalMatrixTransformationsReused() { return totalMatrixTransformationsReused.sum(); }

    public void reset() {
        entityCapabilityMap.clear();
        animationMatrixMap.clear();
        totalCapabilityLookupsCached.reset();
        totalMatrixTransformationsReused.reset();
    }
}
