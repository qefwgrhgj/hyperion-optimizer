package com.hyperion.optimizer.core.gpu;

/**
 * 💻 Multi-Vendor GPU Architecture Profiles.
 *
 * Provides tailored hardware pipelines for:
 * 1. AMD_RADEON_HYBRID: Polaris/Vega/RDNA with Wavefront 64/32, Primitive Discard, VRAM 2GB budget guard.
 * 2. NVIDIA_INTEL_OPTIMUS: NVIDIA GeForce discrete GPU paired with Intel HD/Iris/Arc integrated GPU.
 *    - Reduces PCIe cross-bus bandwidth copying, uses shared DirectX/OpenGL interop or PBO transfers.
 * 3. APPLE_SILICON_M_SERIES: Apple M1/M2/M3/M4 processors.
 *    - Leverages Unified Memory Architecture (UMA) for 100% Zero-Copy RAM/VRAM access.
 *    - Optimizes for Tile-Based Deferred Rendering (TBDR) on Apple Metal / MoltenVK.
 * 4. INTEL_ARC_DEDICATED: Intel Arc A-Series discrete GPUs (A380, A750, A770) with Xe Matrix Extensions.
 * 5. GENERIC_UNIVERSAL: Safe cross-vendor fallback.
 */
public enum GpuVendorProfile {
    AUTO("Авто-определение вендора", true, true, false),
    AMD_RADEON_HYBRID("AMD Radeon + Vega APU (Amdium)", true, true, false),
    NVIDIA_INTEL_OPTIMUS("NVIDIA GeForce + Intel HD/Iris (Optimus)", true, false, false),
    APPLE_SILICON_M_SERIES("Apple Silicon M-Серия (M1/M2/M3/M4, UMA TBDR)", false, true, true),
    INTEL_ARC_DEDICATED("Intel Arc Dedicated GPU (Xe-HPG)", false, false, false),
    GENERIC_UNIVERSAL("Универсальный профиль (OpenGL Core)", false, false, false);

    private final String displayName;
    private final boolean supportsPrimitiveDiscard;
    private final boolean supportsUmaZeroCopy;
    private final boolean isAppleSiliconTbdr;

    GpuVendorProfile(String displayName, boolean supportsPrimitiveDiscard, boolean supportsUmaZeroCopy, boolean isAppleSiliconTbdr) {
        this.displayName = displayName;
        this.supportsPrimitiveDiscard = supportsPrimitiveDiscard;
        this.supportsUmaZeroCopy = supportsUmaZeroCopy;
        this.isAppleSiliconTbdr = isAppleSiliconTbdr;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isSupportsPrimitiveDiscard() {
        return supportsPrimitiveDiscard;
    }

    public boolean isSupportsUmaZeroCopy() {
        return supportsUmaZeroCopy;
    }

    public boolean isAppleSiliconTbdr() {
        return isAppleSiliconTbdr;
    }
}
