package com.hyperion.optimizer.core.light;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncBitsetLightEngine {
    private final boolean enabled;
    private final ExecutorService lightWorkerPool;
    private static final int MAX_QUEUE_CAPACITY = 4096;

    public AsyncBitsetLightEngine(boolean enabled, int workerThreads) {
        this.enabled = enabled;
        if (enabled) {
            // Fix P1-3: Clamp worker threads to min 1 to prevent IllegalArgumentException on 0/negative threads
            int threads = Math.max(1, workerThreads);
            this.lightWorkerPool = new ThreadPoolExecutor(
                threads,
                threads,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(MAX_QUEUE_CAPACITY),
                new ThreadFactory() {
                    private final AtomicInteger threadIndex = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "Hyperion-Light-Worker-" + threadIndex.getAndIncrement());
                        t.setDaemon(true);
                        t.setPriority(Thread.NORM_PRIORITY - 1);
                        return t;
                    }
                },
                new ThreadPoolExecutor.DiscardOldestPolicy() // Discard stale redundant light passes if overloaded
            );
        } else {
            this.lightWorkerPool = null;
        }
    }

    public void dispatchLightTask(Runnable lightCalculationTask) {
        if (!enabled || lightWorkerPool == null || lightWorkerPool.isShutdown()) {
            lightCalculationTask.run(); // Fallback to synchronous execution
            return;
        }
        lightWorkerPool.submit(lightCalculationTask);
    }

    public void shutdown() {
        if (lightWorkerPool != null && !lightWorkerPool.isShutdown()) {
            lightWorkerPool.shutdown();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
