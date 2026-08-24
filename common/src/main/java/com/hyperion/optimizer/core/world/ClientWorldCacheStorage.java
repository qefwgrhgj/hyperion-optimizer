package com.hyperion.optimizer.core.world;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClientWorldCacheStorage {
    private final boolean enabled;
    private final int maxCapacity;

    // Fix P2-1: High-throughput striped LRU segments to eliminate global mutex contention
    private final Segment[] segments;

    private static final class Segment {
        final int cap;
        final LinkedHashMap<Long, byte[]> map;
        final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        Segment(int cap) {
            this.cap = cap;
            this.map = new LinkedHashMap<Long, byte[]>(cap, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, byte[]> eldest) {
                    return size() > Segment.this.cap;
                }
            };
        }
    }

    public ClientWorldCacheStorage(boolean enabled) {
        this(enabled, 2048); // Default bounded to 2048 active chunks in RAM
    }

    public ClientWorldCacheStorage(boolean enabled, int maxCapacity) {
        this.enabled = enabled;
        this.maxCapacity = Math.max(1, maxCapacity);
        int numSegments = Math.min(16, Math.max(1, maxCapacity));
        int segCapacity = Math.max(1, (maxCapacity + numSegments - 1) / numSegments);
        this.segments = new Segment[numSegments];
        for (int i = 0; i < numSegments; i++) {
            this.segments[i] = new Segment(segCapacity);
        }
    }

    private Segment getSegment(long pos) {
        int hash = (int) (pos ^ (pos >>> 32));
        hash = hash ^ (hash >>> 16);
        int index = (hash & 0x7FFFFFFF) % segments.length;
        return segments[index];
    }

    public static long packChunkPos(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    public void storeChunk(int chunkX, int chunkZ, byte[] serializedChunkNbt) {
        if (!enabled || serializedChunkNbt == null) return;
        long pos = packChunkPos(chunkX, chunkZ);
        Segment seg = getSegment(pos);
        seg.lock.writeLock().lock();
        try {
            seg.map.put(pos, serializedChunkNbt);
        } finally {
            seg.lock.writeLock().unlock();
        }
    }

    public byte[] loadChunk(int chunkX, int chunkZ) {
        if (!enabled) return null;
        long pos = packChunkPos(chunkX, chunkZ);
        Segment seg = getSegment(pos);
        seg.lock.writeLock().lock();
        try {
            return seg.map.get(pos);
        } finally {
            seg.lock.writeLock().unlock();
        }
    }

    public boolean hasChunk(int chunkX, int chunkZ) {
        if (!enabled) return false;
        long pos = packChunkPos(chunkX, chunkZ);
        Segment seg = getSegment(pos);
        seg.lock.readLock().lock();
        try {
            return seg.map.containsKey(pos);
        } finally {
            seg.lock.readLock().unlock();
        }
    }

    // Fix P2-3: Evict specific chunk from cache upon server chunk update / unload
    public void invalidateChunk(int chunkX, int chunkZ) {
        if (!enabled) return;
        long pos = packChunkPos(chunkX, chunkZ);
        Segment seg = getSegment(pos);
        seg.lock.writeLock().lock();
        try {
            seg.map.remove(pos);
        } finally {
            seg.lock.writeLock().unlock();
        }
    }

    public int getCachedChunkCount() {
        int total = 0;
        for (Segment seg : segments) {
            seg.lock.readLock().lock();
            try {
                total += seg.map.size();
            } finally {
                seg.lock.readLock().unlock();
            }
        }
        return total;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void clear() {
        for (Segment seg : segments) {
            seg.lock.writeLock().lock();
            try {
                seg.map.clear();
            } finally {
                seg.lock.writeLock().unlock();
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
