package com.hyperion.optimizer.core.world;

import java.util.concurrent.ConcurrentHashMap;

public class FakeChunkManager {
    private final int maxViewDistance;
    private final ClientWorldCacheStorage storage;
    // Set of currently active virtual fake chunks loaded for pure 3D rendering
    private final ConcurrentHashMap<Long, Boolean> activeFakeChunks = new ConcurrentHashMap<>();

    public FakeChunkManager(int maxViewDistance, ClientWorldCacheStorage storage) {
        this.maxViewDistance = maxViewDistance;
        this.storage = storage;
    }

    public boolean isFakeChunk(int chunkX, int chunkZ) {
        return activeFakeChunks.containsKey(ClientWorldCacheStorage.packChunkPos(chunkX, chunkZ));
    }

    public void registerFakeChunk(int chunkX, int chunkZ) {
        activeFakeChunks.put(ClientWorldCacheStorage.packChunkPos(chunkX, chunkZ), Boolean.TRUE);
    }

    public void unregisterFakeChunk(int chunkX, int chunkZ) {
        activeFakeChunks.remove(ClientWorldCacheStorage.packChunkPos(chunkX, chunkZ));
    }

    // Fix P0-2: Atomic invalidation upon real server chunk arrival to prevent Z-fighting and mesh duplication
    public boolean invalidateOnRealChunkArrived(int chunkX, int chunkZ) {
        long packed = ClientWorldCacheStorage.packChunkPos(chunkX, chunkZ);
        return activeFakeChunks.remove(packed) != null;
    }

    public int getMaxViewDistance() {
        return maxViewDistance;
    }

    public ClientWorldCacheStorage getStorage() {
        return storage;
    }

    // Fix P1-1: Clear fake chunks when changing dimensions (Overworld <-> Nether <-> End) to prevent ghost mesh bleed
    public void clearOnDimensionChange() {
        activeFakeChunks.clear();
    }

    public void clear() {
        activeFakeChunks.clear();
    }
}
