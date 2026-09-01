package com.hyperion.optimizer.core.physics;

import com.hyperion.optimizer.core.memory.PrimitiveVectorPool;

import java.util.ArrayList;
import java.util.List;

/**
 * High-Performance Fast Redstone Optimization Engine (Sovereign High-Performance Topological Redstone Architecture).
 * Replaces vanilla Minecraft's exponential-complexity BFS wire propagation with a 1-pass
 * topological network solver, neighbor update batching, light suppression, comparator signal
 * caching, and hopper container occlusion checks.
 */
public class FastRedstoneEngine {
    public static class WireNode {
        public final int x, y, z;
        public final long packedPos;
        public int currentPower;
        public int targetPower;
        public boolean isSource;

        public WireNode(int x, int y, int z, int currentPower, boolean isSource) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.packedPos = PrimitiveVectorPool.packBlockPos(x, y, z);
            this.currentPower = currentPower;
            this.targetPower = isSource ? currentPower : 0;
            this.isSource = isSource;
        }
    }

    public static class NetworkSolveResult {
        public final int updatedWireCount;
        public final long[] notifiedNeighborPositions;
        public final int notifiedNeighborCount;

        public NetworkSolveResult(int updatedWireCount, long[] notifiedNeighborPositions, int notifiedNeighborCount) {
            this.updatedWireCount = updatedWireCount;
            this.notifiedNeighborPositions = notifiedNeighborPositions;
            this.notifiedNeighborCount = notifiedNeighborCount;
        }
    }

    private final boolean enabled;
    private final boolean lightSuppressionEnabled;
    private final boolean comparatorCachingEnabled;
    private final boolean hopperOcclusionFastPathEnabled;
    private final boolean batchUpdatesEnabled;

    public FastRedstoneEngine(boolean enabled,
                              boolean lightSuppressionEnabled,
                              boolean comparatorCachingEnabled,
                              boolean hopperOcclusionFastPathEnabled,
                              boolean batchUpdatesEnabled) {
        this.enabled = enabled;
        this.lightSuppressionEnabled = lightSuppressionEnabled;
        this.comparatorCachingEnabled = comparatorCachingEnabled;
        this.hopperOcclusionFastPathEnabled = hopperOcclusionFastPathEnabled;
        this.batchUpdatesEnabled = batchUpdatesEnabled;
    }

    /**
     * Solves power propagation across a connected network of redstone wire nodes in a single topological pass.
     * Replaces vanilla's recursive 42-updates-per-wire cascade with Alternate Current's 1-pass solver.
     */
    public NetworkSolveResult solveWireNetwork(List<WireNode> networkWires, List<WireNode> powerSources) {
        if (!enabled || networkWires == null || networkWires.isEmpty()) {
            return new NetworkSolveResult(0, new long[0], 0);
        }

        // Initialize target power to 0 for non-sources
        for (WireNode node : networkWires) {
            if (!node.isSource) {
                node.targetPower = 0;
            }
        }

        // 1-Pass Topological Power Propagation Queue
        List<WireNode> queue = new ArrayList<>();
        if (powerSources != null) {
            for (WireNode source : powerSources) {
                if (source.targetPower > 0) {
                    queue.add(source);
                }
            }
        }

        // Build O(1) spatial index for instant neighbor wire lookup
        int rawNodeCap = Math.max(64, networkWires.size() * 2);
        int nodeTableCap = 1 << (32 - Integer.numberOfLeadingZeros(rawNodeCap - 1));
        long[] nodeKeys = new long[nodeTableCap];
        WireNode[] nodeValues = new WireNode[nodeTableCap];
        int nodeMask = nodeTableCap - 1;

        for (WireNode wire : networkWires) {
            long packed = wire.packedPos;
            int slot = ((int) (packed ^ (packed >>> 32))) & nodeMask;
            while (nodeKeys[slot] != 0 && nodeKeys[slot] != packed) {
                slot = (slot + 1) & nodeMask;
            }
            nodeKeys[slot] = packed;
            nodeValues[slot] = wire;
        }

        int[][] neighborOffsets = {
            {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1},
            {1, 1, 0}, {-1, 1, 0}, {0, 1, 1}, {0, 1, -1},
            {1, -1, 0}, {-1, -1, 0}, {0, -1, 1}, {0, -1, -1}
        };

        int queueIndex = 0;
        while (queueIndex < queue.size()) {
            WireNode current = queue.get(queueIndex++);
            int nextPower = current.targetPower - 1;
            if (nextPower <= 0) continue;

            for (int[] off : neighborOffsets) {
                long targetPacked = PrimitiveVectorPool.packBlockPos(current.x + off[0], current.y + off[1], current.z + off[2]);
                int slot = ((int) (targetPacked ^ (targetPacked >>> 32))) & nodeMask;
                int probes = 0;
                while (nodeKeys[slot] != 0 && nodeKeys[slot] != targetPacked && probes++ < 64) {
                    slot = (slot + 1) & nodeMask;
                }
                if (nodeKeys[slot] == targetPacked) {
                    WireNode neighbor = nodeValues[slot];
                    if (neighbor != null && neighbor != current) {
                        if (nextPower > neighbor.targetPower) {
                            neighbor.targetPower = nextPower;
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        // Collect changed wires and build deduplicated neighbor update list
        int changedCount = 0;
        int maxCapacity = networkWires.size() * 6;
        long[] neighborBuffer = new long[maxCapacity];
        int neighborCount = 0;

        // Fix P0-1: Ensure hash table capacity is strictly a power of 2 for bitwise masking
        int rawCap = Math.max(64, maxCapacity * 2);
        int tableCap = 1 << (32 - Integer.numberOfLeadingZeros(rawCap - 1));
        long[] hashKeys = new long[tableCap];
        int hashMask = tableCap - 1;

        for (WireNode wire : networkWires) {
            if (wire.currentPower != wire.targetPower) {
                wire.currentPower = wire.targetPower;
                changedCount++;

                if (batchUpdatesEnabled) {
                    // Add 6 cardinal neighbors for batched notification
                    int[][] offsets = {{1,0,0}, {-1,0,0}, {0,1,0}, {0,-1,0}, {0,0,1}, {0,0,-1}};
                    for (int[] off : offsets) {
                        long packedNeighbor = PrimitiveVectorPool.packBlockPos(wire.x + off[0], wire.y + off[1], wire.z + off[2]);
                        int slot = ((int) (packedNeighbor ^ (packedNeighbor >>> 32))) & hashMask;
                        int probes = 0;
                        while (hashKeys[slot] != 0 && hashKeys[slot] != packedNeighbor && probes++ < tableCap) {
                            slot = (slot + 1) & hashMask;
                        }
                        if (hashKeys[slot] == 0) {
                            hashKeys[slot] = packedNeighbor;
                            if (neighborCount < maxCapacity) {
                                neighborBuffer[neighborCount++] = packedNeighbor;
                            }
                        }
                    }
                }
            }
        }

        long[] finalNeighbors = new long[neighborCount];
        System.arraycopy(neighborBuffer, 0, finalNeighbors, 0, neighborCount);
        return new NetworkSolveResult(changedCount, finalNeighbors, neighborCount);
    }

    /**
     * Calculates the exact vanilla discrete comparator output signal (0-15) based on inventory capacity.
     * Prevents redundant update storms when inventory content shifts do not change the integer power level.
     */
    public int calculateComparatorPower(int totalItems, int maxCapacity) {
        if (!enabled || !comparatorCachingEnabled || maxCapacity <= 0 || totalItems <= 0) {
            return 0;
        }
        float ratio = (float) totalItems / (float) maxCapacity;
        return Math.min(15, (int) Math.floor(ratio * 14.0f) + 1);
    }

    /**
     * Fast occlusion test for hoppers. If a full container (composter, chest, barrel) sits
     * directly above a hopper, entity AABB collision searches (world.getEntitiesByClass) are completely bypassed.
     */
    public boolean isHopperOccludedByContainer(boolean hasBlockAbove, boolean isSolidOrContainer) {
        if (!enabled || !hopperOcclusionFastPathEnabled) {
            return false;
        }
        return hasBlockAbove && isSolidOrContainer;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isLightSuppressionEnabled() {
        return lightSuppressionEnabled;
    }

    public boolean isComparatorCachingEnabled() {
        return comparatorCachingEnabled;
    }

    public boolean isHopperOcclusionFastPathEnabled() {
        return hopperOcclusionFastPathEnabled;
    }

    public boolean isBatchUpdatesEnabled() {
        return batchUpdatesEnabled;
    }
}
