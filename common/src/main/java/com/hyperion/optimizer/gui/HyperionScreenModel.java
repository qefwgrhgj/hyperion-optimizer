package com.hyperion.optimizer.gui;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.api.HyperionConfig;
import com.hyperion.optimizer.api.HyperionConfigStorage;

import java.util.List;

public final class HyperionScreenModel {
    private final HyperionConfig workingConfig;
    private HyperionCategory activeCategory;
    private boolean isDirty = false;
    private String statusMessage = "";

    public HyperionScreenModel() {
        this.workingConfig = HyperionConfigStorage.loadOrCreate();
        this.activeCategory = HyperionCategory.GRAPHICS_SETTINGS;
    }

    public HyperionCategory getActiveCategory() {
        return activeCategory;
    }

    public void setActiveCategory(HyperionCategory category) {
        if (category != null) {
            this.activeCategory = category;
        }
    }

    public List<HyperionOption<?>> getCurrentOptions() {
        return HyperionOptionsRegistry.getOptionsByCategory(activeCategory);
    }

    public HyperionConfig getWorkingConfig() {
        return workingConfig;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void markDirty() {
        this.isDirty = true;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String message) {
        this.statusMessage = message;
    }

    public void applyPreset(HyperionConfigStorage.Preset preset) {
        HyperionConfig presetConfig = HyperionConfigStorage.applyPreset(preset);
        copyConfig(presetConfig, this.workingConfig);
        this.isDirty = true;
        this.statusMessage = "Применен пресет: " + preset.getTitle();
    }

    public void resetToDefaults() {
        HyperionConfig defaults = new HyperionConfig();
        copyConfig(defaults, this.workingConfig);
        this.isDirty = true;
        this.statusMessage = "Сброшено к значениям по умолчанию";
    }

    public boolean saveAndApply() {
        boolean saved = HyperionConfigStorage.save(this.workingConfig);
        if (saved) {
            HyperionEngine.getInstance().reloadConfig(this.workingConfig);
            this.isDirty = false;
            this.statusMessage = "Настройки сохранены и применены в игре!";
            return true;
        } else {
            this.statusMessage = "Ошибка при сохранении конфигурации";
            return false;
        }
    }

    private static void copyConfig(HyperionConfig src, HyperionConfig dst) {
        // Graphics
        dst.enableGpuDrivenRenderer = src.enableGpuDrivenRenderer;
        dst.enableHiZOcclusionCulling = src.enableHiZOcclusionCulling;
        dst.maxGpuIndirectDrawBatchSize = src.maxGpuIndirectDrawBatchSize;
        dst.enableDecoupledHud = src.enableDecoupledHud;
        dst.hudTargetFramerate = src.hudTargetFramerate;
        dst.enableHudDynamicRefresh = src.enableHudDynamicRefresh;
        dst.enableFastParticleEngine = src.enableFastParticleEngine;
        dst.maxParticlesPerBlockPerSecond = src.maxParticlesPerBlockPerSecond;
        dst.maxParticleDistance = src.maxParticleDistance;
        dst.enableFpsStabilizer = src.enableFpsStabilizer;
        dst.targetFramerate = src.targetFramerate;
        dst.maxChunkUploadsPerFrame = src.maxChunkUploadsPerFrame;
        dst.enableDynamicWorkBudgeting = src.enableDynamicWorkBudgeting;
        dst.enableAggressiveCaveCulling = src.enableAggressiveCaveCulling;
        dst.enableBlockEntityDistanceCulling = src.enableBlockEntityDistanceCulling;
        dst.blockEntityCullDistance = src.blockEntityCullDistance;
        dst.enableStaticFastChests = src.enableStaticFastChests;
        dst.enableEntityDepthCulling = src.enableEntityDepthCulling;
        dst.entityCullingMaxDistance = src.entityCullingMaxDistance;
        dst.enableAnimationLod = src.enableAnimationLod;
        dst.animationLodNearDistance = src.animationLodNearDistance;
        dst.animationLodFarDistance = src.animationLodFarDistance;
        dst.enableColorCorrection = src.enableColorCorrection;
        dst.colorGradingMode = src.colorGradingMode;
        dst.colorGammaBoost = src.colorGammaBoost;
        dst.colorVibrance = src.colorVibrance;
        dst.colorSaturation = src.colorSaturation;
        dst.colorContrast = src.colorContrast;
        dst.colorBlackCrushCompensation = src.colorBlackCrushCompensation;
        dst.colorNightAmbientBoost = src.colorNightAmbientBoost;
        dst.colorTemperature = src.colorTemperature;
        dst.enableColorDebanding = src.enableColorDebanding;

        // GPU / Video Cards
        dst.enableAmdHardwareAcceleration = src.enableAmdHardwareAcceleration;
        dst.amdArchitectureProfile = src.amdArchitectureProfile;
        dst.amdWavefrontSize = src.amdWavefrontSize;
        dst.enableAmdPrimitiveDiscard = src.enableAmdPrimitiveDiscard;
        dst.enableAmdMultiDrawIndirectCount = src.enableAmdMultiDrawIndirectCount;
        dst.enableAmdPersistentCoherentBuffers = src.enableAmdPersistentCoherentBuffers;
        dst.enableAmd2GbVramGuard = src.enableAmd2GbVramGuard;
        dst.enableAmdUmaZeroCopy = src.enableAmdUmaZeroCopy;
        dst.enableDualGpuSupport = src.enableDualGpuSupport;
        dst.dualGpuMode = src.dualGpuMode;
        dst.primaryGpuIndex = src.primaryGpuIndex;
        dst.secondaryGpuIndex = src.secondaryGpuIndex;
        dst.enableSecondaryGpuHudOffload = src.enableSecondaryGpuHudOffload;
        dst.enableSecondaryGpuLightOffload = src.enableSecondaryGpuLightOffload;
        dst.enableSecondaryGpuParticleOffload = src.enableSecondaryGpuParticleOffload;

        // CPU & Multithreading
        dst.enableCpuMultithreading = src.enableCpuMultithreading;
        dst.cpuThreadAllocationMode = src.cpuThreadAllocationMode;
        dst.customCpuCoreCount = src.customCpuCoreCount;
        dst.enableParallelChunkMeshing = src.enableParallelChunkMeshing;
        dst.parallelChunkMesherThreads = src.parallelChunkMesherThreads;
        dst.enableMultiCoreEntityPhysics = src.enableMultiCoreEntityPhysics;
        dst.entityPhysicsBatchSize = src.entityPhysicsBatchSize;
        dst.enableAsyncWorldTickDispatcher = src.enableAsyncWorldTickDispatcher;
        dst.enableCpuCoreAffinity = src.enableCpuCoreAffinity;
        dst.enableThreadPriorityBoost = src.enableThreadPriorityBoost;
        dst.enableSimdVectorAcceleration = src.enableSimdVectorAcceleration;

        // Physics, Light, Memory & Network
        dst.enableVoxelCollisionFastCache = src.enableVoxelCollisionFastCache;
        dst.enableSleepingHoppers = src.enableSleepingHoppers;
        dst.enablePathfindingCircuitBreaker = src.enablePathfindingCircuitBreaker;
        dst.maxPathfindingFailuresBeforeBackoff = src.maxPathfindingFailuresBeforeBackoff;
        dst.enableFastExplosions = src.enableFastExplosions;
        dst.explosionMaxRaySteps = src.explosionMaxRaySteps;
        dst.enableFastRedstoneEngine = src.enableFastRedstoneEngine;
        dst.enableRedstoneLightSuppression = src.enableRedstoneLightSuppression;
        dst.enableComparatorDiscreteCaching = src.enableComparatorDiscreteCaching;
        dst.enableHopperContainerOcclusionFastPath = src.enableHopperContainerOcclusionFastPath;
        dst.enableBatchNeighborUpdates = src.enableBatchNeighborUpdates;
        dst.enableSpatialCollisionGrid = src.enableSpatialCollisionGrid;
        dst.maxCollisionsPerEntity = src.maxCollisionsPerEntity;
        dst.brainThrottleDistance = src.brainThrottleDistance;
        dst.enableFastFluidDynamics = src.enableFastFluidDynamics;
        dst.enableFastMathLUT = src.enableFastMathLUT;
        dst.enableFastRegistryCache = src.enableFastRegistryCache;
        dst.enableExperienceOrbClumping = src.enableExperienceOrbClumping;
        dst.orbClumpRadius = src.orbClumpRadius;
        dst.maxOrbClumpCapacity = src.maxOrbClumpCapacity;
        dst.enableHybridLightEngine = src.enableHybridLightEngine;
        dst.enableAsyncLightThreads = src.enableAsyncLightThreads;
        dst.lightWorkerThreads = src.lightWorkerThreads;
        dst.enableLinearMemoryPacking = src.enableLinearMemoryPacking;
        dst.enablePacketFlushConsolidation = src.enablePacketFlushConsolidation;
        dst.enableFastNativeCompression = src.enableFastNativeCompression;
        dst.enableClientWorldCache = src.enableClientWorldCache;
        dst.clientMaxViewDistance = src.clientMaxViewDistance;
        dst.autoCleanOldWorldCache = src.autoCleanOldWorldCache;
        dst.enableZeroAllocMathPooling = src.enableZeroAllocMathPooling;
        dst.enableBlockStateDeduplication = src.enableBlockStateDeduplication;
        dst.enableAsyncAudio = src.enableAsyncAudio;
        dst.maxSimultaneousSoundChannels = src.maxSimultaneousSoundChannels;
    }
}
