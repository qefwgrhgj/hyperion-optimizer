package com.hyperion.optimizer.core.physics;

import com.hyperion.optimizer.core.memory.PrimitiveVectorPool;

/**
 * High-Performance Fast Explosion Engine (Sovereign High-Performance Ray-Bresenham Explosion Architecture).
 * Optimizes vanilla Minecraft 16x16x16 raycasting, block blast resistance evaluations,
 * water shield absorption, and entity damage/exposure calculations.
 */
public class FastExplosionEngine {
    public enum ExplosionType {
        GHAST_FIREBALL(1.0f, true, false, "Ghast Fireball"),
        CREEPER(3.0f, false, false, "Creeper"),
        TNT(4.0f, false, true, "TNT"),
        BED_OR_RESPAWN_ANCHOR(5.0f, true, false, "Bed / Respawn Anchor"),
        CHARGED_CREEPER(6.0f, false, false, "Charged Creeper"),
        END_CRYSTAL(6.0f, false, false, "End Crystal"),
        WITHER_SPAWN(7.0f, false, false, "Wither Spawn"),
        CUSTOM(4.0f, false, false, "Custom");

        public final float defaultPower;
        public final boolean createsFire;
        public final boolean dropsAllItems;
        public final String displayName;

        ExplosionType(float defaultPower, boolean createsFire, boolean dropsAllItems, String displayName) {
            this.defaultPower = defaultPower;
            this.createsFire = createsFire;
            this.dropsAllItems = dropsAllItems;
            this.displayName = displayName;
        }
    }

    @FunctionalInterface
    public interface BlastResistanceProvider {
        float getBlastResistance(int x, int y, int z);
    }

    public static class DamageImpact {
        public final double damage;
        public final double knockbackX;
        public final double knockbackY;
        public final double knockbackZ;

        public DamageImpact(double damage, double knockbackX, double knockbackY, double knockbackZ) {
            this.damage = damage;
            this.knockbackX = knockbackX;
            this.knockbackY = knockbackY;
            this.knockbackZ = knockbackZ;
        }
    }

    public static class ExplosionResult {
        public final long[] affectedBlocks;
        public final int affectedBlockCount;
        public final ExplosionType type;
        public final double x, y, z;
        public final float power;

        public ExplosionResult(long[] affectedBlocks, int affectedBlockCount, ExplosionType type, double x, double y, double z, float power) {
            this.affectedBlocks = affectedBlocks;
            this.affectedBlockCount = affectedBlockCount;
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
            this.power = power;
        }
    }

    public static final int TOTAL_RAYS = 1352;
    private static final float STEP_DISTANCE = 0.3f;
    private static final float RAY_ATTENUATION = 0.225f;
    private static final float[] PRECOMPUTED_RAY_DIRECTIONS = new float[TOTAL_RAYS * 3];

    static {
        // Precompute all 1,352 surface sampling ray direction vectors on class initialization
        int rayIndex = 0;
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    if (x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15) {
                        double dx = (double) x / 15.0 * 2.0 - 1.0;
                        double dy = (double) y / 15.0 * 2.0 - 1.0;
                        double dz = (double) z / 15.0 * 2.0 - 1.0;
                        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        if (len > 1e-6) {
                            PRECOMPUTED_RAY_DIRECTIONS[rayIndex * 3] = (float) ((dx / len) * STEP_DISTANCE);
                            PRECOMPUTED_RAY_DIRECTIONS[rayIndex * 3 + 1] = (float) ((dy / len) * STEP_DISTANCE);
                            PRECOMPUTED_RAY_DIRECTIONS[rayIndex * 3 + 2] = (float) ((dz / len) * STEP_DISTANCE);
                            rayIndex++;
                        }
                    }
                }
            }
        }
    }

    private static final class ExplosionThreadLocalBuffers {
        final long[] blockBuffer = new long[8192];
        final long[] hashKeys = new long[16384];
    }
    private static final ThreadLocal<ExplosionThreadLocalBuffers> BUFFERS =
        ThreadLocal.withInitial(ExplosionThreadLocalBuffers::new);

    private final boolean enabled;
    private final int maxRaySteps;

    public FastExplosionEngine(boolean enabled, int maxRaySteps) {
        this.enabled = enabled;
        this.maxRaySteps = Math.max(16, Math.min(256, maxRaySteps));
    }

    public ExplosionResult calculateExplosion(double expX, double expY, double expZ,
                                             float power, ExplosionType type,
                                             BlastResistanceProvider resistanceProvider) {
        if (!enabled || power <= 0.0f) {
            return new ExplosionResult(new long[0], 0, type, expX, expY, expZ, power);
        }

        ExplosionThreadLocalBuffers bufs = BUFFERS.get();
        long[] blockBuffer = bufs.blockBuffer;
        long[] hashKeys = bufs.hashKeys;
        java.util.Arrays.fill(hashKeys, 0L);

        int maxCapacity = blockBuffer.length;
        int count = 0;
        int hashMask = hashKeys.length - 1;

        for (int r = 0; r < TOTAL_RAYS; r++) {
            float stepX = PRECOMPUTED_RAY_DIRECTIONS[r * 3];
            float stepY = PRECOMPUTED_RAY_DIRECTIONS[r * 3 + 1];
            float stepZ = PRECOMPUTED_RAY_DIRECTIONS[r * 3 + 2];

            float rayPower = power * 0.85f; // Standardized deterministic ray energy
            double currX = expX;
            double currY = expY;
            double currZ = expZ;

            for (int step = 0; step < maxRaySteps && rayPower > 0.0f; step++) {
                int bx = (int) Math.floor(currX);
                int by = (int) Math.floor(currY);
                int bz = (int) Math.floor(currZ);

                float resistance = resistanceProvider.getBlastResistance(bx, by, bz);

                // Water absorption (Resistance 100) or impenetrable block (Bedrock / Barrier / Obsidian >= 1200)
                if (resistance >= 100.0f) {
                    break; // Immediate ray termination
                }

                if (resistance > 0.0f) {
                    float loss = (resistance / 5.0f + 0.3f) * STEP_DISTANCE;
                    rayPower -= loss;
                }

                if (rayPower > 0.0f && resistance < 100.0f) {
                    long packed = PrimitiveVectorPool.packBlockPos(bx, by, bz);
                    // Fast hash set insertion with probe limit
                    int slot = ((int) (packed ^ (packed >>> 32))) & hashMask;
                    int probes = 0;
                    while (hashKeys[slot] != 0 && hashKeys[slot] != packed && probes++ < 64) {
                        slot = (slot + 1) & hashMask;
                    }
                    if (hashKeys[slot] == 0) {
                        hashKeys[slot] = packed;
                        if (count < maxCapacity) {
                            blockBuffer[count++] = packed;
                        }
                    }
                }

                currX += stepX;
                currY += stepY;
                currZ += stepZ;
                rayPower -= RAY_ATTENUATION;
            }
        }

        long[] finalBlocks = new long[count];
        System.arraycopy(blockBuffer, 0, finalBlocks, 0, count);
        return new ExplosionResult(finalBlocks, count, type, expX, expY, expZ, power);
    }

    public DamageImpact calculateEntityImpact(double expX, double expY, double expZ,
                                              float power,
                                              double entX, double entY, double entZ,
                                              double entWidth, double entHeight,
                                              double exposureFraction) {
        if (!enabled || power <= 0.0f) {
            return new DamageImpact(0.0, 0.0, 0.0, 0.0);
        }

        double dx = entX - expX;
        double dy = entY - expY;
        double dz = entZ - expZ;
        double distSq = dx * dx + dy * dy + dz * dz;
        double maxRadius = (double) power * 2.0;
        double maxRadiusSq = maxRadius * maxRadius;

        if (distSq > maxRadiusSq || maxRadius <= 0.0) {
            return new DamageImpact(0.0, 0.0, 0.0, 0.0);
        }

        double dist = Math.sqrt(distSq);
        double normDist = dist / maxRadius;
        double clampedExposure = Math.max(0.0, Math.min(1.0, exposureFraction));
        double impact = (1.0 - normDist) * clampedExposure;

        double damage = ((impact * impact + impact) / 2.0) * 7.0 * (double) power + 1.0;
        double factor = (dist > 1e-6) ? (impact / dist) : 0.0;

        return new DamageImpact(damage, dx * factor, dy * factor, dz * factor);
    }

    public double calculateEntityExposure(double expX, double expY, double expZ,
                                          double minX, double minY, double minZ,
                                          double maxX, double maxY, double maxZ,
                                          BlastResistanceProvider resistanceProvider) {
        if (!enabled) return 1.0;

        int totalSamples = 8;
        int visibleSamples = 0;

        double[] sampleX = {minX, maxX};
        double[] sampleY = {minY, maxY};
        double[] sampleZ = {minZ, maxZ};

        for (double sx : sampleX) {
            for (double sy : sampleY) {
                for (double sz : sampleZ) {
                    if (isRayClear(expX, expY, expZ, sx, sy, sz, resistanceProvider)) {
                        visibleSamples++;
                    }
                }
            }
        }

        return (double) visibleSamples / (double) totalSamples;
    }

    private boolean isRayClear(double startX, double startY, double startZ,
                               double endX, double endY, double endZ,
                               BlastResistanceProvider resistanceProvider) {
        double dx = endX - startX;
        double dy = endY - startY;
        double dz = endZ - startZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist < 1e-6) return true;

        int steps = (int) Math.ceil(dist / 0.5);
        double stepX = dx / steps;
        double stepY = dy / steps;
        double stepZ = dz / steps;

        double cx = startX;
        double cy = startY;
        double cz = startZ;

        for (int i = 0; i < steps; i++) {
            int bx = (int) Math.floor(cx);
            int by = (int) Math.floor(cy);
            int bz = (int) Math.floor(cz);

            float res = resistanceProvider.getBlastResistance(bx, by, bz);
            if (res >= 1.0f) {
                return false; // Occluded by solid block or water
            }

            cx += stepX;
            cy += stepY;
            cz += stepZ;
        }

        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxRaySteps() {
        return maxRaySteps;
    }
}
