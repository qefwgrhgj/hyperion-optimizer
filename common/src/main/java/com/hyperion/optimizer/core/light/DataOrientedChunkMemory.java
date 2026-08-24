package com.hyperion.optimizer.core.light;

public class DataOrientedChunkMemory {
    // 16x16x16 chunk section = 4096 blocks
    public static final int SECTION_BLOCK_COUNT = 4096;

    // Linear 1D flat array for optimal CPU L1/L2/L3 cache line prefetching (64-byte cache lines)
    private final byte[] packedLightArray = new byte[SECTION_BLOCK_COUNT / 2]; // 4 bits per block (0-15 light levels)

    public synchronized int getLight(int x, int y, int z) {
        int index = ((y & 0x0F) << 8) | ((z & 0x0F) << 4) | (x & 0x0F);
        int byteIndex = index >> 1;
        byte val = packedLightArray[byteIndex];
        if ((index & 1) == 0) {
            return val & 0x0F;
        } else {
            return (val >> 4) & 0x0F;
        }
    }

    // Fix P0-1: Synchronized atomic nibble modification prevents data race on shared byte
    public synchronized void setLight(int x, int y, int z, int level) {
        int index = ((y & 0x0F) << 8) | ((z & 0x0F) << 4) | (x & 0x0F);
        int byteIndex = index >> 1;
        byte val = packedLightArray[byteIndex];
        int clampedLevel = Math.max(0, Math.min(15, level));
        if ((index & 1) == 0) {
            packedLightArray[byteIndex] = (byte) ((val & 0xF0) | (clampedLevel & 0x0F));
        } else {
            packedLightArray[byteIndex] = (byte) ((val & 0x0F) | ((clampedLevel & 0x0F) << 4));
        }
    }

    public synchronized void fillSection(byte defaultLight) {
        int clamped = defaultLight & 0x0F;
        byte packed = (byte) (clamped | (clamped << 4));
        java.util.Arrays.fill(packedLightArray, packed);
    }
}
