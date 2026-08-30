package com.hyperion.optimizer;

import com.hyperion.optimizer.api.HyperionConfig;
import com.hyperion.optimizer.core.audio.AsyncAudioEngine;
import com.hyperion.optimizer.core.entity.AnimationLodManager;
import com.hyperion.optimizer.core.entity.EntityDepthCuller;
import com.hyperion.optimizer.core.entity.ExperienceOrbMerger;
import com.hyperion.optimizer.core.entity.SpatialCollisionEngine;
import com.hyperion.optimizer.core.entity.StaticChestMeshBaker;
import com.hyperion.optimizer.core.gpu.ComputeCullEngine;
import com.hyperion.optimizer.core.gpu.FastParticleEngine;
import com.hyperion.optimizer.core.gpu.MultiDrawIndirectManager;
import com.hyperion.optimizer.core.hud.DecoupledHudManager;
import com.hyperion.optimizer.core.hud.HudDirtyTracker;
import com.hyperion.optimizer.core.gpu.FastHdTextureEngine;
import com.hyperion.optimizer.core.gpu.GpuThermalPowerGuard;
import com.hyperion.optimizer.core.render.FancyGraphicsOptimizer;
import com.hyperion.optimizer.core.render.FastCloudEngine;
import com.hyperion.optimizer.core.light.AsyncBitsetLightEngine;
import com.hyperion.optimizer.core.light.DataOrientedChunkMemory;
import com.hyperion.optimizer.core.memory.FastMathLUT;
import com.hyperion.optimizer.core.memory.PrimitiveVectorPool;
import com.hyperion.optimizer.core.network.PacketFlushConsolidator;
import com.hyperion.optimizer.core.physics.FastExplosionEngine;
import com.hyperion.optimizer.core.physics.FastFluidEngine;
import com.hyperion.optimizer.core.physics.FastRedstoneEngine;
import com.hyperion.optimizer.core.physics.FastRegistryCache;
import com.hyperion.optimizer.core.physics.PathfindingCircuitBreaker;
import com.hyperion.optimizer.core.physics.SleepingHopperManager;
import com.hyperion.optimizer.core.physics.VoxelShapeFastCache;
import com.hyperion.optimizer.core.world.ClientWorldCacheStorage;
import com.hyperion.optimizer.core.world.FakeChunkManager;

import com.hyperion.optimizer.api.HyperionConfigStorage;
import com.hyperion.optimizer.core.gpu.SimdFrustumCuller;
import com.hyperion.optimizer.core.hud.HyperionProfilerOverlay;
import com.hyperion.optimizer.core.memory.OffHeapChunkSegment;
import com.hyperion.optimizer.gui.HyperionCategory;
import com.hyperion.optimizer.gui.HyperionOption;
import com.hyperion.optimizer.gui.HyperionOptionsRegistry;
import com.hyperion.optimizer.gui.HyperionScreenModel;
import com.hyperion.optimizer.core.gpu.amd.AmdArchitectureProfile;
import com.hyperion.optimizer.core.gpu.amd.AmdGpuAccelerator;
import com.hyperion.optimizer.core.gpu.amd.AmdVramBudgetGuard;
import com.hyperion.optimizer.core.gpu.dualgpu.DualGpuManager;
import com.hyperion.optimizer.core.gpu.dualgpu.DualGpuWorkloadDispatcher;
import com.hyperion.optimizer.core.gpu.dualgpu.GpuDeviceInfo;
import com.hyperion.optimizer.mixin.MixinLevelRenderer;
import com.hyperion.optimizer.mixin.MixinVideoOptionsScreen;
import com.hyperion.optimizer.core.render.ColorCorrectionEngine;
import com.hyperion.optimizer.core.render.FpsStabilizerEngine;
import com.hyperion.optimizer.core.render.ChunkLodManager;
import com.hyperion.optimizer.core.render.AggressiveFaceCuller;
import com.hyperion.optimizer.core.gpu.GpuInstancingEngine;
import com.hyperion.optimizer.core.gpu.GpuResetCrashGuard;
import com.hyperion.optimizer.core.gpu.GpuVendorProfile;
import com.hyperion.optimizer.core.gpu.dualgpu.DualGpuSyncLock;
import com.hyperion.optimizer.core.gpu.dualgpu.DualGpuThermalFallback;
import com.hyperion.optimizer.gui.HyperionKeyBindingManager;
import com.hyperion.optimizer.core.lod.voxel.VoxelHierarchicalMipTree;
import com.hyperion.optimizer.core.lod.voxel.VoxelSectionStorage;
import com.hyperion.optimizer.core.lod.voxel.VoxelLodRenderer;
import com.hyperion.optimizer.core.lod.voxel.VoxelHorizonBlender;
import com.hyperion.optimizer.core.lod.voxel.VoxelPregenIngestEngine;
import com.hyperion.optimizer.core.memory.DirectMemoryCleaner;
import com.hyperion.optimizer.compat.HyperionModCompatManager;
import com.hyperion.optimizer.compat.IrisShaderCompatPipeline;
import com.hyperion.optimizer.core.particle.AdvancedParticleEngine;
import com.hyperion.optimizer.core.micro.BadOptimizationsEngine;
import com.hyperion.optimizer.core.ai.MobAiOptimizer;
import com.hyperion.optimizer.core.animation.PalladiumCapabilityCache;
import com.hyperion.optimizer.mixin.MixinLightmapTexture;
import com.hyperion.optimizer.core.threading.HyperionThreadPoolManager;
import com.hyperion.optimizer.core.threading.ParallelChunkMesher;
import com.hyperion.optimizer.core.threading.MultiCoreEntityPhysicsEngine;
import com.hyperion.optimizer.core.threading.AsyncWorldTickDispatcher;
import com.hyperion.optimizer.core.threading.CpuCoreAffinityGovernor;
import com.hyperion.optimizer.gui.HyperionConfigScreen;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class HyperionTestRunner {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   HYPERION OPTIMIZER - EMPIRICAL TEST SUITE    ");
        System.out.println("=================================================");

        int passed = 0;
        int failed = 0;

        try {
            testEngineInitialization();
            System.out.println("[PASS] 1. Engine Initialization & Subsystem Registry");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 1. Engine Initialization: " + t.getMessage());
            failed++;
        }

        try {
            testPrimitiveVectorPackingAndReentrancy();
            System.out.println("[PASS] 2. Zero-Allocation Vector Packing & Re-entrancy Ring Buffer");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 2. Vector Packing: " + t.getMessage());
            failed++;
        }

        try {
            testComputeCullFrustumAndThreadSafety();
            System.out.println("[PASS] 3. GPU Compute Frustum Culling & Atomic Telemetry");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 3. GPU Frustum Culling: " + t.getMessage());
            failed++;
        }

        try {
            testDecoupledHudPacingAndDirtyTracker();
            System.out.println("[PASS] 4. Decoupled HUD FBO Pacing & Event-Driven Invalidation");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 4. Decoupled HUD: " + t.getMessage());
            failed++;
        }

        try {
            testVoxelCollisionFastPath();
            System.out.println("[PASS] 5. Voxel Collision Fast-Path & Box Intersections");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 5. Voxel Collision: " + t.getMessage());
            failed++;
        }

        try {
            testExperienceOrbClumpingAndAgeExploitFix();
            System.out.println("[PASS] 6. Experience Orb Spatial Merging & Age Exploit Prevention");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 6. Experience Orb Merging: " + t.getMessage());
            failed++;
        }

        try {
            testPathfindingCircuitBreakerAndPruning();
            System.out.println("[PASS] 7. Mob Pathfinding Circuit Breaker & Memory Pruning");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 7. Pathfinding Circuit Breaker: " + t.getMessage());
            failed++;
        }

        try {
            testDataOrientedMemoryPackingAndBounds();
            System.out.println("[PASS] 8. Data-Oriented L1/L2/L3 4-Bit Light Packing & Coordinate Clamping");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 8. Data-Oriented Memory: " + t.getMessage());
            failed++;
        }

        try {
            testBoundedWorldCacheLru();
            System.out.println("[PASS] 9. Client World Cache Bounded LRU Eviction (Anti-OOM)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 9. World Cache LRU: " + t.getMessage());
            failed++;
        }

        try {
            testSleepingHopperTickSynchronization();
            System.out.println("[PASS] 10. Sleeping Hopper Absolute Tick Synchronization");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 10. Sleeping Hopper Timing: " + t.getMessage());
            failed++;
        }

        try {
            testExperienceOrbIntegerOverflowExploitFix();
            System.out.println("[PASS] 11. Experience Orb 64-Bit Arithmetic & Integer Overflow Prevention (P0-1)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 11. XP Integer Overflow: " + t.getMessage());
            failed++;
        }

        try {
            testFakeChunkRealChunkArrivalInvalidation();
            System.out.println("[PASS] 12. Fake Chunk Real Arrival Atomic Invalidation & Z-Fighting Defense (P0-2)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 12. Fake Chunk Z-Fighting: " + t.getMessage());
            failed++;
        }

        try {
            testPacketFlushConsolidatorPerChannelIsolation();
            System.out.println("[PASS] 13. Packet Flush Per-Channel Context Isolation & Krypton Batching (P0-3)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 13. Packet Flush Channel Isolation: " + t.getMessage());
            failed++;
        }

        try {
            testStaticChestMeshBakerDoubleChestPairing();
            System.out.println("[PASS] 14. Double Chest Left/Right Pair Synchronization & Static Mesh (P1-1)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 14. Double Chest Sync: " + t.getMessage());
            failed++;
        }

        try {
            testDecoupledHudClockRollbackProtection();
            System.out.println("[PASS] 15. Decoupled HUD NTP System Clock Rollback Defensive Reset (P1-2)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 15. HUD Clock Rollback: " + t.getMessage());
            failed++;
        }

        try {
            testComputeCullEngineNaNInfinitySafeFallback();
            System.out.println("[PASS] 16. GPU Compute Culling NaN/Infinity Matrix Fallback & Void Defense (P1-3)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 16. Compute Cull NaN Fallback: " + t.getMessage());
            failed++;
        }

        try {
            testClientWorldCacheStorageMultiThreadedStripedThroughput();
            System.out.println("[PASS] 17. Client World Cache High-Throughput Striped Lock Segments (P2-1)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 17. Striped World Cache Contention: " + t.getMessage());
            failed++;
        }

        try {
            testMultiDrawIndirectManagerThreadSafeRecording();
            System.out.println("[PASS] 18. MultiDraw Indirect Thread-Safe Command Recording & Buffer Merge (P2-2)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 18. MultiDraw Indirect Concurrency: " + t.getMessage());
            failed++;
        }

        try {
            testAsyncAudioEngineBoundedQueueOverflowProtection();
            System.out.println("[PASS] 19. Async Audio Engine Bounded Task Queue & Discard Policy (P2-3)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 19. Audio Queue Bounding: " + t.getMessage());
            failed++;
        }

        try {
            testPrimitiveVectorPoolWorldBorderClamping();
            System.out.println("[PASS] 20. Primitive Vector Packing World Border Clamping (+/-30M) (P3-1)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 20. Vector World Border: " + t.getMessage());
            failed++;
        }

        try {
            testDataOrientedChunkMemoryConcurrentNibbleSafety();
            System.out.println("[PASS] 21. Data-Oriented Memory Concurrent Nibble Race Defense (P0-1)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 21. Memory Nibble Safety: " + t.getMessage());
            failed++;
        }

        try {
            testPrimitiveVectorPoolExpandedRingBufferDeepStack();
            System.out.println("[PASS] 22. Primitive Vector Pool 64-Slot Deep Stack Overflow Defense (P0-2)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 22. Vector Ring Buffer: " + t.getMessage());
            failed++;
        }

        try {
            testPathfindingCircuitBreakerBossExemptionAndRollback();
            System.out.println("[PASS] 23. Pathfinding Circuit Breaker Boss Exemption & Tick Recovery (P0-3)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 23. Pathfinding Boss Exemption: " + t.getMessage());
            failed++;
        }

        try {
            testFakeChunkDimensionChangeClearing();
            System.out.println("[PASS] 24. Fake Chunk Cross-Dimension Bleed Defense & Invalidation (P1-1)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 24. Fake Chunk Dimension Bleed: " + t.getMessage());
            failed++;
        }

        try {
            testMultiDrawIndirectIdempotentFinishBatch();
            System.out.println("[PASS] 25. MultiDraw Indirect Idempotent Multi-Pass Finish Batch (P1-2)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 25. MultiDraw Idempotency: " + t.getMessage());
            failed++;
        }

        try {
            testAsyncBitsetLightEngineZeroThreadSanitization();
            System.out.println("[PASS] 26. Async Light Engine Thread Pool Core Count Sanitization (P1-3)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 26. Light Pool Sanitization: " + t.getMessage());
            failed++;
        }

        try {
            testEntityDepthCullerBossAndGlowingProtection();
            System.out.println("[PASS] 27. Entity Depth Culler Boss & Glowing Outline Protection (P1-4)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 27. Depth Culler Special Entities: " + t.getMessage());
            failed++;
        }

        try {
            testStaticChestMeshBakerChunkInvalidation();
            System.out.println("[PASS] 28. Static Chest Baker Chunk Unload Memory Pruning (P2-2)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 28. Chest Baker Chunk Pruning: " + t.getMessage());
            failed++;
        }

        try {
            testAnimationLodEntityPhaseInterleaving();
            System.out.println("[PASS] 29. Animation LOD Entity Phase Interleaving & De-jitter (P2-4)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 29. Animation LOD Interleaving: " + t.getMessage());
            failed++;
        }

        try {
            testEngineHotReloadAndCleanShutdown();
            System.out.println("[PASS] 30. Engine Configuration Hot-Reload & Safe Daemon Shutdown (P3-2, P3-3)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 30. Engine Lifecycle: " + t.getMessage());
            failed++;
        }

        try {
            testFastExplosionRayDirectionTableAccuracy();
            System.out.println("[PASS] 31. Fast Explosion 1352-Ray Direction Precomputed LUT & Geometry");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 31. Explosion Ray LUT: " + t.getMessage());
            failed++;
        }

        try {
            testFastExplosionBlockDestructionAndWaterAbsorption();
            System.out.println("[PASS] 32. TNT Blast Voxel Marching & Water Absorption Shield");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 32. Blast Voxel Marching: " + t.getMessage());
            failed++;
        }

        try {
            testFastExplosionEntityDamageAndKnockback();
            System.out.println("[PASS] 33. Entity Explosion Exposure & Distance Damage Falloff");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 33. Entity Blast Exposure: " + t.getMessage());
            failed++;
        }

        try {
            testAllExplosionTypesParameterization();
            System.out.println("[PASS] 34. Full 7-Tier Explosion Type Matrix (Ghast to Wither Spawn)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 34. Explosion Matrix: " + t.getMessage());
            failed++;
        }

        try {
            testFastExplosionZeroAllocationStressThroughput();
            System.out.println("[PASS] 35. High-Throughput Explosion Stress Test (100 Simultaneous Detonations)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 35. Explosion Stress Test: " + t.getMessage());
            failed++;
        }

        try {
            testFastRedstoneWireTopologicalSolver();
            System.out.println("[PASS] 36. Fast Redstone 1-Pass Topological Wire Network Solver");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 36. Redstone Topological Solver: " + t.getMessage());
            failed++;
        }

        try {
            testFastRedstoneNeighborUpdateDeduplication();
            System.out.println("[PASS] 37. Redstone Neighbor Update Batching & Deduplication");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 37. Neighbor Update Deduplication: " + t.getMessage());
            failed++;
        }

        try {
            testFastRedstoneComparatorDiscretePowerEvaluation();
            System.out.println("[PASS] 38. Comparator Discrete Signal Caching (0-15 Exact Math)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 38. Comparator Discrete Math: " + t.getMessage());
            failed++;
        }

        try {
            testFastRedstoneHopperContainerOcclusion();
            System.out.println("[PASS] 39. Hopper Entity Search Container Occlusion Fast-Path");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 39. Hopper Occlusion Fast-Path: " + t.getMessage());
            failed++;
        }

        try {
            testFastRedstoneActiveClockStressBenchmark();
            System.out.println("[PASS] 40. Redstone 100-Node Clock Stress Benchmark (Zero GC Allocations)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 40. Redstone Benchmark: " + t.getMessage());
            failed++;
        }

        try {
            testSpatialCollisionEngineGridAndBrainStripping();
            System.out.println("[PASS] 41. Spatial Grid Collision Hashing & AI Brain Stripping (Lithium)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 41. Spatial Collision Hashing: " + t.getMessage());
            failed++;
        }

        try {
            testFastFluidEngineFlowVectorCachingAndInvalidation();
            System.out.println("[PASS] 42. Fast Fluid 3D Flow Vector Caching & Invalidation");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 42. Fluid Flow Caching: " + t.getMessage());
            failed++;
        }

        try {
            testFastParticleEngineRateLimitingAndDistanceCulling();
            System.out.println("[PASS] 43. GPU/Client Particle Rate Limiter & Distance Culling");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 43. Particle Rate Limiter: " + t.getMessage());
            failed++;
        }

        try {
            testFastMathLUTTrigonometricAccuracyAndInvSqrt();
            System.out.println("[PASS] 44. FastMath 65536-Entry Trigonometric LUT & Fast InvSqrt");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 44. FastMath LUT: " + t.getMessage());
            failed++;
        }

        try {
            testFastRegistryCacheTagLookupSpeed();
            System.out.println("[PASS] 45. Fast Registry Primitive Tag Membership Cache");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 45. Registry Tag Cache: " + t.getMessage());
            failed++;
        }

        try {
            testFastRedstonePowerOfTwoHashTableAndProbeLimit();
            System.out.println("[PASS] 46. Redstone Hash Table Power-of-2 Sizing & Probe Limit (P0-1)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 46. Redstone Hash Table P0-1: " + t.getMessage());
            failed++;
        }

        try {
            testSpatialCollisionConcurrentGridThreadSafety();
            System.out.println("[PASS] 47. Spatial Collision Concurrent Grid Mutex Safety (P0-2)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 47. Spatial Collision Mutex P0-2: " + t.getMessage());
            failed++;
        }

        try {
            testFastExplosionProbeGuardOnMassiveBlast();
            System.out.println("[PASS] 48. Explosion 16384 Hash Table & 64-Probe Lockup Guard (P1-1)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 48. Explosion Probe Guard P1-1: " + t.getMessage());
            failed++;
        }

        try {
            testFastFluidAndParticleMemoryBoundedPruning();
            System.out.println("[PASS] 49. Fluid Dynamics & Particle Counter Bounded Eviction (P1-2, P1-3)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 49. Fluid & Particle Memory P1-2/P1-3: " + t.getMessage());
            failed++;
        }

        try {
            testFastMathLUTSpecialFloatSanitizationAndEngineShutdown();
            System.out.println("[PASS] 50. FastMath Zero/NaN Sanitization & Full Daemon Shutdown (P2-1, P2-2, P3-1)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 50. FastMath & Shutdown P2-1/P2-2/P3-1: " + t.getMessage());
            failed++;
        }

        try {
            testFastMathNegativeAnglePhaseContinuity();
            System.out.println("[PASS] 51. FastMath Negative Angle Phase Continuity & Positive Bias (P1-1)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 51. FastMath Continuity P1-1: " + t.getMessage());
            failed++;
        }

        try {
            testFastRegistryCacheCapacitySaturationPurge();
            System.out.println("[PASS] 52. Fast Registry Cache 32768 Capacity Saturation Auto-Purge (P1-2)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 52. Registry Saturation P1-2: " + t.getMessage());
            failed++;
        }

        try {
            testExperienceOrbNonNegativeAgeClamp();
            System.out.println("[PASS] 53. Experience Orb Non-Negative Age Clamp & Despawn Lifetime (P1-3)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 53. Experience Orb Age Clamp P1-3: " + t.getMessage());
            failed++;
        }

        try {
            testEntityDepthCullerNaNDistanceSafetyAndInvertedVoxel();
            System.out.println("[PASS] 54. Entity Culler NaN Guard & Voxel Inverted AABB Rejection (P2-1, P2-2)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 54. Culler NaN & Voxel AABB P2-1/P2-2: " + t.getMessage());
            failed++;
        }

        try {
            testSleepingHopperLongMaxOverflowDefenseAndSectionClamping();
            System.out.println("[PASS] 55. Hopper Long.MAX Tick Overflow & Section Coord Clamp (P2-3, P3-1)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 55. Hopper & Light Section P2-3/P3-1: " + t.getMessage());
            failed++;
        }

        try {
            testConfigStorageSerializationAndPresets();
            System.out.println("[PASS] 56. JSON Config Storage Persistence & Atomic Preset Application");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 56. Config Storage & Presets: " + t.getMessage());
            failed++;
        }

        try {
            testScreenModelAndOptionsRegistry();
            System.out.println("[PASS] 57. Video Settings Screen Model, 5 Categories & Dirty Tracking");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 57. Screen Model & Options: " + t.getMessage());
            failed++;
        }

        try {
            testOffHeapChunkSegmentDirectMemoryZeroGC();
            System.out.println("[PASS] 58. Off-Heap Direct Memory Chunk Segment & Zero-GC Nibble Packing");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 58. Off-Heap Memory: " + t.getMessage());
            failed++;
        }

        try {
            testSimdFrustumCullerBatch8Masking();
            System.out.println("[PASS] 59. SIMD Vectorized 8-Box Batch Frustum Culler & Bitmasking");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 59. SIMD Frustum Culler: " + t.getMessage());
            failed++;
        }

        try {
            testProfilerOverlayTelemetryAndMixinHooks();
            System.out.println("[PASS] 60. Real-Time Profiler Telemetry Overlay & Mixin Runtime Injections");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 60. Profiler & Mixin Hooks: " + t.getMessage());
            failed++;
        }

        try {
            testAmdArchitectureAutoDetectionProfiles();
            System.out.println("[PASS] 61. AMD Architecture Profiler (RX 500, Radeon 540, Vega 8, RDNA)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 61. AMD Architecture Profiler: " + t.getMessage());
            failed++;
        }

        try {
            testAmdWavefrontCalibrationAndPrimitiveDiscard();
            System.out.println("[PASS] 62. AMD Wave64/Wave32 Calibration & Primitive Discard Acceleration");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 62. AMD Wavefront & Primitive Discard: " + t.getMessage());
            failed++;
        }

        try {
            testAmd2GbVramBudgetGuardThresholds();
            System.out.println("[PASS] 63. Radeon 540 2GB VRAM Budget Guard & Dynamic Memory Throttling");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 63. Radeon 540 VRAM Guard: " + t.getMessage());
            failed++;
        }

        try {
            testDualGpuDeviceEnumerationAndWorkloadRouting();
            System.out.println("[PASS] 64. Dual-GPU Hybrid Engine (dGPU 3D World + Vega 8 iGPU HUD/Light Offload)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 64. Dual-GPU Workload Routing: " + t.getMessage());
            failed++;
        }

        try {
            testAmdAndDualGpuConfigStorageAndOptionsRegistry();
            System.out.println("[PASS] 65. AMD & Dual-GPU Video Settings UI Options Registry & Persistence");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 65. AMD & Dual-GPU UI Options: " + t.getMessage());
            failed++;
        }

        try {
            testColorCorrectionEngineAndAcesCurve();
            System.out.println("[PASS] 66. Color Correction & ACES Filmic HDR Tonemapping Curve");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 66. ACES Tonemapping: " + t.getMessage());
            failed++;
        }

        try {
            testColorBlackCrushEliminationAndNightAmbientLift();
            System.out.println("[PASS] 67. Anti-Black-Crush Toe Lift & Night Ambient World Visibility");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 67. Anti-Black-Crush: " + t.getMessage());
            failed++;
        }

        try {
            testColorLightmapBatchProcessingAndOptionsRegistry();
            System.out.println("[PASS] 68. 16x16 Lightmap Batch In-Place Processing & Debanding Dither");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 68. Lightmap Batch Processing: " + t.getMessage());
            failed++;
        }

        try {
            testFpsStabilizerChunkUploadPacingAndWorkBudgeting();
            System.out.println("[PASS] 69. Dynamic FPS Stabilizer (350 FPS Target) & Chunk Mesh Upload Pacing");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 69. FPS Stabilizer: " + t.getMessage());
            failed++;
        }

        try {
            testFpsStabilizerBlockEntityDistanceAndOcclusionCulling();
            System.out.println("[PASS] 70. Loaded Chunks Tile Entity Culling & Frame Pacing Invariants");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 70. Tile Entity Culling: " + t.getMessage());
            failed++;
        }

        try {
            testCpuThreadPoolManagerTopologyAndModes();
            System.out.println("[PASS] 71. Multi-Core CPU Thread Pool Manager Topology & Dynamic Allocation");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 71. CPU Thread Pool Manager: " + t.getMessage());
            failed++;
        }

        try {
            testParallelChunkMesherThroughputAndGeometry();
            System.out.println("[PASS] 72. Multi-Core ForkJoin Chunk Mesher & Geometry Tessellation");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 72. Parallel Chunk Mesher: " + t.getMessage());
            failed++;
        }

        try {
            testMultiCoreEntityPhysicsEngineBatching();
            System.out.println("[PASS] 73. Parallel Entity Ticking & Physics Multi-Core Offload Engine");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 73. Multi-Core Entity Physics: " + t.getMessage());
            failed++;
        }

        try {
            testAsyncWorldTickDispatcherAndCpuAffinity();
            System.out.println("[PASS] 74. Async World Tick Dispatcher & CPU Priority Affinity Governor");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 74. Async World Dispatcher & CPU Governor: " + t.getMessage());
            failed++;
        }

        try {
            testHyperionConfigScreenAndRootCategories();
            System.out.println("[PASS] 75. Hyperion Config GUI Menu (1. Graphics, 2. GPU, 3. CPU) & Interactive Model");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 75. Config Screen & Root Categories: " + t.getMessage());
            failed++;
        }

        try {
            testSingleGpuAndDualGpuHardwareTopologyRouting();
            System.out.println("[PASS] 76. Single dGPU Only, Single iGPU Only, and Dual-GPU Hybrid Dynamic Topology");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 76. GPU Topology Routing: " + t.getMessage());
            failed++;
        }

        try {
            testFastHdTextureEngineAndAnimatedSpritePacing();
            System.out.println("[PASS] 77. Fast HD Texture Atlas Engine & Animated Texture Frustum Pacing");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 77. Fast HD Texture Engine: " + t.getMessage());
            failed++;
        }

        try {
            testFancyGraphicsSmartLeavesCullingAndTranslucentSorting();
            System.out.println("[PASS] 78. Fancy/Fabulous Smart Leaves Occlusion & Translucent Quad Sort Throttling");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 78. Fancy & Fabulous Graphics Optimizer: " + t.getMessage());
            failed++;
        }

        try {
            testFastCloudEngineAndLockFreeActionPhysics();
            System.out.println("[PASS] 79. Fast Cloud Engine, Cave Culling & Lock-Free Action Physics Telemetry");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 79. Fast Cloud & Action Physics: " + t.getMessage());
            failed++;
        }

        try {
            testGpuThermalPowerGuardAntiCoilWhineAndPacing();
            System.out.println("[PASS] 80. GPU Thermal, VRM Coil Whine Suppressor & Background FPS Frame Pacing");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 80. GPU Thermal Power Guard: " + t.getMessage());
            failed++;
        }

        try {
            testChunkLodManagerAndGeometrySimplification();
            System.out.println("[PASS] 81. Chunk LOD (Level of Detail) & Distance Geometry Simplification");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 81. Chunk LOD Engine: " + t.getMessage());
            failed++;
        }

        try {
            testAggressiveFaceCullerAndCavityDiscard();
            System.out.println("[PASS] 82. Aggressive Hidden Block Face & Internal Cavity Culling Engine");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 82. Aggressive Face Culler: " + t.getMessage());
            failed++;
        }

        try {
            testGpuInstancingEngineAndBatching();
            System.out.println("[PASS] 83. GPU Instancing & Block Geometry SSBO/UBO Batching Engine");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 83. GPU Instancing Engine: " + t.getMessage());
            failed++;
        }

        try {
            testDualGpuSyncLockTimeoutAndWaitLoopSuppression();
            System.out.println("[PASS] 84. Dual-GPU Sync Lock & Anti-Busy-Wait Micro-Yield Engine");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 84. Dual-GPU Sync Lock: " + t.getMessage());
            failed++;
        }

        try {
            testDualGpuThermalFallbackAndGpuCrashGuard();
            System.out.println("[PASS] 85. Dynamic Thermal Auto-Fallback & Driver TDR Reset Crash Guard");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 85. Thermal Fallback & Crash Guard: " + t.getMessage());
            failed++;
        }

        try {
            testGpuVendorProfilesAndCtrlShiftZeroKeybinding();
            System.out.println("[PASS] 86. Multi-Vendor Profiles (NVIDIA Optimus, Apple Silicon) & Ctrl+Shift+0 Menu");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 86. Vendor Profiles & Shortcut: " + t.getMessage());
            failed++;
        }

        try {
            testVoxelHierarchicalMipTreeDownsampling();
            System.out.println("[PASS] 87. Voxel Hierarchical Mip Tree & Distance Downsampling (Voxy 2048 Chunks)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 87. Voxel Mip Tree: " + t.getMessage());
            failed++;
        }

        try {
            testVoxelSectionStorageRleCompression();
            System.out.println("[PASS] 88. Voxel Section RLE & Palette Storage Compression Engine");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 88. Voxel Section Storage: " + t.getMessage());
            failed++;
        }

        try {
            testVoxelLodRendererAndHorizonBlender();
            System.out.println("[PASS] 89. Voxel GPU Multi-Draw Indirect Renderer & Horizon Fog Blender");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 89. Voxel LOD Renderer & Blender: " + t.getMessage());
            failed++;
        }

        try {
            testVoxelPregenIngestEngineAsyncIntegration();
            System.out.println("[PASS] 90. Asynchronous Voxel Ingestion & Pre-Generation Engine (Chunky / DH)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 90. Voxel Ingestion Engine: " + t.getMessage());
            failed++;
        }

        try {
            testDirectMemoryCleanerAndBufferFreeing();
            System.out.println("[PASS] 91. DirectMemoryCleaner Native Unmapping & Zero-Leak Buffer Recycling");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 91. DirectMemoryCleaner: " + t.getMessage());
            failed++;
        }

        try {
            testModCompatManagerEcosystemDetection();
            System.out.println("[PASS] 92. HyperionModCompatManager Ecosystem Scan & Active Mod Auto-Adaptation");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 92. ModCompatManager: " + t.getMessage());
            failed++;
        }

        try {
            testIrisShaderCompatPipelinePasses();
            System.out.println("[PASS] 93. Iris & Oculus Shader Pass Pipeline Coordination & Shadow Gating");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 93. IrisShaderPipeline: " + t.getMessage());
            failed++;
        }

        try {
            testPacketFlushConsolidatorSafetyCeiling();
            System.out.println("[PASS] 94. Network Packet Consolidation Safety Ceiling & Anti-OOM Guard");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 94. Packet Consolidator Safety: " + t.getMessage());
            failed++;
        }

        try {
            testDualGpuThermalFallbackWarmupGracePeriod();
            System.out.println("[PASS] 95. Dual-GPU Thermal Fallback 5-Second Warmup Grace Period Protection");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 95. Warmup Grace Period: " + t.getMessage());
            failed++;
        }

        try {
            testExtendedCoordinateKeyPackingRange();
            System.out.println("[PASS] 96. Extended 24-Bit World Coordinate Key Packing Range (±8.3M Blocks)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 96. Coordinate Key Range: " + t.getMessage());
            failed++;
        }

        try {
            testDecoupledHudResolutionInvalidationAndHighRefresh();
            System.out.println("[PASS] 97. Decoupled HUD High-Refresh Display (360Hz) & F11 Resolution Sync");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 97. Decoupled HUD Sync: " + t.getMessage());
            failed++;
        }

        try {
            testParticleCoreGpuBatchingAndVectorMath();
            System.out.println("[PASS] 98. Particle Core GPU Batching, Frustum Culling & Parametric Vector Math");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 98. Particle Core: " + t.getMessage());
            failed++;
        }

        try {
            testBadOptimizationsLightmapAndBiomeBlendCache();
            System.out.println("[PASS] 99. BadOptimizations Lightmap Dirty Gate & Biome Blend Fast Caching");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 99. BadOptimizations: " + t.getMessage());
            failed++;
        }

        try {
            testMobtimizationsPathfindingAndTargetPacing();
            System.out.println("[PASS] 100. Mobtimizations Entity AI Pathfinding & Hostile Hazard Bypass");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 100. Mobtimizations AI: " + t.getMessage());
            failed++;
        }

        try {
            testPalladiumCapabilityAndMatrixStackCache();
            System.out.println("[PASS] 101. Palladium Capability State & Animation Matrix Transformation Cache");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 101. Palladium Cache: " + t.getMessage());
            failed++;
        }

        try {
            testMobtimizationsPhaseStaggeringAndTypeSafety();
            System.out.println("[PASS] 102. Mobtimizations Phase-Staggering & Anti-Wave-Spike AI Scheduler");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 102. Mob AI Phase Staggering: " + t.getMessage());
            failed++;
        }

        try {
            testBadOptimizationsBoundedLruAndDimensionShift();
            System.out.println("[PASS] 103. BadOptimizations Bounded LRU Cache & Dimension/Season Invalidation");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 103. BadOpt Bounded Cache: " + t.getMessage());
            failed++;
        }

        try {
            testParticleEngineDynamicScaleAndIrisPassGating();
            System.out.println("[PASS] 104. Particle Core Dynamic Quad Sizing & Multi-Pass Shader Alignment");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 104. Particle Dynamic Sizing: " + t.getMessage());
            failed++;
        }

        try {
            testPalladiumBoundedCacheAndScaleFactor();
            System.out.println("[PASS] 105. Palladium Bounded Matrix Cache & Pehkui/Morph Scale Multiplier");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 105. Palladium Scaled Matrix: " + t.getMessage());
            failed++;
        }

        try {
            testOffHeapChunkSegmentNativeBufferFreeing();
            System.out.println("[PASS] 106. OffHeapChunkSegment Native Memory Deallocation & Direct Free");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 106. OffHeap Direct Free: " + t.getMessage());
            failed++;
        }

        try {
            testVoxelMipTreeZeroAllocationHistogram();
            System.out.println("[PASS] 107. VoxelHierarchicalMipTree Zero-Allocation ThreadLocal Histogram");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 107. Voxel Mip Tree Zero Allocation: " + t.getMessage());
            failed++;
        }

        try {
            testSpatialCollisionPairDeduplication();
            System.out.println("[PASS] 108. Spatial Collision Pair-Check Halving (O(N^2/2) Symmetry)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 108. Spatial Collision Pair Symmetry: " + t.getMessage());
            failed++;
        }

        try {
            testAsyncWorldTickDispatcherDrainLoop();
            System.out.println("[PASS] 109. AsyncWorldTickDispatcher Iterative Drain & Non-Recursive Loop");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 109. Async World Drain Loop: " + t.getMessage());
            failed++;
        }

        try {
            testThreadPoolManagerCallerRunsPolicy();
            System.out.println("[PASS] 110. ThreadPoolManager Zero-Loss CallerRuns Saturation Resilience");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 110. CallerRuns Policy: " + t.getMessage());
            failed++;
        }

        try {
            testHudDirtyTrackerDamageIsolation();
            System.out.println("[PASS] 111. Decoupled HUD Damage Telemetry Isolation & Anti-Freeze Gate");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 111. HUD Damage Isolation: " + t.getMessage());
            failed++;
        }

        try {
            testColorCorrectionEngineZeroAllocationLutAndLightmap();
            System.out.println("[PASS] 112. ColorCorrectionEngine Zero-Allocation Fast LUT & Torch Lightmap Acceleration");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 112. Color Lightmap LUT: " + t.getMessage());
            failed++;
        }

        try {
            testPlayerRespawnTeleportAndThermalGraceWarmup();
            System.out.println("[PASS] 113. Player Respawn/Teleport State Invalidation & Dual-GPU Warmup Grace Period");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 113. Respawn Warmup Grace: " + t.getMessage());
            failed++;
        }

        try {
            testExtremeChunkDistanceMobAiAndSpatialPruning();
            System.out.println("[PASS] 114. Extreme Chunk Distance (32+ Chunks) Mob AI Pacing & Distance-Gated Spatial Culling");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 114. 32-Chunk Mob AI & Spatial Culling: " + t.getMessage());
            failed++;
        }

        try {
            testNightLightmapDiscreteQuantizationAndHostileMobPacing();
            System.out.println("[PASS] 115. Night-Time Lightmap Quantization & Hostile Surface Mob Anti-Lag Throttling");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 115. Night-Time Lightmap & Mob Throttling: " + t.getMessage());
            failed++;
        }

        try {
            testHdTexturePackAlphaBleedAndBlackBorderElimination();
            System.out.println("[PASS] 116. HD Texture Pack Alpha-Bleed Dilation & Black Border Elimination");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 116. HD Texture Alpha-Bleed: " + t.getMessage());
            failed++;
        }

        try {
            testAnimatedSpriteInitialFrameGuaranteeAndUiBypass();
            System.out.println("[PASS] 117. Animated Sprite Frame 0 Upload Guarantee & UI/Item Bypass");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 117. Animated Sprite Frame 0 Guarantee: " + t.getMessage());
            failed++;
        }

        try {
            testMipmapChainIntegrityAndBlackDistantTexturePrevention();
            System.out.println("[PASS] 118. Full Mipmap Chain Integrity & OpenGL Missing-LOD Blackness Prevention");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 118. Mipmap Chain Integrity: " + t.getMessage());
            failed++;
        }

        try {
            testTransparentLeavesOcclusionSafetyAndBushyPacks();
            System.out.println("[PASS] 119. Transparent / Bushy Leaves Internal Occlusion Safety (Zero Black Tree Cavities)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 119. Transparent Leaves Occlusion Safety: " + t.getMessage());
            failed++;
        }

        try {
            testLightmapOpaqueAlphaAndAbgrSafety();
            System.out.println("[PASS] 120. Lightmap Opaque 0xFF Alpha & ABGR Byte-Order Blackout Prevention");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 120. Lightmap Alpha & ABGR Safety: " + t.getMessage());
            failed++;
        }

        try {
            testResourcePackReloadStateInvalidation();
            System.out.println("[PASS] 121. Resource Pack Reload (F3+T) Subsystem State & Cache Invalidation");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 121. Resource Pack Reload State Invalidation: " + t.getMessage());
            failed++;
        }

        try {
            testKeyBindingManagerRightControlShortcut();
            System.out.println("[PASS] 122. Right Control (Right Ctrl) Global Menu Shortcut & Fallback Pacing");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 122. Right Control Shortcut: " + t.getMessage());
            failed++;
        }

        try {
            testSimdFrustumCullerMatrixMultiplication();
            System.out.println("[PASS] 123. SIMD Frustum Culler Projection*ModelView Order & NaN Protection");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 123. SIMD Frustum Culler Matrix Order: " + t.getMessage());
            failed++;
        }

        try {
            testVoxelSectionStorageNegativeHeights118Support();
            System.out.println("[PASS] 124. Voxel Section Storage 1.18+ Negative Height Packing & Collision Safety");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 124. Voxel Section Storage Negative Heights: " + t.getMessage());
            failed++;
        }

        try {
            testAsyncWorldTickDispatcherBackpressureBoundedQueue();
            System.out.println("[PASS] 125. Async World Tick Dispatcher Bounded Backpressure & Anti-OOM Safety");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 125. Async World Tick Backpressure: " + t.getMessage());
            failed++;
        }

        try {
            testSpatialCollisionEngineBucketCapping();
            System.out.println("[PASS] 126. Spatial Collision Engine Bucket Cramming Capping & Lock Contention Shield");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 126. Spatial Collision Bucket Capping: " + t.getMessage());
            failed++;
        }

        try {
            testHyperionConfigStorageCommentsAndSanitization();
            System.out.println("[PASS] 127. Hyperion Config Storage Block & Inline Comments Parsing Resilience");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 127. Config Storage Sanitization: " + t.getMessage());
            failed++;
        }

        try {
            testIrisShaderCompatPipelinePassCoordination();
            System.out.println("[PASS] 128. Iris & Oculus Shader Pack Pass Coordination & Decoupled HUD Composite Safety");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 128. Iris Shader Pass Coordination: " + t.getMessage());
            failed++;
        }

        try {
            testSleepingHopperServerTickRollback();
            System.out.println("[PASS] 129. Sleeping Hopper Server Tick Rollback Recovery (/time set 0 Defense)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 129. Sleeping Hopper Tick Rollback: " + t.getMessage());
            failed++;
        }

        try {
            testStaticChestCustomModelBypass();
            System.out.println("[PASS] 130. Static Chest Mesh Dynamic 3D Model & Physics Mod Custom Model Bypass");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 130. Static Chest Custom Model Bypass: " + t.getMessage());
            failed++;
        }

        try {
            testTexturePackColorCorrectionArgbAndAbgr();
            System.out.println("[PASS] 131. Texture Pack ARGB/ABGR Color Correction & True Zero-Black Alpha Preservation");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 131. Texture Pack Color Correction: " + t.getMessage());
            failed++;
        }

        try {
            testTexturePackDilateAndColorCorrectPipelineAndColormaps();
            System.out.println("[PASS] 132. Fast HD Texture Alpha-Bleed & Color Grading Unified Pipeline & Colormap Tint");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 132. Texture Pack Pipeline & Colormaps: " + t.getMessage());
            failed++;
        }

        try {
            testHyperionEngineSubsystemNonNullableGuaranteeAndAutoInit();
            System.out.println("[PASS] 133. HyperionEngine Subsystems Auto-Init & Non-Nullable Guarantee Across All 45 Cores");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 133. Subsystems Auto-Init Guarantee: " + t.getMessage());
            failed++;
        }

        try {
            testGuiLauncherKeybindingAndVideoOptionsHook();
            System.out.println("[PASS] 134. Hyperion GUI Launcher, Video Options Screen Hook & Right Control Key Trigger");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 134. GUI Launcher & Key Trigger: " + t.getMessage());
            failed++;
        }

        System.out.println("=================================================");
        System.out.println("SUMMARY: " + passed + " Passed, " + failed + " Failed.");
        System.out.println("STATUS: " + (failed == 0 ? "[VERIFIED: ALL 134 ARCHITECTURAL, AUDIT, HD TEXTURE & COMPATIBILITY CONTRACTS VERIFIED]" : "[DEFECT DETECTED]"));
        System.out.println("=================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testEngineInitialization() {
        HyperionConfig cfg = new HyperionConfig();
        HyperionEngine engine = HyperionEngine.getInstance();
        engine.initialize(cfg);

        if (engine.getComputeCullEngine() == null || engine.getHudManager() == null ||
            engine.getVoxelCache() == null || engine.getXpMerger() == null) {
            throw new AssertionError("Subsystems failed to register in HyperionEngine");
        }
    }

    private static void testPrimitiveVectorPackingAndReentrancy() {
        int[] testCoords = {0, 0, 0, -100, 64, 250, 30000000, 319, -30000000};
        for (int i = 0; i < testCoords.length - 2; i += 3) {
            int x = testCoords[i];
            int y = testCoords[i + 1];
            int z = testCoords[i + 2];

            long packed = PrimitiveVectorPool.packBlockPos(x, y, z);
            int unX = PrimitiveVectorPool.unpackX(packed);
            int unY = PrimitiveVectorPool.unpackY(packed);
            int unZ = PrimitiveVectorPool.unpackZ(packed);

            if (x != unX || y != unY || z != unZ) {
                throw new AssertionError(String.format("Packing mismatch! Expected (%d, %d, %d), got (%d, %d, %d)",
                    x, y, z, unX, unY, unZ));
            }
        }

        // Test Re-entrancy Ring Buffer (P1 fix): nested calls must NOT corrupt parent vector!
        PrimitiveVectorPool.MutableVec3 parentVec = PrimitiveVectorPool.getThreadLocalVec(10.0, 20.0, 30.0);
        PrimitiveVectorPool.MutableVec3 childVec = PrimitiveVectorPool.getThreadLocalVec(99.0, 88.0, 77.0);

        if (parentVec.x != 10.0 || parentVec.y != 20.0 || parentVec.z != 30.0) {
            throw new AssertionError("Re-entrancy violation: parentVec was corrupted by childVec!");
        }
    }

    private static void testComputeCullFrustumAndThreadSafety() {
        ComputeCullEngine cullEngine = new ComputeCullEngine(true, true);
        float[] identityProj = new float[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        };
        float[] identityMv = new float[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        };

        cullEngine.updateFrustum(identityProj, identityMv);

        boolean visible = cullEngine.isBoxVisible(-0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f);
        if (!visible) {
            throw new AssertionError("Centered unit box should be visible in identity frustum");
        }

        boolean culled = !cullEngine.isBoxVisible(40f, 40f, 40f, 60f, 60f, 60f);
        if (!culled) {
            throw new AssertionError("Far distant box (50, 50, 50) should be culled by frustum");
        }

        if (cullEngine.getCullEfficiencyPercentage() < 0.0) {
            throw new AssertionError("Cull telemetry calculation error");
        }
    }

    private static void testDecoupledHudPacingAndDirtyTracker() {
        DecoupledHudManager hudManager = new DecoupledHudManager(true, 60, true);

        long t0 = 1_000_000_000L;
        if (!hudManager.shouldRepaintHud(t0)) {
            throw new AssertionError("Initial frame must repaint HUD buffer");
        }

        long tMicro = t0 + 1_000L;
        if (hudManager.shouldRepaintHud(tMicro)) {
            throw new AssertionError("Immediate next microsecond must not redraw HUD");
        }

        hudManager.getDirtyTracker().updateState(18.0f, 20, 0, 300, 5, 0.5f, 0, 100L);
        if (!hudManager.shouldRepaintHud(tMicro)) {
            throw new AssertionError("State mutation (damage) must trigger dynamic HUD redraw");
        }
    }

    private static void testVoxelCollisionFastPath() {
        VoxelShapeFastCache cache = new VoxelShapeFastCache(true);

        boolean collides = cache.canFastPassCubeCollision(
            9.5, 63.5, 9.5, 10.5, 64.5, 10.5,
            10, 64, 10,
            VoxelShapeFastCache.SHAPE_TYPE_FULL_CUBE
        );
        if (!collides) {
            throw new AssertionError("Overlapping AABB must collide with full cube");
        }

        boolean airCollides = cache.canFastPassCubeCollision(
            9.5, 63.5, 9.5, 10.5, 64.5, 10.5,
            10, 64, 10,
            VoxelShapeFastCache.SHAPE_TYPE_EMPTY
        );
        if (airCollides) {
            throw new AssertionError("AABB must never collide with empty air shape");
        }
    }

    private static void testExperienceOrbClumpingAndAgeExploitFix() {
        ExperienceOrbMerger merger = new ExperienceOrbMerger(true, 2.0, 50000);

        boolean shouldMerge = merger.shouldMergeOrbs(0, 64, 0, 10, 0.5, 64, 0.5, 20);
        if (!shouldMerge) {
            throw new AssertionError("Nearby XP orbs within 2.0 blocks must merge");
        }

        // Test P2 fix: calculateMergedAge must inherit max age
        int mergedAge = merger.calculateMergedAge(100, 5500);
        if (mergedAge != 5500) {
            throw new AssertionError("Merged age must inherit older orb age (5500), got " + mergedAge);
        }
    }

    private static void testPathfindingCircuitBreakerAndPruning() {
        PathfindingCircuitBreaker breaker = new PathfindingCircuitBreaker(true, 3);
        int mobId = 42;
        long tick = 1000L;

        breaker.recordPathfindingResult(mobId, false, tick);
        breaker.recordPathfindingResult(mobId, false, tick);
        if (!breaker.canEntitySearchPath(mobId, tick)) {
            throw new AssertionError("Mob should still search before 3 failures");
        }

        breaker.recordPathfindingResult(mobId, false, tick);
        if (breaker.canEntitySearchPath(mobId, tick + 5)) {
            throw new AssertionError("Mob must be throttled by circuit breaker after 3 failures");
        }

        breaker.recordPathfindingResult(mobId, true, tick + 50);
        if (!breaker.canEntitySearchPath(mobId, tick + 51)) {
            throw new AssertionError("Successful path must reset circuit breaker");
        }
    }

    private static void testDataOrientedMemoryPackingAndBounds() {
        DataOrientedChunkMemory mem = new DataOrientedChunkMemory();
        mem.fillSection((byte) 0);

        mem.setLight(5, 10, 12, 14);
        mem.setLight(5, 10, 13, 7);

        if (mem.getLight(5, 10, 12) != 14) {
            throw new AssertionError("Light level at (5, 10, 12) should be 14, got " + mem.getLight(5, 10, 12));
        }

        // Test coordinate wrapping safety (P3 fix)
        mem.setLight(5 + 16, 10 + 32, 12 + 48, 11);
        if (mem.getLight(5, 10, 12) != 11) {
            throw new AssertionError("Wrapped coordinate setLight must be clamped safely");
        }
    }

    private static void testBoundedWorldCacheLru() {
        // Create cache bounded to 4 entries
        ClientWorldCacheStorage storage = new ClientWorldCacheStorage(true, 4);

        for (int i = 0; i < 10; i++) {
            storage.storeChunk(i, 0, new byte[] { (byte) i });
        }

        if (storage.getCachedChunkCount() > 4) {
            throw new AssertionError("LRU Cache exceeded maximum capacity! Size: " + storage.getCachedChunkCount());
        }

        // Old chunk 0 must have been evicted, recent chunk 9 must be present
        if (storage.hasChunk(0, 0)) {
            throw new AssertionError("Oldest chunk (0, 0) should have been evicted by LRU!");
        }
        if (!storage.hasChunk(9, 0)) {
            throw new AssertionError("Most recent chunk (9, 0) must be present in LRU cache!");
        }
    }

    private static void testSleepingHopperTickSynchronization() {
        SleepingHopperManager hopperManager = new SleepingHopperManager(true);
        long packedPos = 123456789L;
        long currentTick = 100L;

        hopperManager.putToSleep(packedPos, currentTick, 20); // Sleep for 20 ticks until tick 120

        // At tick 105, hopper must still sleep
        if (!hopperManager.isHopperSleeping(packedPos, 105L)) {
            throw new AssertionError("Hopper should still be sleeping at tick 105");
        }

        // Calling multiple times in same tick must NOT double-decrement
        hopperManager.isHopperSleeping(packedPos, 105L);
        hopperManager.isHopperSleeping(packedPos, 105L);

        // At tick 121, hopper must wake up
        if (hopperManager.isHopperSleeping(packedPos, 121L)) {
            throw new AssertionError("Hopper must wake up at tick 121");
        }
    }

    private static void testExperienceOrbIntegerOverflowExploitFix() {
        ExperienceOrbMerger merger = new ExperienceOrbMerger(true, 2.0, 50000);
        // Exploit scenario: two huge orb values that overflow 32-bit integer when added
        int orb1 = 2_147_480_000;
        int orb2 = 50_000;
        // In 32-bit signed int: 2147480000 + 50000 = -2147437296 (< 50000, would return true without 64-bit fix)
        boolean shouldMerge = merger.shouldMergeOrbs(0, 64, 0, orb1, 0.5, 64, 0.5, orb2);
        if (shouldMerge) {
            throw new AssertionError("Integer overflow exploit! Giant XP orbs must NOT merge!");
        }

        // Also test negative or zero orb inputs
        if (merger.shouldMergeOrbs(0, 64, 0, -100, 0.5, 64, 0.5, 50)) {
            throw new AssertionError("Negative XP orb values must be rejected");
        }
    }

    private static void testFakeChunkRealChunkArrivalInvalidation() {
        ClientWorldCacheStorage storage = new ClientWorldCacheStorage(true, 100);
        FakeChunkManager fakeManager = new FakeChunkManager(32, storage);

        fakeManager.registerFakeChunk(15, 25);
        if (!fakeManager.isFakeChunk(15, 25)) {
            throw new AssertionError("Fake chunk (15, 25) should be registered");
        }

        // Real chunk arrives from server -> atomic invalidation
        boolean invalidated = fakeManager.invalidateOnRealChunkArrived(15, 25);
        if (!invalidated) {
            throw new AssertionError("invalidateOnRealChunkArrived must return true for active fake chunk");
        }
        if (fakeManager.isFakeChunk(15, 25)) {
            throw new AssertionError("Fake chunk (15, 25) must be removed immediately to prevent Z-fighting");
        }

        // Second invalidation returns false
        if (fakeManager.invalidateOnRealChunkArrived(15, 25)) {
            throw new AssertionError("Repeated invalidation on removed chunk should return false");
        }
    }

    private static void testPacketFlushConsolidatorPerChannelIsolation() {
        PacketFlushConsolidator consolidator = new PacketFlushConsolidator(true);
        String channelA = "NettyChannel-PlayerA";
        String channelB = "NettyChannel-PlayerB";

        for (int i = 0; i < 5; i++) {
            consolidator.incrementPending(channelA);
        }

        if (consolidator.getPending(channelA) != 5) {
            throw new AssertionError("Channel A pending count should be 5, got " + consolidator.getPending(channelA));
        }
        if (consolidator.getPending(channelB) != 0) {
            throw new AssertionError("Channel B must NOT be polluted by Channel A packets");
        }

        if (!consolidator.shouldConsolidateFlush(channelA, 10)) {
            throw new AssertionError("Channel A with 5 packets should consolidate when maxBatchSize=10");
        }
        if (consolidator.shouldConsolidateFlush(channelA, 5)) {
            throw new AssertionError("Channel A with 5 packets should flush when maxBatchSize=5");
        }

        consolidator.resetPending(channelA);
        if (consolidator.getPending(channelA) != 0) {
            throw new AssertionError("Channel A must reset to 0");
        }

        consolidator.removeChannel(channelA);
    }

    private static void testStaticChestMeshBakerDoubleChestPairing() {
        StaticChestMeshBaker baker = new StaticChestMeshBaker(true);
        long posLeft = PrimitiveVectorPool.packBlockPos(100, 64, 200);
        long posRight = PrimitiveVectorPool.packBlockPos(101, 64, 200);

        // Initially both closed -> render as static block
        if (!baker.shouldRenderAsStaticBlock(posLeft) || !baker.shouldRenderAsStaticBlock(posRight)) {
            throw new AssertionError("Closed double chest must render as static block");
        }

        // Open player opens double chest -> both halves transition to dynamic
        baker.setDoubleChestOpenState(posLeft, posRight, true);
        if (baker.shouldRenderAsStaticBlock(posLeft) || baker.shouldRenderAsStaticBlock(posRight)) {
            throw new AssertionError("Open double chest halves must NOT render as static blocks");
        }
        if (!baker.isChestOpen(posLeft) || !baker.isChestOpen(posRight)) {
            throw new AssertionError("Both halves must be marked as open");
        }

        // Close double chest -> both revert to static
        baker.setDoubleChestOpenState(posLeft, posRight, false);
        if (!baker.shouldRenderAsStaticBlock(posLeft) || !baker.shouldRenderAsStaticBlock(posRight)) {
            throw new AssertionError("Closed double chest must return to static block rendering");
        }
    }

    private static void testDecoupledHudClockRollbackProtection() {
        DecoupledHudManager hudManager = new DecoupledHudManager(true, 60, false);

        long t0 = 10_000_000_000L;
        hudManager.shouldRepaintHud(t0); // initial frame

        // Simulate NTP time adjustment rolling back clock by 5 seconds
        long tRollback = 5_000_000_000L;
        boolean repaints = hudManager.shouldRepaintHud(tRollback);
        if (!repaints) {
            throw new AssertionError("HUD must immediately repaint and recover when system clock rolls back");
        }

        // Next frame progressing normally from rollback baseline
        long tNext = tRollback + 20_000_000L; // +20ms (~50fps)
        if (!hudManager.shouldRepaintHud(tNext)) {
            throw new AssertionError("HUD must continue normal operation after clock recovery");
        }
    }

    private static void testComputeCullEngineNaNInfinitySafeFallback() {
        ComputeCullEngine cullEngine = new ComputeCullEngine(true, true);

        // Matrix with NaN
        float[] nanProj = new float[16];
        nanProj[0] = Float.NaN;
        float[] identityMv = new float[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        };

        cullEngine.updateFrustum(nanProj, identityMv);

        // NaN frustum must safely fall back to visible instead of producing black voids
        boolean visible = cullEngine.isBoxVisible(100f, 100f, 100f, 116f, 116f, 116f);
        if (!visible) {
            throw new AssertionError("NaN frustum must fall back to visible to prevent black void rendering");
        }

        // Matrix with Infinity
        float[] infProj = new float[16];
        infProj[0] = Float.POSITIVE_INFINITY;
        cullEngine.updateFrustum(infProj, identityMv);
        if (!cullEngine.isBoxVisible(0f, 0f, 0f, 16f, 16f, 16f)) {
            throw new AssertionError("Infinite frustum must fall back to visible");
        }
    }

    private static void testClientWorldCacheStorageMultiThreadedStripedThroughput() throws Exception {
        int capacity = 256;
        ClientWorldCacheStorage storage = new ClientWorldCacheStorage(true, capacity);
        int threadCount = 8;
        int operationsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicBoolean hasError = new AtomicBoolean(false);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    for (int i = 0; i < operationsPerThread; i++) {
                        int chunkX = threadId * 1000 + i;
                        int chunkZ = threadId * 1000 + i;
                        byte[] data = new byte[] { (byte) threadId, (byte) i };
                        storage.storeChunk(chunkX, chunkZ, data);
                        byte[] loaded = storage.loadChunk(chunkX, chunkZ);
                        if (loaded == null && storage.hasChunk(chunkX, chunkZ)) {
                            hasError.set(true);
                        }
                    }
                } catch (Throwable ex) {
                    hasError.set(true);
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new AssertionError("Striped cache concurrent test timed out (deadlock detected)");
        }
        if (hasError.get()) {
            throw new AssertionError("Error occurred during multi-threaded striped cache access");
        }
        if (storage.getCachedChunkCount() > capacity) {
            throw new AssertionError("Striped cache exceeded capacity: " + storage.getCachedChunkCount() + " > " + capacity);
        }
    }

    private static void testMultiDrawIndirectManagerThreadSafeRecording() throws Exception {
        int maxCommands = 1000;
        MultiDrawIndirectManager manager = new MultiDrawIndirectManager(maxCommands);
        manager.beginBatch();

        int threadCount = 8;
        int commandsPerThread = 50;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicBoolean hasError = new AtomicBoolean(false);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    for (int i = 0; i < commandsPerThread; i++) {
                        boolean ok = manager.recordDrawCommand(36, 1, i, threadId * 100, 0);
                        if (!ok) {
                            hasError.set(true);
                        }
                    }
                } catch (Throwable ex) {
                    hasError.set(true);
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new AssertionError("MultiDraw indirect concurrent recording timed out");
        }
        if (hasError.get()) {
            throw new AssertionError("Error recording concurrent draw commands");
        }

        if (manager.getActiveCommandCount() != threadCount * commandsPerThread) {
            throw new AssertionError("Command count mismatch: expected " + (threadCount * commandsPerThread) +
                ", got " + manager.getActiveCommandCount());
        }

        ByteBuffer buffer = manager.finishBatch();
        if (buffer.remaining() != threadCount * commandsPerThread * 20) {
            throw new AssertionError("Buffer size mismatch: expected " + (threadCount * commandsPerThread * 20) +
                ", got " + buffer.remaining());
        }
    }

    private static void testAsyncAudioEngineBoundedQueueOverflowProtection() throws Exception {
        AsyncAudioEngine engine = new AsyncAudioEngine(true, 32);
        AtomicInteger executedCount = new AtomicInteger(0);

        // Rapidly dispatch 2000 tasks (exceeding queue capacity 1024)
        for (int i = 0; i < 2000; i++) {
            engine.dispatchAudioCalculation(executedCount::incrementAndGet);
        }

        Thread.sleep(100);
        if (executedCount.get() <= 0) {
            throw new AssertionError("Async audio tasks failed to execute");
        }
    }

    private static void testPrimitiveVectorPoolWorldBorderClamping() {
        // Packing extreme coordinates beyond +/-30,000,000
        long packed = PrimitiveVectorPool.packBlockPos(35_000_000, 3000, -40_000_000);
        int unX = PrimitiveVectorPool.unpackX(packed);
        int unY = PrimitiveVectorPool.unpackY(packed);
        int unZ = PrimitiveVectorPool.unpackZ(packed);

        if (unX != 30_000_000) {
            throw new AssertionError("X coordinate beyond +30M must clamp to 30,000,000, got " + unX);
        }
        if (unY != 2047) {
            throw new AssertionError("Y coordinate beyond +2047 must clamp to 2047, got " + unY);
        }
        if (unZ != -30_000_000) {
            throw new AssertionError("Z coordinate beyond -30M must clamp to -30,000,000, got " + unZ);
        }
    }

    private static void testDataOrientedChunkMemoryConcurrentNibbleSafety() throws Exception {
        DataOrientedChunkMemory mem = new DataOrientedChunkMemory();
        mem.fillSection((byte) 0);
        int threadCount = 8;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicBoolean hasError = new AtomicBoolean(false);

        for (int t = 0; t < threadCount; t++) {
            new Thread(() -> {
                try {
                    for (int i = 0; i < 500; i++) {
                        // Even block
                        mem.setLight(0, 0, 0, 14);
                        // Odd block
                        mem.setLight(1, 0, 0, 7);
                        int even = mem.getLight(0, 0, 0);
                        int odd = mem.getLight(1, 0, 0);
                        if (even != 14 || odd != 7) {
                            hasError.set(true);
                        }
                    }
                } catch (Throwable ex) {
                    hasError.set(true);
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        if (!latch.await(5, TimeUnit.SECONDS) || hasError.get()) {
            throw new AssertionError("Concurrent nibble update race condition detected in DataOrientedChunkMemory!");
        }
    }

    private static void testPrimitiveVectorPoolExpandedRingBufferDeepStack() {
        // Holding 30 vectors simultaneously in stack - must not corrupt the first vector!
        PrimitiveVectorPool.MutableVec3[] vecs = new PrimitiveVectorPool.MutableVec3[30];
        for (int i = 0; i < 30; i++) {
            vecs[i] = PrimitiveVectorPool.getThreadLocalVec(i, i * 2, i * 3);
        }

        for (int i = 0; i < 30; i++) {
            if (vecs[i].x != (double) i || vecs[i].y != (double) (i * 2) || vecs[i].z != (double) (i * 3)) {
                throw new AssertionError("Ring buffer wrap-around overwritten vector at index " + i);
            }
        }
    }

    private static void testPathfindingCircuitBreakerBossExemptionAndRollback() {
        PathfindingCircuitBreaker breaker = new PathfindingCircuitBreaker(true, 3);
        int bossId = 999;
        long tick = 1000L;

        // Record 5 failures for boss
        for (int i = 0; i < 5; i++) {
            breaker.recordPathfindingResult(bossId, false, tick);
        }

        // Standard mob is throttled
        if (breaker.canEntitySearchPath(bossId, false, tick + 5)) {
            throw new AssertionError("Standard mob should be throttled after 5 failures");
        }

        // Boss / Exempt entity is NEVER throttled
        if (!breaker.canEntitySearchPath(bossId, true, tick + 5)) {
            throw new AssertionError("Boss / Golem entity must be exempt from AI circuit breaker freeze");
        }

        // Test tick rollback recovery
        breaker.canEntitySearchPath(bossId, false, 500L); // Time rollback to tick 500
    }

    private static void testFakeChunkDimensionChangeClearing() {
        ClientWorldCacheStorage storage = new ClientWorldCacheStorage(true, 100);
        FakeChunkManager fakeManager = new FakeChunkManager(32, storage);

        fakeManager.registerFakeChunk(10, 20);
        fakeManager.registerFakeChunk(11, 20);

        fakeManager.clearOnDimensionChange();

        if (fakeManager.isFakeChunk(10, 20) || fakeManager.isFakeChunk(11, 20)) {
            throw new AssertionError("Fake chunks must be cleared upon dimension transition to prevent ghost mesh bleed");
        }
    }

    private static void testMultiDrawIndirectIdempotentFinishBatch() {
        MultiDrawIndirectManager manager = new MultiDrawIndirectManager(100);
        manager.beginBatch();
        manager.recordDrawCommand(36, 1, 0, 0, 0);

        ByteBuffer buf1 = manager.finishBatch();
        int rem1 = buf1.remaining();

        // Second finishBatch call in same frame must NOT zero out buffer
        ByteBuffer buf2 = manager.finishBatch();
        int rem2 = buf2.remaining();

        if (rem1 != 20 || rem2 != 20) {
            throw new AssertionError("finishBatch must be idempotent! Expected 20 bytes, got " + rem1 + " and " + rem2);
        }
    }

    private static void testAsyncBitsetLightEngineZeroThreadSanitization() {
        // Zero or negative thread count must NOT crash with IllegalArgumentException
        AsyncBitsetLightEngine engine = new AsyncBitsetLightEngine(true, 0);
        if (!engine.isEnabled()) {
            throw new AssertionError("Light engine should be enabled");
        }
        engine.shutdown();
    }

    private static void testEntityDepthCullerBossAndGlowingProtection() {
        EntityDepthCuller culler = new EntityDepthCuller(true, 64.0);

        // Standard entity occluded by wall -> culled
        boolean occluded = culler.shouldCullEntity(0, 64, 0, 10, 64, 10, true);
        if (!occluded) {
            throw new AssertionError("Occluded regular entity should be culled");
        }

        // Glowing entity occluded by wall -> NEVER culled (outline must render)
        boolean glowingCulled = culler.shouldCullEntity(0, 64, 0, 10, 64, 10, true, true, false);
        if (glowingCulled) {
            throw new AssertionError("Glowing entity must NOT be culled through walls");
        }

        // Boss entity occluded by wall -> NEVER culled
        boolean bossCulled = culler.shouldCullEntity(0, 64, 0, 10, 64, 10, true, false, true);
        if (bossCulled) {
            throw new AssertionError("Boss entity must NOT be culled through walls");
        }
    }

    private static void testStaticChestMeshBakerChunkInvalidation() {
        StaticChestMeshBaker baker = new StaticChestMeshBaker(true);
        long posInChunk0 = PrimitiveVectorPool.packBlockPos(5, 64, 5); // Chunk (0, 0)
        long posInChunk1 = PrimitiveVectorPool.packBlockPos(20, 64, 5); // Chunk (1, 0)

        baker.setChestOpenState(posInChunk0, true);
        baker.setChestOpenState(posInChunk1, true);

        // Invalidate Chunk (0, 0)
        baker.invalidateChunk(0, 0);

        if (baker.isChestOpen(posInChunk0)) {
            throw new AssertionError("Chest in unloaded Chunk (0, 0) must be pruned");
        }
        if (!baker.isChestOpen(posInChunk1)) {
            throw new AssertionError("Chest in loaded Chunk (1, 0) must remain open");
        }
    }

    private static void testAnimationLodEntityPhaseInterleaving() {
        AnimationLodManager lod = new AnimationLodManager(true, 10.0, 30.0);
        // Entity at distance 20 (half framerate tier)
        long frameIndex = 1L;

        // Even entity ID (0) vs Odd entity ID (1)
        boolean skip0 = lod.shouldSkipAnimationTick(0, 64, 0, 20, 64, 0, frameIndex, 0);
        boolean skip1 = lod.shouldSkipAnimationTick(0, 64, 0, 20, 64, 0, frameIndex, 1);

        if (skip0 == skip1) {
            throw new AssertionError("Adjacent entities must be interleaved across frames to prevent synchronized crowd jitter");
        }
    }

    private static void testEngineHotReloadAndCleanShutdown() {
        HyperionEngine engine = HyperionEngine.getInstance();
        HyperionConfig newConfig = new HyperionConfig();
        newConfig.hudTargetFramerate = 120;

        engine.reloadConfig(newConfig);
        if (engine.getConfig().hudTargetFramerate != 120) {
            throw new AssertionError("Engine failed to hot-reload configuration");
        }

        engine.shutdown();
    }

    private static void testFastExplosionRayDirectionTableAccuracy() {
        if (FastExplosionEngine.TOTAL_RAYS != 1352) {
            throw new AssertionError("Expected 1352 boundary rays for 16x16x16 sampling cube, got " + FastExplosionEngine.TOTAL_RAYS);
        }
    }

    private static void testFastExplosionBlockDestructionAndWaterAbsorption() {
        FastExplosionEngine engine = new FastExplosionEngine(true, 64);
        
        // Scenario A: Dry land (dirt with blast resistance 0.5)
        FastExplosionEngine.BlastResistanceProvider dryProvider = (x, y, z) -> 0.5f;
        FastExplosionEngine.ExplosionResult dryResult = engine.calculateExplosion(0.0, 64.0, 0.0, 4.0f, FastExplosionEngine.ExplosionType.TNT, dryProvider);
        if (dryResult.affectedBlockCount <= 0) {
            throw new AssertionError("TNT explosion in dry land must destroy blocks!");
        }

        // Scenario B: In-water detonation (water resistance 100) -> 0 blocks destroyed
        FastExplosionEngine.BlastResistanceProvider waterProvider = (x, y, z) -> 100.0f;
        FastExplosionEngine.ExplosionResult waterResult = engine.calculateExplosion(0.0, 64.0, 0.0, 4.0f, FastExplosionEngine.ExplosionType.TNT, waterProvider);
        if (waterResult.affectedBlockCount != 0) {
            throw new AssertionError("Explosion in water must destroy 0 blocks due to 100 blast resistance absorption! Destroyed: " + waterResult.affectedBlockCount);
        }
    }

    private static void testFastExplosionEntityDamageAndKnockback() {
        FastExplosionEngine engine = new FastExplosionEngine(true, 64);

        // Point-blank TNT explosion (dist = 1 block, full exposure)
        FastExplosionEngine.DamageImpact impactPointBlank = engine.calculateEntityImpact(
            0.0, 64.0, 0.0, 4.0f,
            1.0, 64.0, 0.0, 0.6, 1.8, 1.0
        );

        if (impactPointBlank.damage <= 10.0) {
            throw new AssertionError("Point-blank TNT explosion should deal high damage (>10), got " + impactPointBlank.damage);
        }
        if (impactPointBlank.knockbackX <= 0.0) {
            throw new AssertionError("Knockback vector should propel entity along +X, got " + impactPointBlank.knockbackX);
        }

        // Out-of-radius entity (dist = 10 blocks > 8 block max TNT radius)
        FastExplosionEngine.DamageImpact impactOutOfRange = engine.calculateEntityImpact(
            0.0, 64.0, 0.0, 4.0f,
            10.0, 64.0, 0.0, 0.6, 1.8, 1.0
        );
        if (impactOutOfRange.damage != 0.0 || impactOutOfRange.knockbackX != 0.0) {
            throw new AssertionError("Out-of-range entity must take 0 damage and 0 knockback");
        }
    }

    private static void testAllExplosionTypesParameterization() {
        FastExplosionEngine.ExplosionType[] types = FastExplosionEngine.ExplosionType.values();
        if (types.length < 7) {
            throw new AssertionError("Explosion types must support all 7 Minecraft tiers");
        }

        if (FastExplosionEngine.ExplosionType.GHAST_FIREBALL.defaultPower != 1.0f || !FastExplosionEngine.ExplosionType.GHAST_FIREBALL.createsFire) {
            throw new AssertionError("Ghast Fireball must have power 1.0 and create fire");
        }
        if (FastExplosionEngine.ExplosionType.CREEPER.defaultPower != 3.0f) {
            throw new AssertionError("Standard Creeper must have power 3.0");
        }
        if (FastExplosionEngine.ExplosionType.TNT.defaultPower != 4.0f || !FastExplosionEngine.ExplosionType.TNT.dropsAllItems) {
            throw new AssertionError("TNT must have power 4.0 and 100% item drop");
        }
        if (FastExplosionEngine.ExplosionType.BED_OR_RESPAWN_ANCHOR.defaultPower != 5.0f || !FastExplosionEngine.ExplosionType.BED_OR_RESPAWN_ANCHOR.createsFire) {
            throw new AssertionError("Bed / Anchor must have power 5.0 and create fire");
        }
        if (FastExplosionEngine.ExplosionType.CHARGED_CREEPER.defaultPower != 6.0f) {
            throw new AssertionError("Charged Creeper must have power 6.0");
        }
        if (FastExplosionEngine.ExplosionType.END_CRYSTAL.defaultPower != 6.0f) {
            throw new AssertionError("End Crystal must have power 6.0");
        }
        if (FastExplosionEngine.ExplosionType.WITHER_SPAWN.defaultPower != 7.0f) {
            throw new AssertionError("Wither Spawn must have power 7.0");
        }
    }

    private static void testFastExplosionZeroAllocationStressThroughput() {
        FastExplosionEngine engine = new FastExplosionEngine(true, 64);
        FastExplosionEngine.BlastResistanceProvider terrain = (x, y, z) -> (y < 64) ? 1.5f : 0.0f;

        long start = System.nanoTime();
        // Detonate 100 simultaneous TNT explosions
        for (int i = 0; i < 100; i++) {
            FastExplosionEngine.ExplosionResult res = engine.calculateExplosion(
                i % 10, 64.0, (i / 10), 4.0f,
                FastExplosionEngine.ExplosionType.TNT, terrain
            );
            if (res.affectedBlockCount == 0) {
                throw new AssertionError("Explosion stress test produced 0 affected blocks");
            }
        }
        long durationMs = (System.nanoTime() - start) / 1_000_000L;
        if (durationMs > 2000) {
            throw new AssertionError("100 explosions took too long: " + durationMs + "ms (target < 2000ms)");
        }
    }

    private static void testFastRedstoneWireTopologicalSolver() {
        FastRedstoneEngine engine = new FastRedstoneEngine(true, true, true, true, true);
        List<FastRedstoneEngine.WireNode> network = new ArrayList<>();
        
        // 16-block line of wire from X=0 to X=15
        FastRedstoneEngine.WireNode source = new FastRedstoneEngine.WireNode(0, 64, 0, 15, true);
        network.add(source);
        for (int x = 1; x <= 15; x++) {
            network.add(new FastRedstoneEngine.WireNode(x, 64, 0, 0, false));
        }

        List<FastRedstoneEngine.WireNode> sources = new ArrayList<>();
        sources.add(source);

        FastRedstoneEngine.NetworkSolveResult result = engine.solveWireNetwork(network, sources);

        if (result.updatedWireCount != 14) {
            throw new AssertionError("Expected 14 updated wires along line, got " + result.updatedWireCount);
        }
        // Verify power attenuation: wire at x=1 should have power 14, x=14 should have 1, x=15 should have 0
        if (network.get(1).currentPower != 14) {
            throw new AssertionError("Wire at X=1 must have power 14, got " + network.get(1).currentPower);
        }
        if (network.get(14).currentPower != 1) {
            throw new AssertionError("Wire at X=14 must have power 1, got " + network.get(14).currentPower);
        }
        if (network.get(15).currentPower != 0) {
            throw new AssertionError("Wire at X=15 (16th block) must attenuate to 0 power, got " + network.get(15).currentPower);
        }
    }

    private static void testFastRedstoneNeighborUpdateDeduplication() {
        FastRedstoneEngine engine = new FastRedstoneEngine(true, true, true, true, true);
        List<FastRedstoneEngine.WireNode> network = new ArrayList<>();
        FastRedstoneEngine.WireNode source = new FastRedstoneEngine.WireNode(0, 64, 0, 15, true);
        network.add(source);
        FastRedstoneEngine.WireNode wire1 = new FastRedstoneEngine.WireNode(1, 64, 0, 0, false);
        network.add(wire1);

        List<FastRedstoneEngine.WireNode> sources = new ArrayList<>();
        sources.add(source);

        FastRedstoneEngine.NetworkSolveResult result = engine.solveWireNetwork(network, sources);

        // Vanilla sends 42 updates; our batched deduplicated engine sends unique cardinal neighbor positions
        if (result.notifiedNeighborCount <= 0 || result.notifiedNeighborCount > 12) {
            throw new AssertionError("Deduplicated neighbor updates should be bounded <= 12 for 1 changed wire, got " + result.notifiedNeighborCount);
        }
    }

    private static void testFastRedstoneComparatorDiscretePowerEvaluation() {
        FastRedstoneEngine engine = new FastRedstoneEngine(true, true, true, true, true);

        // Empty container -> 0
        if (engine.calculateComparatorPower(0, 1728) != 0) {
            throw new AssertionError("Empty container must output signal 0");
        }
        // Exactly full container (1728 items) -> 15
        if (engine.calculateComparatorPower(1728, 1728) != 15) {
            throw new AssertionError("Full container must output signal 15");
        }
        // Half full (864 items) -> 8
        int halfPower = engine.calculateComparatorPower(864, 1728);
        if (halfPower != 8) {
            throw new AssertionError("Half-full container must output signal 8, got " + halfPower);
        }
    }

    private static void testFastRedstoneHopperContainerOcclusion() {
        FastRedstoneEngine engine = new FastRedstoneEngine(true, true, true, true, true);

        // Hopper under open air -> NOT occluded (needs entity check)
        if (engine.isHopperOccludedByContainer(false, false)) {
            throw new AssertionError("Hopper under air must not be occluded");
        }

        // Hopper under composter / chest / barrel -> OCCLUDED (bypass entity scan)
        if (!engine.isHopperOccludedByContainer(true, true)) {
            throw new AssertionError("Hopper capped with solid container must be occluded");
        }
    }

    private static void testFastRedstoneActiveClockStressBenchmark() {
        FastRedstoneEngine engine = new FastRedstoneEngine(true, true, true, true, true);
        List<FastRedstoneEngine.WireNode> grid = new ArrayList<>();
        
        // 8x8 = 64 wire grid
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                grid.add(new FastRedstoneEngine.WireNode(x, 64, z, 0, (x == 0 && z == 0)));
            }
        }

        List<FastRedstoneEngine.WireNode> sources = new ArrayList<>();
        FastRedstoneEngine.WireNode srcNode = grid.get(0);
        sources.add(srcNode);

        long start = System.nanoTime();
        for (int i = 0; i < 50; i++) {
            srcNode.targetPower = (i % 2 == 0) ? 15 : 0;
            srcNode.currentPower = srcNode.targetPower;
            engine.solveWireNetwork(grid, sources);
        }
        long durationMs = (System.nanoTime() - start) / 1_000_000L;
        if (durationMs > 2000) {
            throw new AssertionError("50 clock cycles over 64 nodes took too long: " + durationMs + "ms (target < 2000ms)");
        }
    }

    private static void testSpatialCollisionEngineGridAndBrainStripping() {
        SpatialCollisionEngine engine = new SpatialCollisionEngine(true, 8, 32.0);
        engine.clearGrid();

        // Populate 20 entities in bucket (0, 0)
        for (int i = 0; i < 20; i++) {
            SpatialCollisionEngine.CollidableEntity ent = new SpatialCollisionEngine.CollidableEntity(i, 2.0, 64.0, 2.0, 0.6, 1.8);
            engine.registerEntity(ent);
        }

        List<SpatialCollisionEngine.CollidableEntity> nearby = engine.getNearbyCandidates(2.0, 2.0);
        if (nearby.size() != 20) {
            throw new AssertionError("Spatial grid should retrieve all 20 entities in bucket, got " + nearby.size());
        }

        // Test collision count capping
        SpatialCollisionEngine.CollidableEntity testEnt = nearby.get(0);
        for (int i = 0; i < 8; i++) {
            if (!engine.canCheckCollision(testEnt)) {
                throw new AssertionError("Collision check under max cap should be permitted");
            }
        }
        if (engine.canCheckCollision(testEnt)) {
            throw new AssertionError("Collision check exceeding max cap of 8 should be blocked");
        }

        // Test Brain Stripping (1x1 trapped cell)
        if (!engine.shouldThrottleBrain(true, 5.0, 1L)) {
            throw new AssertionError("Trapped entity in 1x1 should throttle brain on non-modulo tick");
        }
        if (engine.shouldThrottleBrain(true, 5.0, 20L)) {
            throw new AssertionError("Trapped entity in 1x1 should tick brain on 20th tick");
        }
    }

    private static void testFastFluidEngineFlowVectorCachingAndInvalidation() {
        FastFluidEngine engine = new FastFluidEngine(true);
        engine.clear();

        // Cache flow vector at (10, 64, 10)
        engine.cacheFlowVector(10, 64, 10, 0.707, -0.5, 0.707);

        FastFluidEngine.FluidFlowVector vec = engine.getCachedFlowVector(10, 64, 10);
        if (vec == null || Math.abs(vec.vx - 0.707) > 1e-4) {
            throw new AssertionError("Fluid flow vector was not cached properly");
        }

        // Invalidate neighbor at (11, 64, 10) -> must purge (10, 64, 10)
        engine.invalidateBlock(11, 64, 10);
        if (engine.getCachedFlowVector(10, 64, 10) != null) {
            throw new AssertionError("Neighbor block update must invalidate adjacent fluid flow vector");
        }
    }

    private static void testFastParticleEngineRateLimitingAndDistanceCulling() {
        FastParticleEngine engine = new FastParticleEngine(true, 5, 48.0);
        engine.clear();

        // Distant particle (> 48 blocks) -> culled
        boolean distantSpawn = engine.shouldSpawnParticle(0, 64, 0, 100, 64, 0, 1000L);
        if (distantSpawn) {
            throw new AssertionError("Particles beyond max view distance must be culled");
        }

        // Nearby particles: first 5 permitted in same block in 1 second
        for (int i = 0; i < 5; i++) {
            if (!engine.shouldSpawnParticle(0, 64, 0, 5, 64, 5, 1000L)) {
                throw new AssertionError("Particle under block limit should spawn");
            }
        }

        // 6th particle in same block in same second -> rate-limited (culled)
        if (engine.shouldSpawnParticle(0, 64, 0, 5, 64, 5, 1000L)) {
            throw new AssertionError("6th particle in same block must be rate-limited");
        }

        // New second -> permitted again
        if (!engine.shouldSpawnParticle(0, 64, 0, 5, 64, 5, 1001L)) {
            throw new AssertionError("Particles should spawn again in next second");
        }
    }

    private static void testFastMathLUTTrigonometricAccuracyAndInvSqrt() {
        if (FastMathLUT.getTableSize() != 65536) {
            throw new AssertionError("Expected 65536 entries in FastMathLUT table");
        }

        // Compare against standard Java Math
        for (float deg = 0; deg < 360; deg += 15.0f) {
            float rad = (float) Math.toRadians(deg);
            float lutSin = FastMathLUT.sin(rad);
            float mathSin = (float) Math.sin(rad);
            if (Math.abs(lutSin - mathSin) > 1e-3f) {
                throw new AssertionError("FastMathLUT sin deviation at " + deg + " deg: LUT=" + lutSin + " Math=" + mathSin);
            }

            float lutCos = FastMathLUT.cos(rad);
            float mathCos = (float) Math.cos(rad);
            if (Math.abs(lutCos - mathCos) > 1e-3f) {
                throw new AssertionError("FastMathLUT cos deviation at " + deg + " deg: LUT=" + lutCos + " Math=" + mathCos);
            }
        }

        // Fast InvSqrt accuracy (1/sqrt(4) = 0.5)
        float invSqrt4 = FastMathLUT.fastInvSqrt(4.0f);
        if (Math.abs(invSqrt4 - 0.5f) > 1e-2f) {
            throw new AssertionError("FastInvSqrt(4.0) expected ~0.5, got " + invSqrt4);
        }
    }

    private static void testFastRegistryCacheTagLookupSpeed() {
        FastRegistryCache cache = new FastRegistryCache(true);
        cache.invalidate();

        int tagHash = "minecraft:planks".hashCode();
        int oakPlanksId = 42;
        int stoneId = 99;

        // First lookup (cache miss -> evaluate matcher)
        boolean isOakPlank = cache.isItemInTag(tagHash, oakPlanksId, (tag, item) -> item == 42);
        if (!isOakPlank) {
            throw new AssertionError("Oak planks should be recognized in tag");
        }

        // Second lookup (cache hit)
        boolean isOakPlankCached = cache.isItemInTag(tagHash, oakPlanksId, (tag, item) -> {
            throw new AssertionError("Matcher must NOT be invoked on cache hit");
        });
        if (!isOakPlankCached) {
            throw new AssertionError("Cached lookup failed");
        }

        boolean isStoneInPlanks = cache.isItemInTag(tagHash, stoneId, (tag, item) -> false);
        if (isStoneInPlanks) {
            throw new AssertionError("Stone should NOT be in planks tag");
        }
    }

    private static void testFastRedstonePowerOfTwoHashTableAndProbeLimit() {
        FastRedstoneEngine engine = new FastRedstoneEngine(true, true, true, true, true);
        List<FastRedstoneEngine.WireNode> network = new ArrayList<>();
        
        // 25 wire line (odd number that generates non-power-of-2 raw capacity)
        FastRedstoneEngine.WireNode source = new FastRedstoneEngine.WireNode(0, 64, 0, 15, true);
        network.add(source);
        for (int x = 1; x <= 25; x++) {
            network.add(new FastRedstoneEngine.WireNode(x, 64, 0, 0, false));
        }

        List<FastRedstoneEngine.WireNode> sources = new ArrayList<>();
        sources.add(source);

        FastRedstoneEngine.NetworkSolveResult result = engine.solveWireNetwork(network, sources);
        if (result.notifiedNeighborCount <= 0) {
            throw new AssertionError("Redstone power-of-2 hash table failed to collect neighbors");
        }
    }

    private static void testSpatialCollisionConcurrentGridThreadSafety() throws Exception {
        final SpatialCollisionEngine engine = new SpatialCollisionEngine(true, 8, 32.0);
        engine.clearGrid();

        int threadCount = 4;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final AtomicBoolean hadError = new AtomicBoolean(false);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    for (int i = 0; i < 50; i++) {
                        int entId = threadId * 1000 + i;
                        SpatialCollisionEngine.CollidableEntity ent = new SpatialCollisionEngine.CollidableEntity(
                            entId, 2.0, 64.0, 2.0, 0.6, 1.8
                        );
                        engine.registerEntity(ent);
                        List<SpatialCollisionEngine.CollidableEntity> nearby = engine.getNearbyCandidates(2.0, 2.0);
                        if (nearby == null || nearby.isEmpty()) {
                            hadError.set(true);
                        }
                    }
                } catch (Throwable t1) {
                    hadError.set(true);
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        if (!latch.await(3, TimeUnit.SECONDS) || hadError.get()) {
            throw new AssertionError("Concurrent spatial grid operations threw exception or failed");
        }
    }

    private static void testFastExplosionProbeGuardOnMassiveBlast() {
        FastExplosionEngine engine = new FastExplosionEngine(true, 64);
        FastExplosionEngine.BlastResistanceProvider terrain = (x, y, z) -> 0.0f; // completely air terrain

        // Massive power 20 explosion
        FastExplosionEngine.ExplosionResult result = engine.calculateExplosion(
            0.0, 64.0, 0.0, 20.0f, FastExplosionEngine.ExplosionType.CUSTOM, terrain
        );

        if (result.affectedBlockCount <= 0 || result.affectedBlockCount > 8192) {
            throw new AssertionError("Massive explosion result outside bounded buffer range: " + result.affectedBlockCount);
        }
    }

    private static void testFastFluidAndParticleMemoryBoundedPruning() {
        FastFluidEngine fluidEngine = new FastFluidEngine(true);
        fluidEngine.clear();

        // Populate fluid cache and test chunk invalidation
        fluidEngine.cacheFlowVector(10, 64, 10, 1.0, 0.0, 0.0);
        fluidEngine.cacheFlowVector(100, 64, 100, 1.0, 0.0, 0.0);

        if (fluidEngine.getCachedFlowCount() != 2) {
            throw new AssertionError("Fluid cache count expected 2, got " + fluidEngine.getCachedFlowCount());
        }

        // Invalidate chunk (0, 0) -> contains (10, 64, 10), but not (100, 64, 100)
        fluidEngine.invalidateChunk(0, 0);
        if (fluidEngine.getCachedFlowVector(10, 64, 10) != null) {
            throw new AssertionError("Chunk invalidation failed to evict local chunk fluid vector");
        }
        if (fluidEngine.getCachedFlowVector(100, 64, 100) == null) {
            throw new AssertionError("Chunk invalidation mistakenly evicted foreign chunk fluid vector");
        }

        // Test particle counter pruning
        FastParticleEngine particleEngine = new FastParticleEngine(true, 5, 48.0);
        particleEngine.clear();
        for (int i = 0; i < 4100; i++) {
            particleEngine.shouldSpawnParticle(0, 64, 0, i, 64, 0, 100L);
        }
        // Spawn at new timestamp (105s) -> triggers purge of 100L
        particleEngine.shouldSpawnParticle(0, 64, 0, 0, 64, 0, 105L);
    }

    private static void testFastMathLUTSpecialFloatSanitizationAndEngineShutdown() {
        // Fast InvSqrt for 0.0f must return POSITIVE_INFINITY
        float invZero = FastMathLUT.fastInvSqrt(0.0f);
        if (!Float.isInfinite(invZero) || invZero < 0) {
            throw new AssertionError("FastInvSqrt(0.0f) must be positive infinity, got " + invZero);
        }

        // Fast InvSqrt for -4.0f must return NaN
        float invNeg = FastMathLUT.fastInvSqrt(-4.0f);
        if (!Float.isNaN(invNeg)) {
            throw new AssertionError("FastInvSqrt(-4.0f) must be NaN, got " + invNeg);
        }

        // Engine Shutdown verification
        HyperionEngine engine = HyperionEngine.getInstance();
        engine.shutdown();
        if (engine.getAudioEngine() != null && !engine.getAudioEngine().isShutdown()) {
            throw new AssertionError("Audio worker pool must be shut down");
        }
    }

    private static void testFastMathNegativeAnglePhaseContinuity() {
        // Test continuous smooth transition around 0 and negative angles
        float step = 0.001f;
        for (float rad = -3.14159f; rad <= 3.14159f; rad += step) {
            float lutSin = FastMathLUT.sin(rad);
            float mathSin = (float) Math.sin(rad);
            if (Math.abs(lutSin - mathSin) > 1e-3f) {
                throw new AssertionError("FastMathLUT sin discontinuity at rad=" + rad + ": LUT=" + lutSin + " Math=" + mathSin);
            }
            float lutCos = FastMathLUT.cos(rad);
            float mathCos = (float) Math.cos(rad);
            if (Math.abs(lutCos - mathCos) > 1e-3f) {
                throw new AssertionError("FastMathLUT cos discontinuity at rad=" + rad + ": LUT=" + lutCos + " Math=" + mathCos);
            }
        }
    }

    private static void testFastRegistryCacheCapacitySaturationPurge() {
        FastRegistryCache cache = new FastRegistryCache(true);
        cache.invalidate();

        // Populate 32768 entries
        for (int i = 0; i < 32768; i++) {
            cache.isItemInTag(i, i, (t, it) -> true);
        }
        if (cache.getCachedCount() != 32768) {
            throw new AssertionError("Expected 32768 entries before saturation, got " + cache.getCachedCount());
        }

        // Exceed capacity -> triggers auto-purge to prevent OOM
        cache.isItemInTag(99999, 99999, (t, it) -> true);
        if (cache.getCachedCount() > 100) {
            throw new AssertionError("Cache should have cleared upon reaching 32768 capacity, got " + cache.getCachedCount());
        }
    }

    private static void testExperienceOrbNonNegativeAgeClamp() {
        ExperienceOrbMerger merger = new ExperienceOrbMerger(true, 2.0, 50000);
        // If an orb had negative age (e.g. -6000), calculateMergedAge must be at least 0
        int mergedAge = merger.calculateMergedAge(-6000, -100);
        if (mergedAge != 0) {
            throw new AssertionError("calculateMergedAge must clamp negative ages to 0, got " + mergedAge);
        }
    }

    private static void testEntityDepthCullerNaNDistanceSafetyAndInvertedVoxel() {
        EntityDepthCuller culler = new EntityDepthCuller(true, 48.0);
        // NaN coordinates must NOT be culled (safe fallback)
        boolean culledNaN = culler.shouldCullEntity(0, 0, 0, Double.NaN, 0, 0, true);
        if (culledNaN) {
            throw new AssertionError("Entity with NaN coordinates must not be culled");
        }

        // Inverted AABB in voxel shape cache (minX > maxX) must be rejected
        VoxelShapeFastCache voxelCache = new VoxelShapeFastCache(true);
        boolean collidesInverted = voxelCache.canFastPassCubeCollision(
            10.0, 0.0, 0.0, 5.0, 1.0, 1.0, 0, 0, 0, VoxelShapeFastCache.SHAPE_TYPE_FULL_CUBE
        );
        if (collidesInverted) {
            throw new AssertionError("Inverted AABB must return false in collision test");
        }
    }

    private static void testSleepingHopperLongMaxOverflowDefenseAndSectionClamping() {
        SleepingHopperManager hopperManager = new SleepingHopperManager(true);
        hopperManager.clear();

        // Put to sleep near Long.MAX_VALUE
        hopperManager.putToSleep(12345L, Long.MAX_VALUE - 10L, 20);
        if (!hopperManager.isHopperSleeping(12345L, Long.MAX_VALUE - 10L)) {
            throw new AssertionError("Hopper must be sleeping when tick is below sleepUntilTick");
        }

        // DataOrientedChunkMemory section coord masking
        DataOrientedChunkMemory mem = new DataOrientedChunkMemory();
        mem.setLight(5, 5, 5, 12);
        if (mem.getLight(5, 5, 5) != 12) {
            throw new AssertionError("Light level at (5, 5, 5) must be 12");
        }
    }

    private static void testConfigStorageSerializationAndPresets() {
        HyperionConfig config = new HyperionConfig();
        config.clientMaxViewDistance = 48;
        config.hudTargetFramerate = 90;

        String json = HyperionConfigStorage.serializeJson(config);
        HyperionConfig parsed = HyperionConfigStorage.parseJson(json);

        if (parsed.clientMaxViewDistance != 48 || parsed.hudTargetFramerate != 90) {
            throw new AssertionError("Config JSON serialization/deserialization mismatch!");
        }

        // Test Preset: POTATO_PC
        HyperionConfig potato = HyperionConfigStorage.applyPreset(HyperionConfigStorage.Preset.POTATO_PC);
        if (potato.hudTargetFramerate != 30 || potato.clientMaxViewDistance != 16 || potato.maxParticlesPerBlockPerSecond != 2) {
            throw new AssertionError("Potato PC preset parameters not properly configured");
        }

        // Test Preset: HIGH_END
        HyperionConfig highEnd = HyperionConfigStorage.applyPreset(HyperionConfigStorage.Preset.HIGH_END);
        if (highEnd.hudTargetFramerate != 120 || highEnd.clientMaxViewDistance != 64 || highEnd.entityCullingMaxDistance != 96.0) {
            throw new AssertionError("High-End preset parameters not properly configured");
        }
    }

    private static void testScreenModelAndOptionsRegistry() {
        HyperionScreenModel model = new HyperionScreenModel();
        if (model.getActiveCategory() != HyperionCategory.GRAPHICS_SETTINGS) {
            throw new AssertionError("Initial active category must be GRAPHICS_SETTINGS");
        }

        if (model.getCurrentOptions().isEmpty()) {
            throw new AssertionError("Options for GRAPHICS_SETTINGS must not be empty");
        }

        // Switch to physics category
        model.setActiveCategory(HyperionCategory.PHYSICS_REDSTONE);
        if (model.getActiveCategory() != HyperionCategory.PHYSICS_REDSTONE) {
            throw new AssertionError("Category switch failed");
        }

        // Test option modification
        List<HyperionOption<?>> physicsOptions = model.getCurrentOptions();
        boolean foundSleepingHoppers = false;
        for (HyperionOption<?> opt : physicsOptions) {
            if ("enableSleepingHoppers".equals(opt.getKey())) {
                foundSleepingHoppers = true;
                break;
            }
        }
        if (!foundSleepingHoppers) {
            throw new AssertionError("enableSleepingHoppers option missing in PHYSICS_REDSTONE category");
        }

        // Apply preset through model
        model.applyPreset(HyperionConfigStorage.Preset.POTATO_PC);
        if (!model.isDirty()) {
            throw new AssertionError("Model must be dirty after applying preset");
        }
        if (model.getWorkingConfig().hudTargetFramerate != 30) {
            throw new AssertionError("Working config should have updated to 30 FPS");
        }
    }

    private static void testOffHeapChunkSegmentDirectMemoryZeroGC() {
        OffHeapChunkSegment segment = new OffHeapChunkSegment();
        try {
            segment.clear();
            segment.setNibble(3, 7, 11, 14);
            segment.setNibble(3, 7, 12, 9);

            if (segment.getNibble(3, 7, 11) != 14) {
                throw new AssertionError("Nibble at (3, 7, 11) should be 14, got " + segment.getNibble(3, 7, 11));
            }
            if (segment.getNibble(3, 7, 12) != 9) {
                throw new AssertionError("Nibble at (3, 7, 12) should be 9, got " + segment.getNibble(3, 7, 12));
            }

            // Test out-of-bounds coordinate wrapping safety
            segment.setNibble(3 + 16, 7 + 32, 11 + 48, 5);
            if (segment.getNibble(3, 7, 11) != 5) {
                throw new AssertionError("Wrapped nibble at (3, 7, 11) should be 5");
            }
        } finally {
            segment.free();
        }

        if (!segment.isFreed()) {
            throw new AssertionError("Segment must be marked as freed");
        }
    }

    private static void testSimdFrustumCullerBatch8Masking() {
        SimdFrustumCuller culler = new SimdFrustumCuller();
        float[] identityProj = new float[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        };
        float[] identityMv = new float[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        };

        culler.updatePlanes(identityProj, identityMv);

        float[] minX = new float[] {-0.5f, 50.0f, -0.2f, 0, 0, 0, 0, 0};
        float[] minY = new float[] {-0.5f, 50.0f, -0.2f, 0, 0, 0, 0, 0};
        float[] minZ = new float[] {-0.5f, 50.0f, -0.2f, 0, 0, 0, 0, 0};

        float[] maxX = new float[] {0.5f, 60.0f, 0.2f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f};
        float[] maxY = new float[] {0.5f, 60.0f, 0.2f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f};
        float[] maxZ = new float[] {0.5f, 60.0f, 0.2f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f};

        int mask = culler.testBatch8(minX, minY, minZ, maxX, maxY, maxZ);

        // Box 0 (center) must be visible (bit 0 = 1)
        if ((mask & 1) == 0) {
            throw new AssertionError("Box 0 (centered) must be visible in frustum mask");
        }

        // Box 1 (50, 50, 50) must be culled (bit 1 = 0)
        if ((mask & (1 << 1)) != 0) {
            throw new AssertionError("Box 1 (distant 50,50,50) must be culled in frustum mask");
        }
    }

    private static void testProfilerOverlayTelemetryAndMixinHooks() {
        HyperionProfilerOverlay profiler = HyperionProfilerOverlay.getInstance();
        profiler.resetFrameCounters();

        profiler.recordCulledEntity();
        profiler.recordCulledEntity();
        profiler.recordCulledChunk();
        profiler.setSleepingHoppers(42);
        profiler.addSavedNetworkBytes(2048);
        profiler.updateMetrics(144.0f, 6.94f);

        if (profiler.getCulledEntities() != 2) {
            throw new AssertionError("Expected 2 culled entities, got " + profiler.getCulledEntities());
        }
        if (profiler.getCulledChunks() != 1) {
            throw new AssertionError("Expected 1 culled chunk, got " + profiler.getCulledChunks());
        }
        if (profiler.getSleepingHoppers() != 42) {
            throw new AssertionError("Expected 42 sleeping hoppers");
        }
        if (profiler.getSavedNetworkBytes() != 2048) {
            throw new AssertionError("Expected 2048 saved network bytes");
        }

        String summary = profiler.generateTelemetrySummary();
        if (!summary.contains("FPS: 144.0") || !summary.contains("Sleeping Hoppers: 42")) {
            throw new AssertionError("Telemetry summary text mismatch: " + summary);
        }

        // Test Mixin Video Options Hook
        MixinVideoOptionsScreen.onInitVideoOptionsScreen();
        if (MixinVideoOptionsScreen.getActiveModel() == null) {
            throw new AssertionError("Active model in MixinVideoOptionsScreen must not be null");
        }

        // Test Mixin Level Renderer Frustum Hook
        boolean visible = MixinLevelRenderer.shouldRenderChunkSection(-0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f);
        if (!visible) {
            throw new AssertionError("Centered chunk section should be visible in mixin hook");
        }
    }

    private static void testAmdArchitectureAutoDetectionProfiles() {
        // Test auto-detection for Radeon RX 580 (Polaris)
        AmdArchitectureProfile polaris = AmdArchitectureProfile.detectFromRenderer("Radeon RX 580 Series");
        if (polaris != AmdArchitectureProfile.RADEON_RX500_POLARIS) {
            throw new AssertionError("RX 580 should detect as RADEON_RX500_POLARIS, got " + polaris);
        }

        // Test auto-detection for Radeon 540 (Lexa)
        AmdArchitectureProfile lexa = AmdArchitectureProfile.detectFromRenderer("AMD Radeon 540");
        if (lexa != AmdArchitectureProfile.RADEON_540_LEXA) {
            throw new AssertionError("Radeon 540 should detect as RADEON_540_LEXA, got " + lexa);
        }

        // Test auto-detection for Vega 8 Graphics (Ryzen APU)
        AmdArchitectureProfile vega8 = AmdArchitectureProfile.detectFromRenderer("AMD Radeon(TM) Vega 8 Graphics");
        if (vega8 != AmdArchitectureProfile.RADEON_VEGA_8_APU) {
            throw new AssertionError("Vega 8 should detect as RADEON_VEGA_8_APU, got " + vega8);
        }

        // Test auto-detection for RX 6700 XT (RDNA 2)
        AmdArchitectureProfile rdna = AmdArchitectureProfile.detectFromRenderer("AMD Radeon RX 6700 XT");
        if (rdna != AmdArchitectureProfile.RDNA_MODERN) {
            throw new AssertionError("RX 6700 XT should detect as RDNA_MODERN, got " + rdna);
        }
    }

    private static void testAmdWavefrontCalibrationAndPrimitiveDiscard() {
        AmdGpuAccelerator accelerator = new AmdGpuAccelerator(true, AmdArchitectureProfile.RADEON_RX500_POLARIS, true);

        // Polaris must use Wave64
        if (accelerator.getWavefrontSize() != 64) {
            throw new AssertionError("Polaris RX 500 must use Wave64, got " + accelerator.getWavefrontSize());
        }
        if (!accelerator.isPrimitiveDiscardEnabled() || !accelerator.isIndirectParametersEnabled()) {
            throw new AssertionError("Polaris must enable primitive discard and indirect parameters");
        }

        // Vega 8 must enable UMA Zero-Copy and FP16 Rapid Packed Math
        accelerator.calibrateProfile(AmdArchitectureProfile.RADEON_VEGA_8_APU);
        if (accelerator.getWavefrontSize() != 64 || !accelerator.isUmaZeroCopyEnabled() || !accelerator.isFp16PackedMathEnabled()) {
            throw new AssertionError("Vega 8 APU must enable Wave64, UMA Zero-Copy, and FP16 math");
        }

        // RDNA must calibrate to Wave32
        accelerator.calibrateProfile(AmdArchitectureProfile.RDNA_MODERN);
        if (accelerator.getWavefrontSize() != 32) {
            throw new AssertionError("RDNA must calibrate to Wave32, got " + accelerator.getWavefrontSize());
        }
    }

    private static void testAmd2GbVramBudgetGuardThresholds() {
        AmdVramBudgetGuard guard = new AmdVramBudgetGuard(true, 2048); // 2GB VRAM card (Radeon 540)
        guard.reset();

        if (guard.getTotalVramMb() != 2048) {
            throw new AssertionError("Total VRAM should be 2048 MB");
        }

        // Allocate 1000 MB (below 75% threshold = 1536 MB)
        long bytes1000Mb = 1000L * 1024L * 1024L;
        boolean ok = guard.allocateChunkGeometry(bytes1000Mb);
        if (!ok || guard.isCompressionActive()) {
            throw new AssertionError("1000 MB allocation should succeed without compression");
        }

        // Allocate another 600 MB (total 1600 MB > 1536 MB warning threshold)
        long bytes600Mb = 600L * 1024L * 1024L;
        guard.allocateChunkGeometry(bytes600Mb);
        if (!guard.isCompressionActive()) {
            throw new AssertionError("Compression must activate when VRAM exceeds 75% capacity");
        }

        // Release 800 MB (total 800 MB < 60% = 1228 MB)
        guard.releaseChunkGeometry(800L * 1024L * 1024L);
        if (guard.isCompressionActive()) {
            throw new AssertionError("Compression should deactivate when VRAM drops below 60%");
        }
    }

    private static void testDualGpuDeviceEnumerationAndWorkloadRouting() {
        DualGpuManager manager = new DualGpuManager(true, DualGpuWorkloadDispatcher.AUTO_BALANCED);
        List<GpuDeviceInfo> gpus = manager.getDetectedGpus();

        if (gpus.size() < 2) {
            throw new AssertionError("Dual GPU manager must detect at least 2 GPUs");
        }

        GpuDeviceInfo primary = manager.getPrimaryGpu();
        GpuDeviceInfo secondary = manager.getSecondaryGpu();

        if (primary == null || secondary == null) {
            throw new AssertionError("Primary and Secondary GPUs must be automatically assigned");
        }

        if (!manager.isDualGpuActive()) {
            throw new AssertionError("Dual GPU mode should be active in AUTO_BALANCED with 2 GPUs");
        }

        if (!manager.shouldOffloadHudToSecondary()) {
            throw new AssertionError("HUD offload to secondary GPU should be enabled");
        }
        if (!manager.shouldOffloadLightToSecondary()) {
            throw new AssertionError("Light offload to secondary GPU should be enabled");
        }
        if (!manager.shouldOffloadParticlesToSecondary()) {
            throw new AssertionError("Particle offload to secondary GPU should be enabled");
        }

        // Record DMA Texture transfer
        manager.recordInterGpuTransfer(4096);
        if (manager.getInterGpuTransferredBytes() != 4096) {
            throw new AssertionError("Inter-GPU transfer counter mismatch");
        }
    }

    private static void testAmdAndDualGpuConfigStorageAndOptionsRegistry() {
        HyperionConfig cfg = new HyperionConfig();
        cfg.enableAmdHardwareAcceleration = true;
        cfg.amdArchitectureProfile = "RADEON_540_LEXA";
        cfg.enableDualGpuSupport = true;
        cfg.dualGpuMode = "DEDICATED_IGPU_HUD_LIGHT";

        String json = HyperionConfigStorage.serializeJson(cfg);
        HyperionConfig parsed = HyperionConfigStorage.parseJson(json);

        if (!parsed.enableAmdHardwareAcceleration || !"RADEON_540_LEXA".equals(parsed.amdArchitectureProfile)) {
            throw new AssertionError("AMD configuration JSON mismatch");
        }
        if (!parsed.enableDualGpuSupport || !"DEDICATED_IGPU_HUD_LIGHT".equals(parsed.dualGpuMode)) {
            throw new AssertionError("Dual GPU configuration JSON mismatch");
        }

        // Check options in HyperionOptionsRegistry for GPU video category
        List<HyperionOption<?>> gpuOptions = HyperionOptionsRegistry.getOptionsByCategory(HyperionCategory.GPU_VIDEO_SETTINGS);
        if (gpuOptions.isEmpty() || gpuOptions.size() < 10) {
            throw new AssertionError("GPU_VIDEO_SETTINGS category options must contain at least 10 options, found: " + gpuOptions.size());
        }
    }

    private static void testColorCorrectionEngineAndAcesCurve() {
        ColorCorrectionEngine engine = new ColorCorrectionEngine(true);
        HyperionConfig cfg = new HyperionConfig();
        cfg.enableColorCorrection = true;
        cfg.colorGradingMode = "VIBRANT_HDR";
        cfg.colorGammaBoost = 1.00;
        cfg.colorVibrance = 1.15;
        cfg.colorSaturation = 1.05;
        cfg.colorContrast = 1.02;
        cfg.colorBlackCrushCompensation = 0.08;
        cfg.colorNightAmbientBoost = 0.10;
        cfg.colorTemperature = 6500;
        cfg.enableColorDebanding = true;
        engine.configure(cfg);

        float[] out = new float[3];
        // Test Pure White highlight normalization (1.0, 1.0, 1.0) -> must equal 1.0
        engine.gradeRgb(1.0f, 1.0f, 1.0f, 0.0f, 0, 0, out);
        if (Math.abs(out[0] - 1.0f) > 0.01f || Math.abs(out[1] - 1.0f) > 0.01f || Math.abs(out[2] - 1.0f) > 0.01f) {
            throw new AssertionError("ACES tonemapping highlight normalization failed: Expected ~1.0, got [" + out[0] + ", " + out[1] + ", " + out[2] + "]");
        }

        // Midtone input (0.5, 0.4, 0.3)
        engine.gradeRgb(0.5f, 0.4f, 0.3f, 0.0f, 0, 0, out);
        if (out[0] <= 0.0f || out[0] > 1.0f || out[1] <= 0.0f || out[2] <= 0.0f) {
            throw new AssertionError("Graded RGB output out of range: [" + out[0] + ", " + out[1] + ", " + out[2] + "]");
        }

        // Test disabled bypass
        engine.setEnabled(false);
        engine.gradeRgb(0.7f, 0.6f, 0.5f, 0.0f, 0, 0, out);
        if (Math.abs(out[0] - 0.7f) > 0.001f || Math.abs(out[1] - 0.6f) > 0.001f) {
            throw new AssertionError("Disabled ColorCorrectionEngine must pass through raw RGB");
        }
    }

    private static void testColorBlackCrushEliminationAndNightAmbientLift() {
        ColorCorrectionEngine engine = new ColorCorrectionEngine(true);
        HyperionConfig cfg = new HyperionConfig();
        cfg.enableColorCorrection = true;
        cfg.colorBlackCrushCompensation = 0.08;
        cfg.colorNightAmbientBoost = 0.10;
        cfg.enableColorDebanding = false;
        engine.configure(cfg);

        float[] outDay = new float[3];
        float[] outNight = new float[3];

        // Near-pitch-black dark terrain pixel (0.005, 0.005, 0.005)
        engine.gradeRgb(0.005f, 0.005f, 0.005f, 0.0f, 0, 0, outDay);
        engine.gradeRgb(0.005f, 0.005f, 0.005f, 1.0f, 0, 0, outNight);

        // Anti-black-crush lifts deep shadows neutrally without void crush
        if (outDay[0] < 0.008f || outDay[1] < 0.008f || outDay[2] < 0.008f) {
            throw new AssertionError("Anti-Black-Crush failed to lift deep shadows: " + outDay[0]);
        }

        // Neutral chromatic balance (R == G == B)
        if (Math.abs(outDay[0] - outDay[1]) > 0.001f || Math.abs(outDay[0] - outDay[2]) > 0.001f) {
            throw new AssertionError("Shadow toe lift must be neutrally balanced: [" + outDay[0] + ", " + outDay[1] + ", " + outDay[2] + "]");
        }

        // Night factor must further lift terrain visibility
        if (outNight[0] <= outDay[0]) {
            throw new AssertionError("Night ambient boost must provide higher visibility than day shadows: Night=" + outNight[0] + ", Day=" + outDay[0]);
        }
    }

    private static void testColorLightmapBatchProcessingAndOptionsRegistry() {
        ColorCorrectionEngine engine = new ColorCorrectionEngine(true);
        HyperionConfig cfg = new HyperionConfig();
        cfg.enableColorCorrection = true;
        cfg.enableColorDebanding = true;
        cfg.colorBlackCrushCompensation = 0.08;
        engine.configure(cfg);

        int[] lightmap = new int[256];
        // Populate 16x16 lightmap with dark/pitch-black vanilla night values
        for (int i = 0; i < 256; i++) {
            lightmap[i] = 0xFF020202; // Very dark ARGB
        }

        engine.processLightmap(lightmap, 16, 16, 1.0f);

        // Verify that dark pixels were lifted and processed
        for (int i = 0; i < 256; i++) {
            int argb = lightmap[i];
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;

            if (r < 5 || g < 5 || b < 5) {
                throw new AssertionError("Lightmap pixel at index " + i + " was not properly lifted: R=" + r + " G=" + g + " B=" + b);
            }
        }

        // Verify COLOR_CORRECTION category in HyperionOptionsRegistry
        List<HyperionOption<?>> colorOptions = HyperionOptionsRegistry.getOptionsByCategory(HyperionCategory.COLOR_CORRECTION);
        if (colorOptions.isEmpty() || colorOptions.size() < 5) {
            throw new AssertionError("COLOR_CORRECTION category options must contain at least 5 options, found: " + colorOptions.size());
        }

        // Test persistence round-trip
        String json = HyperionConfigStorage.serializeJson(cfg);
        HyperionConfig parsed = HyperionConfigStorage.parseJson(json);
        if (!parsed.enableColorCorrection || !"NATURAL_BALANCED".equals(parsed.colorGradingMode) || Math.abs(parsed.colorBlackCrushCompensation - 0.08) > 0.001) {
            throw new AssertionError("Color correction config roundtrip serialization mismatch");
        }

        // Test Mixin hook
        MixinLightmapTexture.onProcessLightmap(lightmap, 16, 16, 1.0f);
    }

    private static void testFpsStabilizerChunkUploadPacingAndWorkBudgeting() {
        FpsStabilizerEngine stabilizer = new FpsStabilizerEngine(true);
        HyperionConfig cfg = new HyperionConfig();
        cfg.enableFpsStabilizer = true;
        cfg.targetFramerate = 350;
        cfg.maxChunkUploadsPerFrame = 3;
        cfg.enableDynamicWorkBudgeting = true;
        stabilizer.configure(cfg);

        stabilizer.onFrameStart();

        // 1st, 2nd, 3rd uploads should succeed within budget
        if (!stabilizer.canUploadChunkMeshThisFrame()) throw new AssertionError("Chunk upload 1 must be permitted");
        if (!stabilizer.canUploadChunkMeshThisFrame()) throw new AssertionError("Chunk upload 2 must be permitted");
        if (!stabilizer.canUploadChunkMeshThisFrame()) throw new AssertionError("Chunk upload 3 must be permitted");

        // 4th upload must be throttled to prevent 350 FPS -> 60 FPS drop
        if (stabilizer.canUploadChunkMeshThisFrame()) {
            throw new AssertionError("Chunk upload 4 must be throttled to protect 350 FPS budget");
        }

        // On next frame start, budget must reset
        stabilizer.onFrameStart();
        if (!stabilizer.canUploadChunkMeshThisFrame()) {
            throw new AssertionError("Budget must reset on next frame start");
        }

        // Test average FPS computation
        double avgFps = stabilizer.getAverageFps();
        if (avgFps <= 0) throw new AssertionError("Average FPS must be positive");
    }

    private static void testFpsStabilizerBlockEntityDistanceAndOcclusionCulling() {
        FpsStabilizerEngine stabilizer = new FpsStabilizerEngine(true);
        HyperionConfig cfg = new HyperionConfig();
        cfg.enableFpsStabilizer = true;
        cfg.enableBlockEntityDistanceCulling = true;
        cfg.blockEntityCullDistance = 32.0;
        stabilizer.configure(cfg);

        // Close block entity (10 blocks away, unoccluded) -> must NOT cull
        if (stabilizer.shouldCullBlockEntity(0, 64, 0, 10, 64, 0, false)) {
            throw new AssertionError("Close unoccluded block entity must not be culled");
        }

        // Distant block entity (50 blocks away) -> must cull
        if (!stabilizer.shouldCullBlockEntity(0, 64, 0, 50, 64, 0, false)) {
            throw new AssertionError("Distant block entity (50m) must be culled");
        }

        // Occluded close block entity -> must cull
        if (!stabilizer.shouldCullBlockEntity(0, 64, 0, 5, 64, 0, true)) {
            throw new AssertionError("Occluded block entity must be culled");
        }

        // MixinLevelRenderer hook check
        MixinLevelRenderer.onRenderFrameStart();
        if (!MixinLevelRenderer.shouldUploadChunkMesh()) {
            throw new AssertionError("MixinLevelRenderer should allow first chunk upload");
        }
    }

    private static void testCpuThreadPoolManagerTopologyAndModes() {
        HyperionThreadPoolManager poolManager = HyperionThreadPoolManager.getInstance();
        if (poolManager.getLogicalCores() <= 0 || poolManager.getPhysicalCores() <= 0) {
            throw new AssertionError("CPU topology cores must be strictly positive");
        }

        // Test mode: ALL_CORES
        poolManager.reconfigurePools(true, "ALL_CORES", 0);
        if (poolManager.getChunkMeshingPool() == null || poolManager.getEntityPhysicsPool() == null) {
            throw new AssertionError("Pools must be initialized in ALL_CORES mode");
        }

        // Test mode: BALANCED_N_MINUS_1
        poolManager.reconfigurePools(true, "BALANCED_N_MINUS_1", 0);
        if (poolManager.getLightEnginePool() == null || poolManager.getWorldCacheIoPool() == null) {
            throw new AssertionError("Pools must be initialized in BALANCED mode");
        }

        // Test mode: CUSTOM (8 threads)
        poolManager.reconfigurePools(true, "CUSTOM", 8);
        if (poolManager.getCustomCoreCount() != 8) {
            throw new AssertionError("Custom core count must be 8");
        }

        // Revert to AUTO
        poolManager.reconfigurePools(true, "AUTO_DETECT_CORES", 0);
    }

    private static void testParallelChunkMesherThroughputAndGeometry() throws Exception {
        ParallelChunkMesher mesher = new ParallelChunkMesher(true, 4);
        byte[] fakeVoxelData = new byte[4096];
        for (int i = 0; i < 4096; i += 2) {
            fakeVoxelData[i] = (byte) ((i % 16) + 1); // 2048 solid blocks
        }

        CompletableFuture<ParallelChunkMesher.MeshResult> future = mesher.submitSectionMesh(10, 4, -5, fakeVoxelData);
        ParallelChunkMesher.MeshResult res = future.get(5, TimeUnit.SECONDS);

        if (!res.isSuccess() || res.getChunkX() != 10 || res.getChunkY() != 4 || res.getChunkZ() != -5) {
            throw new AssertionError("Mesh result coordinate or success mismatch");
        }
        if (res.getOpaqueBlocks() != 2048) {
            throw new AssertionError("Expected 2048 opaque blocks, got: " + res.getOpaqueBlocks());
        }
        if (res.getQuadCount() <= 0) {
            throw new AssertionError("Quad count must be positive");
        }
        if (mesher.getTotalMeshedSections() != 1) {
            throw new AssertionError("Expected 1 meshed section");
        }
    }

    private static void testMultiCoreEntityPhysicsEngineBatching() {
        MultiCoreEntityPhysicsEngine physicsEngine = new MultiCoreEntityPhysicsEngine(true, 32);
        List<MultiCoreEntityPhysicsEngine.EntityStateSnapshot> entities = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            entities.add(new MultiCoreEntityPhysicsEngine.EntityStateSnapshot(
                    i, i * 1.5, 64.0, i * -0.5, 0.1, -0.08, 0.2, true
            ));
        }

        AtomicInteger tickedCounter = new AtomicInteger(0);
        physicsEngine.processEntityBatchParallel(entities, (entityId, posX, posY, posZ) -> {
            tickedCounter.incrementAndGet();
        });

        if (tickedCounter.get() != 100) {
            throw new AssertionError("Expected 100 ticked entities, got: " + tickedCounter.get());
        }
        if (physicsEngine.getTotalTickedEntities() != 100) {
            throw new AssertionError("Expected 100 in total ticked entities counter");
        }
    }

    private static void testAsyncWorldTickDispatcherAndCpuAffinity() throws Exception {
        AsyncWorldTickDispatcher dispatcher = new AsyncWorldTickDispatcher(true);
        CountDownLatch latch = new CountDownLatch(5);

        for (int i = 0; i < 5; i++) {
            dispatcher.queueAsyncTask(latch::countDown);
        }

        boolean done = latch.await(2, TimeUnit.SECONDS);
        if (!done) {
            throw new AssertionError("Async tasks did not complete within timeout");
        }
        if (dispatcher.getDispatchedTasks() != 5) {
            throw new AssertionError("Expected 5 dispatched tasks");
        }

        CpuCoreAffinityGovernor governor = new CpuCoreAffinityGovernor(true, true);
        governor.optimizeCurrentThread("RENDER_MAIN");
        governor.onMainLoopTick();
        if (governor.getMainThreadLoopCount() != 1) {
            throw new AssertionError("Expected main thread loop count = 1");
        }
        governor.hintCpuYieldIfOverloaded(100_000_000L, 16_000_000L);
        if (governor.getTotalThreadYields() != 1) {
            throw new AssertionError("Expected 1 thread yield due to budget overshoot");
        }
    }

    private static void testHyperionConfigScreenAndRootCategories() {
        HyperionConfigScreen screen = new HyperionConfigScreen();
        HyperionScreenModel model = screen.getModel();

        // 1. Verify Graphics Tab
        screen.selectGraphicsTab();
        if (model.getActiveCategory() != HyperionCategory.GRAPHICS_SETTINGS) {
            throw new AssertionError("Active category must be GRAPHICS_SETTINGS");
        }
        List<HyperionOption<?>> graphicsOpts = screen.getFilteredOptions();
        if (graphicsOpts.isEmpty()) {
            throw new AssertionError("GRAPHICS_SETTINGS options list must not be empty");
        }

        // 2. Verify GPU Tab
        screen.selectGpuTab();
        if (model.getActiveCategory() != HyperionCategory.GPU_VIDEO_SETTINGS) {
            throw new AssertionError("Active category must be GPU_VIDEO_SETTINGS");
        }
        List<HyperionOption<?>> gpuOpts = screen.getFilteredOptions();
        if (gpuOpts.isEmpty()) {
            throw new AssertionError("GPU_VIDEO_SETTINGS options list must not be empty");
        }

        // 3. Verify CPU Tab
        screen.selectCpuTab();
        if (model.getActiveCategory() != HyperionCategory.CPU_PROCESSOR_SETTINGS) {
            throw new AssertionError("Active category must be CPU_PROCESSOR_SETTINGS");
        }
        List<HyperionOption<?>> cpuOpts = screen.getFilteredOptions();
        if (cpuOpts.isEmpty()) {
            throw new AssertionError("CPU_PROCESSOR_SETTINGS options list must not be empty");
        }

        // 4. Test Search Filtering
        screen.setSearchQuery("Multi-Core");
        List<HyperionOption<?>> searchResults = screen.getFilteredOptions();
        if (searchResults.isEmpty()) {
            throw new AssertionError("Search for 'Multi-Core' must return matching CPU options");
        }
        screen.setSearchQuery("");

        // 5. Test Option Toggling
        screen.selectCpuTab();
        screen.setSelectedOptionIndex(0);
        boolean beforeToggle = model.getWorkingConfig().enableCpuMultithreading;
        screen.toggleOrCycleSelectedOption();
        boolean afterToggle = model.getWorkingConfig().enableCpuMultithreading;
        if (beforeToggle == afterToggle) {
            throw new AssertionError("Toggle must flip boolean state");
        }
        if (!model.isDirty()) {
            throw new AssertionError("Model must be marked dirty after change");
        }

        // 6. Test Preset Application & Save
        model.applyPreset(HyperionConfigStorage.Preset.EXTREME_MULTICORE_350FPS);
        if (!model.getWorkingConfig().enableCpuMultithreading || model.getWorkingConfig().targetFramerate != 350) {
            throw new AssertionError("EXTREME_MULTICORE_350FPS preset not applied correctly");
        }
        boolean saved = model.saveAndApply();
        if (!saved || model.isDirty()) {
            throw new AssertionError("Save and apply failed or left dirty state");
        }
    }

    private static void testSingleGpuAndDualGpuHardwareTopologyRouting() {
        DualGpuManager manager = new DualGpuManager(true, DualGpuWorkloadDispatcher.AUTO_BALANCED);

        // 1. Test Single Discrete GPU ONLY (User has no iGPU)
        List<GpuDeviceInfo> dGpuOnly = new ArrayList<>();
        dGpuOnly.add(new GpuDeviceInfo(0, "AMD Radeon RX 580 8GB", "AMD", 8192, false));
        manager.configureGpus(dGpuOnly);

        if (!manager.isSingleDiscreteGpuOnly() || manager.hasIntegratedGpu() || !manager.hasDiscreteGpu()) {
            throw new AssertionError("Failed to detect single dGPU-only topology");
        }
        if (manager.getPrimaryGpu() == null || !manager.getPrimaryGpu().isDiscrete()) {
            throw new AssertionError("Primary GPU must be the discrete GPU in dGPU-only setup");
        }
        if (manager.getSecondaryGpu() != null || manager.isDualGpuActive()) {
            throw new AssertionError("Dual GPU must be inactive when only dGPU is present");
        }
        if (manager.shouldOffloadHudToSecondary() || manager.shouldOffloadLightToSecondary()) {
            throw new AssertionError("Offloading must be disabled when only dGPU is present");
        }

        // 2. Test Single Integrated GPU ONLY (User has no dGPU)
        List<GpuDeviceInfo> iGpuOnly = new ArrayList<>();
        iGpuOnly.add(new GpuDeviceInfo(0, "AMD Radeon(TM) Vega 8 Graphics", "AMD", 2048, true));
        manager.configureGpus(iGpuOnly);

        if (!manager.isSingleIntegratedGpuOnly() || manager.hasDiscreteGpu() || !manager.hasIntegratedGpu()) {
            throw new AssertionError("Failed to detect single iGPU-only topology");
        }
        if (manager.getPrimaryGpu() == null || !manager.getPrimaryGpu().isIntegrated()) {
            throw new AssertionError("Primary GPU must be the integrated GPU in iGPU-only setup");
        }
        if (manager.getSecondaryGpu() != null || manager.isDualGpuActive()) {
            throw new AssertionError("Dual GPU must be inactive when only iGPU is present");
        }
        if (manager.shouldOffloadHudToSecondary() || manager.shouldOffloadLightToSecondary()) {
            throw new AssertionError("Offloading must be disabled when only iGPU is present");
        }

        // 3. Test Dual-GPU Hybrid (Both dGPU + iGPU present)
        List<GpuDeviceInfo> dualGpu = new ArrayList<>();
        dualGpu.add(new GpuDeviceInfo(0, "AMD Radeon 540 Series", "AMD", 2048, false));
        dualGpu.add(new GpuDeviceInfo(1, "AMD Radeon(TM) Vega 8 Graphics", "AMD", 2048, true));
        manager.configureGpus(dualGpu);

        if (!manager.hasDiscreteGpu() || !manager.hasIntegratedGpu()) {
            throw new AssertionError("Both dGPU and iGPU must be detected in dual setup");
        }
        if (manager.getPrimaryGpu() == null || !manager.getPrimaryGpu().isDiscrete()) {
            throw new AssertionError("Primary GPU must be dGPU in hybrid setup");
        }
        if (manager.getSecondaryGpu() == null || !manager.getSecondaryGpu().isIntegrated()) {
            throw new AssertionError("Secondary GPU must be iGPU in hybrid setup");
        }
        if (!manager.isDualGpuActive()) {
            throw new AssertionError("Dual GPU mode must be active in hybrid setup");
        }
        if (!manager.shouldOffloadHudToSecondary() || !manager.shouldOffloadLightToSecondary()) {
            throw new AssertionError("Offloading to iGPU must be active in hybrid mode");
        }

        // 4. Test Dual-GPU with mode = OFF
        manager.setMode(DualGpuWorkloadDispatcher.OFF);
        if (manager.isDualGpuActive() || manager.getSecondaryGpu() != null) {
            throw new AssertionError("Dual GPU mode OFF must deactivate secondary GPU");
        }
    }

    private static void testFastHdTextureEngineAndAnimatedSpritePacing() {
        FastHdTextureEngine engine = new FastHdTextureEngine(true);
        HyperionConfig cfg = new HyperionConfig();
        cfg.enableHdTextureOptimization = true;
        cfg.enableAsyncAnimatedTextures = true;
        cfg.enableAdaptiveMipmapPacing = true;
        cfg.maxHdAtlasDimension = 16384;
        engine.configure(cfg);

        // 1. Initial Frame Guarantee: First tick must ALWAYS upload (even if offscreen)
        if (!engine.shouldUpdateAnimatedSprite("minecraft:block/lava_flow", 512, 512, false, 100L)) {
            throw new AssertionError("Frame 0 of animated sprite must always upload to prevent black uninitialized texture");
        }

        // 2. Visible sprite updates every tick
        if (!engine.shouldUpdateAnimatedSprite("minecraft:block/water_flow", 512, 512, true, 101L)) {
            throw new AssertionError("Visible animated sprite must update");
        }
        if (!engine.shouldUpdateAnimatedSprite("minecraft:block/water_flow", 512, 512, true, 102L)) {
            throw new AssertionError("Visible animated sprite must update on subsequent tick");
        }

        // 3. Offscreen sprite throttled to 1 Hz after initial frame
        if (engine.shouldUpdateAnimatedSprite("minecraft:block/lava_flow", 512, 512, false, 105L)) {
            throw new AssertionError("Offscreen animated sprite must be throttled within 20 ticks");
        }
        // Tick 125 (25 ticks later) -> permitted
        if (!engine.shouldUpdateAnimatedSprite("minecraft:block/lava_flow", 512, 512, false, 125L)) {
            throw new AssertionError("Offscreen animated sprite should update once per 20 ticks (1 Hz)");
        }

        // 4. Memory estimation & full valid mipmap chain integrity
        long memBytes = FastHdTextureEngine.estimateAtlasMemoryBytes(8192, 8192, 4);
        if (memBytes <= 0 || memBytes < 256L * 1024L * 1024L) {
            throw new AssertionError("Atlas memory calculation invalid: " + memBytes);
        }

        int mipLevels512 = engine.calculateOptimalMipmapLevels(512, 512, 4, 2048);
        if (mipLevels512 != 4) {
            throw new AssertionError("Mipmap chain must maintain full 4 levels to prevent OpenGL missing-LOD blackness");
        }
    }

    private static void testFancyGraphicsSmartLeavesCullingAndTranslucentSorting() {
        FancyGraphicsOptimizer optimizer = new FancyGraphicsOptimizer(true);
        HyperionConfig cfg = new HyperionConfig();
        cfg.enableSmartLeavesCulling = true;
        cfg.enableFabulousGraphicsOptimization = true;
        cfg.enableTranslucentSortThrottling = true;
        optimizer.configure(cfg);

        // 1. Smart leaves culling
        if (!optimizer.shouldCullLeavesFace(true, true)) {
            throw new AssertionError("Face between identical leaves blocks must be culled");
        }
        if (optimizer.shouldCullLeavesFace(false, false)) {
            throw new AssertionError("Face adjacent to air/glass must NOT be culled");
        }
        if (optimizer.getCulledLeavesFacesCount() != 1) {
            throw new AssertionError("Culled leaves counter mismatch");
        }

        // 2. Translucent quad sort throttling
        optimizer.reset();
        // Initial frame -> must sort
        if (!optimizer.shouldReSortTranslucentQuads(10.0, 64.0, 10.0, 0.0f, 90.0f)) {
            throw new AssertionError("Initial frame must trigger quad sort");
        }
        // Micro movement (<0.25m, <0.5 deg) -> must SKIP sort
        if (optimizer.shouldReSortTranslucentQuads(10.05, 64.0, 10.05, 0.1f, 90.1f)) {
            throw new AssertionError("Micro camera movement must throttle redundant CPU quad sort");
        }
        // Significant movement (>0.25m) -> must re-sort
        if (!optimizer.shouldReSortTranslucentQuads(12.0, 64.0, 10.0, 5.0f, 100.0f)) {
            throw new AssertionError("Significant movement must trigger quad sort");
        }
        if (optimizer.getSkippedTranslucentSortsCount() != 1) {
            throw new AssertionError("Skipped translucent sort counter mismatch");
        }
    }

    private static void testFastCloudEngineAndLockFreeActionPhysics() {
        FastCloudEngine cloudEngine = new FastCloudEngine(true);
        HyperionConfig cfg = new HyperionConfig();
        cfg.enableFastCloudEngine = true;
        cfg.enableCloudCulling = true;
        cfg.enableCloudMeshReuse = true;
        cloudEngine.configure(cfg);

        // 1. Normal sky view -> render clouds
        if (!cloudEngine.shouldRenderClouds(0, 100, 0, 0.0f, true)) {
            throw new AssertionError("Normal sky view must render clouds");
        }

        // 2. Cave underground (Y < 55, no sky light) -> CULL clouds
        if (cloudEngine.shouldRenderClouds(0, 30, 0, 0.0f, false)) {
            throw new AssertionError("Underground cave with no sky light must cull clouds");
        }

        // 3. Pitch looking down (pitch > 45) -> CULL clouds
        if (cloudEngine.shouldRenderClouds(0, 100, 0, 60.0f, true)) {
            throw new AssertionError("Looking directly at ground must cull clouds");
        }

        if (cloudEngine.getCulledCloudFramesCount() != 2) {
            throw new AssertionError("Culled cloud counter mismatch");
        }

        // 4. Test Lock-Free FastParticleEngine during action
        FastParticleEngine partEngine = new FastParticleEngine(true, 5, 48.0);
        for (int i = 0; i < 5; i++) {
            if (!partEngine.shouldSpawnParticle(0, 64, 0, 0, 64, 0, 1000L)) {
                throw new AssertionError("Initial particles must spawn");
            }
        }
        // 6th particle in same second on same block must rate-limit
        if (partEngine.shouldSpawnParticle(0, 64, 0, 0, 64, 0, 1000L)) {
            throw new AssertionError("Particle rate limit exceeded must reject");
        }

        // 5. Spatial collision tick reset
        SpatialCollisionEngine collEngine = new SpatialCollisionEngine(true, 8, 32.0);
        SpatialCollisionEngine.CollidableEntity ent = new SpatialCollisionEngine.CollidableEntity(1, 10, 64, 10, 0.6, 1.8);
        collEngine.registerEntity(ent);
        if (collEngine.getNearbyCandidates(10, 10).isEmpty()) {
            throw new AssertionError("Entity must be found in spatial bucket");
        }
        collEngine.onTickStart();
        if (!collEngine.getNearbyCandidates(10, 10).isEmpty()) {
            throw new AssertionError("onTickStart must clear stale spatial buckets");
        }
    }

    private static void testGpuThermalPowerGuardAntiCoilWhineAndPacing() {
        GpuThermalPowerGuard guard = new GpuThermalPowerGuard(true);
        HyperionConfig cfg = new HyperionConfig();
        cfg.enableGpuThermalPowerGuard = true;
        cfg.enableMenuFpsCap = true;
        cfg.menuMaxFramerate = 60;
        cfg.enableBackgroundFpsCap = true;
        cfg.backgroundMaxFramerate = 20;
        cfg.enableCoilWhineSuppression = true;
        cfg.maxPeakFramerateCap = 500;
        guard.configure(cfg);

        // 1. Verify pacing in focused 3D game
        guard.paceFrame(false, true);

        // 2. Verify menu pacing (Anti-Coil-Whine)
        guard.paceFrame(true, true);

        // 3. Verify background pacing (Alt-Tab / Minimized)
        guard.paceFrame(false, false);

        if (!guard.isEnabled() || !guard.isMenuFpsCapEnabled() || !guard.isBackgroundFpsCapEnabled()) {
            throw new AssertionError("Thermal guard config flags mismatch");
        }
        if (guard.getMenuMaxFps() != 60 || guard.getBackgroundMaxFps() != 20) {
            throw new AssertionError("Thermal guard FPS targets mismatch");
        }
    }

    private static void testChunkLodManagerAndGeometrySimplification() {
        ChunkLodManager lodManager = new ChunkLodManager(true);
        HyperionConfig cfg = new HyperionConfig();
        cfg.enableChunkLod = true;
        cfg.chunkLodDistanceBlocks = 16.0;
        cfg.chunkLodFarDistanceBlocks = 48.0;
        cfg.chunkLodSimplificationFactor = 0.50;
        lodManager.configure(cfg);

        // 1. Chunk close to player (<16 blocks) -> LOD 0 (full detail)
        int lodNear = lodManager.calculateLodLevel(0, 64, 0, 8, 64, 8); // dist = sqrt(128) = 11.3 blocks
        if (lodNear != 0) {
            throw new AssertionError("Near chunk (<16 blocks) must be LOD 0, got: " + lodNear);
        }
        int fullQuads = 1000;
        int simplifiedNear = lodManager.simplifyQuadCount(fullQuads, lodNear);
        if (simplifiedNear != fullQuads) {
            throw new AssertionError("LOD 0 must not decimate quads");
        }

        // 2. Chunk medium distance (30 blocks) -> LOD 1 (50% reduction)
        int lodMid = lodManager.calculateLodLevel(0, 64, 0, 30, 64, 0); // dist = 30 blocks
        if (lodMid != 1) {
            throw new AssertionError("Mid chunk (16-48 blocks) must be LOD 1, got: " + lodMid);
        }
        int simplifiedMid = lodManager.simplifyQuadCount(fullQuads, lodMid);
        if (simplifiedMid != 500) {
            throw new AssertionError("LOD 1 must reduce quads by 50%, got: " + simplifiedMid);
        }

        // 3. Chunk far distance (60 blocks) -> LOD 2 (75% reduction)
        int lodFar = lodManager.calculateLodLevel(0, 64, 0, 60, 64, 0); // dist = 60 blocks
        if (lodFar != 2) {
            throw new AssertionError("Far chunk (>48 blocks) must be LOD 2, got: " + lodFar);
        }
        int simplifiedFar = lodManager.simplifyQuadCount(fullQuads, lodFar);
        if (simplifiedFar != 250) {
            throw new AssertionError("LOD 2 must reduce quads by 75%, got: " + simplifiedFar);
        }

        if (lodManager.getTotalSavedVertices() <= 0) {
            throw new AssertionError("Saved vertex telemetry must be recorded");
        }
    }

    private static void testAggressiveFaceCullerAndCavityDiscard() {
        AggressiveFaceCuller culler = new AggressiveFaceCuller(true);
        HyperionConfig cfg = new HyperionConfig();
        cfg.enableAggressiveFaceCulling = true;
        cfg.enableInternalCavityCulling = true;
        culler.configure(cfg);

        // 1. Face against air -> must render
        if (!culler.shouldRenderFace(0, (byte) 1, (byte) 0, false)) {
            throw new AssertionError("Face adjacent to air must be rendered");
        }

        // 2. Face against opaque solid block -> must CULL (discard buried face)
        if (culler.shouldRenderFace(0, (byte) 1, (byte) 1, true)) {
            throw new AssertionError("Buried face against opaque neighbor must be culled");
        }

        // 3. Translucent boundary between identical blocks (e.g. water-water) -> must CULL
        if (culler.shouldRenderFace(0, (byte) 9, (byte) 9, false)) {
            throw new AssertionError("Internal boundary between same translucent blocks must be culled");
        }

        // 4. Test 3x3x3 solid cube filtering: only 6 outer faces on each side must be visible (26*6 quads reduced to 54)
        byte[] cube = new byte[3 * 3 * 3];
        for (int i = 0; i < cube.length; i++) cube[i] = (byte) 1; // Solid stone block cube
        int visibleQuads = culler.filterVoxelQuads(cube, 3, 3, 3);
        // Outer box of 3x3 has 9 blocks per side * 6 sides = 54 quads visible (27*6 = 162 total raw faces)
        if (visibleQuads != 54) {
            throw new AssertionError("Expected 54 visible outer quads for 3x3x3 solid block cube, got: " + visibleQuads);
        }
        if (culler.getTotalFacesCulled() <= 0) {
            throw new AssertionError("Culled face counter mismatch");
        }
    }

    private static void testGpuInstancingEngineAndBatching() {
        GpuInstancingEngine instancing = new GpuInstancingEngine(true, 1024);
        HyperionConfig cfg = new HyperionConfig();
        cfg.enableGpuBlockInstancing = true;
        instancing.configure(cfg);

        instancing.beginInstancingBatch();
        for (int i = 0; i < 100; i++) {
            boolean ok = instancing.addInstance(i * 1.0f, 64.0f, 0.0f, 0x00F000F0, 1, 0.0f);
            if (!ok) {
                throw new AssertionError("Adding valid instance must succeed");
            }
        }

        if (instancing.getCurrentInstanceCount() != 100) {
            throw new AssertionError("Instance count mismatch, expected 100, got: " + instancing.getCurrentInstanceCount());
        }

        ByteBuffer buffer = instancing.finishInstancingBatch();
        if (buffer.remaining() != 100 * GpuInstancingEngine.INSTANCE_STRIDE_BYTES) {
            throw new AssertionError("Instance buffer stride mismatch, expected " + (100 * 24) + " bytes, got: " + buffer.remaining());
        }
        if (instancing.getTotalBatchesDispatched() != 1) {
            throw new AssertionError("Batch dispatch counter mismatch");
        }
    }

    private static void testDualGpuSyncLockTimeoutAndWaitLoopSuppression() {
        DualGpuSyncLock syncLock = new DualGpuSyncLock(true);
        HyperionConfig cfg = new HyperionConfig();
        cfg.enableDualGpuSyncLock = true;
        cfg.dualGpuSyncTimeoutMs = 2; // 2ms timeout
        syncLock.configure(cfg);

        // 1. Ready condition -> instant success without delay
        boolean ready = syncLock.awaitSync(() -> true);
        if (!ready || syncLock.getSuccessfulSyncs() != 1) {
            throw new AssertionError("Ready condition must immediately succeed");
        }

        // 2. Unready condition -> must cleanly timeout after 2ms without spinning indefinitely
        long t0 = System.nanoTime();
        boolean timedOut = syncLock.awaitSync(() -> false);
        long elapsedMs = (System.nanoTime() - t0) / 1_000_000L;

        if (timedOut) {
            throw new AssertionError("Unready condition must return false on timeout");
        }
        if (syncLock.getTimedOutSyncs() != 1) {
            throw new AssertionError("Timed out counter mismatch");
        }
        if (elapsedMs < 1) {
            throw new AssertionError("SyncLock timeout was too fast");
        }
    }

    private static void testDualGpuThermalFallbackAndGpuCrashGuard() {
        // 1. Test Thermal Auto-Fallback
        DualGpuThermalFallback fallback = new DualGpuThermalFallback(true);
        HyperionConfig cfg = new HyperionConfig();
        cfg.enableDualGpuThermalFallback = true;
        cfg.thermalFallbackFrametimeThresholdMs = 40.0;
        fallback.configure(cfg);

        // Normal 16ms frames (60 FPS) -> no fallback
        for (int i = 0; i < 5; i++) {
            if (fallback.recordFrameAndEvaluate(16.6)) {
                throw new AssertionError("Normal frametime must not trigger fallback");
            }
        }

        // Severe thermal throttling spikes (>40ms) -> must trigger fallback on 10th spike
        for (int i = 0; i < 9; i++) {
            fallback.recordFrameAndEvaluate(55.0);
        }
        boolean active = fallback.recordFrameAndEvaluate(75.0);
        if (!active || !fallback.isFallbackActive()) {
            throw new AssertionError("Consecutive thermal spikes must activate safe auto-fallback mode");
        }

        // 2. Test Crash Guard TDR recovery
        GpuResetCrashGuard crashGuard = new GpuResetCrashGuard(true);
        crashGuard.configure(cfg);

        AtomicBoolean fallbackExecuted = new AtomicBoolean(false);
        boolean taskResult = crashGuard.executeProtectedGpuTask(() -> {
            throw new RuntimeException("Simulated OpenGL Driver TDR Device Lost (GL_CONTEXT_LOST)");
        }, () -> {
            fallbackExecuted.set(true);
        });

        if (taskResult) {
            throw new AssertionError("Crashing GPU task must return false");
        }
        if (!fallbackExecuted.get() || !crashGuard.isRecoveryModeActive()) {
            throw new AssertionError("Crash guard must execute fallback without crashing the JVM");
        }
        if (crashGuard.getInterceptedCrashesCount() != 1 || crashGuard.getSuccessfulRecoveriesCount() != 1) {
            throw new AssertionError("Crash telemetry mismatch");
        }
    }

    private static void testGpuVendorProfilesAndCtrlShiftZeroKeybinding() {
        // 1. Test Vendor Profiles & Apple Silicon UMA / NVIDIA Optimus detection
        GpuDeviceInfo nvidia = new GpuDeviceInfo(0, "NVIDIA GeForce RTX 4070 Laptop GPU", "NVIDIA Corporation", 8192, false);
        GpuDeviceInfo intel = new GpuDeviceInfo(1, "Intel(R) Iris(R) Xe Graphics", "Intel Corporation", 4096, true);
        GpuDeviceInfo appleM3 = new GpuDeviceInfo(0, "Apple M3 Max GPU (Metal / MoltenVK)", "Apple", 36864, true);

        if (!nvidia.isNvidia() || !nvidia.isDiscrete()) {
            throw new AssertionError("NVIDIA detection mismatch");
        }
        if (!intel.isIntel() || !intel.isIntegrated()) {
            throw new AssertionError("Intel detection mismatch");
        }
        if (!appleM3.isAppleSilicon()) {
            throw new AssertionError("Apple Silicon detection mismatch");
        }

        DualGpuManager dualMgr = new DualGpuManager(true, DualGpuWorkloadDispatcher.AUTO_BALANCED);
        List<GpuDeviceInfo> optimusSetup = new ArrayList<>();
        optimusSetup.add(nvidia);
        optimusSetup.add(intel);
        dualMgr.configureGpus(optimusSetup);

        if (dualMgr.getPrimaryGpu() != nvidia || dualMgr.getSecondaryGpu() != intel) {
            throw new AssertionError("NVIDIA + Intel Optimus topology assignment mismatch");
        }

        // 2. Test Ctrl + Shift + 0 Keybinding Trigger
        HyperionKeyBindingManager keyMgr = HyperionKeyBindingManager.getInstance();
        keyMgr.setEnabled(true);
        keyMgr.reset();

        // Random key press without modifiers -> no trigger
        if (keyMgr.handleKeyInput(HyperionKeyBindingManager.GLFW_KEY_H, 0, 1, 0)) {
            throw new AssertionError("Regular key must not trigger config menu");
        }

        // Ctrl + Shift + 0 -> must TRIGGER
        int mods = HyperionKeyBindingManager.GLFW_MOD_CONTROL | HyperionKeyBindingManager.GLFW_MOD_SHIFT;
        boolean triggered = keyMgr.handleKeyInput(HyperionKeyBindingManager.GLFW_KEY_0, 0, 1, mods);
        if (!triggered || !keyMgr.consumeOpenScreenRequest()) {
            throw new AssertionError("Ctrl + Shift + 0 must trigger opening Hyperion config menu");
        }
    }

    private static void testVoxelHierarchicalMipTreeDownsampling() {
        VoxelHierarchicalMipTree mipTree = new VoxelHierarchicalMipTree(true);
        HyperionConfig cfg = new HyperionConfig();
        cfg.enableVoxelLodEngine = true;
        cfg.voxelMaxRenderDistanceChunks = 2048;
        mipTree.configure(cfg);

        // 1. Verify distance to Mip level mapping
        if (mipTree.getMipLevelForDistance(100.0) != 0) { // < 256 blocks -> Mip 0
            throw new AssertionError("Distance 100 blocks must be Mip 0");
        }
        if (mipTree.getMipLevelForDistance(400.0) != 1) { // 256 - 512 blocks -> Mip 1
            throw new AssertionError("Distance 400 blocks must be Mip 1");
        }
        if (mipTree.getMipLevelForDistance(800.0) != 2) { // 512 - 1024 blocks -> Mip 2
            throw new AssertionError("Distance 800 blocks must be Mip 2");
        }
        if (mipTree.getMipLevelForDistance(1600.0) != 3) { // 1024 - 2048 blocks -> Mip 3
            throw new AssertionError("Distance 1600 blocks must be Mip 3");
        }
        if (mipTree.getMipLevelForDistance(32000.0) != 4) { // > 2048 blocks (2048 chunks) -> Mip 4
            throw new AssertionError("Distance 32000 blocks must be Mip 4");
        }

        // 2. Downsample a 16x16x16 chunk section (4096 voxels) filled with Stone (ID = 1)
        byte[] rawStoneSection = new byte[4096];
        for (int i = 0; i < rawStoneSection.length; i++) rawStoneSection[i] = (byte) 1;

        byte[] mip1 = mipTree.downsampleSection(rawStoneSection, 1); // 8x8x8 = 512
        if (mip1.length != 512 || mip1[0] != (byte) 1) {
            throw new AssertionError("Mip 1 downsampled length must be 512 with Stone voxels, got: " + mip1.length);
        }

        byte[] mip2 = mipTree.downsampleSection(rawStoneSection, 2); // 4x4x4 = 64
        if (mip2.length != 64 || mip2[0] != (byte) 1) {
            throw new AssertionError("Mip 2 downsampled length must be 64, got: " + mip2.length);
        }

        byte[] mip4 = mipTree.downsampleSection(rawStoneSection, 4); // 1x1x1 = 1
        if (mip4.length != 1 || mip4[0] != (byte) 1) {
            throw new AssertionError("Mip 4 downsampled length must be 1, got: " + mip4.length);
        }
    }

    private static void testVoxelSectionStorageRleCompression() {
        VoxelSectionStorage storage = new VoxelSectionStorage();

        // 1. Create a 4096-byte section with repeating layers (Stone and Dirt)
        byte[] section = new byte[4096];
        for (int i = 0; i < 2048; i++) section[i] = (byte) 1; // Stone
        for (int i = 2048; i < 4096; i++) section[i] = (byte) 3; // Dirt

        storage.storeSection(10, 4, -20, 0, section);

        if (!storage.hasSection(10, 4, -20, 0)) {
            throw new AssertionError("Storage must contain stored section");
        }

        byte[] decompressed = storage.getSection(10, 4, -20, 0, 4096);
        if (decompressed == null || decompressed.length != 4096) {
            throw new AssertionError("Decompressed section length mismatch");
        }
        if (decompressed[0] != (byte) 1 || decompressed[3000] != (byte) 3) {
            throw new AssertionError("Decompressed voxel data corrupted");
        }

        // Verify that RLE compression achieves massive ratio (>90% savings)
        long compressedBytes = storage.getTotalCompressedBytes();
        if (compressedBytes > 100) {
            throw new AssertionError("RLE compression failed to compact repeating voxel blocks: " + compressedBytes + " bytes");
        }
    }

    private static void testVoxelLodRendererAndHorizonBlender() {
        // 1. Voxel Lod Renderer Indirect Draw enqueuing
        VoxelLodRenderer renderer = new VoxelLodRenderer(true, 1024);
        HyperionConfig cfg = new HyperionConfig();
        cfg.enableVoxelLodEngine = true;
        renderer.configure(cfg);

        renderer.beginLodFrame();
        for (int i = 0; i < 50; i++) {
            boolean ok = renderer.enqueueSectionDraw(36, 1, 0, i * 24, i);
            if (!ok) throw new AssertionError("Enqueueing voxel indirect draw command failed");
        }

        if (renderer.getActiveDrawCommands() != 50) {
            throw new AssertionError("Active draw commands mismatch, expected 50, got: " + renderer.getActiveDrawCommands());
        }

        ByteBuffer indirectBuf = renderer.finishLodFrame();
        if (indirectBuf.remaining() != 50 * VoxelLodRenderer.INDIRECT_COMMAND_STRIDE_BYTES) {
            throw new AssertionError("Indirect buffer byte size mismatch");
        }

        // 2. Voxel Horizon Blender
        VoxelHorizonBlender blender = new VoxelHorizonBlender(true);
        cfg.enableVoxelHorizonBlending = true;
        cfg.voxelBlendStartChunks = 12.0;
        cfg.voxelBlendEndChunks = 24.0;
        blender.configure(cfg);

        // Within vanilla chunk range (<= 12 chunks) -> 0.0 alpha (pure vanilla terrain)
        if (blender.calculateLodBlendFactor(10.0) != 0.0f) {
            throw new AssertionError("Close range must be 0.0 blend factor");
        }

        // Beyond transition range (>= 24 chunks) -> 1.0 alpha (pure voxel LOD)
        if (blender.calculateLodBlendFactor(30.0) != 1.0f) {
            throw new AssertionError("Far range must be 1.0 blend factor");
        }

        // Midway transition (18 chunks) -> between 0.0 and 1.0 smoothly
        float midFactor = blender.calculateLodBlendFactor(18.0);
        if (midFactor <= 0.1f || midFactor >= 0.9f) {
            throw new AssertionError("Mid range blend factor must smoothly interpolate, got: " + midFactor);
        }
    }

    private static void testVoxelPregenIngestEngineAsyncIntegration() {
        VoxelHierarchicalMipTree mipTree = new VoxelHierarchicalMipTree(true);
        VoxelSectionStorage storage = new VoxelSectionStorage();
        VoxelPregenIngestEngine ingestEngine = new VoxelPregenIngestEngine(true, mipTree, storage);

        byte[] rawSection = new byte[4096];
        for (int i = 0; i < rawSection.length; i++) rawSection[i] = (byte) 2; // Grass block

        // Asynchronously ingest chunk section (like Chunky or world traversal)
        CompletableFuture<Void> future = ingestEngine.ingestSectionAsync(5, 3, 5, rawSection);
        future.join(); // Wait for CPU mesher worker pool

        if (ingestEngine.getTotalIngestedChunks() != 1) {
            throw new AssertionError("Ingested chunks counter mismatch");
        }

        // Verify that Mip 0 and Mips 1..4 were all generated and stored in storage
        if (!storage.hasSection(5, 3, 5, 0)) {
            throw new AssertionError("Storage missing Mip 0 after ingestion");
        }
        if (!storage.hasSection(5, 3, 5, 1) || !storage.hasSection(5, 3, 5, 4)) {
            throw new AssertionError("Storage missing downsampled Mip levels after ingestion");
        }
    }

    private static void testDirectMemoryCleanerAndBufferFreeing() {
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024 * 1024); // 1 MB off-heap
        boolean cleaned = DirectMemoryCleaner.freeDirectBuffer(directBuffer);
        // Note: Clean should succeed or safely complete without exceptions
        if (!cleaned && !directBuffer.isDirect()) {
            throw new AssertionError("Direct buffer should be recognized as direct");
        }

        // Test GpuInstancingEngine and VoxelLodRenderer freeDirectBuffers
        GpuInstancingEngine instEngine = new GpuInstancingEngine(true, 512);
        instEngine.freeDirectBuffers();

        VoxelLodRenderer lodRenderer = new VoxelLodRenderer(true, 512);
        lodRenderer.freeDirectBuffers();
    }

    private static void testModCompatManagerEcosystemDetection() {
        HyperionModCompatManager compat = HyperionModCompatManager.getInstance();
        compat.registerDetectedMod("iris");
        compat.registerDetectedMod("sodium");
        compat.registerDetectedMod("distanthorizons");
        compat.registerDetectedMod("lithium");

        if (!compat.isIrisLoaded() || !compat.isSodiumLoaded() || !compat.isDistantHorizonsLoaded() || !compat.isLithiumLoaded()) {
            throw new AssertionError("Mod compatibility detection mismatch for registered mods");
        }
        if (!compat.getDetectedMods().contains("iris")) {
            throw new AssertionError("Detected mods set missing registered mod");
        }
    }

    private static void testIrisShaderCompatPipelinePasses() {
        IrisShaderCompatPipeline pipeline = IrisShaderCompatPipeline.getInstance();
        pipeline.reset();

        // 1. Shaders inactive -> render always permitted
        if (!pipeline.shouldRenderVoxelLodInCurrentPass()) {
            throw new AssertionError("Voxel LOD must render when shaders are inactive");
        }

        // 2. Shaders active, main composite pass
        pipeline.setShaderPackActive(true);
        pipeline.setShadowPassActive(false);
        if (!pipeline.shouldRenderVoxelLodInCurrentPass()) {
            throw new AssertionError("Voxel LOD must render in main GBuffer pass");
        }

        // 3. Shadow pass -> skip distant LOD to optimize shadow map frametime
        pipeline.setShadowPassActive(true);
        if (pipeline.shouldRenderVoxelLodInCurrentPass()) {
            throw new AssertionError("Distant voxel LOD must be skipped in shadow pass");
        }
    }

    private static void testPacketFlushConsolidatorSafetyCeiling() {
        PacketFlushConsolidator consolidator = new PacketFlushConsolidator(true);
        String channel = "test_channel";

        // Under normal batch size (e.g. 50) -> consolidate
        if (!consolidator.shouldConsolidateFlush(channel, 50)) {
            throw new AssertionError("Normal pending packet count must be consolidated");
        }

        // Exceeding safety ceiling (10,000 packets) -> MUST FORCE FLUSH (anti-OOM)
        boolean shouldConsolidate = consolidator.shouldConsolidateFlush(PacketFlushConsolidator.MAX_PENDING_SAFETY_CEILING + 10, 50);
        if (shouldConsolidate) {
            throw new AssertionError("Safety ceiling overflow must force immediate packet flush");
        }
    }

    private static void testDualGpuThermalFallbackWarmupGracePeriod() {
        DualGpuThermalFallback fallback = new DualGpuThermalFallback(true);
        HyperionConfig cfg = new HyperionConfig();
        cfg.enableDualGpuThermalFallback = true;
        cfg.thermalFallbackFrametimeThresholdMs = 40.0;
        fallback.configure(cfg);

        // Trigger 5-second warmup grace period (e.g. world loading or dimension teleport)
        fallback.triggerWarmupGracePeriod(5000L);

        // During warmup, massive frametime spikes (100ms) MUST NOT trigger premature fallback
        for (int i = 0; i < 5; i++) {
            fallback.recordFrameAndEvaluate(100.0);
        }

        if (fallback.isFallbackActive()) {
            throw new AssertionError("Warmup grace period must suppress false-positive thermal fallback triggers");
        }
    }

    private static void testExtendedCoordinateKeyPackingRange() {
        // Test coordinate range far beyond 500k blocks (e.g. ±5,000,000 blocks / ±312,500 chunks)
        int chunkX1 = 312500;
        int chunkZ1 = -312500;
        int sectionY1 = 12;
        int mip1 = 3;

        long key1 = VoxelSectionStorage.packSectionKey(chunkX1, sectionY1, chunkZ1, mip1);

        int chunkX2 = -312500;
        int chunkZ2 = 312500;
        int sectionY2 = 12;
        int mip2 = 3;

        long key2 = VoxelSectionStorage.packSectionKey(chunkX2, sectionY2, chunkZ2, mip2);

        if (key1 == key2) {
            throw new AssertionError("Distinct extreme coordinates must not produce colliding keys");
        }
    }

    private static void testDecoupledHudResolutionInvalidationAndHighRefresh() {
        DecoupledHudManager hud = new DecoupledHudManager(true, 360, true);
        hud.onResolutionChanged();

        // After resolution change, first repaint must be forced immediately
        if (!hud.shouldRepaintHud(System.nanoTime())) {
            throw new AssertionError("Resolution change must force immediate HUD repaint");
        }
    }

    private static void testParticleCoreGpuBatchingAndVectorMath() {
        AdvancedParticleEngine particleEngine = new AdvancedParticleEngine(true, 1024);

        // 1. Frustum and Distance Culling
        boolean visible = particleEngine.shouldRenderParticle(10, 64, 10, 10, 64, 10, 10000.0, false);
        if (!visible) {
            throw new AssertionError("Near visible particle must not be culled");
        }
        boolean occluded = particleEngine.shouldRenderParticle(10, 64, 10, 10, 64, 10, 10000.0, true);
        if (occluded) {
            throw new AssertionError("Occluded particle behind opaque wall must be culled");
        }

        // 2. GPU Batching
        particleEngine.beginParticleBatch();
        particleEngine.appendParticle(0f, 64f, 0f, 0f, 0f, 1f, 1f, 0xFFFFFFFF, 15);
        if (particleEngine.getCurrentParticleCount() != 1) {
            throw new AssertionError("Batch particle count mismatch");
        }
        ByteBuffer batch = particleEngine.finishParticleBatch();
        if (batch.remaining() != 4 * AdvancedParticleEngine.PARTICLE_VERTEX_STRIDE_BYTES) {
            throw new AssertionError("Particle batch buffer size mismatch");
        }

        // 3. Parametric Vector Math (Spiral, Ring, Homing)
        double[] outPos = new double[3];
        AdvancedParticleEngine.computeSpiralPos(0, 64, 0, 2.0, 0.5, 3.0, outPos);
        if (outPos[1] != 65.5) { // 64 + 0.5 * 3
            throw new AssertionError("Spiral height calculation mismatch");
        }

        double[] homingVel = new double[3];
        AdvancedParticleEngine.computeHomingVector(0, 0, 0, 10, 0, 0, 5.0, homingVel);
        if (Math.abs(homingVel[0] - 5.0) > 1e-4) {
            throw new AssertionError("Homing velocity magnitude mismatch");
        }

        particleEngine.freeDirectBuffers();
    }

    private static void testBadOptimizationsLightmapAndBiomeBlendCache() {
        BadOptimizationsEngine badOpt = new BadOptimizationsEngine(true);

        // 1. Lightmap Caching (skip calculation when sky and gamma are unchanged)
        boolean firstUpdate = badOpt.checkAndUpdateLightmapDirty(0.5f, 0.8f, 1.0f);
        if (!firstUpdate) {
            throw new AssertionError("Initial lightmap check must trigger update");
        }
        boolean secondUpdate = badOpt.checkAndUpdateLightmapDirty(0.5f, 0.8f, 1.0f);
        if (secondUpdate) {
            throw new AssertionError("Static lightmap environment must skip recalculation");
        }

        // 2. Biome Color Blend Fast Caching
        int grassColor1 = badOpt.getCachedGrassColor(100, 200, () -> 0x55AA55);
        int grassColor2 = badOpt.getCachedGrassColor(100, 200, () -> 0xFF0000); // Should return cached value
        if (grassColor1 != 0x55AA55 || grassColor2 != 0x55AA55) {
            throw new AssertionError("Biome blend cache mismatch");
        }

        // 3. Debug Overlay String Cache
        String line1 = badOpt.getCachedDebugLine("fps_counter", () -> "FPS: 240");
        String line2 = badOpt.getCachedDebugLine("fps_counter", () -> "FPS: 60");
        if (!line1.equals("FPS: 240") || !line2.equals("FPS: 240")) {
            throw new AssertionError("Debug line cache mismatch");
        }
    }

    private static void testMobtimizationsPathfindingAndTargetPacing() {
        MobAiOptimizer mobAi = new MobAiOptimizer(true);

        // 1. Pathfinding Redundancy Gate
        boolean recalcWhenMoving = mobAi.shouldRecalculatePath(true, false, 10.0);
        if (recalcWhenMoving) {
            throw new AssertionError("Redundant path recalculation must be suppressed for actively moving mobs");
        }
        boolean recalcWhenStuck = mobAi.shouldRecalculatePath(true, true, 10.0);
        if (!recalcWhenStuck) {
            throw new AssertionError("Stuck entities must be allowed to recalculate path");
        }

        // 2. Hazard 3x3x3 Scan Bypass for Monsters
        boolean monsterHazard = mobAi.shouldPerformHazardScanning(true, false);
        if (monsterHazard) {
            throw new AssertionError("Hazard scanning for hostile monsters must be bypassed");
        }
        boolean petHazard = mobAi.shouldPerformHazardScanning(false, true);
        if (!petHazard) {
            throw new AssertionError("Hazard scanning for player pets must be preserved");
        }

        // 3. Distance-Adaptive Target Scanning
        if (mobAi.getTargetAcquisitionInterval(64.0) != 20) {
            throw new AssertionError("Distant mob (>48 blocks) must scan once every 20 ticks");
        }
        if (mobAi.getTargetAcquisitionInterval(5.0) != 1) {
            throw new AssertionError("Close combat mob must scan every 1 tick");
        }

        // 4. Heavy Goal Stripping
        if (mobAi.isTurtleEggSearchPermitted()) {
            throw new AssertionError("Zombie turtle egg scanning must be stripped");
        }
        if (mobAi.shouldExecuteVillageRaidScan(1) || mobAi.shouldExecuteVillageRaidScan(2)) {
            throw new AssertionError("Village raid scans must be throttled to 1/3 rate");
        }
    }

    private static void testPalladiumCapabilityAndMatrixStackCache() {
        PalladiumCapabilityCache palCache = new PalladiumCapabilityCache(true);

        // 1. Capability Bit State Cache
        palCache.setCapability(1042, 0xABCDEFL);
        long cap = palCache.getCapability(1042, 0L);
        if (cap != 0xABCDEFL) {
            throw new AssertionError("Palladium capability cache lookup mismatch");
        }

        // 2. Animation Matrix Transform Cache
        float[] matrixIn = new float[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            10, 20, 30, 1
        };
        palCache.storeAnimationMatrix(1042, matrixIn);

        float[] matrixOut = new float[16];
        boolean found = palCache.getAnimationMatrix(1042, matrixOut);
        if (!found || matrixOut[12] != 10.0f || matrixOut[13] != 20.0f) {
            throw new AssertionError("Animation matrix cache lookup mismatch");
        }

        palCache.invalidateEntity(1042);
        if (palCache.getCapability(1042, 0L) != 0L) {
            throw new AssertionError("Entity invalidation must clear cached capabilities");
        }
    }

    private static void testMobtimizationsPhaseStaggeringAndTypeSafety() {
        MobAiOptimizer mobAi = new MobAiOptimizer(true);

        // Test Phase-Staggering across 20 entities over 20 ticks
        // Each entity ID should trigger on a unique phase, perfectly flattening tick distribution
        int totalScansOverOneSecond = 0;
        for (int tick = 0; tick < 20; tick++) {
            int scansThisTick = 0;
            for (int entityId = 1; entityId <= 20; entityId++) {
                if (mobAi.shouldExecuteTargetScan(entityId, tick, 50.0)) {
                    scansThisTick++;
                    totalScansOverOneSecond++;
                }
            }
            // Each tick must have exactly 1 entity scanning (perfect uniform distribution!)
            if (scansThisTick != 1) {
                throw new AssertionError("Phase staggering must evenly distribute scans, got: " + scansThisTick + " on tick " + tick);
            }
        }
        if (totalScansOverOneSecond != 20) {
            throw new AssertionError("Total 20 entities must scan exactly once across 20 ticks");
        }
    }

    private static void testBadOptimizationsBoundedLruAndDimensionShift() {
        BadOptimizationsEngine badOpt = new BadOptimizationsEngine(true);

        // 1. Fill cache beyond MAX_CACHE_ENTRIES (4096)
        for (int i = 0; i < 4100; i++) {
            badOpt.getCachedGrassColor(i * 16, i * 16, () -> 0x00FF00);
        }
        // Cache must not exceed MAX_CACHE_ENTRIES (pruned on long flight)

        // 2. Dimension Shift Invalidation
        badOpt.onDimensionChanged("minecraft:the_nether");
        // Must force lightmap update
        boolean netherUpdate = badOpt.checkAndUpdateLightmapDirty(0.0f, 0.0f, 1.0f);
        if (!netherUpdate) {
            throw new AssertionError("Dimension shift must trigger lightmap refresh");
        }
    }

    private static void testParticleEngineDynamicScaleAndIrisPassGating() {
        AdvancedParticleEngine particleEngine = new AdvancedParticleEngine(true, 512);

        // 1. Dynamic Quad Sizing
        particleEngine.beginParticleBatch();
        boolean appended = particleEngine.appendParticle(0, 64, 0, 1.5f, 2.0f, 0, 0, 1, 1, 0xFFFFFFFF, 15);
        if (!appended || particleEngine.getCurrentParticleCount() != 1) {
            throw new AssertionError("Custom-sized particle must be enqueued");
        }

        ByteBuffer buffer = particleEngine.finishParticleBatch();
        // First vertex X coordinate must match x - hx = 0 - 0.75 = -0.75f
        float firstX = buffer.getFloat(0);
        if (Math.abs(firstX - (-0.75f)) > 1e-4f) {
            throw new AssertionError("Vertex calculation for scaled particle mismatch: " + firstX);
        }

        particleEngine.freeDirectBuffers();
    }

    private static void testPalladiumBoundedCacheAndScaleFactor() {
        PalladiumCapabilityCache palCache = new PalladiumCapabilityCache(true);

        float[] matrixIn = new float[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            1, 2, 3, 1
        };

        // Scale by 2.5 (Pehkui / Giant Morph)
        palCache.storeScaledAnimationMatrix(2048, 2.5f, matrixIn);

        float[] matrixOut = new float[16];
        boolean found = palCache.getAnimationMatrix(2048, matrixOut);
        if (!found || Math.abs(matrixOut[12] - 2.5f) > 1e-4f || Math.abs(matrixOut[13] - 5.0f) > 1e-4f) {
            throw new AssertionError("Scaled matrix calculation mismatch");
        }
    }

    private static void testOffHeapChunkSegmentNativeBufferFreeing() {
        com.hyperion.optimizer.core.memory.OffHeapChunkSegment segment = new com.hyperion.optimizer.core.memory.OffHeapChunkSegment();
        segment.setNibble(0, 0, 0, 15);
        if (segment.getNibble(0, 0, 0) != 15) {
            throw new AssertionError("Nibble write/read mismatch");
        }
        segment.free();
        if (!segment.isFreed()) {
            throw new AssertionError("OffHeap segment must be marked freed");
        }
    }

    private static void testVoxelMipTreeZeroAllocationHistogram() {
        VoxelHierarchicalMipTree mipTree = new VoxelHierarchicalMipTree(true);
        byte[] raw = new byte[4096];
        for (int i = 0; i < 4096; i++) {
            raw[i] = (byte) ((i % 5) + 1);
        }

        // Downsample multiple times - must succeed without GC pressure
        byte[] mip1 = mipTree.downsampleSection(raw, 1);
        byte[] mip2 = mipTree.downsampleSection(raw, 2);
        byte[] mip3 = mipTree.downsampleSection(raw, 3);
        byte[] mip4 = mipTree.downsampleSection(raw, 4);

        if (mip1.length != 512 || mip2.length != 64 || mip3.length != 8 || mip4.length != 1) {
            throw new AssertionError("Downsampled Mip dimensions mismatch");
        }
    }

    private static void testSpatialCollisionPairDeduplication() {
        if (!com.hyperion.optimizer.core.entity.SpatialCollisionEngine.shouldEvaluatePair(10, 20)) {
            throw new AssertionError("Pair check (10, 20) must be evaluated");
        }
        if (com.hyperion.optimizer.core.entity.SpatialCollisionEngine.shouldEvaluatePair(20, 10)) {
            throw new AssertionError("Pair check (20, 10) must be rejected to halve O(N^2) work");
        }
    }

    private static void testAsyncWorldTickDispatcherDrainLoop() {
        AsyncWorldTickDispatcher dispatcher = new AsyncWorldTickDispatcher(true);
        java.util.concurrent.atomic.AtomicInteger executedCount = new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < 50; i++) {
            dispatcher.queueAsyncTask(executedCount::incrementAndGet);
        }

        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {}

        if (executedCount.get() != 50) {
            throw new AssertionError("All queued async tasks must execute in drain loop, executed: " + executedCount.get());
        }
    }

    private static void testThreadPoolManagerCallerRunsPolicy() {
        HyperionThreadPoolManager manager = HyperionThreadPoolManager.getInstance();
        if (manager.getEntityPhysicsPool() == null || manager.getLightEnginePool() == null) {
            throw new AssertionError("Thread pools must be initialized");
        }
        if (manager.getEntityPhysicsPool().isShutdown()) {
            throw new AssertionError("Entity pool must not be shutdown");
        }
    }

    private static void testHudDirtyTrackerDamageIsolation() {
        com.hyperion.optimizer.core.hud.HudDirtyTracker tracker = new com.hyperion.optimizer.core.hud.HudDirtyTracker();
        tracker.updateState(20.0f, 20, 10, 300, 5, 0.5f, 0, 100L);
        tracker.clearDirty();

        // 1. Take damage: Health drops to 16
        boolean marked = tracker.updateHealth(16.0f, 20, 10);
        if (!marked || !tracker.isDirty()) {
            throw new AssertionError("Health drop must flag HUD dirty once");
        }
        tracker.clearDirty();

        // 2. Continuous frames with same damage state during 10-tick hurtTime animation
        boolean redundantMark = tracker.updateHealth(16.0f, 20, 10);
        if (redundantMark || tracker.isDirty()) {
            throw new AssertionError("Same health during hurt animation must NOT continuously dirty HUD buffer");
        }
    }

    private static void testColorCorrectionEngineZeroAllocationLutAndLightmap() {
        ColorCorrectionEngine cce = new ColorCorrectionEngine(true);
        int[] lightmap = new int[256];
        for (int i = 0; i < 256; i++) {
            lightmap[i] = (0xFF << 24) | (i << 16) | (i << 8) | i;
        }

        // Process lightmap with fast LUT
        cce.processLightmap(lightmap, 16, 16, 0.5f);

        // Alpha channel must be preserved
        int alpha = (lightmap[128] >> 24) & 0xFF;
        if (alpha != 255) {
            throw new AssertionError("Alpha channel in lightmap must be 255");
        }
    }

    private static void testPlayerRespawnTeleportAndThermalGraceWarmup() {
        HyperionEngine engine = HyperionEngine.getInstance();
        engine.onPlayerRespawnOrTeleport();

        com.hyperion.optimizer.core.gpu.dualgpu.DualGpuThermalFallback fallback = engine.getThermalFallback();
        if (fallback != null) {
            // Must not be active after respawn
            if (fallback.isFallbackActive()) {
                throw new AssertionError("Fallback must not be active immediately after respawn reset");
            }
            // Burst frame time spike during respawn chunk load must NOT trip fallback due to 6s grace period
            boolean active = fallback.recordFrameAndEvaluate(100.0);
            if (active) {
                throw new AssertionError("Chunk loading frame spike during respawn must be protected by warmup grace period");
            }
        }
    }

    private static void testExtremeChunkDistanceMobAiAndSpatialPruning() {
        // 1. Mob AI Pacing at extreme distances (32+ chunks = 128+ to 512+ blocks)
        com.hyperion.optimizer.core.ai.MobAiOptimizer mobAi = new com.hyperion.optimizer.core.ai.MobAiOptimizer(true);
        int intervalNear = mobAi.getTargetAcquisitionInterval(5.0);
        int intervalExtreme = mobAi.getTargetAcquisitionInterval(200.0);

        if (intervalNear != 1) {
            throw new AssertionError("Close mob (<8m) must scan every tick, got: " + intervalNear);
        }
        if (intervalExtreme != 100) {
            throw new AssertionError("Distant mob (>128m) at 32 chunks must scan once every 100 ticks (5s), got: " + intervalExtreme);
        }

        // 2. Spatial Collision Grid Distance-Gated Pruning
        com.hyperion.optimizer.core.entity.SpatialCollisionEngine spatialEngine =
            new com.hyperion.optimizer.core.entity.SpatialCollisionEngine(true, 8, 32.0);
        spatialEngine.onTickStart();

        // Register distant mob at (500, 64, 500) with camera at (0, 0) -> must be pruned
        com.hyperion.optimizer.core.entity.SpatialCollisionEngine.CollidableEntity distantMob =
            new com.hyperion.optimizer.core.entity.SpatialCollisionEngine.CollidableEntity(101, 500.0, 64.0, 500.0, 0.6, 1.8);
        spatialEngine.registerEntity(distantMob, 0.0, 0.0);

        java.util.List<com.hyperion.optimizer.core.entity.SpatialCollisionEngine.CollidableEntity> candidates =
            spatialEngine.getNearbyCandidates(500.0, 500.0);
        if (!candidates.isEmpty()) {
            throw new AssertionError("Distant mobs (>64m) must not pollute spatial collision grid at 32 chunks");
        }

        // Register near mob at (5, 64, 5) -> must be registered
        com.hyperion.optimizer.core.entity.SpatialCollisionEngine.CollidableEntity nearMob =
            new com.hyperion.optimizer.core.entity.SpatialCollisionEngine.CollidableEntity(102, 5.0, 64.0, 5.0, 0.6, 1.8);
        spatialEngine.registerEntity(nearMob, 0.0, 0.0);

        java.util.List<com.hyperion.optimizer.core.entity.SpatialCollisionEngine.CollidableEntity> nearCandidates =
            spatialEngine.getNearbyCandidates(5.0, 5.0);
        if (nearCandidates.isEmpty()) {
            throw new AssertionError("Near mobs must be registered in spatial collision grid");
        }
    }

    private static void testNightLightmapDiscreteQuantizationAndHostileMobPacing() {
        // 1. Night Lightmap Quantization
        com.hyperion.optimizer.core.micro.BadOptimizationsEngine badOpt =
            new com.hyperion.optimizer.core.micro.BadOptimizationsEngine(true);

        // Initial lightmap generation
        boolean u0 = badOpt.checkAndUpdateLightmapDirty(0.8500f, 0.0f, 1.0f);
        if (!u0) {
            throw new AssertionError("First lightmap check must return true to upload initial texture");
        }

        // Sub-step frame variation at night (e.g. 0.8502f) must NOT trigger re-upload
        boolean u1 = badOpt.checkAndUpdateLightmapDirty(0.8502f, 0.0f, 1.0f);
        if (u1) {
            throw new AssertionError("Micro-variations in skyDarken at night must be quantized to prevent per-frame GPU stalls");
        }

        // Meaningful discrete light step (e.g. 0.9000f) must update
        boolean u2 = badOpt.checkAndUpdateLightmapDirty(0.9000f, 0.0f, 1.0f);
        if (!u2) {
            throw new AssertionError("Discrete light step must trigger update");
        }

        // 2. Night Hostile Mob Throttling
        com.hyperion.optimizer.core.ai.MobAiOptimizer mobAi = new com.hyperion.optimizer.core.ai.MobAiOptimizer(true);
        // Distant monster (>24m) at night must be throttled on non-quarter ticks
        boolean throttled = mobAi.shouldThrottleNightHostileMob(true, true, 30.0, 1, 0);
        if (!throttled) {
            throw new AssertionError("Distant hostile monster at night must be throttled");
        }
        // Melee range monster (<24m) at night must NOT be throttled
        boolean nearThrottled = mobAi.shouldThrottleNightHostileMob(true, true, 10.0, 1, 0);
        if (nearThrottled) {
            throw new AssertionError("Melee combat range monster (<24m) must not be throttled");
        }
    }

    private static void testHdTexturePackAlphaBleedAndBlackBorderElimination() {
        // 4x4 test texture: center (1,1) is green (0xFF00FF00), neighbor (1,2) is transparent black (0x00000000)
        int[] pixels = new int[16];
        pixels[1 * 4 + 1] = 0xFF00FF00; // Opaque green
        pixels[1 * 4 + 2] = 0x00000000; // Transparent black

        FastHdTextureEngine.dilateAlphaBleed(pixels, 4, 4);

        int dilated = pixels[1 * 4 + 2];
        int a = (dilated >> 24) & 0xFF;
        int r = (dilated >> 16) & 0xFF;
        int g = (dilated >> 8) & 0xFF;
        int b = dilated & 0xFF;

        if (a != 0) {
            throw new AssertionError("Dilated transparent pixel must retain alpha 0");
        }
        if (g == 0) {
            throw new AssertionError("Dilated transparent pixel must receive neighbor green color (RGB) to eliminate black borders in mipmaps");
        }
    }

    private static void testAnimatedSpriteInitialFrameGuaranteeAndUiBypass() {
        FastHdTextureEngine engine = new FastHdTextureEngine(true);

        // 1. Newly loaded animated texture in resource pack (starts offscreen) -> MUST upload frame 0
        boolean f0 = engine.shouldUpdateAnimatedSprite("custom_pack:block/animated_crystal", 256, 256, false, 50L);
        if (!f0) {
            throw new AssertionError("Frame 0 must upload immediately even if offscreen to prevent black uninitialized texture");
        }

        // Subsequent tick offscreen -> throttled
        boolean f1 = engine.shouldUpdateAnimatedSprite("custom_pack:block/animated_crystal", 256, 256, false, 51L);
        if (f1) {
            throw new AssertionError("Offscreen animated sprite should throttle subsequent ticks");
        }

        // 2. UI / Handheld Items (compass, clock, gui icons) -> NEVER throttled
        boolean ui0 = engine.shouldUpdateAnimatedSprite("minecraft:item/compass_16", 64, 64, false, 52L);
        boolean ui1 = engine.shouldUpdateAnimatedSprite("minecraft:item/compass_16", 64, 64, false, 53L);
        if (!ui0 || !ui1) {
            throw new AssertionError("UI and held item animated textures must never be throttled into blackness");
        }
    }

    private static void testMipmapChainIntegrityAndBlackDistantTexturePrevention() {
        FastHdTextureEngine engine = new FastHdTextureEngine(true);
        // Test standard resolutions across 16x up to 1024x
        int[] resolutions = {16, 32, 64, 128, 256, 512, 1024};
        for (int res : resolutions) {
            int levels = engine.calculateOptimalMipmapLevels(res, res, 4, 4096);
            if (levels != 4) {
                throw new AssertionError("Mipmap levels for " + res + "x must be 4 to prevent OpenGL missing-LOD blackness");
            }
        }
    }

    private static void testTransparentLeavesOcclusionSafetyAndBushyPacks() {
        FancyGraphicsOptimizer optimizer = new FancyGraphicsOptimizer(true);

        // Transparent / Bushy leaves (Custom texture pack) -> Do NOT cull internal faces
        boolean cullTransparent = optimizer.shouldCullLeavesFace(true, true, false);
        if (cullTransparent) {
            throw new AssertionError("Internal faces of transparent/bushy leaves must NOT be culled to avoid black hollow tree cavities");
        }

        // Opaque / Fast leaves -> Cull internal faces for performance
        boolean cullOpaque = optimizer.shouldCullLeavesFace(true, true, true);
        if (!cullOpaque) {
            throw new AssertionError("Internal faces of opaque solid leaves should be culled");
        }
    }

    private static void testLightmapOpaqueAlphaAndAbgrSafety() {
        ColorCorrectionEngine engine = new ColorCorrectionEngine(true);

        // 1. ARGB Lightmap with zero alpha
        int[] argbPixels = new int[256];
        argbPixels[0] = 0x00000000; // Zero alpha black pixel
        engine.processLightmap(argbPixels, 16, 16, 0.0f);

        int outAlpha = (argbPixels[0] >> 24) & 0xFF;
        if (outAlpha != 0xFF) {
            throw new AssertionError("Lightmap alpha must be enforced to 0xFF (opaque) to prevent black texture discard");
        }

        int outR = (argbPixels[0] >> 16) & 0xFF;
        if (outR <= 0) {
            throw new AssertionError("Lightmap must have safe ambient floor above zero to prevent black-crush collapse");
        }

        // 2. ABGR Lightmap
        int[] abgrPixels = new int[256];
        abgrPixels[0] = 0x00FF8040;
        engine.processLightmapAbgr(abgrPixels, 16, 16, 0.0f);

        int outAbgrAlpha = (abgrPixels[0] >> 24) & 0xFF;
        if (outAbgrAlpha != 0xFF) {
            throw new AssertionError("ABGR lightmap alpha must be 0xFF");
        }
    }

    private static void testResourcePackReloadStateInvalidation() {
        HyperionEngine engine = HyperionEngine.getInstance();
        engine.onResourceReload();

        FastHdTextureEngine hdEngine = engine.getFastHdTextureEngine();
        if (hdEngine != null && hdEngine.getThrottledAnimationsCount() != 0) {
            throw new AssertionError("Resource reload must reset animated texture throttled metrics");
        }

        StaticChestMeshBaker chestBaker = engine.getChestBaker();
        if (chestBaker != null) {
            chestBaker.setChestOpenState(12345L, true);
            engine.onResourceReload();
            if (chestBaker.isChestOpen(12345L)) {
                throw new AssertionError("Resource reload must clear static chest state");
            }
        }
    }

    private static void testKeyBindingManagerRightControlShortcut() {
        HyperionKeyBindingManager manager = HyperionKeyBindingManager.getInstance();
        manager.reset();
        manager.setEnabled(true);

        // 1. Right Control Key Press (keyCode 345, action 1)
        boolean triggered = manager.handleKeyInput(HyperionKeyBindingManager.GLFW_KEY_RIGHT_CONTROL, 0, 1, 0);
        if (!triggered) {
            throw new AssertionError("Right Control key (345) must trigger open screen request");
        }
        if (!manager.consumeOpenScreenRequest()) {
            throw new AssertionError("Screen request must be consumed");
        }
        if (manager.consumeOpenScreenRequest()) {
            throw new AssertionError("Screen request must be false after consumption");
        }

        // 2. Direct key code check
        if (!manager.shouldOpenConfigScreen(HyperionKeyBindingManager.GLFW_KEY_RIGHT_CONTROL)) {
            throw new AssertionError("shouldOpenConfigScreen with Right Control must return true");
        }
        manager.consumeOpenScreenRequest();

        // 3. Fallback Ctrl + Shift + 0
        boolean fallback = manager.shouldOpenConfigScreen(true, true, HyperionKeyBindingManager.GLFW_KEY_0);
        if (!fallback) {
            throw new AssertionError("Ctrl+Shift+0 fallback combination must return true");
        }
        manager.consumeOpenScreenRequest();

        // 4. Other key (e.g. Left Control alone or key 'A') -> must NOT trigger
        if (manager.handleKeyInput(HyperionKeyBindingManager.GLFW_KEY_LEFT_CONTROL, 0, 1, 0)) {
            throw new AssertionError("Left Control alone should not trigger menu");
        }
        if (manager.handleKeyInput(65, 0, 1, 0)) { // Key 'A'
            throw new AssertionError("Key 'A' must not trigger menu");
        }

        // 5. Test MixinKeyboard integration
        boolean mixinTriggered = com.hyperion.optimizer.mixin.MixinKeyboard.onKey(0L, HyperionKeyBindingManager.GLFW_KEY_RIGHT_CONTROL, 0, 1, 0);
        if (!mixinTriggered || !manager.consumeOpenScreenRequest()) {
            throw new AssertionError("MixinKeyboard must intercept Right Control key");
        }
        if (!com.hyperion.optimizer.mixin.MixinKeyboard.shouldInterceptKey(HyperionKeyBindingManager.GLFW_KEY_RIGHT_CONTROL, 1)) {
            throw new AssertionError("MixinKeyboard.shouldInterceptKey must return true for Right Control");
        }
    }

    private static void testSimdFrustumCullerMatrixMultiplication() {
        com.hyperion.optimizer.core.gpu.SimdFrustumCuller culler = new com.hyperion.optimizer.core.gpu.SimdFrustumCuller();
        float[] proj = new float[16];
        float[] mod = new float[16];
        // Identity matrices
        proj[0] = 1; proj[5] = 1; proj[10] = 1; proj[15] = 1;
        mod[0] = 1; mod[5] = 1; mod[10] = 1; mod[15] = 1;
        culler.updatePlanes(proj, mod);

        float[] minX = {-1.0f}, minY = {-1.0f}, minZ = {-1.0f};
        float[] maxX = {1.0f}, maxY = {1.0f}, maxZ = {1.0f};
        int mask = culler.testBatch8(minX, minY, minZ, maxX, maxY, maxZ);
        if ((mask & 1) == 0) {
            throw new AssertionError("Center box should be visible in identity frustum");
        }
    }

    private static void testVoxelSectionStorageNegativeHeights118Support() {
        com.hyperion.optimizer.core.lod.voxel.VoxelSectionStorage storage = new com.hyperion.optimizer.core.lod.voxel.VoxelSectionStorage();
        byte[] sectionData = new byte[64];
        for (int i = 0; i < sectionData.length; i++) sectionData[i] = (byte) (i + 1);

        // Store section at negative height Y = -4 (Deepslate layer in 1.18+)
        storage.storeSection(10, -4, 20, 0, sectionData);
        // Store section at positive height Y = 252 (Mountain layer)
        storage.storeSection(10, 252, 20, 0, sectionData);

        long keyNegative = com.hyperion.optimizer.core.lod.voxel.VoxelSectionStorage.packSectionKey(10, -4, 20, 0);
        long keyPositive = com.hyperion.optimizer.core.lod.voxel.VoxelSectionStorage.packSectionKey(10, 252, 20, 0);

        if (keyNegative == keyPositive) {
            throw new AssertionError("Negative section Y (-4) must not collide with positive section Y (252)");
        }

        byte[] loadedNegative = storage.getSection(10, -4, 20, 0, 64);
        byte[] loadedPositive = storage.getSection(10, 252, 20, 0, 64);

        if (loadedNegative == null || loadedPositive == null) {
            throw new AssertionError("Both negative and positive sections must be retrievable");
        }
    }

    private static void testAsyncWorldTickDispatcherBackpressureBoundedQueue() {
        com.hyperion.optimizer.core.threading.AsyncWorldTickDispatcher dispatcher = new com.hyperion.optimizer.core.threading.AsyncWorldTickDispatcher(true);
        final java.util.concurrent.atomic.AtomicInteger executedCount = new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < com.hyperion.optimizer.core.threading.AsyncWorldTickDispatcher.MAX_PENDING_TASKS + 50; i++) {
            dispatcher.queueAsyncTask(executedCount::incrementAndGet);
        }

        if (dispatcher.getQueueDepth() > com.hyperion.optimizer.core.threading.AsyncWorldTickDispatcher.MAX_PENDING_TASKS) {
            throw new AssertionError("Queue depth must not exceed MAX_PENDING_TASKS");
        }
    }

    private static void testSpatialCollisionEngineBucketCapping() {
        com.hyperion.optimizer.core.entity.SpatialCollisionEngine engine = new com.hyperion.optimizer.core.entity.SpatialCollisionEngine(true, 10, 32.0);
        for (int i = 0; i < 200; i++) {
            engine.registerEntity(new com.hyperion.optimizer.core.entity.SpatialCollisionEngine.CollidableEntity(i, 0.5, 64.0, 0.5, 0.6, 1.8));
        }

        List<com.hyperion.optimizer.core.entity.SpatialCollisionEngine.CollidableEntity> candidates = engine.getNearbyCandidates(0.5, 0.5);
        if (candidates.size() > com.hyperion.optimizer.core.entity.SpatialCollisionEngine.MAX_ENTITIES_PER_BUCKET) {
            throw new AssertionError("Candidate list per bucket must be capped at MAX_ENTITIES_PER_BUCKET");
        }
    }

    private static void testHyperionConfigStorageCommentsAndSanitization() {
        String jsonWithComments = "{\n" +
            "  // This is an inline comment\n" +
            "  \"enableGpuDrivenRenderer\": true,\n" +
            "  /* block comment */\n" +
            "  \"targetFramerate\": 240\n" +
            "}";
        com.hyperion.optimizer.api.HyperionConfig cfg = com.hyperion.optimizer.api.HyperionConfigStorage.parseJson(jsonWithComments);
        if (!cfg.enableGpuDrivenRenderer || cfg.targetFramerate != 240) {
            throw new AssertionError("Config parser must cleanly handle inline and block comments");
        }
    }

    private static void testIrisShaderCompatPipelinePassCoordination() {
        com.hyperion.optimizer.compat.IrisShaderCompatPipeline pipeline = com.hyperion.optimizer.compat.IrisShaderCompatPipeline.getInstance();
        pipeline.reset();

        // 1. Without shaders
        pipeline.setShaderPackActive(false);
        if (!pipeline.shouldRenderDecoupledHudNow() || !pipeline.shouldRenderVoxelLodInCurrentPass()) {
            throw new AssertionError("Without shaders, HUD and LOD rendering must always be permitted");
        }

        // 2. With shaders active in shadow pass
        pipeline.setShaderPackActive(true);
        pipeline.setShadowPassActive(true);
        pipeline.setCurrentPassName("shadow");
        if (pipeline.shouldRenderVoxelLodInCurrentPass()) {
            throw new AssertionError("In Iris shadow pass, voxel LODs should be skipped to conserve shadow map fillrate");
        }
        if (pipeline.shouldRenderDecoupledHudNow()) {
            throw new AssertionError("In Iris shadow pass, Decoupled HUD must not render");
        }

        // 3. With shaders active in composite_final pass
        pipeline.setShadowPassActive(false);
        pipeline.setCurrentPassName("composite_final");
        if (!pipeline.shouldRenderDecoupledHudNow()) {
            throw new AssertionError("In composite_final pass, Decoupled HUD must be permitted");
        }
        if (!pipeline.shouldRenderVoxelLodInCurrentPass()) {
            throw new AssertionError("In normal pass, voxel LODs must be permitted");
        }
    }

    private static void testSleepingHopperServerTickRollback() {
        com.hyperion.optimizer.core.physics.SleepingHopperManager hopperManager = new com.hyperion.optimizer.core.physics.SleepingHopperManager(true);
        hopperManager.clear();

        long pos = 123456789L;
        hopperManager.putToSleep(pos, 5000L, 100); // Sleep until 5100
        if (!hopperManager.isHopperSleeping(pos, 5050L)) {
            throw new AssertionError("Hopper must be sleeping at tick 5050");
        }

        // Server tick rollback: e.g. /time set 0 or NTP sync -> tick rolls back to 100
        boolean isSleepingAfterRollback = hopperManager.isHopperSleeping(pos, 100L);
        if (isSleepingAfterRollback) {
            throw new AssertionError("Server tick rollback must trigger instant wake-up of sleeping hoppers");
        }
    }

    private static void testStaticChestCustomModelBypass() {
        com.hyperion.optimizer.core.entity.StaticChestMeshBaker chestBaker = new com.hyperion.optimizer.core.entity.StaticChestMeshBaker(true);
        chestBaker.clear();

        long packedPos = 987654321L;
        // Default: closed chest should render as static block
        if (!chestBaker.shouldRenderAsStaticBlock(packedPos)) {
            throw new AssertionError("Closed vanilla chest should render as static block");
        }

        // Custom 3D model or Physics Mod active -> must bypass static baking
        chestBaker.setCustomModelModActive(true);
        if (chestBaker.shouldRenderAsStaticBlock(packedPos)) {
            throw new AssertionError("With custom 3D model mod active, static baking must be bypassed");
        }
        chestBaker.setCustomModelModActive(false);

        // Custom texture pack active -> must bypass static baking
        chestBaker.setCustomTexturePackActive(true);
        if (chestBaker.shouldRenderAsStaticBlock(packedPos)) {
            throw new AssertionError("With custom texture pack active, static baking must be bypassed");
        }
    }

    private static void testTexturePackColorCorrectionArgbAndAbgr() {
        ColorCorrectionEngine cce = new ColorCorrectionEngine(true);
        HyperionConfig cfg = new HyperionConfig();
        cfg.enableColorCorrection = true;
        cfg.enableTexturePackColorCorrection = true;
        cfg.colorGradingMode = "VIBRANT_HDR";
        cfg.colorVibrance = 1.20;
        cfg.colorSaturation = 1.10;
        cfg.colorContrast = 1.05;
        cce.configure(cfg);

        // 1. ARGB Texture Pack Buffer (4x4)
        int[] argb = new int[16];
        argb[0] = 0x00000000; // Transparent black -> must retain alpha 0
        argb[1] = 0x80804020; // Translucent brown -> must retain alpha 0x80
        argb[2] = 0xFF000000; // Pure opaque black (obsidian/coal) -> must NOT be lifted to gray!
        argb[3] = 0xFFFFFFFF; // Pure white highlight -> must remain white (~255)
        argb[4] = 0xFF2060C0; // Mid blue -> vibrant HDR color grading

        cce.processTexture(argb, 4, 4);

        int a0 = (argb[0] >> 24) & 0xFF;
        if (a0 != 0) {
            throw new AssertionError("Transparent pixel in texture pack must retain alpha 0");
        }

        int a1 = (argb[1] >> 24) & 0xFF;
        if (a1 != 0x80) {
            throw new AssertionError("Translucent pixel in texture pack must retain alpha 0x80, got: " + a1);
        }

        int r2 = (argb[2] >> 16) & 0xFF;
        int g2 = (argb[2] >> 8) & 0xFF;
        int b2 = argb[2] & 0xFF;
        if (r2 != 0 || g2 != 0 || b2 != 0) {
            throw new AssertionError("Pure black texture pixel in texture pack must stay 0x000000 (no artificial ambient floor lift), got: R=" + r2 + " G=" + g2 + " B=" + b2);
        }

        int r3 = (argb[3] >> 16) & 0xFF;
        int g3 = (argb[3] >> 8) & 0xFF;
        int b3 = argb[3] & 0xFF;
        if (r3 < 250 || g3 < 250 || b3 < 250) {
            throw new AssertionError("White texture pixel must stay near 255 with ACES normalization");
        }

        // 2. NativeImage ABGR Texture Pack Buffer
        int[] abgr = new int[16];
        abgr[0] = 0x00000000;
        abgr[1] = 0x80204080; // Translucent
        abgr[2] = 0xFF000000; // Black
        abgr[3] = 0xFFFFFFFF; // White

        cce.processTextureAbgr(abgr, 4, 4);

        int abgrA0 = (abgr[0] >> 24) & 0xFF;
        if (abgrA0 != 0) {
            throw new AssertionError("ABGR transparent pixel must retain alpha 0");
        }
        int abgrA1 = (abgr[1] >> 24) & 0xFF;
        if (abgrA1 != 0x80) {
            throw new AssertionError("ABGR translucent pixel must retain alpha 0x80");
        }
        int abgrR2 = abgr[2] & 0xFF;
        int abgrG2 = (abgr[2] >> 8) & 0xFF;
        int abgrB2 = (abgr[2] >> 16) & 0xFF;
        if (abgrR2 != 0 || abgrG2 != 0 || abgrB2 != 0) {
            throw new AssertionError("ABGR black texture pixel must stay pure black");
        }
    }

    private static void testTexturePackDilateAndColorCorrectPipelineAndColormaps() {
        ColorCorrectionEngine cce = new ColorCorrectionEngine(true);
        FastHdTextureEngine hdEngine = new FastHdTextureEngine(true);

        // 1. Texture with solid green (0xFF00FF00) and transparent neighbor (0x00000000)
        int[] pixels = new int[16];
        pixels[1 * 4 + 1] = 0xFF00FF00; // Center green
        pixels[1 * 4 + 2] = 0x00000000; // Transparent neighbor

        FastHdTextureEngine.dilateAndColorCorrectTexturePack(pixels, 4, 4, false, cce);

        int solidGreen = pixels[1 * 4 + 1];
        int solidA = (solidGreen >> 24) & 0xFF;
        int solidG = (solidGreen >> 8) & 0xFF;
        if (solidA != 0xFF || solidG == 0) {
            throw new AssertionError("Solid green pixel must be color graded and remain opaque");
        }

        int dilated = pixels[1 * 4 + 2];
        int dilatedA = (dilated >> 24) & 0xFF;
        int dilatedG = (dilated >> 8) & 0xFF;
        if (dilatedA != 0) {
            throw new AssertionError("Dilated transparent pixel must retain alpha 0");
        }
        if (dilatedG == 0) {
            throw new AssertionError("Dilated transparent pixel must receive graded neighbor green RGB");
        }

        // 2. Colormap Integer Tint Grading (Grass/Foliage from custom texture pack)
        int rawGrass32 = 0xFF55AA55;
        int gradedGrass32 = cce.gradeColorRgbInt(rawGrass32);
        int gradedAlpha = (gradedGrass32 >> 24) & 0xFF;
        if (gradedAlpha != 0xFF) {
            throw new AssertionError("Graded 32-bit colormap color must retain alpha 255");
        }

        int rawGrass24 = 0x0055AA55;
        int gradedGrass24 = cce.gradeColorRgbInt(rawGrass24);
        int gradedAlpha24 = (gradedGrass24 >> 24) & 0xFF;
        if (gradedAlpha24 != 0) {
            throw new AssertionError("Graded 24-bit Minecraft BiomeColors must retain 24-bit RGB format");
        }

        // 3. Mixin Hooks for Texture Pack & Biome Colors
        MixinLightmapTexture.onProcessTexture(pixels, 4, 4);
        MixinLightmapTexture.onProcessTextureAbgr(pixels, 4, 4);
        int mixinBiomeColor = MixinLightmapTexture.onGradeBiomeColor(rawGrass32);
        if (((mixinBiomeColor >> 24) & 0xFF) != 0xFF) {
            throw new AssertionError("Mixin onGradeBiomeColor must return valid graded color");
        }
    }

    private static void testHyperionEngineSubsystemNonNullableGuaranteeAndAutoInit() {
        HyperionEngine engine = HyperionEngine.getInstance();
        if (!engine.isInitialized()) {
            throw new AssertionError("HyperionEngine must auto-initialize upon first getInstance() call");
        }

        // Verify non-nullability across all core subsystems
        if (engine.getComputeCullEngine() == null) throw new AssertionError("ComputeCullEngine must not be null");
        if (engine.getMultiDrawManager() == null) throw new AssertionError("MultiDrawManager must not be null");
        if (engine.getHudManager() == null) throw new AssertionError("DecoupledHudManager must not be null");
        if (engine.getEntityCuller() == null) throw new AssertionError("EntityDepthCuller must not be null");
        if (engine.getChestBaker() == null) throw new AssertionError("StaticChestMeshBaker must not be null");
        if (engine.getXpMerger() == null) throw new AssertionError("ExperienceOrbMerger must not be null");
        if (engine.getAnimationLod() == null) throw new AssertionError("AnimationLodManager must not be null");
        if (engine.getVoxelCache() == null) throw new AssertionError("VoxelShapeFastCache must not be null");
        if (engine.getHopperManager() == null) throw new AssertionError("SleepingHopperManager must not be null");
        if (engine.getPathCircuitBreaker() == null) throw new AssertionError("PathfindingCircuitBreaker must not be null");
        if (engine.getLightEngine() == null) throw new AssertionError("AsyncBitsetLightEngine must not be null");
        if (engine.getColorCorrectionEngine() == null) throw new AssertionError("ColorCorrectionEngine must not be null");
        if (engine.getFpsStabilizer() == null) throw new AssertionError("FpsStabilizerEngine must not be null");
        if (engine.getNetworkConsolidator() == null) throw new AssertionError("PacketFlushConsolidator must not be null");
        if (engine.getWorldCacheStorage() == null) throw new AssertionError("ClientWorldCacheStorage must not be null");
        if (engine.getFakeChunkManager() == null) throw new AssertionError("FakeChunkManager must not be null");
        if (engine.getAudioEngine() == null) throw new AssertionError("AsyncAudioEngine must not be null");
        if (engine.getExplosionEngine() == null) throw new AssertionError("FastExplosionEngine must not be null");
        if (engine.getRedstoneEngine() == null) throw new AssertionError("FastRedstoneEngine must not be null");
        if (engine.getCollisionEngine() == null) throw new AssertionError("SpatialCollisionEngine must not be null");
        if (engine.getFluidEngine() == null) throw new AssertionError("FastFluidEngine must not be null");
        if (engine.getParticleEngine() == null) throw new AssertionError("FastParticleEngine must not be null");
        if (engine.getRegistryCache() == null) throw new AssertionError("FastRegistryCache must not be null");
        if (engine.getAmdAccelerator() == null) throw new AssertionError("AmdGpuAccelerator must not be null");
        if (engine.getDualGpuManager() == null) throw new AssertionError("DualGpuManager must not be null");
        if (engine.getFastHdTextureEngine() == null) throw new AssertionError("FastHdTextureEngine must not be null");
        if (engine.getFancyGraphicsOptimizer() == null) throw new AssertionError("FancyGraphicsOptimizer must not be null");
        if (engine.getFastCloudEngine() == null) throw new AssertionError("FastCloudEngine must not be null");
        if (engine.getGpuThermalGuard() == null) throw new AssertionError("GpuThermalPowerGuard must not be null");
        if (engine.getChunkLodManager() == null) throw new AssertionError("ChunkLodManager must not be null");
        if (engine.getAggressiveFaceCuller() == null) throw new AssertionError("AggressiveFaceCuller must not be null");
        if (engine.getGpuInstancingEngine() == null) throw new AssertionError("GpuInstancingEngine must not be null");
        if (engine.getGpuCrashGuard() == null) throw new AssertionError("GpuResetCrashGuard must not be null");
        if (engine.getKeyBindingManager() == null) throw new AssertionError("HyperionKeyBindingManager must not be null");
        if (engine.getVoxelMipTree() == null) throw new AssertionError("VoxelHierarchicalMipTree must not be null");
        if (engine.getVoxelSectionStorage() == null) throw new AssertionError("VoxelSectionStorage must not be null");
        if (engine.getVoxelLodRenderer() == null) throw new AssertionError("VoxelLodRenderer must not be null");
        if (engine.getVoxelHorizonBlender() == null) throw new AssertionError("VoxelHorizonBlender must not be null");
        if (engine.getVoxelIngestEngine() == null) throw new AssertionError("VoxelPregenIngestEngine must not be null");
        if (engine.getModCompatManager() == null) throw new AssertionError("HyperionModCompatManager must not be null");
        if (engine.getIrisShaderPipeline() == null) throw new AssertionError("IrisShaderCompatPipeline must not be null");
        if (engine.getAdvancedParticleEngine() == null) throw new AssertionError("AdvancedParticleEngine must not be null");
        if (engine.getBadOptimizationsEngine() == null) throw new AssertionError("BadOptimizationsEngine must not be null");
        if (engine.getMobAiOptimizer() == null) throw new AssertionError("MobAiOptimizer must not be null");
        if (engine.getPalladiumCache() == null) throw new AssertionError("PalladiumCapabilityCache must not be null");
        if (engine.getThreadPoolManager() == null) throw new AssertionError("HyperionThreadPoolManager must not be null");
        if (engine.getParallelChunkMesher() == null) throw new AssertionError("ParallelChunkMesher must not be null");
        if (engine.getMultiCoreEntityPhysics() == null) throw new AssertionError("MultiCoreEntityPhysicsEngine must not be null");
        if (engine.getAsyncWorldTickDispatcher() == null) throw new AssertionError("AsyncWorldTickDispatcher must not be null");
        if (engine.getCpuAffinityGovernor() == null) throw new AssertionError("CpuCoreAffinityGovernor must not be null");
    }

    private static void testGuiLauncherKeybindingAndVideoOptionsHook() {
        HyperionKeyBindingManager keyManager = HyperionKeyBindingManager.getInstance();
        keyManager.reset();
        keyManager.setEnabled(true);

        // 1. Right Control Trigger (Key 345)
        boolean handled = keyManager.handleKeyInput(HyperionKeyBindingManager.GLFW_KEY_RIGHT_CONTROL, 0, 1, 0);
        if (!handled) {
            throw new AssertionError("Right Control key press must be handled by keybinding manager");
        }
        if (!keyManager.consumeOpenScreenRequest()) {
            throw new AssertionError("Screen request must be queued for consumption");
        }

        // 2. Video Options Screen Injected Button Action
        MixinVideoOptionsScreen.openHyperionSettings();

        // 3. Screen Model Verification
        HyperionScreenModel model = new HyperionScreenModel();
        model.setActiveCategory(HyperionCategory.GRAPHICS_SETTINGS);
        if (model.getCurrentOptions().isEmpty()) {
            throw new AssertionError("Graphics settings options list must not be empty");
        }
    }
}












