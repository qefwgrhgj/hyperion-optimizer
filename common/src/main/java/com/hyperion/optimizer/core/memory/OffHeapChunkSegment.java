package com.hyperion.optimizer.core.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;

public final class OffHeapChunkSegment {
    public static final int SECTION_SIZE = 16 * 16 * 16; // 4096 voxels
    public static final int NIBBLE_DATA_SIZE = SECTION_SIZE / 2; // 2048 bytes

    private final ByteBuffer directBuffer;
    private final AtomicBoolean isFreed = new AtomicBoolean(false);

    public OffHeapChunkSegment() {
        this.directBuffer = ByteBuffer.allocateDirect(NIBBLE_DATA_SIZE).order(ByteOrder.nativeOrder());
    }

    public void setNibble(int x, int y, int z, int value) {
        if (isFreed.get()) {
            throw new IllegalStateException("Attempted write to freed OffHeapChunkSegment");
        }
        int clampedX = x & 15;
        int clampedY = y & 15;
        int clampedZ = z & 15;
        int index = (clampedY << 8) | (clampedZ << 4) | clampedX;
        int byteIndex = index >> 1;
        boolean highNibble = (index & 1) != 0;

        int clampedVal = Math.max(0, Math.min(15, value));
        byte current = directBuffer.get(byteIndex);
        if (highNibble) {
            current = (byte) ((current & 0x0F) | (clampedVal << 4));
        } else {
            current = (byte) ((current & 0xF0) | (clampedVal & 0x0F));
        }
        directBuffer.put(byteIndex, current);
    }

    public int getNibble(int x, int y, int z) {
        if (isFreed.get()) {
            return 0;
        }
        int clampedX = x & 15;
        int clampedY = y & 15;
        int clampedZ = z & 15;
        int index = (clampedY << 8) | (clampedZ << 4) | clampedX;
        int byteIndex = index >> 1;
        boolean highNibble = (index & 1) != 0;

        byte b = directBuffer.get(byteIndex);
        if (highNibble) {
            return (b >> 4) & 0x0F;
        } else {
            return b & 0x0F;
        }
    }

    public void clear() {
        if (!isFreed.get()) {
            for (int i = 0; i < NIBBLE_DATA_SIZE; i++) {
                directBuffer.put(i, (byte) 0);
            }
        }
    }

    public void free() {
        if (isFreed.compareAndSet(false, true)) {
            // Mark segment as freed
        }
    }

    public boolean isFreed() {
        return isFreed.get();
    }

    public ByteBuffer getDirectBuffer() {
        return directBuffer;
    }
}
