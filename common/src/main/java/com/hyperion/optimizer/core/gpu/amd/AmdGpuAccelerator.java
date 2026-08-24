package com.hyperion.optimizer.core.gpu.amd;

public final class AmdGpuAccelerator {
    private final boolean enabled;
    private AmdArchitectureProfile activeProfile;
    private int wavefrontSize; // 32 for RDNA, 64 for GCN/Polaris/Vega
    private boolean primitiveDiscardEnabled;
    private boolean indirectParametersEnabled;
    private boolean persistentBuffersEnabled;
    private boolean umaZeroCopyEnabled;
    private boolean fp16PackedMathEnabled;
    private final AmdVramBudgetGuard vramGuard;

    public AmdGpuAccelerator(boolean enabled, AmdArchitectureProfile requestedProfile, boolean enableVramGuard) {
        this.enabled = enabled;
        this.activeProfile = (requestedProfile != null) ? requestedProfile : AmdArchitectureProfile.AUTO;
        this.vramGuard = new AmdVramBudgetGuard(enableVramGuard, 2048); // Default 2GB budget, adjustable
        calibrateProfile(this.activeProfile);
    }

    public void calibrateProfile(AmdArchitectureProfile profile) {
        this.activeProfile = profile;
        if (!enabled || profile == null) {
            this.wavefrontSize = 64;
            this.primitiveDiscardEnabled = false;
            this.indirectParametersEnabled = false;
            this.persistentBuffersEnabled = false;
            this.umaZeroCopyEnabled = false;
            this.fp16PackedMathEnabled = false;
            return;
        }

        switch (profile) {
            case RADEON_RX500_POLARIS:
                this.wavefrontSize = 64;
                this.primitiveDiscardEnabled = true;
                this.indirectParametersEnabled = true;
                this.persistentBuffersEnabled = true;
                this.umaZeroCopyEnabled = false;
                this.fp16PackedMathEnabled = false;
                break;

            case RADEON_540_LEXA:
                this.wavefrontSize = 64;
                this.primitiveDiscardEnabled = true;
                this.indirectParametersEnabled = true;
                this.persistentBuffersEnabled = true;
                this.umaZeroCopyEnabled = false;
                this.fp16PackedMathEnabled = false;
                break;

            case RADEON_VEGA_8_APU:
                this.wavefrontSize = 64;
                this.primitiveDiscardEnabled = true;
                this.indirectParametersEnabled = true;
                this.persistentBuffersEnabled = true;
                this.umaZeroCopyEnabled = true; // Unified Memory with CPU (System RAM)
                this.fp16PackedMathEnabled = true; // Rapid Packed Math (2x compute speed)
                break;

            case RDNA_MODERN:
                this.wavefrontSize = 32; // RDNA native Wave32
                this.primitiveDiscardEnabled = true;
                this.indirectParametersEnabled = true;
                this.persistentBuffersEnabled = true;
                this.umaZeroCopyEnabled = false;
                this.fp16PackedMathEnabled = true;
                break;

            case AUTO:
            case GENERIC_FALLBACK:
            default:
                this.wavefrontSize = 64;
                this.primitiveDiscardEnabled = true;
                this.indirectParametersEnabled = true;
                this.persistentBuffersEnabled = true;
                this.umaZeroCopyEnabled = false;
                this.fp16PackedMathEnabled = false;
                break;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public AmdArchitectureProfile getActiveProfile() {
        return activeProfile;
    }

    public int getWavefrontSize() {
        return wavefrontSize;
    }

    public boolean isPrimitiveDiscardEnabled() {
        return enabled && primitiveDiscardEnabled;
    }

    public boolean isIndirectParametersEnabled() {
        return enabled && indirectParametersEnabled;
    }

    public boolean isPersistentBuffersEnabled() {
        return enabled && persistentBuffersEnabled;
    }

    public boolean isUmaZeroCopyEnabled() {
        return enabled && umaZeroCopyEnabled;
    }

    public boolean isFp16PackedMathEnabled() {
        return enabled && fp16PackedMathEnabled;
    }

    public AmdVramBudgetGuard getVramGuard() {
        return vramGuard;
    }
}
