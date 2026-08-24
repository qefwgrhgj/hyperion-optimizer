package com.hyperion.optimizer.core.memory;

/**
 * High-Performance Fast Trigonometric Look-Up Table (LUT) Engine.
 * Precomputes 65,536 sin and cos entries, providing O(1) trigonometric operations
 * and fast inverse square roots without calling native Math.sin/cos FPU instructions.
 */
public final class FastMathLUT {
    private static final int TABLE_SIZE = 65536;
    private static final int TABLE_MASK = TABLE_SIZE - 1;
    private static final float RAD_TO_INDEX = (float) (TABLE_SIZE / (2.0 * Math.PI));
    private static final float[] SIN_TABLE = new float[TABLE_SIZE];

    static {
        for (int i = 0; i < TABLE_SIZE; i++) {
            SIN_TABLE[i] = (float) Math.sin((double) i * Math.PI * 2.0 / (double) TABLE_SIZE);
        }
    }

    // Fix P1-1: Large positive bias ensures continuous floor rounding across negative angles
    private static final float POSITIVE_BIAS = (float) (TABLE_SIZE * 1024);

    public static float sin(float rad) {
        int index = (int) (rad * RAD_TO_INDEX + POSITIVE_BIAS) & TABLE_MASK;
        return SIN_TABLE[index];
    }

    public static float cos(float rad) {
        int index = (int) (rad * RAD_TO_INDEX + (float) (TABLE_SIZE / 4) + POSITIVE_BIAS) & TABLE_MASK;
        return SIN_TABLE[index];
    }

    /**
     * Fast Inverse Square Root (Quake III / Fast InvSqrt algorithm).
     */
    public static float fastInvSqrt(float x) {
        if (x <= 0.0f || Float.isNaN(x)) {
            return (x == 0.0f) ? Float.POSITIVE_INFINITY : Float.NaN;
        }
        float xhalf = 0.5f * x;
        int i = Float.floatToIntBits(x);
        i = 0x5f3759df - (i >> 1);
        x = Float.intBitsToFloat(i);
        x = x * (1.5f - xhalf * x * x);
        return x;
    }

    public static int getTableSize() {
        return TABLE_SIZE;
    }
}
