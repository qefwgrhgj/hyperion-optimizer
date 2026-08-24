package com.hyperion.optimizer.core.audio;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncAudioEngine {
    private final boolean enabled;
    private final int maxSimultaneousChannels;
    private final AtomicInteger activePlayingChannels = new AtomicInteger(0);
    private final ExecutorService audioWorkerPool;

    public AsyncAudioEngine(boolean enabled, int maxSimultaneousChannels) {
        this.enabled = enabled;
        this.maxSimultaneousChannels = maxSimultaneousChannels;
        if (enabled) {
            // Fix P2-3: Bounded task queue (1024) with DiscardOldestPolicy to prevent memory exhaustion
            this.audioWorkerPool = new ThreadPoolExecutor(
                1, 1,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1024),
                r -> {
                    Thread t = new Thread(r, "Hyperion-Async-Audio");
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.DiscardOldestPolicy()
            );
        } else {
            this.audioWorkerPool = null;
        }
    }

    public boolean canPlaySound(double distanceSq, double maxAudibleDistSq) {
        if (!enabled) return true;
        if (distanceSq > maxAudibleDistSq) return false;

        // Dynamic voice clamping if channels exceed limit
        return activePlayingChannels.get() < maxSimultaneousChannels;
    }

    public void dispatchAudioCalculation(Runnable audioTask) {
        if (!enabled || audioWorkerPool == null || audioWorkerPool.isShutdown()) {
            audioTask.run();
            return;
        }
        audioWorkerPool.submit(audioTask);
    }

    public void incrementPlayingChannel() {
        activePlayingChannels.incrementAndGet();
    }

    public void decrementPlayingChannel() {
        activePlayingChannels.updateAndGet(val -> Math.max(0, val - 1));
    }

    // Fix P2: Synchronize with actual OpenAL sound sources to prevent voice starvation
    public void syncActiveChannels(int realPlayingSources) {
        activePlayingChannels.set(Math.max(0, realPlayingSources));
    }

    public int getActivePlayingChannels() {
        return activePlayingChannels.get();
    }

    public boolean isEnabled() {
        return enabled;
    }

    // Fix P2-2: Gracefully shutdown audio worker pool
    public void shutdown() {
        if (audioWorkerPool != null) {
            audioWorkerPool.shutdownNow();
        }
    }

    public boolean isShutdown() {
        return audioWorkerPool == null || audioWorkerPool.isShutdown();
    }
}
