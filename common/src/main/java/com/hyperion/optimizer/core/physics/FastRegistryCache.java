package com.hyperion.optimizer.core.physics;

import java.util.concurrent.ConcurrentHashMap;

/**
 * High-Speed Item & Tag Registry Membership Cache.
 * Provides O(1) tag and recipe checks, eliminating expensive deep registry scans.
 */
public class FastRegistryCache {
    private static final int MAX_REGISTRY_CACHE_CAPACITY = 32768;
    private final boolean enabled;
    // Map of (TagIdHashCode ^ ItemId) -> Boolean
    private final ConcurrentHashMap<Long, Boolean> tagMembershipCache = new ConcurrentHashMap<>();

    public FastRegistryCache(boolean enabled) {
        this.enabled = enabled;
    }

    private static long packTagKey(int tagIdHash, int itemId) {
        return (((long) tagIdHash) << 32) | (itemId & 0xFFFFFFFFL);
    }

    public boolean isItemInTag(int tagIdHash, int itemId, java.util.function.BiPredicate<Integer, Integer> fallbackMatcher) {
        if (!enabled) {
            return fallbackMatcher.test(tagIdHash, itemId);
        }
        if (tagMembershipCache.size() >= MAX_REGISTRY_CACHE_CAPACITY) {
            tagMembershipCache.clear();
        }
        long key = packTagKey(tagIdHash, itemId);
        Boolean cached = tagMembershipCache.get(key);
        if (cached != null) {
            return cached.booleanValue();
        }

        boolean match = fallbackMatcher.test(tagIdHash, itemId);
        tagMembershipCache.put(key, match ? Boolean.TRUE : Boolean.FALSE);
        return match;
    }

    public void invalidate() {
        tagMembershipCache.clear();
    }

    public int getCachedCount() {
        return tagMembershipCache.size();
    }

    public boolean isEnabled() {
        return enabled;
    }
}
