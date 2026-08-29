package com.hyperion.optimizer.compat;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 🌈 Iris & Oculus Shader Pack Compatibility Pipeline.
 *
 * Coordinates depth buffer writes and composite pass bindings when shader packs are active:
 * - Directs Voxel LOD geometry into Iris G-Buffer passes (`gbuffers_terrain` / `gbuffers_textured`).
 * - Ensures custom dual-GPU HUD FBO blits are composited AFTER Iris post-processing shaders (`composite_final`),
 *   preventing blurry or incorrectly lit UI elements.
 */
public final class IrisShaderCompatPipeline {
    private static final IrisShaderCompatPipeline INSTANCE = new IrisShaderCompatPipeline();

    private final AtomicBoolean shaderPackActive = new AtomicBoolean(false);
    private final AtomicBoolean shadowPassActive = new AtomicBoolean(false);
    private volatile String currentPassName = "none";

    private IrisShaderCompatPipeline() {}

    public static IrisShaderCompatPipeline getInstance() {
        return INSTANCE;
    }

    public void setShaderPackActive(boolean active) {
        shaderPackActive.set(active);
    }

    public boolean isShaderPackActive() {
        return shaderPackActive.get();
    }

    public void setShadowPassActive(boolean inShadowPass) {
        shadowPassActive.set(inShadowPass);
    }

    public boolean isShadowPassActive() {
        return shadowPassActive.get();
    }

    public void setCurrentPassName(String passName) {
        this.currentPassName = passName != null ? passName : "none";
    }

    public String getCurrentPassName() {
        return currentPassName;
    }

    /**
     * Ensures Decoupled HUD is only composited during or after composite_final pass when shaders are active.
     */
    public boolean shouldRenderDecoupledHudNow() {
        if (!shaderPackActive.get()) return true;
        return "composite_final".equalsIgnoreCase(currentPassName) || "final".equalsIgnoreCase(currentPassName) || "none".equalsIgnoreCase(currentPassName);
    }

    /**
     * Determines whether voxel LOD rendering should proceed given current shader pass.
     */
    public boolean shouldRenderVoxelLodInCurrentPass() {
        if (!shaderPackActive.get()) return true;
        // In shadow pass, distant LODs can be conditionally skipped or simplified to save shadow map render time
        return !shadowPassActive.get();
    }

    public void reset() {
        shaderPackActive.set(false);
        shadowPassActive.set(false);
        currentPassName = "none";
    }
}
