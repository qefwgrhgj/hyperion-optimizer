package com.hyperion.optimizer.core.threading;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

/**
 * AsyncWorldTickDispatcher
 * Dispatches non-critical world tasks (fluid spreading simulations, sleeping block entity checks,
 * redstone propagation queues, lighting dirty updates) to asynchronous CPU worker threads.
 */
public final class AsyncWorldTickDispatcher {
    public static final int MAX_PENDING_TASKS = 8192;

    private final boolean enabled;
    private final ConcurrentLinkedQueue<Runnable> asyncWorldTasks = new ConcurrentLinkedQueue<>();
    private final java.util.concurrent.atomic.AtomicInteger pendingQueueSize = new java.util.concurrent.atomic.AtomicInteger(0);
    private final ExecutorService lightPool;
    private final LongAdder dispatchedTasks = new LongAdder();
    private final LongAdder completedTasks = new LongAdder();
    private final AtomicBoolean isFlushing = new AtomicBoolean(false);

    public AsyncWorldTickDispatcher(boolean enabled) {
        this.enabled = enabled;
        this.lightPool = HyperionThreadPoolManager.getInstance().getLightEnginePool();
    }

    public void queueAsyncTask(Runnable task) {
        if (task == null) return;
        if (!enabled || lightPool == null || lightPool.isShutdown()) {
            task.run();
            dispatchedTasks.increment();
            completedTasks.increment();
            return;
        }

        // Bounded queue backpressure protection: run synchronously if overloaded
        if (pendingQueueSize.get() >= MAX_PENDING_TASKS) {
            task.run();
            dispatchedTasks.increment();
            completedTasks.increment();
            return;
        }

        asyncWorldTasks.offer(task);
        pendingQueueSize.incrementAndGet();
        dispatchedTasks.increment();
        triggerAsyncDrain();
    }

    private void triggerAsyncDrain() {
        if (isFlushing.compareAndSet(false, true)) {
            lightPool.submit(() -> {
                try {
                    do {
                        Runnable r;
                        while ((r = asyncWorldTasks.poll()) != null) {
                            pendingQueueSize.decrementAndGet();
                            try {
                                r.run();
                                completedTasks.increment();
                            } catch (Throwable t) {
                                // Suppress individual task errors to prevent worker death
                            }
                        }
                    } while (!asyncWorldTasks.isEmpty());
                } finally {
                    isFlushing.set(false);
                    // Final safety drain check
                    if (!asyncWorldTasks.isEmpty()) {
                        triggerAsyncDrain();
                    }
                }
            });
        }
    }

    public long getDispatchedTasks() {
        return dispatchedTasks.sum();
    }

    public long getCompletedTasks() {
        return completedTasks.sum();
    }

    public int getQueueDepth() {
        return Math.max(0, pendingQueueSize.get());
    }

    public boolean isEnabled() {
        return enabled;
    }
}
