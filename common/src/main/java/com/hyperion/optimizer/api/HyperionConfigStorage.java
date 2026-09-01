package com.hyperion.optimizer.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HyperionConfigStorage {
    private static final Logger LOGGER = Logger.getLogger("HyperionConfig");
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE_NAME = "hyperion.json";
    private static final String TMP_FILE_NAME = "hyperion.json.tmp";

    public enum Preset {
        POTATO_PC("Картофельный ПК (Макс. FPS)", "Агрессивное отсечение, 30 FPS HUD, лимит частиц 2/сек, дальность кэша 16"),
        BALANCED("Сбалансированный (Рекомендуемый)", "Оптимальный баланс высокой производительности и отличной графики"),
        HIGH_END("Максимальная графика (High-End GPU)", "Дальность прорисовки 64 чанка, 120 FPS HUD, полный Hi-Z culling"),
        EXTREME_MULTICORE_350FPS("Экстремальный Multi-Core (350+ FPS)", "Максимальная загрузка всех ядер CPU, 350 FPS Target, GPU-Driven рендер и VRAM Guard");

        private final String title;
        private final String description;

        Preset(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }
    }

    private HyperionConfigStorage() {}

    public static File getConfigFile() {
        File dir = new File(CONFIG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, CONFIG_FILE_NAME);
    }

    public static HyperionConfig loadOrCreate() {
        File file = getConfigFile();
        if (!file.exists()) {
            HyperionConfig defaultConfig = new HyperionConfig();
            save(defaultConfig);
            return defaultConfig;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int read;
            while ((read = reader.read(buf)) != -1) {
                sb.append(buf, 0, read);
            }
            return parseJson(sb.toString());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[Hyperion] Failed to read config file, falling back to defaults", e);
            return new HyperionConfig();
        }
    }

    public static boolean save(HyperionConfig config) {
        if (config == null) {
            return false;
        }
        File dir = new File(CONFIG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File tmpFile = new File(dir, TMP_FILE_NAME);
        File targetFile = new File(dir, CONFIG_FILE_NAME);

        String json = serializeJson(config);

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(tmpFile), StandardCharsets.UTF_8)) {
            writer.write(json);
            writer.flush();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[Hyperion] Failed to write temp config file", e);
            return false;
        }

        if (targetFile.exists() && !targetFile.delete()) {
            LOGGER.log(Level.WARNING, "[Hyperion] Could not delete old target config before atomic rename");
        }

        if (tmpFile.renameTo(targetFile)) {
            LOGGER.info("[Hyperion] Config saved successfully to " + targetFile.getAbsolutePath());
            return true;
        } else {
            LOGGER.log(Level.SEVERE, "[Hyperion] Failed to rename temp config file to final destination");
            return false;
        }
    }

    public static HyperionConfig applyPreset(Preset preset) {
        HyperionConfig cfg = new HyperionConfig();
        if (preset == null) {
            return cfg;
        }

        switch (preset) {
            case POTATO_PC:
                cfg.enableGpuDrivenRenderer = true;
                cfg.enableHiZOcclusionCulling = true;
                cfg.maxGpuIndirectDrawBatchSize = 32768;
                cfg.enableDecoupledHud = true;
                cfg.hudTargetFramerate = 30;
                cfg.enableHudDynamicRefresh = true;
                cfg.enableStaticFastChests = true;
                cfg.enableEntityDepthCulling = true;
                cfg.entityCullingMaxDistance = 48.0;
                cfg.enableAnimationLod = true;
                cfg.animationLodNearDistance = 8.0;
                cfg.animationLodFarDistance = 20.0;
                cfg.enableExperienceOrbClumping = true;
                cfg.orbClumpRadius = 3.0;
                cfg.maxOrbClumpCapacity = 100000;
                cfg.enableVoxelCollisionFastCache = true;
                cfg.enableSleepingHoppers = true;
                cfg.enablePathfindingCircuitBreaker = true;
                cfg.maxPathfindingFailuresBeforeBackoff = 2;
                cfg.enableFastExplosions = true;
                cfg.explosionMaxRaySteps = 32;
                cfg.enableFastRedstoneEngine = true;
                cfg.enableRedstoneLightSuppression = true;
                cfg.enableComparatorDiscreteCaching = true;
                cfg.enableHopperContainerOcclusionFastPath = true;
                cfg.enableBatchNeighborUpdates = true;
                cfg.enableSpatialCollisionGrid = true;
                cfg.maxCollisionsPerEntity = 4;
                cfg.brainThrottleDistance = 24.0;
                cfg.enableFastFluidDynamics = true;
                cfg.enableFastParticleEngine = true;
                cfg.maxParticlesPerBlockPerSecond = 2;
                cfg.maxParticleDistance = 32.0;
                cfg.enableFastMathLUT = true;
                cfg.enableFastRegistryCache = true;
                cfg.enableHybridLightEngine = true;
                cfg.enableAsyncLightThreads = true;
                cfg.lightWorkerThreads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
                cfg.enableLinearMemoryPacking = true;
                cfg.enablePacketFlushConsolidation = true;
                cfg.enableFastNativeCompression = true;
                cfg.enableClientWorldCache = true;
                cfg.clientMaxViewDistance = 16;
                cfg.autoCleanOldWorldCache = true;
                cfg.enableZeroAllocMathPooling = true;
                cfg.enableBlockStateDeduplication = true;
                cfg.enableAsyncAudio = true;
                cfg.maxSimultaneousSoundChannels = 16;
                cfg.enableColorCorrection = false;
                cfg.enableFpsStabilizer = true;
                cfg.targetFramerate = 144;
                cfg.maxChunkUploadsPerFrame = 2;
                cfg.enableDynamicWorkBudgeting = true;
                cfg.enableAggressiveCaveCulling = true;
                cfg.enableBlockEntityDistanceCulling = true;
                cfg.blockEntityCullDistance = 24.0;
                cfg.enableCpuMultithreading = true;
                cfg.cpuThreadAllocationMode = "BALANCED_N_MINUS_1";
                break;

            case BALANCED:
                // Default constructor settings are balanced
                break;

            case HIGH_END:
                cfg.enableGpuDrivenRenderer = true;
                cfg.enableHiZOcclusionCulling = true;
                cfg.maxGpuIndirectDrawBatchSize = 131072;
                cfg.enableDecoupledHud = true;
                cfg.hudTargetFramerate = 120;
                cfg.enableHudDynamicRefresh = true;
                cfg.enableStaticFastChests = true;
                cfg.enableEntityDepthCulling = true;
                cfg.entityCullingMaxDistance = 96.0;
                cfg.enableAnimationLod = true;
                cfg.animationLodNearDistance = 16.0;
                cfg.animationLodFarDistance = 48.0;
                cfg.enableExperienceOrbClumping = true;
                cfg.orbClumpRadius = 2.0;
                cfg.maxOrbClumpCapacity = 50000;
                cfg.enableVoxelCollisionFastCache = true;
                cfg.enableSleepingHoppers = true;
                cfg.enablePathfindingCircuitBreaker = true;
                cfg.maxPathfindingFailuresBeforeBackoff = 4;
                cfg.enableFastExplosions = true;
                cfg.explosionMaxRaySteps = 96;
                cfg.enableFastRedstoneEngine = true;
                cfg.enableRedstoneLightSuppression = true;
                cfg.enableComparatorDiscreteCaching = true;
                cfg.enableHopperContainerOcclusionFastPath = true;
                cfg.enableBatchNeighborUpdates = true;
                cfg.enableSpatialCollisionGrid = true;
                cfg.maxCollisionsPerEntity = 16;
                cfg.brainThrottleDistance = 48.0;
                cfg.enableFastFluidDynamics = true;
                cfg.enableFastParticleEngine = false; // Allow full particles on High-End
                cfg.enableFastMathLUT = true;
                cfg.enableFastRegistryCache = true;
                cfg.enableHybridLightEngine = true;
                cfg.enableAsyncLightThreads = true;
                cfg.lightWorkerThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
                cfg.enableLinearMemoryPacking = true;
                cfg.enablePacketFlushConsolidation = true;
                cfg.enableFastNativeCompression = true;
                cfg.enableClientWorldCache = true;
                cfg.clientMaxViewDistance = 64;
                cfg.autoCleanOldWorldCache = true;
                cfg.enableZeroAllocMathPooling = true;
                cfg.enableBlockStateDeduplication = true;
                cfg.enableAsyncAudio = true;
                cfg.maxSimultaneousSoundChannels = 64;
                cfg.enableColorCorrection = true;
                cfg.enableFpsStabilizer = true;
                cfg.targetFramerate = 240;
                cfg.maxChunkUploadsPerFrame = 6;
                cfg.enableDynamicWorkBudgeting = false;
                cfg.enableAggressiveCaveCulling = false;
                cfg.enableBlockEntityDistanceCulling = true;
                cfg.blockEntityCullDistance = 48.0;
                cfg.enableCpuMultithreading = true;
                cfg.cpuThreadAllocationMode = "ALL_CORES";
                break;

            case EXTREME_MULTICORE_350FPS:
                cfg.enableGpuDrivenRenderer = true;
                cfg.enableHiZOcclusionCulling = true;
                cfg.maxGpuIndirectDrawBatchSize = 65536;
                cfg.enableAmdHardwareAcceleration = true;
                cfg.enableAmdPrimitiveDiscard = true;
                cfg.enableAmdMultiDrawIndirectCount = true;
                cfg.enableAmdPersistentCoherentBuffers = true;
                cfg.enableAmd2GbVramGuard = true;
                cfg.enableAmdUmaZeroCopy = true;
                cfg.enableDualGpuSupport = true;
                cfg.dualGpuMode = "AUTO_BALANCED";
                cfg.enableDecoupledHud = true;
                cfg.hudTargetFramerate = 60;
                cfg.enableFpsStabilizer = true;
                cfg.targetFramerate = 350;
                cfg.maxChunkUploadsPerFrame = 3;
                cfg.enableDynamicWorkBudgeting = true;
                cfg.enableAggressiveCaveCulling = true;
                cfg.enableBlockEntityDistanceCulling = true;
                cfg.blockEntityCullDistance = 32.0;
                cfg.enableCpuMultithreading = true;
                cfg.cpuThreadAllocationMode = "ALL_CORES";
                cfg.enableParallelChunkMeshing = true;
                cfg.enableMultiCoreEntityPhysics = true;
                cfg.enableAsyncWorldTickDispatcher = true;
                cfg.enableCpuCoreAffinity = true;
                cfg.enableThreadPriorityBoost = true;
                cfg.enableSimdVectorAcceleration = true;
                break;
        }

        return cfg;
    }

    public static String serializeJson(HyperionConfig c) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        // 1. Graphics
        sb.append("  \"enableGpuDrivenRenderer\": ").append(c.enableGpuDrivenRenderer).append(",\n");
        sb.append("  \"enableHiZOcclusionCulling\": ").append(c.enableHiZOcclusionCulling).append(",\n");
        sb.append("  \"maxGpuIndirectDrawBatchSize\": ").append(c.maxGpuIndirectDrawBatchSize).append(",\n");
        sb.append("  \"enableDecoupledHud\": ").append(c.enableDecoupledHud).append(",\n");
        sb.append("  \"hudTargetFramerate\": ").append(c.hudTargetFramerate).append(",\n");
        sb.append("  \"enableHudDynamicRefresh\": ").append(c.enableHudDynamicRefresh).append(",\n");
        sb.append("  \"enableFastParticleEngine\": ").append(c.enableFastParticleEngine).append(",\n");
        sb.append("  \"maxParticlesPerBlockPerSecond\": ").append(c.maxParticlesPerBlockPerSecond).append(",\n");
        sb.append("  \"maxParticleDistance\": ").append(c.maxParticleDistance).append(",\n");
        sb.append("  \"enableFpsStabilizer\": ").append(c.enableFpsStabilizer).append(",\n");
        sb.append("  \"targetFramerate\": ").append(c.targetFramerate).append(",\n");
        sb.append("  \"maxChunkUploadsPerFrame\": ").append(c.maxChunkUploadsPerFrame).append(",\n");
        sb.append("  \"enableDynamicWorkBudgeting\": ").append(c.enableDynamicWorkBudgeting).append(",\n");
        sb.append("  \"enableAggressiveCaveCulling\": ").append(c.enableAggressiveCaveCulling).append(",\n");
        sb.append("  \"enableBlockEntityDistanceCulling\": ").append(c.enableBlockEntityDistanceCulling).append(",\n");
        sb.append("  \"blockEntityCullDistance\": ").append(c.blockEntityCullDistance).append(",\n");
        sb.append("  \"enableStaticFastChests\": ").append(c.enableStaticFastChests).append(",\n");
        sb.append("  \"enableEntityDepthCulling\": ").append(c.enableEntityDepthCulling).append(",\n");
        sb.append("  \"entityCullingMaxDistance\": ").append(c.entityCullingMaxDistance).append(",\n");
        sb.append("  \"enableAnimationLod\": ").append(c.enableAnimationLod).append(",\n");
        sb.append("  \"animationLodNearDistance\": ").append(c.animationLodNearDistance).append(",\n");
        sb.append("  \"animationLodFarDistance\": ").append(c.animationLodFarDistance).append(",\n");
        sb.append("  \"enableColorCorrection\": ").append(c.enableColorCorrection).append(",\n");
        sb.append("  \"colorGradingMode\": \"").append(c.colorGradingMode).append("\",\n");
        sb.append("  \"colorGammaBoost\": ").append(c.colorGammaBoost).append(",\n");
        sb.append("  \"colorVibrance\": ").append(c.colorVibrance).append(",\n");
        sb.append("  \"colorSaturation\": ").append(c.colorSaturation).append(",\n");
        sb.append("  \"colorContrast\": ").append(c.colorContrast).append(",\n");
        sb.append("  \"colorBlackCrushCompensation\": ").append(c.colorBlackCrushCompensation).append(",\n");
        sb.append("  \"colorNightAmbientBoost\": ").append(c.colorNightAmbientBoost).append(",\n");
        sb.append("  \"colorTemperature\": ").append(c.colorTemperature).append(",\n");
        sb.append("  \"enableColorDebanding\": ").append(c.enableColorDebanding).append(",\n");
        sb.append("  \"enableTexturePackColorCorrection\": ").append(c.enableTexturePackColorCorrection).append(",\n");

        // 2. Video Cards / GPU
        sb.append("  \"enableAmdHardwareAcceleration\": ").append(c.enableAmdHardwareAcceleration).append(",\n");
        sb.append("  \"amdArchitectureProfile\": \"").append(c.amdArchitectureProfile).append("\",\n");
        sb.append("  \"amdWavefrontSize\": ").append(c.amdWavefrontSize).append(",\n");
        sb.append("  \"enableAmdPrimitiveDiscard\": ").append(c.enableAmdPrimitiveDiscard).append(",\n");
        sb.append("  \"enableAmdMultiDrawIndirectCount\": ").append(c.enableAmdMultiDrawIndirectCount).append(",\n");
        sb.append("  \"enableAmdPersistentCoherentBuffers\": ").append(c.enableAmdPersistentCoherentBuffers).append(",\n");
        sb.append("  \"enableAmd2GbVramGuard\": ").append(c.enableAmd2GbVramGuard).append(",\n");
        sb.append("  \"enableAmdUmaZeroCopy\": ").append(c.enableAmdUmaZeroCopy).append(",\n");
        sb.append("  \"enableDualGpuSupport\": ").append(c.enableDualGpuSupport).append(",\n");
        sb.append("  \"dualGpuMode\": \"").append(c.dualGpuMode).append("\",\n");
        sb.append("  \"primaryGpuIndex\": ").append(c.primaryGpuIndex).append(",\n");
        sb.append("  \"secondaryGpuIndex\": ").append(c.secondaryGpuIndex).append(",\n");
        sb.append("  \"enableSecondaryGpuHudOffload\": ").append(c.enableSecondaryGpuHudOffload).append(",\n");
        sb.append("  \"enableSecondaryGpuLightOffload\": ").append(c.enableSecondaryGpuLightOffload).append(",\n");
        sb.append("  \"enableSecondaryGpuParticleOffload\": ").append(c.enableSecondaryGpuParticleOffload).append(",\n");

        // 3. CPU & Multithreading
        sb.append("  \"enableCpuMultithreading\": ").append(c.enableCpuMultithreading).append(",\n");
        sb.append("  \"cpuThreadAllocationMode\": \"").append(c.cpuThreadAllocationMode).append("\",\n");
        sb.append("  \"customCpuCoreCount\": ").append(c.customCpuCoreCount).append(",\n");
        sb.append("  \"enableParallelChunkMeshing\": ").append(c.enableParallelChunkMeshing).append(",\n");
        sb.append("  \"parallelChunkMesherThreads\": ").append(c.parallelChunkMesherThreads).append(",\n");
        sb.append("  \"enableMultiCoreEntityPhysics\": ").append(c.enableMultiCoreEntityPhysics).append(",\n");
        sb.append("  \"entityPhysicsBatchSize\": ").append(c.entityPhysicsBatchSize).append(",\n");
        sb.append("  \"enableAsyncWorldTickDispatcher\": ").append(c.enableAsyncWorldTickDispatcher).append(",\n");
        sb.append("  \"enableCpuCoreAffinity\": ").append(c.enableCpuCoreAffinity).append(",\n");
        sb.append("  \"enableThreadPriorityBoost\": ").append(c.enableThreadPriorityBoost).append(",\n");
        sb.append("  \"enableSimdVectorAcceleration\": ").append(c.enableSimdVectorAcceleration).append(",\n");

        // 4. Physics, Redstone & World
        sb.append("  \"enableVoxelCollisionFastCache\": ").append(c.enableVoxelCollisionFastCache).append(",\n");
        sb.append("  \"enableSleepingHoppers\": ").append(c.enableSleepingHoppers).append(",\n");
        sb.append("  \"enablePathfindingCircuitBreaker\": ").append(c.enablePathfindingCircuitBreaker).append(",\n");
        sb.append("  \"maxPathfindingFailuresBeforeBackoff\": ").append(c.maxPathfindingFailuresBeforeBackoff).append(",\n");
        sb.append("  \"enableFastExplosions\": ").append(c.enableFastExplosions).append(",\n");
        sb.append("  \"explosionMaxRaySteps\": ").append(c.explosionMaxRaySteps).append(",\n");
        sb.append("  \"enableFastRedstoneEngine\": ").append(c.enableFastRedstoneEngine).append(",\n");
        sb.append("  \"enableRedstoneLightSuppression\": ").append(c.enableRedstoneLightSuppression).append(",\n");
        sb.append("  \"enableComparatorDiscreteCaching\": ").append(c.enableComparatorDiscreteCaching).append(",\n");
        sb.append("  \"enableHopperContainerOcclusionFastPath\": ").append(c.enableHopperContainerOcclusionFastPath).append(",\n");
        sb.append("  \"enableBatchNeighborUpdates\": ").append(c.enableBatchNeighborUpdates).append(",\n");
        sb.append("  \"enableSpatialCollisionGrid\": ").append(c.enableSpatialCollisionGrid).append(",\n");
        sb.append("  \"maxCollisionsPerEntity\": ").append(c.maxCollisionsPerEntity).append(",\n");
        sb.append("  \"brainThrottleDistance\": ").append(c.brainThrottleDistance).append(",\n");
        sb.append("  \"enableFastFluidDynamics\": ").append(c.enableFastFluidDynamics).append(",\n");
        sb.append("  \"enableFastMathLUT\": ").append(c.enableFastMathLUT).append(",\n");
        sb.append("  \"enableFastRegistryCache\": ").append(c.enableFastRegistryCache).append(",\n");
        sb.append("  \"enableExperienceOrbClumping\": ").append(c.enableExperienceOrbClumping).append(",\n");
        sb.append("  \"orbClumpRadius\": ").append(c.orbClumpRadius).append(",\n");
        sb.append("  \"maxOrbClumpCapacity\": ").append(c.maxOrbClumpCapacity).append(",\n");

        sb.append("  \"enableHybridLightEngine\": ").append(c.enableHybridLightEngine).append(",\n");
        sb.append("  \"enableAsyncLightThreads\": ").append(c.enableAsyncLightThreads).append(",\n");
        sb.append("  \"lightWorkerThreads\": ").append(c.lightWorkerThreads).append(",\n");
        sb.append("  \"enableLinearMemoryPacking\": ").append(c.enableLinearMemoryPacking).append(",\n");

        sb.append("  \"enablePacketFlushConsolidation\": ").append(c.enablePacketFlushConsolidation).append(",\n");
        sb.append("  \"enableFastNativeCompression\": ").append(c.enableFastNativeCompression).append(",\n");

        sb.append("  \"enableClientWorldCache\": ").append(c.enableClientWorldCache).append(",\n");
        sb.append("  \"clientMaxViewDistance\": ").append(c.clientMaxViewDistance).append(",\n");
        sb.append("  \"autoCleanOldWorldCache\": ").append(c.autoCleanOldWorldCache).append(",\n");

        sb.append("  \"enableZeroAllocMathPooling\": ").append(c.enableZeroAllocMathPooling).append(",\n");
        sb.append("  \"enableBlockStateDeduplication\": ").append(c.enableBlockStateDeduplication).append(",\n");

        sb.append("  \"enableAsyncAudio\": ").append(c.enableAsyncAudio).append(",\n");
        sb.append("  \"maxSimultaneousSoundChannels\": ").append(c.maxSimultaneousSoundChannels).append(",\n");

        // 5. Voxel LOD & Infinite Horizon
        sb.append("  \"enableVoxelLodEngine\": ").append(c.enableVoxelLodEngine).append(",\n");
        sb.append("  \"voxelMaxRenderDistanceChunks\": ").append(c.voxelMaxRenderDistanceChunks).append(",\n");
        sb.append("  \"enableVoxelHorizonBlending\": ").append(c.enableVoxelHorizonBlending).append(",\n");
        sb.append("  \"voxelBlendStartChunks\": ").append(c.voxelBlendStartChunks).append(",\n");
        sb.append("  \"voxelBlendEndChunks\": ").append(c.voxelBlendEndChunks).append(",\n");
        sb.append("  \"enableVoxelAtmosphericFog\": ").append(c.enableVoxelAtmosphericFog).append(",\n");
        sb.append("  \"voxelStorageCompression\": \"").append(c.voxelStorageCompression).append("\",\n");

        // 6. Advanced Mesh, Geometry & Resource Packs
        sb.append("  \"enableChunkLod\": ").append(c.enableChunkLod).append(",\n");
        sb.append("  \"chunkLodDistanceBlocks\": ").append(c.chunkLodDistanceBlocks).append(",\n");
        sb.append("  \"chunkLodFarDistanceBlocks\": ").append(c.chunkLodFarDistanceBlocks).append(",\n");
        sb.append("  \"chunkLodSimplificationFactor\": ").append(c.chunkLodSimplificationFactor).append(",\n");
        sb.append("  \"enableAggressiveFaceCulling\": ").append(c.enableAggressiveFaceCulling).append(",\n");
        sb.append("  \"enableInternalCavityCulling\": ").append(c.enableInternalCavityCulling).append(",\n");
        sb.append("  \"enableGpuBlockInstancing\": ").append(c.enableGpuBlockInstancing).append(",\n");
        sb.append("  \"maxInstancesPerBatch\": ").append(c.maxInstancesPerBatch).append(",\n");
        sb.append("  \"enableGpuResetCrashGuard\": ").append(c.enableGpuResetCrashGuard).append(",\n");
        sb.append("  \"enableHdTextureOptimization\": ").append(c.enableHdTextureOptimization).append(",\n");
        sb.append("  \"enableAsyncAnimatedTextures\": ").append(c.enableAsyncAnimatedTextures).append(",\n");
        sb.append("  \"enableAdaptiveMipmapPacing\": ").append(c.enableAdaptiveMipmapPacing).append(",\n");
        sb.append("  \"maxHdAtlasDimension\": ").append(c.maxHdAtlasDimension).append(",\n");
        sb.append("  \"enableSmartLeavesCulling\": ").append(c.enableSmartLeavesCulling).append(",\n");
        sb.append("  \"enableFabulousGraphicsOptimization\": ").append(c.enableFabulousGraphicsOptimization).append(",\n");
        sb.append("  \"enableTranslucentSortThrottling\": ").append(c.enableTranslucentSortThrottling).append(",\n");
        sb.append("  \"enableFastCloudEngine\": ").append(c.enableFastCloudEngine).append(",\n");
        sb.append("  \"enableCloudCulling\": ").append(c.enableCloudCulling).append(",\n");
        sb.append("  \"enableCloudMeshReuse\": ").append(c.enableCloudMeshReuse).append("\n");

        sb.append("}\n");
        return sb.toString();
    }

    public static HyperionConfig parseJson(String json) {
        HyperionConfig c = new HyperionConfig();
        if (json == null || json.trim().isEmpty()) {
            return c;
        }

        Map<String, String> map = parseSimpleJsonMap(json);

        // Graphics
        if (map.containsKey("enableGpuDrivenRenderer")) c.enableGpuDrivenRenderer = Boolean.parseBoolean(map.get("enableGpuDrivenRenderer"));
        if (map.containsKey("enableHiZOcclusionCulling")) c.enableHiZOcclusionCulling = Boolean.parseBoolean(map.get("enableHiZOcclusionCulling"));
        if (map.containsKey("maxGpuIndirectDrawBatchSize")) c.maxGpuIndirectDrawBatchSize = parseInt(map.get("maxGpuIndirectDrawBatchSize"), c.maxGpuIndirectDrawBatchSize);
        if (map.containsKey("enableDecoupledHud")) c.enableDecoupledHud = Boolean.parseBoolean(map.get("enableDecoupledHud"));
        if (map.containsKey("hudTargetFramerate")) c.hudTargetFramerate = parseInt(map.get("hudTargetFramerate"), c.hudTargetFramerate);
        if (map.containsKey("enableHudDynamicRefresh")) c.enableHudDynamicRefresh = Boolean.parseBoolean(map.get("enableHudDynamicRefresh"));
        if (map.containsKey("enableFastParticleEngine")) c.enableFastParticleEngine = Boolean.parseBoolean(map.get("enableFastParticleEngine"));
        if (map.containsKey("maxParticlesPerBlockPerSecond")) c.maxParticlesPerBlockPerSecond = parseInt(map.get("maxParticlesPerBlockPerSecond"), c.maxParticlesPerBlockPerSecond);
        if (map.containsKey("maxParticleDistance")) c.maxParticleDistance = parseDouble(map.get("maxParticleDistance"), c.maxParticleDistance);
        if (map.containsKey("enableFpsStabilizer")) c.enableFpsStabilizer = Boolean.parseBoolean(map.get("enableFpsStabilizer"));
        if (map.containsKey("targetFramerate")) c.targetFramerate = parseInt(map.get("targetFramerate"), c.targetFramerate);
        if (map.containsKey("maxChunkUploadsPerFrame")) c.maxChunkUploadsPerFrame = parseInt(map.get("maxChunkUploadsPerFrame"), c.maxChunkUploadsPerFrame);
        if (map.containsKey("enableDynamicWorkBudgeting")) c.enableDynamicWorkBudgeting = Boolean.parseBoolean(map.get("enableDynamicWorkBudgeting"));
        if (map.containsKey("enableAggressiveCaveCulling")) c.enableAggressiveCaveCulling = Boolean.parseBoolean(map.get("enableAggressiveCaveCulling"));
        if (map.containsKey("enableBlockEntityDistanceCulling")) c.enableBlockEntityDistanceCulling = Boolean.parseBoolean(map.get("enableBlockEntityDistanceCulling"));
        if (map.containsKey("blockEntityCullDistance")) c.blockEntityCullDistance = parseDouble(map.get("blockEntityCullDistance"), c.blockEntityCullDistance);
        if (map.containsKey("enableStaticFastChests")) c.enableStaticFastChests = Boolean.parseBoolean(map.get("enableStaticFastChests"));
        if (map.containsKey("enableEntityDepthCulling")) c.enableEntityDepthCulling = Boolean.parseBoolean(map.get("enableEntityDepthCulling"));
        if (map.containsKey("entityCullingMaxDistance")) c.entityCullingMaxDistance = parseDouble(map.get("entityCullingMaxDistance"), c.entityCullingMaxDistance);
        if (map.containsKey("enableAnimationLod")) c.enableAnimationLod = Boolean.parseBoolean(map.get("enableAnimationLod"));
        if (map.containsKey("animationLodNearDistance")) c.animationLodNearDistance = parseDouble(map.get("animationLodNearDistance"), c.animationLodNearDistance);
        if (map.containsKey("animationLodFarDistance")) c.animationLodFarDistance = parseDouble(map.get("animationLodFarDistance"), c.animationLodFarDistance);
        if (map.containsKey("enableColorCorrection")) c.enableColorCorrection = Boolean.parseBoolean(map.get("enableColorCorrection"));
        if (map.containsKey("colorGradingMode")) c.colorGradingMode = map.get("colorGradingMode");
        if (map.containsKey("colorGammaBoost")) c.colorGammaBoost = parseDouble(map.get("colorGammaBoost"), c.colorGammaBoost);
        if (map.containsKey("colorVibrance")) c.colorVibrance = parseDouble(map.get("colorVibrance"), c.colorVibrance);
        if (map.containsKey("colorSaturation")) c.colorSaturation = parseDouble(map.get("colorSaturation"), c.colorSaturation);
        if (map.containsKey("colorContrast")) c.colorContrast = parseDouble(map.get("colorContrast"), c.colorContrast);
        if (map.containsKey("colorBlackCrushCompensation")) c.colorBlackCrushCompensation = parseDouble(map.get("colorBlackCrushCompensation"), c.colorBlackCrushCompensation);
        if (map.containsKey("colorNightAmbientBoost")) c.colorNightAmbientBoost = parseDouble(map.get("colorNightAmbientBoost"), c.colorNightAmbientBoost);
        if (map.containsKey("colorTemperature")) c.colorTemperature = parseInt(map.get("colorTemperature"), c.colorTemperature);
        if (map.containsKey("enableColorDebanding")) c.enableColorDebanding = Boolean.parseBoolean(map.get("enableColorDebanding"));
        if (map.containsKey("enableTexturePackColorCorrection")) c.enableTexturePackColorCorrection = Boolean.parseBoolean(map.get("enableTexturePackColorCorrection"));

        // GPU / Video Cards
        if (map.containsKey("enableAmdHardwareAcceleration")) c.enableAmdHardwareAcceleration = Boolean.parseBoolean(map.get("enableAmdHardwareAcceleration"));
        if (map.containsKey("amdArchitectureProfile")) c.amdArchitectureProfile = map.get("amdArchitectureProfile");
        if (map.containsKey("amdWavefrontSize")) c.amdWavefrontSize = parseInt(map.get("amdWavefrontSize"), c.amdWavefrontSize);
        if (map.containsKey("enableAmdPrimitiveDiscard")) c.enableAmdPrimitiveDiscard = Boolean.parseBoolean(map.get("enableAmdPrimitiveDiscard"));
        if (map.containsKey("enableAmdMultiDrawIndirectCount")) c.enableAmdMultiDrawIndirectCount = Boolean.parseBoolean(map.get("enableAmdMultiDrawIndirectCount"));
        if (map.containsKey("enableAmdPersistentCoherentBuffers")) c.enableAmdPersistentCoherentBuffers = Boolean.parseBoolean(map.get("enableAmdPersistentCoherentBuffers"));
        if (map.containsKey("enableAmd2GbVramGuard")) c.enableAmd2GbVramGuard = Boolean.parseBoolean(map.get("enableAmd2GbVramGuard"));
        if (map.containsKey("enableAmdUmaZeroCopy")) c.enableAmdUmaZeroCopy = Boolean.parseBoolean(map.get("enableAmdUmaZeroCopy"));
        if (map.containsKey("enableDualGpuSupport")) c.enableDualGpuSupport = Boolean.parseBoolean(map.get("enableDualGpuSupport"));
        if (map.containsKey("dualGpuMode")) c.dualGpuMode = map.get("dualGpuMode");
        if (map.containsKey("primaryGpuIndex")) c.primaryGpuIndex = parseInt(map.get("primaryGpuIndex"), c.primaryGpuIndex);
        if (map.containsKey("secondaryGpuIndex")) c.secondaryGpuIndex = parseInt(map.get("secondaryGpuIndex"), c.secondaryGpuIndex);
        if (map.containsKey("enableSecondaryGpuHudOffload")) c.enableSecondaryGpuHudOffload = Boolean.parseBoolean(map.get("enableSecondaryGpuHudOffload"));
        if (map.containsKey("enableSecondaryGpuLightOffload")) c.enableSecondaryGpuLightOffload = Boolean.parseBoolean(map.get("enableSecondaryGpuLightOffload"));
        if (map.containsKey("enableSecondaryGpuParticleOffload")) c.enableSecondaryGpuParticleOffload = Boolean.parseBoolean(map.get("enableSecondaryGpuParticleOffload"));

        // CPU & Multithreading
        if (map.containsKey("enableCpuMultithreading")) c.enableCpuMultithreading = Boolean.parseBoolean(map.get("enableCpuMultithreading"));
        if (map.containsKey("cpuThreadAllocationMode")) c.cpuThreadAllocationMode = map.get("cpuThreadAllocationMode");
        if (map.containsKey("customCpuCoreCount")) c.customCpuCoreCount = parseInt(map.get("customCpuCoreCount"), c.customCpuCoreCount);
        if (map.containsKey("enableParallelChunkMeshing")) c.enableParallelChunkMeshing = Boolean.parseBoolean(map.get("enableParallelChunkMeshing"));
        if (map.containsKey("parallelChunkMesherThreads")) c.parallelChunkMesherThreads = parseInt(map.get("parallelChunkMesherThreads"), c.parallelChunkMesherThreads);
        if (map.containsKey("enableMultiCoreEntityPhysics")) c.enableMultiCoreEntityPhysics = Boolean.parseBoolean(map.get("enableMultiCoreEntityPhysics"));
        if (map.containsKey("entityPhysicsBatchSize")) c.entityPhysicsBatchSize = parseInt(map.get("entityPhysicsBatchSize"), c.entityPhysicsBatchSize);
        if (map.containsKey("enableAsyncWorldTickDispatcher")) c.enableAsyncWorldTickDispatcher = Boolean.parseBoolean(map.get("enableAsyncWorldTickDispatcher"));
        if (map.containsKey("enableCpuCoreAffinity")) c.enableCpuCoreAffinity = Boolean.parseBoolean(map.get("enableCpuCoreAffinity"));
        if (map.containsKey("enableThreadPriorityBoost")) c.enableThreadPriorityBoost = Boolean.parseBoolean(map.get("enableThreadPriorityBoost"));
        if (map.containsKey("enableSimdVectorAcceleration")) c.enableSimdVectorAcceleration = Boolean.parseBoolean(map.get("enableSimdVectorAcceleration"));

        // Physics, Redstone & World
        if (map.containsKey("enableVoxelCollisionFastCache")) c.enableVoxelCollisionFastCache = Boolean.parseBoolean(map.get("enableVoxelCollisionFastCache"));
        if (map.containsKey("enableSleepingHoppers")) c.enableSleepingHoppers = Boolean.parseBoolean(map.get("enableSleepingHoppers"));
        if (map.containsKey("enablePathfindingCircuitBreaker")) c.enablePathfindingCircuitBreaker = Boolean.parseBoolean(map.get("enablePathfindingCircuitBreaker"));
        if (map.containsKey("maxPathfindingFailuresBeforeBackoff")) c.maxPathfindingFailuresBeforeBackoff = parseInt(map.get("maxPathfindingFailuresBeforeBackoff"), c.maxPathfindingFailuresBeforeBackoff);
        if (map.containsKey("enableFastExplosions")) c.enableFastExplosions = Boolean.parseBoolean(map.get("enableFastExplosions"));
        if (map.containsKey("explosionMaxRaySteps")) c.explosionMaxRaySteps = parseInt(map.get("explosionMaxRaySteps"), c.explosionMaxRaySteps);
        if (map.containsKey("enableFastRedstoneEngine")) c.enableFastRedstoneEngine = Boolean.parseBoolean(map.get("enableFastRedstoneEngine"));
        if (map.containsKey("enableRedstoneLightSuppression")) c.enableRedstoneLightSuppression = Boolean.parseBoolean(map.get("enableRedstoneLightSuppression"));
        if (map.containsKey("enableComparatorDiscreteCaching")) c.enableComparatorDiscreteCaching = Boolean.parseBoolean(map.get("enableComparatorDiscreteCaching"));
        if (map.containsKey("enableHopperContainerOcclusionFastPath")) c.enableHopperContainerOcclusionFastPath = Boolean.parseBoolean(map.get("enableHopperContainerOcclusionFastPath"));
        if (map.containsKey("enableBatchNeighborUpdates")) c.enableBatchNeighborUpdates = Boolean.parseBoolean(map.get("enableBatchNeighborUpdates"));
        if (map.containsKey("enableSpatialCollisionGrid")) c.enableSpatialCollisionGrid = Boolean.parseBoolean(map.get("enableSpatialCollisionGrid"));
        if (map.containsKey("maxCollisionsPerEntity")) c.maxCollisionsPerEntity = parseInt(map.get("maxCollisionsPerEntity"), c.maxCollisionsPerEntity);
        if (map.containsKey("brainThrottleDistance")) c.brainThrottleDistance = parseDouble(map.get("brainThrottleDistance"), c.brainThrottleDistance);
        if (map.containsKey("enableFastFluidDynamics")) c.enableFastFluidDynamics = Boolean.parseBoolean(map.get("enableFastFluidDynamics"));
        if (map.containsKey("enableFastMathLUT")) c.enableFastMathLUT = Boolean.parseBoolean(map.get("enableFastMathLUT"));
        if (map.containsKey("enableFastRegistryCache")) c.enableFastRegistryCache = Boolean.parseBoolean(map.get("enableFastRegistryCache"));
        if (map.containsKey("enableExperienceOrbClumping")) c.enableExperienceOrbClumping = Boolean.parseBoolean(map.get("enableExperienceOrbClumping"));
        if (map.containsKey("orbClumpRadius")) c.orbClumpRadius = parseDouble(map.get("orbClumpRadius"), c.orbClumpRadius);
        if (map.containsKey("maxOrbClumpCapacity")) c.maxOrbClumpCapacity = parseInt(map.get("maxOrbClumpCapacity"), c.maxOrbClumpCapacity);

        if (map.containsKey("enableHybridLightEngine")) c.enableHybridLightEngine = Boolean.parseBoolean(map.get("enableHybridLightEngine"));
        if (map.containsKey("enableAsyncLightThreads")) c.enableAsyncLightThreads = Boolean.parseBoolean(map.get("enableAsyncLightThreads"));
        if (map.containsKey("lightWorkerThreads")) c.lightWorkerThreads = parseInt(map.get("lightWorkerThreads"), c.lightWorkerThreads);
        if (map.containsKey("enableLinearMemoryPacking")) c.enableLinearMemoryPacking = Boolean.parseBoolean(map.get("enableLinearMemoryPacking"));

        if (map.containsKey("enablePacketFlushConsolidation")) c.enablePacketFlushConsolidation = Boolean.parseBoolean(map.get("enablePacketFlushConsolidation"));
        if (map.containsKey("enableFastNativeCompression")) c.enableFastNativeCompression = Boolean.parseBoolean(map.get("enableFastNativeCompression"));

        if (map.containsKey("enableClientWorldCache")) c.enableClientWorldCache = Boolean.parseBoolean(map.get("enableClientWorldCache"));
        if (map.containsKey("clientMaxViewDistance")) c.clientMaxViewDistance = parseInt(map.get("clientMaxViewDistance"), c.clientMaxViewDistance);
        if (map.containsKey("autoCleanOldWorldCache")) c.autoCleanOldWorldCache = Boolean.parseBoolean(map.get("autoCleanOldWorldCache"));

        if (map.containsKey("enableZeroAllocMathPooling")) c.enableZeroAllocMathPooling = Boolean.parseBoolean(map.get("enableZeroAllocMathPooling"));
        if (map.containsKey("enableBlockStateDeduplication")) c.enableBlockStateDeduplication = Boolean.parseBoolean(map.get("enableBlockStateDeduplication"));

        if (map.containsKey("enableAsyncAudio")) c.enableAsyncAudio = Boolean.parseBoolean(map.get("enableAsyncAudio"));
        if (map.containsKey("maxSimultaneousSoundChannels")) c.maxSimultaneousSoundChannels = parseInt(map.get("maxSimultaneousSoundChannels"), c.maxSimultaneousSoundChannels);

        // Voxel LOD Ultra-Distance
        if (map.containsKey("enableVoxelLodEngine")) c.enableVoxelLodEngine = Boolean.parseBoolean(map.get("enableVoxelLodEngine"));
        if (map.containsKey("voxelMaxRenderDistanceChunks")) c.voxelMaxRenderDistanceChunks = parseInt(map.get("voxelMaxRenderDistanceChunks"), c.voxelMaxRenderDistanceChunks);
        if (map.containsKey("enableVoxelHorizonBlending")) c.enableVoxelHorizonBlending = Boolean.parseBoolean(map.get("enableVoxelHorizonBlending"));
        if (map.containsKey("voxelBlendStartChunks")) c.voxelBlendStartChunks = parseDouble(map.get("voxelBlendStartChunks"), c.voxelBlendStartChunks);
        if (map.containsKey("voxelBlendEndChunks")) c.voxelBlendEndChunks = parseDouble(map.get("voxelBlendEndChunks"), c.voxelBlendEndChunks);
        if (map.containsKey("enableVoxelAtmosphericFog")) c.enableVoxelAtmosphericFog = Boolean.parseBoolean(map.get("enableVoxelAtmosphericFog"));
        if (map.containsKey("voxelStorageCompression")) c.voxelStorageCompression = map.get("voxelStorageCompression");

        // Advanced Mesh & Geometry Tweaks
        if (map.containsKey("enableChunkLod")) c.enableChunkLod = Boolean.parseBoolean(map.get("enableChunkLod"));
        if (map.containsKey("chunkLodDistanceBlocks")) c.chunkLodDistanceBlocks = parseDouble(map.get("chunkLodDistanceBlocks"), c.chunkLodDistanceBlocks);
        if (map.containsKey("chunkLodFarDistanceBlocks")) c.chunkLodFarDistanceBlocks = parseDouble(map.get("chunkLodFarDistanceBlocks"), c.chunkLodFarDistanceBlocks);
        if (map.containsKey("chunkLodSimplificationFactor")) c.chunkLodSimplificationFactor = parseDouble(map.get("chunkLodSimplificationFactor"), c.chunkLodSimplificationFactor);
        if (map.containsKey("enableAggressiveFaceCulling")) c.enableAggressiveFaceCulling = Boolean.parseBoolean(map.get("enableAggressiveFaceCulling"));
        if (map.containsKey("enableInternalCavityCulling")) c.enableInternalCavityCulling = Boolean.parseBoolean(map.get("enableInternalCavityCulling"));
        if (map.containsKey("enableGpuBlockInstancing")) c.enableGpuBlockInstancing = Boolean.parseBoolean(map.get("enableGpuBlockInstancing"));
        if (map.containsKey("maxInstancesPerBatch")) c.maxInstancesPerBatch = parseInt(map.get("maxInstancesPerBatch"), c.maxInstancesPerBatch);
        if (map.containsKey("enableGpuResetCrashGuard")) c.enableGpuResetCrashGuard = Boolean.parseBoolean(map.get("enableGpuResetCrashGuard"));
        if (map.containsKey("enableHdTextureOptimization")) c.enableHdTextureOptimization = Boolean.parseBoolean(map.get("enableHdTextureOptimization"));
        if (map.containsKey("enableAsyncAnimatedTextures")) c.enableAsyncAnimatedTextures = Boolean.parseBoolean(map.get("enableAsyncAnimatedTextures"));
        if (map.containsKey("enableAdaptiveMipmapPacing")) c.enableAdaptiveMipmapPacing = Boolean.parseBoolean(map.get("enableAdaptiveMipmapPacing"));
        if (map.containsKey("maxHdAtlasDimension")) c.maxHdAtlasDimension = parseInt(map.get("maxHdAtlasDimension"), c.maxHdAtlasDimension);
        if (map.containsKey("enableSmartLeavesCulling")) c.enableSmartLeavesCulling = Boolean.parseBoolean(map.get("enableSmartLeavesCulling"));
        if (map.containsKey("enableFabulousGraphicsOptimization")) c.enableFabulousGraphicsOptimization = Boolean.parseBoolean(map.get("enableFabulousGraphicsOptimization"));
        if (map.containsKey("enableTranslucentSortThrottling")) c.enableTranslucentSortThrottling = Boolean.parseBoolean(map.get("enableTranslucentSortThrottling"));
        if (map.containsKey("enableFastCloudEngine")) c.enableFastCloudEngine = Boolean.parseBoolean(map.get("enableFastCloudEngine"));
        if (map.containsKey("enableCloudCulling")) c.enableCloudCulling = Boolean.parseBoolean(map.get("enableCloudCulling"));
        if (map.containsKey("enableCloudMeshReuse")) c.enableCloudMeshReuse = Boolean.parseBoolean(map.get("enableCloudMeshReuse"));

        return c;
    }

    private static Map<String, String> parseSimpleJsonMap(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        // Strip block comments /* ... */
        String sanitized = json.replaceAll("/\\*.*?\\*/", "");
        String[] lines = sanitized.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            // Strip inline comments
            int commentIdx = trimmed.indexOf("//");
            if (commentIdx >= 0) {
                trimmed = trimmed.substring(0, commentIdx).trim();
            }
            if (trimmed.startsWith("{") || trimmed.startsWith("}") || trimmed.isEmpty()) {
                continue;
            }
            int colonIdx = trimmed.indexOf(':');
            if (colonIdx > 0) {
                String keyPart = trimmed.substring(0, colonIdx).trim().replace("\"", "").trim();
                String valPart = trimmed.substring(colonIdx + 1).trim();
                if (valPart.endsWith(",")) {
                    valPart = valPart.substring(0, valPart.length() - 1).trim();
                }
                if (valPart.startsWith("\"") && valPart.endsWith("\"") && valPart.length() >= 2) {
                    valPart = valPart.substring(1, valPart.length() - 1);
                }
                map.put(keyPart, valPart);
            }
        }
        return map;
    }

    private static int parseInt(String val, int def) {
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static double parseDouble(String val, double def) {
        try {
            return Double.parseDouble(val.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
