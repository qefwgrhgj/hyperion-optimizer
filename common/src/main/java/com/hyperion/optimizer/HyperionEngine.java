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
    private com.hyperion.optimizer.core.gpu.FastHdTextureEngine fastHdTextureEngine;
    private com.hyperion.optimizer.core.render.FancyGraphicsOptimizer fancyGraphicsOptimizer;
    private com.hyperion.optimizer.core.render.FastCloudEngine fastCloudEngine;
    private com.hyperion.optimizer.core.gpu.GpuThermalPowerGuard gpuThermalGuard;
    private com.hyperion.optimizer.core.render.ChunkLodManager chunkLodManager;
    private com.hyperion.optimizer.core.render.AggressiveFaceCuller aggressiveFaceCuller;
    private com.hyperion.optimizer.core.gpu.GpuInstancingEngine gpuInstancingEngine;
    private com.hyperion.optimizer.core.gpu.GpuResetCrashGuard gpuCrashGuard;
    private com.hyperion.optimizer.gui.HyperionKeyBindingManager keyBindingManager;

    // Voxel LOD Ultra-Distance Subsystems (2048+ Chunks Horizon)
    private com.hyperion.optimizer.core.lod.voxel.VoxelHierarchicalMipTree voxelMipTree;
    private com.hyperion.optimizer.core.lod.voxel.VoxelSectionStorage voxelSectionStorage;
    private com.hyperion.optimizer.core.lod.voxel.VoxelLodRenderer voxelLodRenderer;
    private com.hyperion.optimizer.core.lod.voxel.VoxelHorizonBlender voxelHorizonBlender;
    private com.hyperion.optimizer.core.lod.voxel.VoxelPregenIngestEngine voxelIngestEngine;

    // Mod Ecosystem Compatibility Subsystems
    private com.hyperion.optimizer.compat.HyperionModCompatManager modCompatManager;
    private com.hyperion.optimizer.compat.IrisShaderCompatPipeline irisShaderPipeline;

    // Advanced Technical Subsystems (Particle Core, BadOptimizations, Mobtimizations, Palladium)
    private com.hyperion.optimizer.core.particle.AdvancedParticleEngine advancedParticleEngine;
    private com.hyperion.optimizer.core.micro.BadOptimizationsEngine badOptimizationsEngine;
    private com.hyperion.optimizer.core.ai.MobAiOptimizer mobAiOptimizer;
    private com.hyperion.optimizer.core.animation.PalladiumCapabilityCache palladiumCache;

    // Multi-Core Multithreading Subsystems
    private HyperionThreadPoolManager threadPoolManager;
    private ParallelChunkMesher parallelChunkMesher;
    private MultiCoreEntityPhysicsEngine multiCoreEntityPhysics;
    private AsyncWorldTickDispatcher asyncWorldTickDispatcher;
    private CpuCoreAffinityGovernor cpuAffinityGovernor;
    private com.hyperion.optimizer.core.render.ShineStylizedEngine shineEngine;

    private HyperionEngine() {}

    public static HyperionEngine getInstance() {
        if (!INSTANCE.initialized) {
            synchronized (INSTANCE) {
                if (!INSTANCE.initialized) {
                    INSTANCE.initialize(com.hyperion.optimizer.api.HyperionConfigStorage.loadOrCreate());
                }
            }
        }
        return INSTANCE;
    }

    public synchronized void initialize(HyperionConfig customConfig) {
        if (this.initialized) {
            if (customConfig != null) {
                reloadConfig(customConfig);
            }
            return;
        }
        if (customConfig != null) {
            this.config = customConfig;
        }

        LOGGER.info("[Hyperion] Initializing Sovereign Multi-Core Cross-Platform Optimizer Core...");

        if (isDedicatedServer()) {
            LOGGER.info("[Hyperion] Dedicated Server environment detected - disabling client-only rendering modules.");
            config.enableGpuDrivenRenderer = false;
            config.enableHiZOcclusionCulling = false;
            config.enableDecoupledHud = false;
            config.enableColorCorrection = false;
            config.enableFpsStabilizer = false;
            config.enableClientWorldCache = false;
            config.enableAsyncAudio = false;
            config.enableVoxelLodEngine = false;
            config.enableGpuBlockInstancing = false;
            config.enableChunkLod = false;
            config.enableAggressiveFaceCulling = false;
        }

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

        // 3. Physics, Collision & AI (Core Physics Engine)
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

        // 7. Hybrid Light Engine (Async Bitset)
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

        // 8. Network Consolidator
        this.networkConsolidator = new PacketFlushConsolidator(config.enablePacketFlushConsolidation);

        // 9. World Cache Storage
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

        // 13. HD Resource Packs & Fancy/Fabulous Graphics Optimizers
        this.fastHdTextureEngine = new com.hyperion.optimizer.core.gpu.FastHdTextureEngine(config.enableHdTextureOptimization);
        this.fastHdTextureEngine.configure(config);
        this.fancyGraphicsOptimizer = new com.hyperion.optimizer.core.render.FancyGraphicsOptimizer(
            config.enableSmartLeavesCulling || config.enableFabulousGraphicsOptimization
        );
        this.fancyGraphicsOptimizer.configure(config);
        this.fastCloudEngine = new com.hyperion.optimizer.core.render.FastCloudEngine(config.enableFastCloudEngine);
        this.fastCloudEngine.configure(config);
        this.gpuThermalGuard = new com.hyperion.optimizer.core.gpu.GpuThermalPowerGuard(config.enableGpuThermalPowerGuard);
        this.gpuThermalGuard.configure(config);

        // 14. Chunk LOD, Aggressive Face Culling, GPU Instancing, Crash Guard & Keybindings
        this.chunkLodManager = new com.hyperion.optimizer.core.render.ChunkLodManager(config.enableChunkLod);
        this.chunkLodManager.configure(config);
        this.aggressiveFaceCuller = new com.hyperion.optimizer.core.render.AggressiveFaceCuller(config.enableAggressiveFaceCulling);
        this.aggressiveFaceCuller.configure(config);
        this.gpuInstancingEngine = new com.hyperion.optimizer.core.gpu.GpuInstancingEngine(config.enableGpuBlockInstancing, config.maxInstancesPerBatch);
        this.gpuInstancingEngine.configure(config);
        this.gpuCrashGuard = new com.hyperion.optimizer.core.gpu.GpuResetCrashGuard(config.enableGpuResetCrashGuard);
        this.gpuCrashGuard.configure(config);
        this.keyBindingManager = com.hyperion.optimizer.gui.HyperionKeyBindingManager.getInstance();
        this.keyBindingManager.setEnabled(config.enableConfigMenuShortcut);
        if (config.enableConfigMenuShortcut) {
            this.keyBindingManager.startGlfwKeyPoller();
        }

        // 15. Voxel LOD Ultra-Distance Horizon Engine (Voxy 2048+ Chunks)
        this.voxelMipTree = new com.hyperion.optimizer.core.lod.voxel.VoxelHierarchicalMipTree(config.enableVoxelLodEngine);
        this.voxelMipTree.configure(config);
        this.voxelSectionStorage = new com.hyperion.optimizer.core.lod.voxel.VoxelSectionStorage();
        this.voxelLodRenderer = new com.hyperion.optimizer.core.lod.voxel.VoxelLodRenderer(config.enableVoxelLodEngine, 65536);
        this.voxelLodRenderer.configure(config);
        this.voxelHorizonBlender = new com.hyperion.optimizer.core.lod.voxel.VoxelHorizonBlender(config.enableVoxelHorizonBlending);
        this.voxelHorizonBlender.configure(config);
        this.voxelIngestEngine = new com.hyperion.optimizer.core.lod.voxel.VoxelPregenIngestEngine(
            config.enableVoxelLodEngine, this.voxelMipTree, this.voxelSectionStorage
        );
        this.voxelIngestEngine.configure(config);

        // 16. Universal Mod Ecosystem Compatibility & Iris Pipeline
        this.modCompatManager = com.hyperion.optimizer.compat.HyperionModCompatManager.getInstance();
        this.irisShaderPipeline = com.hyperion.optimizer.compat.IrisShaderCompatPipeline.getInstance();

        // 17. Advanced Particle Core & Vector Math Engine
        this.advancedParticleEngine = new com.hyperion.optimizer.core.particle.AdvancedParticleEngine(config.enableFastParticleEngine, 16384);
        this.advancedParticleEngine.configure(config);

        // 18. BadOptimizations Micro-Optimization Hot-Path Engine
        this.badOptimizationsEngine = new com.hyperion.optimizer.core.micro.BadOptimizationsEngine(config.enableGpuDrivenRenderer);
        this.badOptimizationsEngine.configure(config);

        // 19. Mobtimizations Entity AI & Pathfinding Throttle Optimizer
        this.mobAiOptimizer = new com.hyperion.optimizer.core.ai.MobAiOptimizer(config.enablePathfindingCircuitBreaker);
        this.mobAiOptimizer.configure(config);

        // 20. Palladium Entity Capability & Animation Matrix Cache
        this.palladiumCache = new com.hyperion.optimizer.core.animation.PalladiumCapabilityCache(true);

        // 21. Shine Stylized Visual Engine (Bloom, Colored Light, Rim Light, Cel-Shading)
        this.shineEngine = com.hyperion.optimizer.core.render.ShineStylizedEngine.getInstance();
        this.shineEngine.configure(config);

        this.initialized = true;
        LOGGER.info("[Hyperion] Optimizer Core Initialized with Multi-Core, GPU Voxel LOD (2048+), Particle Core, BadOptimizations & Mob AI.");
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
            if (fastHdTextureEngine != null) {
                fastHdTextureEngine.configure(newConfig);
            }
            if (fancyGraphicsOptimizer != null) {
                fancyGraphicsOptimizer.configure(newConfig);
            }
            if (fastCloudEngine != null) {
                fastCloudEngine.configure(newConfig);
            }
            if (gpuThermalGuard != null) {
                gpuThermalGuard.configure(newConfig);
            }
            if (chunkLodManager != null) {
                chunkLodManager.configure(newConfig);
            }
            if (aggressiveFaceCuller != null) {
                aggressiveFaceCuller.configure(newConfig);
            }
            if (gpuInstancingEngine != null) {
                gpuInstancingEngine.configure(newConfig);
            }
            if (gpuCrashGuard != null) {
                gpuCrashGuard.configure(newConfig);
            }
            if (keyBindingManager != null) {
                keyBindingManager.setEnabled(newConfig.enableConfigMenuShortcut);
            }
            if (voxelMipTree != null) {
                voxelMipTree.configure(newConfig);
            }
            if (voxelLodRenderer != null) {
                voxelLodRenderer.configure(newConfig);
            }
            if (voxelHorizonBlender != null) {
                voxelHorizonBlender.configure(newConfig);
            }
            if (voxelIngestEngine != null) {
                voxelIngestEngine.configure(newConfig);
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
        if (fastHdTextureEngine != null) {
            fastHdTextureEngine.resetFrameMetrics();
        }
        if (fancyGraphicsOptimizer != null) {
            fancyGraphicsOptimizer.reset();
        }
        if (fastCloudEngine != null) {
            fastCloudEngine.reset();
        }
        if (gpuThermalGuard != null) {
            gpuThermalGuard.reset();
        }
        if (chunkLodManager != null) {
            chunkLodManager.reset();
        }
        if (aggressiveFaceCuller != null) {
            aggressiveFaceCuller.reset();
        }
        if (gpuInstancingEngine != null) {
            gpuInstancingEngine.freeDirectBuffers();
        }
        if (gpuCrashGuard != null) {
            gpuCrashGuard.reset();
        }
        if (keyBindingManager != null) {
            keyBindingManager.reset();
        }
        if (voxelMipTree != null) {
            voxelMipTree.reset();
        }
        if (voxelSectionStorage != null) {
            voxelSectionStorage.clear();
        }
        if (voxelLodRenderer != null) {
            voxelLodRenderer.freeDirectBuffers();
        }
        if (voxelIngestEngine != null) {
            voxelIngestEngine.reset();
        }
        if (irisShaderPipeline != null) {
            irisShaderPipeline.reset();
        }
        if (advancedParticleEngine != null) {
            advancedParticleEngine.freeDirectBuffers();
        }
        if (badOptimizationsEngine != null) {
            badOptimizationsEngine.reset();
        }
        if (mobAiOptimizer != null) {
            mobAiOptimizer.reset();
        }
        if (palladiumCache != null) {
            palladiumCache.reset();
        }
        LOGGER.info("[Hyperion] Subsystems safely shut down.");
    }

    public com.hyperion.optimizer.core.gpu.FastHdTextureEngine getFastHdTextureEngine() {
        return fastHdTextureEngine;
    }

    public com.hyperion.optimizer.core.render.FancyGraphicsOptimizer getFancyGraphicsOptimizer() {
        return fancyGraphicsOptimizer;
    }

    public com.hyperion.optimizer.core.render.FastCloudEngine getFastCloudEngine() {
        return fastCloudEngine;
    }

    public com.hyperion.optimizer.core.gpu.GpuThermalPowerGuard getGpuThermalGuard() {
        return gpuThermalGuard;
    }

    public com.hyperion.optimizer.core.render.ChunkLodManager getChunkLodManager() {
        return chunkLodManager;
    }

    public com.hyperion.optimizer.core.render.AggressiveFaceCuller getAggressiveFaceCuller() {
        return aggressiveFaceCuller;
    }

    public com.hyperion.optimizer.core.gpu.GpuInstancingEngine getGpuInstancingEngine() {
        return gpuInstancingEngine;
    }

    public com.hyperion.optimizer.core.gpu.GpuResetCrashGuard getGpuCrashGuard() {
        return gpuCrashGuard;
    }

    public com.hyperion.optimizer.gui.HyperionKeyBindingManager getKeyBindingManager() {
        return keyBindingManager;
    }

    public com.hyperion.optimizer.core.lod.voxel.VoxelHierarchicalMipTree getVoxelMipTree() {
        return voxelMipTree;
    }

    public com.hyperion.optimizer.core.lod.voxel.VoxelSectionStorage getVoxelSectionStorage() {
        return voxelSectionStorage;
    }

    public com.hyperion.optimizer.core.lod.voxel.VoxelLodRenderer getVoxelLodRenderer() {
        return voxelLodRenderer;
    }

    public com.hyperion.optimizer.core.lod.voxel.VoxelHorizonBlender getVoxelHorizonBlender() {
        return voxelHorizonBlender;
    }

    public com.hyperion.optimizer.core.lod.voxel.VoxelPregenIngestEngine getVoxelIngestEngine() {
        return voxelIngestEngine;
    }

    public com.hyperion.optimizer.compat.HyperionModCompatManager getModCompatManager() {
        return modCompatManager;
    }

    public com.hyperion.optimizer.compat.IrisShaderCompatPipeline getIrisShaderPipeline() {
        return irisShaderPipeline;
    }

    public com.hyperion.optimizer.core.particle.AdvancedParticleEngine getAdvancedParticleEngine() {
        return advancedParticleEngine;
    }

    public com.hyperion.optimizer.core.micro.BadOptimizationsEngine getBadOptimizationsEngine() {
        return badOptimizationsEngine;
    }

    public com.hyperion.optimizer.core.ai.MobAiOptimizer getMobAiOptimizer() {
        return mobAiOptimizer;
    }

    public com.hyperion.optimizer.core.animation.PalladiumCapabilityCache getPalladiumCache() {
        return palladiumCache;
    }

    public com.hyperion.optimizer.core.gpu.dualgpu.DualGpuThermalFallback getThermalFallback() {
        return dualGpuManager != null ? dualGpuManager.getThermalFallback() : null;
    }

    /**
     * Handles player respawn or long-distance dimension teleport.
     * Flushes stale cave data, resets fallback states, and triggers warm-up grace period.
     */
    public void onPlayerRespawnOrTeleport() {
        if (dualGpuManager != null && dualGpuManager.getThermalFallback() != null) {
            dualGpuManager.getThermalFallback().onRespawnOrTeleport();
        }
        if (hudManager != null) {
            hudManager.onPlayerRespawn();
        }
        if (collisionEngine != null) {
            collisionEngine.clearGrid();
        }
        if (badOptimizationsEngine != null) {
            badOptimizationsEngine.invalidateBiomeCaches();
        }
        if (palladiumCache != null) {
            palladiumCache.reset();
        }
        if (chestBaker != null) {
            chestBaker.clear();
        }
    }

    /**
     * Handles resource pack reload, texture reload (F3+T), or switching texture packs in video settings.
     * Flushes animated sprite tracking, biome blend caches, lightmap dirty states, and resets chunk geometry.
     */
    public void onResourceReload() {
        if (fastHdTextureEngine != null) {
            fastHdTextureEngine.onResourceReload();
        }
        if (badOptimizationsEngine != null) {
            badOptimizationsEngine.invalidateBiomeCaches();
            badOptimizationsEngine.markLightmapDirty();
        }
        if (colorCorrectionEngine != null) {
            colorCorrectionEngine.configure(config);
        }
        if (chestBaker != null) {
            chestBaker.clear();
        }
        if (fancyGraphicsOptimizer != null) {
            fancyGraphicsOptimizer.reset();
        }
        if (chunkLodManager != null) {
            chunkLodManager.reset();
        }
        if (voxelSectionStorage != null) {
            voxelSectionStorage.clear();
        }
        if (shineEngine != null) {
            shineEngine.configure(config);
        }
        LOGGER.info("[Hyperion] Resource pack reload handled: Texture caches, lightmaps & sprite trackers safely invalidated.");
    }

    public com.hyperion.optimizer.core.render.ShineStylizedEngine getShineEngine() {
        return shineEngine != null ? shineEngine : com.hyperion.optimizer.core.render.ShineStylizedEngine.getInstance();
    }

    public static boolean isDedicatedServer() {
        try {
            Class.forName("net.minecraft.client.Minecraft");
            return false;
        } catch (Throwable ignored) {
            try {
                Class.forName("net.minecraft.class_310");
                return false;
            } catch (Throwable ignored2) {
                return true;
            }
        }
    }
}
