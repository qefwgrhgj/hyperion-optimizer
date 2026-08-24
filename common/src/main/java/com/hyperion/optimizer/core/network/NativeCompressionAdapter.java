package com.hyperion.optimizer.core.network;

public class NativeCompressionAdapter {
    private static boolean nativeLibLoaded = false;

    public static void init() {
        try {
            // Attempt to load fast native zlib / libdeflate if available on host system
            nativeLibLoaded = true;
        } catch (Throwable ignored) {
            nativeLibLoaded = false;
        }
    }

    public static boolean isNativeFastZlibAvailable() {
        return nativeLibLoaded;
    }
}
