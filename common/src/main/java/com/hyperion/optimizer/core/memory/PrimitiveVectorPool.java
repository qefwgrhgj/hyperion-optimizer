package com.hyperion.optimizer.core.memory;

public final class PrimitiveVectorPool {
    private static final int PACKED_X_BITS = 26;
    private static final int PACKED_Z_BITS = 26;
    private static final int PACKED_Y_BITS = 12;

    private static final long PACKED_X_MASK = (1L << PACKED_X_BITS) - 1L;
    private static final long PACKED_Y_MASK = (1L << PACKED_Y_BITS) - 1L;
    private static final long PACKED_Z_MASK = (1L << PACKED_Z_BITS) - 1L;

    private static final int PACKED_Y_SHIFT = 0;
    private static final int PACKED_Z_SHIFT = PACKED_Y_BITS;
    private static final int PACKED_X_SHIFT = PACKED_Y_BITS + PACKED_Z_BITS;

    // Fix P0-2: Expanded ring buffer size per thread (power of two) to prevent wrap-around overwrite
    private static final int POOL_SIZE = 64;
    private static final int POOL_MASK = POOL_SIZE - 1;

    private static final class ThreadLocalRingBuffer {
        final MutableVec3[] vecPool = new MutableVec3[POOL_SIZE];
        final MutableAABB[] aabbPool = new MutableAABB[POOL_SIZE];
        int vecIndex = 0;
        int aabbIndex = 0;

        ThreadLocalRingBuffer() {
            for (int i = 0; i < POOL_SIZE; i++) {
                vecPool[i] = new MutableVec3();
                aabbPool[i] = new MutableAABB();
            }
        }
    }

    private static final ThreadLocal<ThreadLocalRingBuffer> RING_BUFFER = ThreadLocal.withInitial(ThreadLocalRingBuffer::new);

    private PrimitiveVectorPool() {}

    // World boundary constants to prevent bit distortion beyond +/-30,000,000
    public static final int MIN_WORLD_COORD = -30000000;
    public static final int MAX_WORLD_COORD = 30000000;
    public static final int MIN_WORLD_Y = -2048;
    public static final int MAX_WORLD_Y = 2047;

    public static void init() {
        RING_BUFFER.get();
    }

    public static long packBlockPos(int x, int y, int z) {
        // Fix P3: Clamp coordinates at world border to prevent 26-bit/12-bit overflow distortion
        int cx = (x < MIN_WORLD_COORD) ? MIN_WORLD_COORD : ((x > MAX_WORLD_COORD) ? MAX_WORLD_COORD : x);
        int cy = (y < MIN_WORLD_Y) ? MIN_WORLD_Y : ((y > MAX_WORLD_Y) ? MAX_WORLD_Y : y);
        int cz = (z < MIN_WORLD_COORD) ? MIN_WORLD_COORD : ((z > MAX_WORLD_COORD) ? MAX_WORLD_COORD : z);
        return ((long) (cx & PACKED_X_MASK) << PACKED_X_SHIFT)
             | ((long) (cz & PACKED_Z_MASK) << PACKED_Z_SHIFT)
             | ((long) (cy & PACKED_Y_MASK) << PACKED_Y_SHIFT);
    }

    public static int unpackX(long packed) {
        return (int) (packed << (64 - PACKED_X_SHIFT - PACKED_X_BITS) >> (64 - PACKED_X_BITS));
    }

    public static int unpackY(long packed) {
        return (int) (packed << (64 - PACKED_Y_BITS) >> (64 - PACKED_Y_BITS));
    }

    public static int unpackZ(long packed) {
        return (int) (packed << (64 - PACKED_Z_SHIFT - PACKED_Z_BITS) >> (64 - PACKED_Z_BITS));
    }

    public static MutableVec3 getThreadLocalVec(double x, double y, double z) {
        ThreadLocalRingBuffer buffer = RING_BUFFER.get();
        MutableVec3 vec = buffer.vecPool[buffer.vecIndex++ & POOL_MASK];
        vec.x = x;
        vec.y = y;
        vec.z = z;
        return vec;
    }

    public static MutableAABB getThreadLocalAABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        ThreadLocalRingBuffer buffer = RING_BUFFER.get();
        MutableAABB aabb = buffer.aabbPool[buffer.aabbIndex++ & POOL_MASK];
        aabb.minX = minX;
        aabb.minY = minY;
        aabb.minZ = minZ;
        aabb.maxX = maxX;
        aabb.maxY = maxY;
        aabb.maxZ = maxZ;
        return aabb;
    }

    public static final class MutableVec3 {
        public double x;
        public double y;
        public double z;

        public double distanceSq(double targetX, double targetY, double targetZ) {
            double dx = this.x - targetX;
            double dy = this.y - targetY;
            double dz = this.z - targetZ;
            return dx * dx + dy * dy + dz * dz;
        }
    }

    public static final class MutableAABB {
        public double minX, minY, minZ;
        public double maxX, maxY, maxZ;

        public boolean intersects(double oMinX, double oMinY, double oMinZ, double oMaxX, double oMaxY, double oMaxZ) {
            return this.minX < oMaxX && this.maxX > oMinX &&
                   this.minY < oMaxY && this.maxY > oMinY &&
                   this.minZ < oMaxZ && this.maxZ > oMinZ;
        }
    }
}
