package com.hyperion.optimizer;

import com.hyperion.optimizer.api.HyperionConfig;
import com.hyperion.optimizer.core.audio.AsyncAudioEngine;
import com.hyperion.optimizer.core.entity.AnimationLodManager;
import com.hyperion.optimizer.core.entity.EntityDepthCuller;
import com.hyperion.optimizer.core.entity.ExperienceOrbMerger;
import com.hyperion.optimizer.core.entity.StaticChestMeshBaker;
import com.hyperion.optimizer.core.gpu.ComputeCullEngine;
import com.hyperion.optimizer.core.gpu.MultiDrawIndirectManager;
import com.hyperion.optimizer.core.hud.DecoupledHudManager;
import com.hyperion.optimizer.core.hud.HyperionProfilerOverlay;
import com.hyperion.optimizer.core.light.AsyncBitsetLightEngine;
import com.hyperion.optimizer.core.memory.PrimitiveVectorPool;
import com.hyperion.optimizer.core.network.PacketFlushConsolidator;
import com.hyperion.optimizer.core.entity.SpatialCollisionEngine;
import com.hyperion.optimizer.core.gpu.FastParticleEngine;
import com.hyperion.optimizer.core.physics.FastExplosionEngine;
import com.hyperion.optimizer.core.physics.FastFluidEngine;
import com.hyperion.optimizer.core.physics.FastRedstoneEngine;
import com.hyperion.optimizer.core.physics.FastRegistryCache;
import com.hyperion.optimizer.core.physics.PathfindingCircuitBreaker;
import com.hyperion.optimizer.core.physics.SleepingHopperManager;
import com.hyperion.optimizer.core.physics.VoxelShapeFastCache;
import com.hyperion.optimizer.core.render.ColorCorrectionEngine;
import com.hyperion.optimizer.core.render.FpsStabilizerEngine;
import com.hyperion.optimizer.core.world.ClientWorldCacheStorage;
import com.hyperion.optimizer.core.world.FakeChunkManager;
import com.hyperion.optimizer.core.threading.HyperionThreadPoolManager;
import com.hyperion.optimizer.core.threading.ParallelChunkMesher;
import com.hyperion.optimizer.core.threading.MultiCoreEntityPhysicsEngine;
import com.hyperion.optimizer.core.threading.AsyncWorldTickDispatcher;
import com.hyperion.optimizer.core.threading.CpuCoreAffinityGovernor;

import java.util.logging.Logger;

public final class HyperionEngine {
    private static final Logger LOGGER = Logger.getLogger("Hyperion");
    private static final HyperionEngine INSTANCE = new HyperionEngine();

    private HyperionConfig config = new HyperionConfig();
    private boolean initialized = false;

    // Subsystems
    private ComputeCullEngine computeCullEngine;
    private MultiDrawIndirectManager multiDrawManager;
    private DecoupledHudManager hudManager;
    private EntityDepthCuller entityCuller;
    private StaticChestMeshBaker chestBaker;
    private ExperienceOrbMerger xpMerger;
    private AnimationLodManager animationLod;
    private VoxelShapeFastCache voxelCache;
    private SleepingHopperManager hopperManager;
    private PathfindingCircuitBreaker pathCircuitBreaker;
    private AsyncBitsetLightEngine lightEngine;
    private ColorCorrectionEngine colorCorrectionEngine;
    private FpsStabilizerEngine fpsStabilizer;
    private PacketFlushConsolidator networkConsolidator;
    private ClientWorldCacheStorage worldCacheStorage;
    private FakeChunkManager fakeChunkManager;
    private AsyncAudioEngine audioEngine;
    private FastExplosionEngine explosionEngine;
    private FastRedstoneEngine redstoneEngine;
    private SpatialCollisionEngine collisionEngine;
    private FastFluidEngine fluidEngine;
    private FastParticleEngine particleEngine;
    private FastRegistryCache registryCache;
    private com.hyperion.optimizer.core.gpu.amd.AmdGpuAccelerator amdAccelerator;
    private com.hyperion.optimizer.core.gpu.dualgpu.DualGpuManager dualGpuManager;

    // Multi-Core Multithreading Subsystems
    private HyperionThreadPoolManager threadPoolManager;
    private ParallelChunkMesher parallelChunkMesher;
    private MultiCoreEntityPhysicsEngine multiCoreEntityPhysics;
    private AsyncWorldTickDispatcher asyncWorldTickDispatcher;
    private CpuCoreAffinityGovernor cpuAffinityGovernor;

    private HyperionEngine() {}

    public static HyperionEngine getInstance() {
        return INSTANCE;
    }

    public synchronized void initialize(HyperionConfig customConfig) {
        if (this.initialized) {
            return;
        }
        if (customConfig != null) {
            this.config = customConfig;
        }

        LOGGER.info("[Hyperion] Initializing Sovereign Multi-Core Cross-Platform Optimizer Core...");

        // 1. CPU Multi-Core Thread Pool Orchestrator
        this.threadPoolManager = HyperionThreadPoolManager.getInstance();
        this.threadPoolManager.reconfigurePools(
                config.enableCpuMultithreading,
                config.cpuThreadAllocationMode,
                config.customCpuCoreCount
        );
        this.parallelChunkMesher = new ParallelChunkMesher(config.enableParallelChunkMeshing, config.parallelChunkMesherThreads);
        this.multiCoreEntityPhysics = new MultiCoreEntityPhysicsEngine(config.enableMultiCoreEntityPhysics, config.entityPhysicsBatchSize);
        this.asyncWorldTickDispatcher = new AsyncWorldTickDispatcher(config.enableAsyncWorldTickDispatcher);
        this.cpuAffinityGovernor = new CpuCoreAffinityGovernor(config.enableCpuCoreAffinity, config.enableThreadPriorityBoost);

        // 2. Memory & Pool Initialization
        PrimitiveVectorPool.init();

        // 3. Physics, Collision & AI (Lithium Core)
        this.voxelCache = new VoxelShapeFastCache(config.enableVoxelCollisionFastCache);
        this.hopperManager = new SleepingHopperManager(config.enableSleepingHoppers);
        this.pathCircuitBreaker = new PathfindingCircuitBreaker(
            config.enablePathfindingCircuitBreaker,
            config.maxPathfindingFailuresBeforeBackoff
        );

        // 4. Entity & Tile Optimizations
        this.chestBaker = new StaticChestMeshBaker(config.enableStaticFastChests);
        this.entityCuller = new EntityDepthCuller(config.enableEntityDepthCulling, config.entityCullingMaxDistance);
        this.xpMerger = new ExperienceOrbMerger(
            config.enableExperienceOrbClumping,
            config.orbClumpRadius,
            config.maxOrbClumpCapacity
        );
        this.animationLod = new AnimationLodManager(
            config.enableAnimationLod,
            config.animationLodNearDistance,
            config.animationLodFarDistance
        );

        // 5. GPU-Driven Engine (AMD / Intel / Nvidia)
        this.computeCullEngine = new ComputeCullEngine(
            config.enableGpuDrivenRenderer,
            config.enableHiZOcclusionCulling
        );
        this.multiDrawManager = new MultiDrawIndirectManager(config.maxGpuIndirectDrawBatchSize);

        // 6. Decoupled HUD (F1 Mode)
        this.hudManager = new DecoupledHudManager(
            config.enableDecoupledHud,
            config.hudTargetFramerate,
            config.enableHudDynamicRefresh
        );

        // 7. Hybrid Light Engine (Starlight + Phosphor)
        this.lightEngine = new AsyncBitsetLightEngine(
            config.enableHybridLightEngine,
            config.lightWorkerThreads
        );

        // 7.1 Color Correction, Anti-Black-Crush & HDR Tone Mapping Engine
        this.colorCorrectionEngine = new ColorCorrectionEngine(config.enableColorCorrection);
        this.colorCorrectionEngine.configure(config);

        // 7.2 Dynamic FPS Stabilizer & Frame Pacing Engine (Maintains 350+ FPS)
        this.fpsStabilizer = new FpsStabilizerEngine(config.enableFpsStabilizer);
        this.fpsStabilizer.configure(config);

        // 8. Network Consolidator (Krypton)
        this.networkConsolidator = new PacketFlushConsolidator(config.enablePacketFlushConsolidation);

        // 9. World Cache Storage (Bobby)
        this.worldCacheStorage = new ClientWorldCacheStorage(config.enableClientWorldCache);
        this.fakeChunkManager = new FakeChunkManager(config.clientMaxViewDistance, this.worldCacheStorage);

        // 10. Async Audio Engine
        this.audioEngine = new AsyncAudioEngine(config.enableAsyncAudio, config.maxSimultaneousSoundChannels);

        // 11. Physics & Logic Engines
        this.explosionEngine = new FastExplosionEngine(config.enableFastExplosions, config.explosionMaxRaySteps);
        this.redstoneEngine = new FastRedstoneEngine(
            config.enableFastRedstoneEngine,
            config.enableRedstoneLightSuppression,
            config.enableComparatorDiscreteCaching,
            config.enableHopperContainerOcclusionFastPath,
            config.enableBatchNeighborUpdates
        );
        this.collisionEngine = new SpatialCollisionEngine(
            config.enableSpatialCollisionGrid,
            config.maxCollisionsPerEntity,
            config.brainThrottleDistance
        );
        this.fluidEngine = new FastFluidEngine(config.enableFastFluidDynamics);
        this.particleEngine = new FastParticleEngine(
            config.enableFastParticleEngine,
            config.maxParticlesPerBlockPerSecond,
            config.maxParticleDistance
        );
        this.registryCache = new FastRegistryCache(config.enableFastRegistryCache);

        // 12. AMD Hardware Acceleration & Dual-GPU Management
        com.hyperion.optimizer.core.gpu.amd.AmdArchitectureProfile amdProf =
            com.hyperion.optimizer.core.gpu.amd.AmdArchitectureProfile.AUTO;
        try {
            if (config.amdArchitectureProfile != null) {
                amdProf = com.hyperion.optimizer.core.gpu.amd.AmdArchitectureProfile.valueOf(config.amdArchitectureProfile);
            }
        } catch (Exception ignored) {}
        this.amdAccelerator = new com.hyperion.optimizer.core.gpu.amd.AmdGpuAccelerator(
            config.enableAmdHardwareAcceleration,
            amdProf,
            config.enableAmd2GbVramGuard
        );

        com.hyperion.optimizer.core.gpu.dualgpu.DualGpuWorkloadDispatcher dualMode =
            com.hyperion.optimizer.core.gpu.dualgpu.DualGpuWorkloadDispatcher.AUTO_BALANCED;
        try {
            if (config.dualGpuMode != null) {
                dualMode = com.hyperion.optimizer.core.gpu.dualgpu.DualGpuWorkloadDispatcher.valueOf(config.dualGpuMode);
            }
        } catch (Exception ignored) {}
        this.dualGpuManager = new com.hyperion.optimizer.core.gpu.dualgpu.DualGpuManager(
            config.enableDualGpuSupport,
            dualMode
        );

        this.initialized = true;
        LOGGER.info("[Hyperion] Optimizer Core Initialized with Multi-Core and GPU Acceleration.");
    }

    public HyperionConfig getConfig() {
        return config;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public ComputeCullEngine getComputeCullEngine() {
        return computeCullEngine;
    }

    public MultiDrawIndirectManager getMultiDrawManager() {
        return multiDrawManager;
    }

    public DecoupledHudManager getHudManager() {
        return hudManager;
    }

    public EntityDepthCuller getEntityCuller() {
        return entityCuller;
    }

    public StaticChestMeshBaker getChestBaker() {
        return chestBaker;
    }

    public ExperienceOrbMerger getXpMerger() {
        return xpMerger;
    }

    public AnimationLodManager getAnimationLod() {
        return animationLod;
    }

    public VoxelShapeFastCache getVoxelCache() {
        return voxelCache;
    }

    public SleepingHopperManager getHopperManager() {
        return hopperManager;
    }

    public PathfindingCircuitBreaker getPathCircuitBreaker() {
        return pathCircuitBreaker;
    }

    public AsyncBitsetLightEngine getLightEngine() {
        return lightEngine;
    }

    public ColorCorrectionEngine getColorCorrectionEngine() {
        return colorCorrectionEngine;
    }

    public FpsStabilizerEngine getFpsStabilizer() {
        return fpsStabilizer;
    }

    public PacketFlushConsolidator getNetworkConsolidator() {
        return networkConsolidator;
    }

    public ClientWorldCacheStorage getWorldCacheStorage() {
        return worldCacheStorage;
    }

    public FakeChunkManager getFakeChunkManager() {
        return fakeChunkManager;
    }

    public AsyncAudioEngine getAudioEngine() {
        return audioEngine;
    }

    public FastExplosionEngine getExplosionEngine() {
        return explosionEngine;
    }

    public FastRedstoneEngine getRedstoneEngine() {
        return redstoneEngine;
    }

    public SpatialCollisionEngine getCollisionEngine() {
        return collisionEngine;
    }

    public FastFluidEngine getFluidEngine() {
        return fluidEngine;
    }

    public FastParticleEngine getParticleEngine() {
        return particleEngine;
    }

    public FastRegistryCache getRegistryCache() {
        return registryCache;
    }

    public HyperionProfilerOverlay getProfiler() {
        return HyperionProfilerOverlay.getInstance();
    }

    public com.hyperion.optimizer.core.gpu.amd.AmdGpuAccelerator getAmdAccelerator() {
        return amdAccelerator;
    }

    public com.hyperion.optimizer.core.gpu.dualgpu.DualGpuManager getDualGpuManager() {
        return dualGpuManager;
    }

    public HyperionThreadPoolManager getThreadPoolManager() {
        return threadPoolManager;
    }

    public ParallelChunkMesher getParallelChunkMesher() {
        return parallelChunkMesher;
    }

    public MultiCoreEntityPhysicsEngine getMultiCoreEntityPhysics() {
        return multiCoreEntityPhysics;
    }

    public AsyncWorldTickDispatcher getAsyncWorldTickDispatcher() {
        return asyncWorldTickDispatcher;
    }

    public CpuCoreAffinityGovernor getCpuAffinityGovernor() {
        return cpuAffinityGovernor;
    }

    public synchronized void reloadConfig(HyperionConfig newConfig) {
        if (newConfig != null) {
            this.config = newConfig;
            if (threadPoolManager != null) {
                threadPoolManager.reconfigurePools(
                        newConfig.enableCpuMultithreading,
                        newConfig.cpuThreadAllocationMode,
                        newConfig.customCpuCoreCount
                );
            }
            if (amdAccelerator != null) {
                com.hyperion.optimizer.core.gpu.amd.AmdArchitectureProfile prof =
                    com.hyperion.optimizer.core.gpu.amd.AmdArchitectureProfile.AUTO;
                try {
                    if (newConfig.amdArchitectureProfile != null) {
                        prof = com.hyperion.optimizer.core.gpu.amd.AmdArchitectureProfile.valueOf(newConfig.amdArchitectureProfile);
                    }
                } catch (Exception ignored) {}
                amdAccelerator.calibrateProfile(prof);
            }
            if (dualGpuManager != null) {
                com.hyperion.optimizer.core.gpu.dualgpu.DualGpuWorkloadDispatcher mode =
                    com.hyperion.optimizer.core.gpu.dualgpu.DualGpuWorkloadDispatcher.AUTO_BALANCED;
                try {
                    if (newConfig.dualGpuMode != null) {
                        mode = com.hyperion.optimizer.core.gpu.dualgpu.DualGpuWorkloadDispatcher.valueOf(newConfig.dualGpuMode);
                    }
                } catch (Exception ignored) {}
                dualGpuManager.setMode(mode);
                dualGpuManager.setOffloadHud(newConfig.enableSecondaryGpuHudOffload);
                dualGpuManager.setOffloadLight(newConfig.enableSecondaryGpuLightOffload);
                dualGpuManager.setOffloadParticles(newConfig.enableSecondaryGpuParticleOffload);
            }
            if (colorCorrectionEngine != null) {
                colorCorrectionEngine.configure(newConfig);
            }
            if (fpsStabilizer != null) {
                fpsStabilizer.configure(newConfig);
            }
            LOGGER.info("[Hyperion] Configuration hot-reloaded successfully.");
        }
    }

    public synchronized void shutdown() {
        if (lightEngine != null) {
            lightEngine.shutdown();
        }
        if (worldCacheStorage != null) {
            worldCacheStorage.clear();
        }
        if (fakeChunkManager != null) {
            fakeChunkManager.clear();
        }
        if (chestBaker != null) {
            chestBaker.clear();
        }
        if (hopperManager != null) {
            hopperManager.clear();
        }
        if (collisionEngine != null) {
            collisionEngine.clearGrid();
        }
        if (fluidEngine != null) {
            fluidEngine.clear();
        }
        if (particleEngine != null) {
            particleEngine.clear();
        }
        if (registryCache != null) {
            registryCache.invalidate();
        }
        if (audioEngine != null) {
            audioEngine.shutdown();
        }
        if (pathCircuitBreaker != null) {
            pathCircuitBreaker.clear();
        }
        if (networkConsolidator != null) {
            networkConsolidator.clear();
        }
        LOGGER.info("[Hyperion] Subsystems safely shut down.");
    }
}
