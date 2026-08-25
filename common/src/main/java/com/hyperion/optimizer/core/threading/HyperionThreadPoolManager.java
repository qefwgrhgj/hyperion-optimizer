package com.hyperion.optimizer.core.threading;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * HyperionThreadPoolManager
 * Sovereign Multi-Core CPU Orchestration Engine.
 * Dynamically detects CPU core topology, balances thread allocations across subsystems,
 * and maintains dedicated work-stealing executor pools.
 */
public final class HyperionThreadPoolManager {
    private static final Logger LOGGER = Logger.getLogger("Hyperion-CPU");
    private static final HyperionThreadPoolManager INSTANCE = new HyperionThreadPoolManager();

    private final int physicalCores;
    private final int logicalCores;

    private boolean multithreadingEnabled = true;
    private String allocationMode = "AUTO_DETECT_CORES"; // AUTO_DETECT_CORES, ALL_CORES, BALANCED_N_MINUS_1, CUSTOM
    private int customCoreCount = 0;

    // Subsystem dedicated thread pools
    private ForkJoinPool chunkMeshingPool;
    private ExecutorService entityPhysicsPool;
    private ExecutorService lightEnginePool;
    private ExecutorService worldCacheIoPool;
    private ScheduledExecutorService tickScheduler;

    private HyperionThreadPoolManager() {
        this.logicalCores = Runtime.getRuntime().availableProcessors();
        // Empirical approximation of physical cores
        this.physicalCores = Math.max(1, logicalCores > 4 ? (logicalCores * 3) / 4 : logicalCores);
        reconfigurePools(this.multithreadingEnabled, this.allocationMode, this.logicalCores);
    }

    public static HyperionThreadPoolManager getInstance() {
        return INSTANCE;
    }

    public synchronized void reconfigurePools(boolean enabled, String mode, int customThreads) {
        this.multithreadingEnabled = enabled;
        this.allocationMode = mode != null ? mode : "AUTO_DETECT_CORES";
        this.customCoreCount = customThreads;

        shutdownPools();

        if (!multithreadingEnabled) {
            LOGGER.info("[Hyperion-CPU] Multithreading disabled. Running in single-core fallback mode.");
            this.chunkMeshingPool = new ForkJoinPool(1);
            this.entityPhysicsPool = Executors.newSingleThreadExecutor(new NamedThreadFactory("Hyperion-Entity-1", Thread.NORM_PRIORITY));
            this.lightEnginePool = Executors.newSingleThreadExecutor(new NamedThreadFactory("Hyperion-Light-1", Thread.NORM_PRIORITY));
            this.worldCacheIoPool = Executors.newSingleThreadExecutor(new NamedThreadFactory("Hyperion-IO-1", Thread.MIN_PRIORITY));
            this.tickScheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Hyperion-Tick-1", Thread.MAX_PRIORITY));
            return;
        }

        int targetThreads;
        switch (this.allocationMode) {
            case "ALL_CORES":
                targetThreads = Math.max(2, logicalCores);
                break;
            case "BALANCED_N_MINUS_1":
                targetThreads = Math.max(1, logicalCores - 1);
                break;
            case "CUSTOM":
                targetThreads = Math.max(1, Math.min(64, customThreads > 0 ? customThreads : logicalCores));
                break;
            case "AUTO_DETECT_CORES":
            default:
                targetThreads = logicalCores >= 8 ? logicalCores : Math.max(2, logicalCores);
                break;
        }

        int meshingThreads = Math.max(1, (targetThreads * 5) / 8);
        int entityThreads = Math.max(1, targetThreads / 4);
        int lightThreads = Math.max(1, targetThreads / 4);
        int ioThreads = Math.max(1, Math.min(4, targetThreads / 4));

        LOGGER.info(String.format("[Hyperion-CPU] Initialized Multi-Core Engine: Detected %d Logical / %d Physical Cores. Allocated: Meshing=%d, Entity=%d, Light=%d, IO=%d threads.",
                logicalCores, physicalCores, meshingThreads, entityThreads, lightThreads, ioThreads));

        this.chunkMeshingPool = new ForkJoinPool(
                meshingThreads,
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                (t, e) -> LOGGER.warning("[Hyperion-CPU] Uncaught exception in Chunk Meshing: " + e.getMessage()),
                true
        );

        this.entityPhysicsPool = new ThreadPoolExecutor(
                entityThreads, entityThreads,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2048),
                new NamedThreadFactory("Hyperion-Entity-Worker", Thread.NORM_PRIORITY + 1),
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );

        this.lightEnginePool = new ThreadPoolExecutor(
                lightThreads, lightThreads,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(4096),
                new NamedThreadFactory("Hyperion-Light-Worker", Thread.NORM_PRIORITY),
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );

        this.worldCacheIoPool = new ThreadPoolExecutor(
                ioThreads, ioThreads,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1024),
                new NamedThreadFactory("Hyperion-IO-Worker", Thread.MIN_PRIORITY),
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );

        this.tickScheduler = Executors.newScheduledThreadPool(
                2,
                new NamedThreadFactory("Hyperion-Scheduler", Thread.MAX_PRIORITY)
        );
    }

    private void shutdownPools() {
        if (chunkMeshingPool != null && !chunkMeshingPool.isShutdown()) {
            chunkMeshingPool.shutdownNow();
        }
        if (entityPhysicsPool != null && !entityPhysicsPool.isShutdown()) {
            entityPhysicsPool.shutdownNow();
        }
        if (lightEnginePool != null && !lightEnginePool.isShutdown()) {
            lightEnginePool.shutdownNow();
        }
        if (worldCacheIoPool != null && !worldCacheIoPool.isShutdown()) {
            worldCacheIoPool.shutdownNow();
        }
        if (tickScheduler != null && !tickScheduler.isShutdown()) {
            tickScheduler.shutdownNow();
        }
    }

    public ForkJoinPool getChunkMeshingPool() {
        return chunkMeshingPool;
    }

    public ExecutorService getEntityPhysicsPool() {
        return entityPhysicsPool;
    }

    public ExecutorService getLightEnginePool() {
        return lightEnginePool;
    }

    public ExecutorService getWorldCacheIoPool() {
        return worldCacheIoPool;
    }

    public ScheduledExecutorService getTickScheduler() {
        return tickScheduler;
    }

    public ScheduledExecutorService getAsyncScheduler() {
        return tickScheduler;
    }

    public int getLogicalCores() {
        return logicalCores;
    }

    public int getPhysicalCores() {
        return physicalCores;
    }

    public boolean isMultithreadingEnabled() {
        return multithreadingEnabled;
    }

    public String getAllocationMode() {
        return allocationMode;
    }

    public int getCustomCoreCount() {
        return customCoreCount;
    }

    private static final class NamedThreadFactory implements ThreadFactory {
        private final String prefix;
        private final int priority;
        private final AtomicInteger counter = new AtomicInteger(1);

        public NamedThreadFactory(String prefix, int priority) {
            this.prefix = prefix;
            this.priority = priority;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, prefix + "-" + counter.getAndIncrement());
            t.setDaemon(true);
            t.setPriority(priority);
            return t;
        }
    }
}
