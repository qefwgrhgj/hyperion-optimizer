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
    private final boolean enabled;
    private final ConcurrentLinkedQueue<Runnable> asyncWorldTasks = new ConcurrentLinkedQueue<>();
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

        asyncWorldTasks.offer(task);
        dispatchedTasks.increment();
        triggerAsyncDrain();
    }

    private void triggerAsyncDrain() {
        if (isFlushing.compareAndSet(false, true)) {
            lightPool.submit(() -> {
                try {
                    Runnable r;
                    while ((r = asyncWorldTasks.poll()) != null) {
                        try {
                            r.run();
                            completedTasks.increment();
                        } catch (Throwable t) {
                            // Suppress individual task errors to prevent worker death
                        }
                    }
                } finally {
                    isFlushing.set(false);
                    // Check if more tasks arrived while finishing
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
        return asyncWorldTasks.size();
    }

    public boolean isEnabled() {
        return enabled;
    }
}
