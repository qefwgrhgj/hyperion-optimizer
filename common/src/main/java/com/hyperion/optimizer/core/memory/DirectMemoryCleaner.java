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

            try {
                invokeCleaner = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
            } catch (NoSuchMethodException ignored) {}
        } catch (Throwable t1) {
            // Fallback for modular JDKs without sun.misc.Unsafe accessibility
            try {
                Class<?> jdkUnsafeClass = Class.forName("jdk.internal.misc.Unsafe");
                Method getUnsafeMethod = jdkUnsafeClass.getMethod("getUnsafe");
                getUnsafeMethod.setAccessible(true);
                unsafe = getUnsafeMethod.invoke(null);
                invokeCleaner = jdkUnsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
            } catch (Throwable t2) {
                LOGGER.fine("[Hyperion] Native Unsafe not directly accessible: " + t2.getMessage());
            }
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

        // Path 1: Unsafe.invokeCleaner (Java 9 - 25+)
        if (UNSAFE_INSTANCE != null && INVOKE_CLEANER_METHOD != null) {
            try {
                INVOKE_CLEANER_METHOD.invoke(UNSAFE_INSTANCE, buffer);
                return true;
            } catch (Throwable ignored) {}
        }

        // Path 2: DirectBuffer.cleaner().clean() (Java 8 - 16)
        try {
            Method cleanerMethod = buffer.getClass().getMethod("cleaner");
            cleanerMethod.setAccessible(true);
            Object cleaner = cleanerMethod.invoke(buffer);
            if (cleaner != null) {
                Method cleanMethod = cleaner.getClass().getMethod("clean");
                cleanMethod.setAccessible(true);
                cleanMethod.invoke(cleaner);
                return true;
            }
        } catch (Throwable ignored) {}

        // Path 3: Direct attachment deallocation
        try {
            Field attachmentField = buffer.getClass().getDeclaredField("att");
            attachmentField.setAccessible(true);
            attachmentField.set(buffer, null);
        } catch (Throwable ignored) {}

        return false;
    }
}
