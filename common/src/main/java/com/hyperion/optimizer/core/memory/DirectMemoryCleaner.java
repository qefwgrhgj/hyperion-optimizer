package com.hyperion.optimizer.core.memory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * 🧹 Deterministic Native & Direct ByteBuffer Deallocation Engine.
 *
 * Safely invokes JDK internal cleaner / unmapper on direct ByteBuffers when resources
 * (VBOs, VAOs, SSBOs, LOD buffer arenas) are discarded or hot-reloaded (F3 + T),
 * completely preventing native memory leaks across Java 8 to 21+.
 */
public final class DirectMemoryCleaner {
    private static final Logger LOGGER = Logger.getLogger("Hyperion-MemoryCleaner");

    private static final Object UNSAFE_INSTANCE;
    private static final Method INVOKE_CLEANER_METHOD;

    static {
        Object unsafe = null;
        Method invokeCleaner = null;

        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = f.get(null);

            // Java 9+ Unsafe.invokeCleaner(ByteBuffer)
            try {
                invokeCleaner = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
            } catch (NoSuchMethodException ignored) {
                // Java 8 fallback
            }
        } catch (Throwable t) {
            LOGGER.fine("[Hyperion] Native Unsafe not directly accessible: " + t.getMessage());
        }

        UNSAFE_INSTANCE = unsafe;
        INVOKE_CLEANER_METHOD = invokeCleaner;
    }

    private DirectMemoryCleaner() {}

    /**
     * Immediately frees the underlying off-heap native memory for a direct ByteBuffer.
     */
    public static boolean freeDirectBuffer(ByteBuffer buffer) {
        if (buffer == null || !buffer.isDirect()) {
            return false;
        }

        try {
            if (UNSAFE_INSTANCE != null && INVOKE_CLEANER_METHOD != null) {
                // Java 9+ standard direct clean
                INVOKE_CLEANER_METHOD.invoke(UNSAFE_INSTANCE, buffer);
                return true;
            } else if (UNSAFE_INSTANCE != null) {
                // Java 8 Cleaner.clean()
                Method cleanerMethod = buffer.getClass().getMethod("cleaner");
                cleanerMethod.setAccessible(true);
                Object cleaner = cleanerMethod.invoke(buffer);
                if (cleaner != null) {
                    Method cleanMethod = cleaner.getClass().getMethod("clean");
                    cleanMethod.setAccessible(true);
                    cleanMethod.invoke(cleaner);
                    return true;
                }
            }
        } catch (Throwable t) {
            LOGGER.fine("[Hyperion] Manual buffer cleaning fallback: " + t.getMessage());
        }

        return false;
    }
}
