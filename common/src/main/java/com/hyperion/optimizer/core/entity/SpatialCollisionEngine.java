package com.hyperion.optimizer.core.entity;

import com.hyperion.optimizer.core.memory.PrimitiveVectorPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-Performance Spatial Collision Hashing & AI Brain Stripping Engine (Lithium Architecture).
 * Eliminates O(N^2) entity collision checks using 4x4 spatial grid buckets, caps max collision checks,
 * and throttles AI brain goals for trapped (1x1 cells) or distant mobs.
 */
public class SpatialCollisionEngine {
    public static class CollidableEntity {
        public final int entityId;
        public double x, y, z;
        public double width, height;
        public int collisionCheckCount = 0;

        public CollidableEntity(int entityId, double x, double y, double z, double width, double height) {
            this.entityId = entityId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.width = width;
            this.height = height;
        }
    }

    private final boolean enabled;
    private final int maxCollisionsPerEntity;
    private final double brainThrottleDistanceSq;
    private final ConcurrentHashMap<Long, List<CollidableEntity>> spatialGrid = new ConcurrentHashMap<>();

    public SpatialCollisionEngine(boolean enabled, int maxCollisionsPerEntity, double brainThrottleDistance) {
        this.enabled = enabled;
        this.maxCollisionsPerEntity = Math.max(1, maxCollisionsPerEntity);
        this.brainThrottleDistanceSq = brainThrottleDistance * brainThrottleDistance;
    }

    public static long packSpatialBucket(double x, double z) {
        int bucketX = ((int) Math.floor(x)) >> 2; // 4x4 block bucket
        int bucketZ = ((int) Math.floor(z)) >> 2;
        return (((long) bucketX) << 32) | (bucketZ & 0xFFFFFFFFL);
    }

    public void clearGrid() {
        spatialGrid.clear();
    }

    public void onTickStart() {
        if (!spatialGrid.isEmpty()) {
            spatialGrid.clear();
        }
    }

    public void registerEntity(CollidableEntity entity, double camX, double camZ) {
        if (!enabled || entity == null) return;
        double dx = entity.x - camX;
        double dz = entity.z - camZ;
        // Distant mobs (> 64 blocks) at 32 chunks never collide with near players/blocks
        if (dx * dx + dz * dz > 4096.0) return;
        registerEntity(entity);
    }

    public void registerEntity(CollidableEntity entity) {
        if (!enabled || entity == null) return;
        entity.collisionCheckCount = 0;
        long bucket = packSpatialBucket(entity.x, entity.z);
        List<CollidableEntity> list = spatialGrid.computeIfAbsent(bucket, k -> new ArrayList<>(8));
        synchronized (list) {
            list.add(entity);
        }
    }

    /**
     * Retrieves adjacent potential collision candidates in O(1) via spatial hashing.
     */
    public List<CollidableEntity> getNearbyCandidates(double x, double z) {
        if (!enabled) return new ArrayList<>();
        List<CollidableEntity> candidates = new ArrayList<>();
        collectNearbyCandidates(x, z, candidates);
        return candidates;
    }

    /**
     * Fills the destination collection with nearby candidates with zero intermediate allocations.
     */
    public void collectNearbyCandidates(double x, double z, List<CollidableEntity> destination) {
        if (!enabled || destination == null) return;
        int baseBucketX = ((int) Math.floor(x)) >> 2;
        int baseBucketZ = ((int) Math.floor(z)) >> 2;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                long bucket = (((long) (baseBucketX + dx)) << 32) | ((baseBucketZ + dz) & 0xFFFFFFFFL);
                List<CollidableEntity> list = spatialGrid.get(bucket);
                if (list != null && !list.isEmpty()) {
                    synchronized (list) {
                        destination.addAll(list);
                    }
                }
            }
        }
    }

    /**
     * Evaluates whether entity can perform another collision push this tick.
     */
    public boolean canCheckCollision(CollidableEntity entity) {
        if (!enabled || entity == null) return true;
        if (entity.collisionCheckCount >= maxCollisionsPerEntity) {
            return false; // Cap reached, skip further collision iterations
        }
        entity.collisionCheckCount++;
        return true;
    }

    /**
     * Determines whether entity AI brain goals and pathfinding should be stripped/throttled.
     * Mobs trapped in 1x1 cells or beyond throttle distance poll AI every 20 ticks instead of 1.
     */
    public boolean shouldThrottleBrain(boolean isTrappedIn1x1, double playerDistSq, long serverTick) {
        if (!enabled) return false;
        if (isTrappedIn1x1) {
            return (serverTick % 20) != 0; // Throttle trapped mobs to 1 Hz
        }
        if (playerDistSq > brainThrottleDistanceSq) {
            return (serverTick % 10) != 0; // Throttle distant mobs to 2 Hz
        }
        return false; // Full 20 Hz tick
    }

    /**
     * Halves pairwise collision checks by enforcing unique evaluation order (idA < idB).
     */
    public static boolean shouldEvaluatePair(int entityIdA, int entityIdB) {
        return entityIdA < entityIdB;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxCollisionsPerEntity() {
        return maxCollisionsPerEntity;
    }
}
