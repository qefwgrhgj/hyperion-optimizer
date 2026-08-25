package com.hyperion.optimizer.core.entity;

import java.util.concurrent.ConcurrentHashMap;

public class StaticChestMeshBaker {
    private final boolean enabled;
    // Track chests that are currently open (and thus need dynamic 3D lid animation)
    private final ConcurrentHashMap<Long, Boolean> openChests = new ConcurrentHashMap<>();

    public StaticChestMeshBaker(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean shouldRenderAsStaticBlock(long packedBlockPos) {
        if (!enabled) return false;
        // If chest is closed (not in openChests map), render as static solid block in chunk mesh
        return !openChests.containsKey(packedBlockPos);
    }

    public void setChestOpenState(long packedBlockPos, boolean isOpen) {
        if (!enabled) return;
        if (isOpen) {
            openChests.put(packedBlockPos, Boolean.TRUE);
        } else {
            openChests.remove(packedBlockPos);
        }
    }

    // Fix P1-1: Synchronized registration for double chests (LEFT and RIGHT halves)
    public void setDoubleChestOpenState(long packedBlockPos1, long packedBlockPos2, boolean isOpen) {
        setChestOpenState(packedBlockPos1, isOpen);
        setChestOpenState(packedBlockPos2, isOpen);
    }

    public boolean isChestOpen(long packedBlockPos) {
        return openChests.containsKey(packedBlockPos);
    }

    // Fix P2-2: Prune open chest state on chunk unload to prevent memory leaks in long sessions
    public void invalidateChunk(int chunkX, int chunkZ) {
        if (!enabled || openChests.isEmpty()) return;
        int minBlockX = chunkX << 4;
        int maxBlockX = minBlockX + 15;
        int minBlockZ = chunkZ << 4;
        int maxBlockZ = minBlockZ + 15;
        openChests.keySet().removeIf(pos -> {
            int x = com.hyperion.optimizer.core.memory.PrimitiveVectorPool.unpackX(pos);
            int z = com.hyperion.optimizer.core.memory.PrimitiveVectorPool.unpackZ(pos);
            return x >= minBlockX && x <= maxBlockX && z >= minBlockZ && z <= maxBlockZ;
        });
    }

    public void clear() {
        openChests.clear();
    }

    public boolean isEnabled() {
        return enabled;
    }
}
