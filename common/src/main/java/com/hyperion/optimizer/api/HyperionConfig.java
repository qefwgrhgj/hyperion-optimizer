package com.hyperion.optimizer.api;

public class HyperionConfig {
    // 1. Graphics Settings (Настройки графики)
    public boolean enableGpuDrivenRenderer = true;
    public boolean enableHiZOcclusionCulling = true;
    public int maxGpuIndirectDrawBatchSize = 65536;
    public boolean enableDecoupledHud = true;
    public int hudTargetFramerate = 60;
    public boolean enableHudDynamicRefresh = true;
    public boolean enableFastParticleEngine = true;
    public int maxParticlesPerBlockPerSecond = 5;
    public double maxParticleDistance = 48.0;
    public boolean enableFpsStabilizer = true;
    public int targetFramerate = 350;
    public int maxChunkUploadsPerFrame = 3;
    public boolean enableDynamicWorkBudgeting = true;
    public boolean enableAggressiveCaveCulling = true;
    public boolean enableBlockEntityDistanceCulling = true;
    public double blockEntityCullDistance = 32.0;
    public boolean enableColorCorrection = true;
    public String colorGradingMode = "VIBRANT_HDR"; // VIBRANT_HDR, NIGHT_VISION_CLEAR, CINEMATIC_FILMIC, NATURAL_BALANCED, CUSTOM
    public double colorGammaBoost = 1.00;
    public double colorVibrance = 1.15;
    public double colorSaturation = 1.05;
    public double colorContrast = 1.02;
    public double colorBlackCrushCompensation = 0.08;
    public double colorNightAmbientBoost = 0.10;
    public int colorTemperature = 6500;
    public boolean enableColorDebanding = true;
    public boolean enableStaticFastChests = true;
    public boolean enableEntityDepthCulling = true;
    public double entityCullingMaxDistance = 64.0;
    public boolean enableAnimationLod = true;
    public double animationLodNearDistance = 12.0;
    public double animationLodFarDistance = 28.0;

    // HD Resource Packs & Fancy/Fabulous Graphics
    public boolean enableHdTextureOptimization = true;
    public boolean enableAsyncAnimatedTextures = true;
    public boolean enableAdaptiveMipmapPacing = true;
    public int maxHdAtlasDimension = 16384;
    public boolean enableSmartLeavesCulling = true;
    public boolean enableFabulousGraphicsOptimization = true;
    public boolean enableTranslucentSortThrottling = true;

    // Fast Cloud Engine
    public boolean enableFastCloudEngine = true;
    public boolean enableCloudCulling = true;
    public boolean enableCloudMeshReuse = true;

    // 2. Video Card / GPU Settings (Настройки видеокарт)
    public boolean enableAmdHardwareAcceleration = true;
    public String amdArchitectureProfile = "AUTO"; // AUTO, RADEON_RX500_POLARIS, RADEON_540_LEXA, RADEON_VEGA_8_APU, RDNA_MODERN
    public int amdWavefrontSize = 64; // 64 for Polaris/Vega, 32 for RDNA
    public boolean enableAmdPrimitiveDiscard = true;
    public boolean enableAmdMultiDrawIndirectCount = true;
    public boolean enableAmdPersistentCoherentBuffers = true;
    public boolean enableAmd2GbVramGuard = true;
    public boolean enableAmdUmaZeroCopy = true;
    public boolean enableDualGpuSupport = true;
    public String dualGpuMode = "AUTO_BALANCED"; // OFF, AUTO_BALANCED, DEDICATED_IGPU_HUD_LIGHT, CUSTOM
    public int primaryGpuIndex = 0;
    public int secondaryGpuIndex = 1;
    public boolean enableSecondaryGpuHudOffload = true;
    public boolean enableSecondaryGpuLightOffload = true;
    public boolean enableSecondaryGpuParticleOffload = true;

    // GPU Thermal & Power Spike Guard (Anti-Coil-Whine)
    public boolean enableGpuThermalPowerGuard = true;
    public boolean enableMenuFpsCap = true;
    public int menuMaxFramerate = 60;
    public boolean enableBackgroundFpsCap = true;
    public int backgroundMaxFramerate = 20;
    public boolean enableCoilWhineSuppression = true;
    public int maxPeakFramerateCap = 500;

    // 3. Processor & Multithreading Settings (Настройки процессора)
    public boolean enableCpuMultithreading = true;
    public String cpuThreadAllocationMode = "AUTO_DETECT_CORES"; // AUTO_DETECT_CORES, ALL_CORES, BALANCED_N_MINUS_1, CUSTOM
    public int customCpuCoreCount = 8;
    public boolean enableParallelChunkMeshing = true;
    public int parallelChunkMesherThreads = Math.max(2, (Runtime.getRuntime().availableProcessors() * 5) / 8);
    public boolean enableMultiCoreEntityPhysics = true;
    public int entityPhysicsBatchSize = 64;
    public boolean enableAsyncWorldTickDispatcher = true;
    public boolean enableCpuCoreAffinity = true;
    public boolean enableThreadPriorityBoost = true;
    public boolean enableSimdVectorAcceleration = true;

    // Physics, Collision & Redstone (Lithium Subsystem)
    public boolean enableVoxelCollisionFastCache = true;
    public boolean enableSleepingHoppers = true;
    public boolean enablePathfindingCircuitBreaker = true;
    public int maxPathfindingFailuresBeforeBackoff = 3;
    public boolean enableFastExplosions = true;
    public int explosionMaxRaySteps = 64;
    public boolean enableFastRedstoneEngine = true;
    public boolean enableRedstoneLightSuppression = true;
    public boolean enableComparatorDiscreteCaching = true;
    public boolean enableHopperContainerOcclusionFastPath = true;
    public boolean enableBatchNeighborUpdates = true;
    public boolean enableSpatialCollisionGrid = true;
    public int maxCollisionsPerEntity = 8;
    public double brainThrottleDistance = 32.0;
    public boolean enableFastFluidDynamics = true;
    public boolean enableFastMathLUT = true;
    public boolean enableFastRegistryCache = true;
    public boolean enableExperienceOrbClumping = true;
    public double orbClumpRadius = 2.0;
    public int maxOrbClumpCapacity = 50000;

    // Hybrid Light Engine (Starlight + Phosphor)
    public boolean enableHybridLightEngine = true;
    public boolean enableAsyncLightThreads = true;
    public int lightWorkerThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
    public boolean enableLinearMemoryPacking = true;

    // Networking, Memory & World Cache
    public boolean enablePacketFlushConsolidation = true;
    public boolean enableFastNativeCompression = true;
    public boolean enableClientWorldCache = true;
    public int clientMaxViewDistance = 32;
    public boolean autoCleanOldWorldCache = true;
    public boolean enableZeroAllocMathPooling = true;
    public boolean enableBlockStateDeduplication = true;

    // Audio
    public boolean enableAsyncAudio = true;
    public int maxSimultaneousSoundChannels = 32;
}
