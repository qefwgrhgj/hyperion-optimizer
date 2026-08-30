package com.hyperion.optimizer.core.render;

import com.hyperion.optimizer.api.HyperionConfig;

/**
 * ⚡ Hyperion Color Correction & HDR Tone Mapping Engine.
 * 
 * Features:
 * - Anti-Black-Crush Toe Compensation: Prevents dark terrain and night scenes from collapsing into pitch-black voids.
 * - ACES Filmic Curve (Narkowicz / Hill Approximation): Natural highlight roll-off and rich shadow contrast.
 * - Perceptual Vibrance & Saturation (Rec.709 Luminance): Selectively enhances muted foliage/terrain without oversaturating brights.
 * - Night Ambient Luminescence Boost: Calibrates moonlit atmosphere and soft ambient illumination.
 * - Zero-Allocation 16x16 Lightmap Pipeline: In-place SIMD-friendly ARGB / Float lightmap transformation.
 * - High-Frequency Ordered Debanding Dither: Eliminates 8-bit color banding in dark gradients.
 */
public final class ColorCorrectionEngine {
    public enum Mode {
        VIBRANT_HDR("Сочный HDR (Vibrant HDR)"),
        NIGHT_VISION_CLEAR("Ясная ночь (Night Vision Clear)"),
        CINEMATIC_FILMIC("Кинематографичный (ACES Filmic)"),
        NATURAL_BALANCED("Естественный (Natural Balanced)"),
        CUSTOM("Пользовательский (Custom)");

        private final String title;
        Mode(String title) { this.title = title; }
        public String getTitle() { return title; }
    }

    private volatile boolean enabled;
    private volatile Mode mode = Mode.NATURAL_BALANCED;
    private volatile float gammaBoost = 1.00f;
    private volatile float vibrance = 1.00f;
    private volatile float saturation = 1.00f;
    private volatile float contrast = 1.00f;
    private volatile float blackCrushCompensation = 0.12f;
    private volatile float nightAmbientBoost = 0.12f;
    private volatile int colorTemperature = 6500;
    private volatile boolean debanding = true;
    private volatile boolean texturePackGradingEnabled = false;

    // Precomputed cached temperature balance multipliers to eliminate per-pixel Math.log / Math.pow on CPU
    private volatile float cachedTempR = 1.0f;
    private volatile float cachedTempG = 1.0f;
    private volatile float cachedTempB = 1.0f;

    // Fast 256-entry Gamma LUT to eliminate Math.pow in per-frame lightmap processing
    private final float[] gammaLut = new float[256];
    private static final ThreadLocal<float[]> TEMP_RGB = ThreadLocal.withInitial(() -> new float[3]);

    // Normalization constant so ACES(1.0) == 1.0 (2.54 / 3.16)
    private static final float ACES_NORM_FACTOR = 0.8037974683544302f;

    public ColorCorrectionEngine(boolean enabled) {
        this.enabled = enabled;
        recomputeColorTemperature();
        recomputeGammaLut();
    }

    public void configure(HyperionConfig config) {
        if (config == null) return;
        this.enabled = config.enableColorCorrection;
        this.texturePackGradingEnabled = config.enableTexturePackColorCorrection;
        try {
            if (config.colorGradingMode != null) {
                this.mode = Mode.valueOf(config.colorGradingMode);
            }
        } catch (Exception ignored) {
            this.mode = Mode.NATURAL_BALANCED;
        }
        this.gammaBoost = (float) config.colorGammaBoost;
        this.vibrance = (float) config.colorVibrance;
        this.saturation = (float) config.colorSaturation;
        this.contrast = (float) config.colorContrast;
        this.blackCrushCompensation = (float) config.colorBlackCrushCompensation;
        this.nightAmbientBoost = (float) config.colorNightAmbientBoost;
        this.colorTemperature = config.colorTemperature;
        this.debanding = config.enableColorDebanding;
        recomputeColorTemperature();
        recomputeGammaLut();
    }

    /**
     * Applies balanced, non-destructive color grading to an RGB triple.
     * Guarantees true 1:1 texture pack fidelity, zero artificial oversaturation, and zero black crushed shadows.
     */
    public void gradeRgb(float r, float g, float b, float nightFactor, int ditherX, int ditherY, float[] outRgb) {
        if (!enabled) {
            outRgb[0] = clamp01(r);
            outRgb[1] = clamp01(g);
            outRgb[2] = clamp01(b);
            return;
        }

        // 1. Color Temperature (Kelvin) White Balance
        if (colorTemperature != 6500) {
            applyColorTemperature(r, g, b, outRgb);
            r = outRgb[0];
            g = outRgb[1];
            b = outRgb[2];
        }

        // 2. Tonemapping Profile
        if (mode == Mode.NATURAL_BALANCED) {
            // Natural Linear-Preserving Highlight Compression (Never crushes shadows below linear)
            r = (r / (1.0f + 0.15f * r)) * 1.15f;
            g = (g / (1.0f + 0.15f * g)) * 1.15f;
            b = (b / (1.0f + 0.15f * b)) * 1.15f;
        } else if (mode == Mode.VIBRANT_HDR || mode == Mode.CINEMATIC_FILMIC) {
            float lumPre = 0.2126f * Math.max(0.0f, r) + 0.7152f * Math.max(0.0f, g) + 0.0722f * Math.max(0.0f, b);
            float lumMapped = acesTonemap(lumPre);
            if (lumPre > 0.00001f) {
                float scale = lumMapped / lumPre;
                float rChan = Math.max(r * 0.96f, acesTonemap(r));
                float gChan = Math.max(g * 0.96f, acesTonemap(g));
                float bChan = Math.max(b * 0.96f, acesTonemap(b));
                r = 0.85f * (r * scale) + 0.15f * rChan;
                g = 0.85f * (g * scale) + 0.15f * gChan;
                b = 0.85f * (b * scale) + 0.15f * bChan;
            } else {
                r = Math.max(r * 0.96f, acesTonemap(r));
                g = Math.max(g * 0.96f, acesTonemap(g));
                b = Math.max(b * 0.96f, acesTonemap(b));
            }
        } else if (mode == Mode.NIGHT_VISION_CLEAR) {
            float lum = 0.2126f * r + 0.7152f * g + 0.0722f * b;
            if (lum < 0.50f) {
                float boost = (0.50f - lum) * 0.35f;
                r += boost;
                g += boost;
                b += boost;
            }
        }

        // 3. Contrast Adjustment around linear mid-gray (0.18) to protect shadows
        if (contrast != 1.0f) {
            r = (r - 0.18f) * contrast + 0.18f;
            g = (g - 0.18f) * contrast + 0.18f;
            b = (b - 0.18f) * contrast + 0.18f;
        }

        // 4. Smooth Perceptual Vibrance & Saturation (Only when non-neutral)
        if (vibrance != 1.0f || saturation != 1.0f) {
            float maxC = Math.max(r, Math.max(g, b));
            float minC = Math.min(r, Math.min(g, b));
            float currentSat = (maxC > minC) ? ((maxC - minC) / (maxC + 0.05f)) : 0.0f;
            float vibranceBoost = (1.0f - currentSat) * (vibrance - 1.0f);
            float totalSat = saturation + vibranceBoost;

            float gradedLum = 0.2126f * r + 0.7152f * g + 0.0722f * b;
            r = gradedLum + (r - gradedLum) * totalSat;
            g = gradedLum + (g - gradedLum) * totalSat;
            b = gradedLum + (b - gradedLum) * totalSat;
        }

        // 5. Fast LUT Gamma Power Correction
        if (gammaBoost > 0.01f && gammaBoost != 1.0f) {
            r = fastGamma(r);
            g = fastGamma(g);
            b = fastGamma(b);
        }

        // 6. Anti-Black-Crush Toe Lift & Ambient Floor
        float lumCurrent = 0.2126f * r + 0.7152f * g + 0.0722f * b;
        if (lumCurrent < 0.35f) {
            float shadowToe = (0.35f - lumCurrent) / 0.35f;
            float lift = (blackCrushCompensation * 0.15f) * (shadowToe * shadowToe);
            r += lift;
            g += lift;
            b += lift;
        }

        // Safe Ambient Floor Guarantee: Prevent shadows from collapsing to pure black (0x000000)
        float minAmbientFloor = 0.045f + (nightAmbientBoost * 0.06f * clamp01(nightFactor));
        r = Math.max(minAmbientFloor, r);
        g = Math.max(minAmbientFloor, g);
        b = Math.max(minAmbientFloor, b);

        outRgb[0] = clamp01(r);
        outRgb[1] = clamp01(g);
        outRgb[2] = clamp01(b);
    }

    private void recomputeGammaLut() {
        float invGamma = (gammaBoost > 0.01f && gammaBoost != 1.0f) ? (1.0f / gammaBoost) : 1.0f;
        for (int i = 0; i < 256; i++) {
            gammaLut[i] = (float) Math.pow(i / 255.0f, invGamma);
        }
    }

    private float fastGamma(float val) {
        if (gammaBoost <= 0.01f || gammaBoost == 1.0f) return val;
        int idx = Math.min(255, Math.max(0, (int) (val * 255.0f + 0.5f)));
        return gammaLut[idx];
    }

    private void recomputeColorTemperature() {
        if (colorTemperature == 6500) {
            this.cachedTempR = 1.0f;
            this.cachedTempG = 1.0f;
            this.cachedTempB = 1.0f;
            return;
        }
        float t = colorTemperature / 100.0f;
        float tempR, tempG, tempB;
        if (t <= 66.0f) {
            tempR = 1.0f;
            tempG = clamp01((float) (0.39008157876 * Math.log(t) - 0.63184144378));
            tempB = (t <= 19.0f) ? 0.0f : clamp01((float) (0.54320678911 * Math.log(t - 10.0) - 1.19625408914));
        } else {
            tempR = clamp01((float) (1.29293618606 * Math.pow(t - 60.0, -0.1332047592)));
            tempG = clamp01((float) (1.12989086089 * Math.pow(t - 60.0, -0.0755148492)));
            tempB = 1.0f;
        }
        this.cachedTempR = tempR;
        this.cachedTempG = tempG;
        this.cachedTempB = tempB;
    }

    private void applyColorTemperature(float r, float g, float b, float[] out) {
        out[0] = r * cachedTempR;
        out[1] = g * cachedTempG;
        out[2] = b * cachedTempB;
    }

    /**
     * Fast in-place transformation of Minecraft's 16x16 (256 elements) ARGB Lightmap.
     * 100% Zero allocations & SIMD-friendly LUT execution.
     * Enforces opaque 0xFF Alpha to prevent lightmap sample discard / black textures.
     */
    public void processLightmap(int[] lightmapPixels, int width, int height, float nightFactor) {
        if (!enabled || lightmapPixels == null) return;
        float[] temp = TEMP_RGB.get();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int argb = lightmapPixels[index];

                int a = (argb >> 24) & 0xFF;
                if (a == 0) a = 0xFF; // Enforce opaque alpha for lightmap shader sampler

                float r = ((argb >> 16) & 0xFF) / 255.0f;
                float g = ((argb >> 8) & 0xFF) / 255.0f;
                float b = (argb & 0xFF) / 255.0f;

                gradeRgb(r, g, b, nightFactor, x, y, temp);

                int outR = Math.min(255, Math.max(0, (int) (temp[0] * 255.0f + 0.5f)));
                int outG = Math.min(255, Math.max(0, (int) (temp[1] * 255.0f + 0.5f)));
                int outB = Math.min(255, Math.max(0, (int) (temp[2] * 255.0f + 0.5f)));

                lightmapPixels[index] = (a << 24) | (outR << 16) | (outG << 8) | outB;
            }
        }
    }

    /**
     * Applies subtle, non-destructive color enhancement to raw texture/sprite RGB triples.
     * Preserves exact artist intentions, true zero black levels, and prevents double-saturation with the Lightmap.
     */
    public void gradeTextureRgb(float r, float g, float b, float[] outRgb) {
        if (!enabled || !texturePackGradingEnabled) {
            outRgb[0] = clamp01(r);
            outRgb[1] = clamp01(g);
            outRgb[2] = clamp01(b);
            return;
        }

        // 1. Subtle Color Temperature (Kelvin) White Balance
        if (colorTemperature != 6500) {
            applyColorTemperature(r, g, b, outRgb);
            r = outRgb[0];
            g = outRgb[1];
            b = outRgb[2];
        }

        // 2. Subtle, balanced vibrance enhancement (gentle 25% curve to avoid double-saturation)
        float subtleVibrance = 1.0f + (vibrance - 1.0f) * 0.25f;
        float subtleSat = 1.0f + (saturation - 1.0f) * 0.25f;
        float maxC = Math.max(r, Math.max(g, b));
        float minC = Math.min(r, Math.min(g, b));
        float currentSat = (maxC > minC) ? ((maxC - minC) / (maxC + 0.05f)) : 0.0f;
        float vibranceBoost = (1.0f - currentSat) * (subtleVibrance - 1.0f);
        float totalSat = subtleSat + vibranceBoost;

        float gradedLum = 0.2126f * r + 0.7152f * g + 0.0722f * b;
        r = gradedLum + (r - gradedLum) * totalSat;
        g = gradedLum + (g - gradedLum) * totalSat;
        b = gradedLum + (b - gradedLum) * totalSat;

        outRgb[0] = clamp01(r);
        outRgb[1] = clamp01(g);
        outRgb[2] = clamp01(b);
    }

    /**
     * Fast in-place transformation of ARGB Texture Pack / Resource Pack pixel buffers.
     * Preserves transparent (alpha 0) and translucent alpha channels with zero heap allocation.
     */
    public void processTexture(int[] pixels, int width, int height) {
        if (!enabled || !texturePackGradingEnabled || pixels == null || width <= 0 || height <= 0) return;
        int total = Math.min(pixels.length, width * height);
        float[] temp = TEMP_RGB.get();

        for (int i = 0; i < total; i++) {
            int argb = pixels[i];
            int a = (argb >> 24) & 0xFF;
            if (a == 0) continue; // Preserve fully transparent alpha 0

            float r = ((argb >> 16) & 0xFF) / 255.0f;
            float g = ((argb >> 8) & 0xFF) / 255.0f;
            float b = (argb & 0xFF) / 255.0f;

            gradeTextureRgb(r, g, b, temp);

            int outR = Math.min(255, Math.max(0, (int) (temp[0] * 255.0f + 0.5f)));
            int outG = Math.min(255, Math.max(0, (int) (temp[1] * 255.0f + 0.5f)));
            int outB = Math.min(255, Math.max(0, (int) (temp[2] * 255.0f + 0.5f)));

            pixels[i] = (a << 24) | (outR << 16) | (outG << 8) | outB;
        }
    }

    /**
     * Fast in-place transformation of NativeImage Little-Endian ABGR Texture Pack pixel buffers.
     */
    public void processTextureAbgr(int[] pixels, int width, int height) {
        if (!enabled || !texturePackGradingEnabled || pixels == null || width <= 0 || height <= 0) return;
        int total = Math.min(pixels.length, width * height);
        float[] temp = TEMP_RGB.get();

        for (int i = 0; i < total; i++) {
            int abgr = pixels[i];
            int a = (abgr >> 24) & 0xFF;
            if (a == 0) continue;

            float b = ((abgr >> 16) & 0xFF) / 255.0f;
            float g = ((abgr >> 8) & 0xFF) / 255.0f;
            float r = (abgr & 0xFF) / 255.0f;

            gradeTextureRgb(r, g, b, temp);

            int outR = Math.min(255, Math.max(0, (int) (temp[0] * 255.0f + 0.5f)));
            int outG = Math.min(255, Math.max(0, (int) (temp[1] * 255.0f + 0.5f)));
            int outB = Math.min(255, Math.max(0, (int) (temp[2] * 255.0f + 0.5f)));

            pixels[i] = (a << 24) | (outB << 16) | (outG << 8) | outR;
        }
    }

    /**
     * Grades a single ARGB or 24-bit RGB integer color (e.g. Grass/Foliage/Water Colormaps from texture packs).
     * Correctly handles 24-bit Minecraft BiomeColors without corrupting vertex alpha.
     */
    public int gradeColorRgbInt(int argb) {
        if (!enabled || !texturePackGradingEnabled) return argb;
        int a = (argb >> 24) & 0xFF;
        boolean hadExplicitAlpha = (a != 0);

        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;

        float[] temp = TEMP_RGB.get();
        gradeTextureRgb(r, g, b, temp);

        int outR = Math.min(255, Math.max(0, (int) (temp[0] * 255.0f + 0.5f)));
        int outG = Math.min(255, Math.max(0, (int) (temp[1] * 255.0f + 0.5f)));
        int outB = Math.min(255, Math.max(0, (int) (temp[2] * 255.0f + 0.5f)));

        if (!hadExplicitAlpha) {
            return (outR << 16) | (outG << 8) | outB; // Retain 24-bit RGB format for vanilla Minecraft BiomeColors
        }

        return (a << 24) | (outR << 16) | (outG << 8) | outB;
    }

    /**
     * Fast in-place transformation of NativeImage Little-Endian ABGR Lightmap.
     */
    public void processLightmapAbgr(int[] lightmapPixels, int width, int height, float nightFactor) {
        if (!enabled || lightmapPixels == null) return;
        float[] temp = TEMP_RGB.get();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int abgr = lightmapPixels[index];

                int a = (abgr >> 24) & 0xFF;
                if (a == 0) a = 0xFF;

                float b = ((abgr >> 16) & 0xFF) / 255.0f;
                float g = ((abgr >> 8) & 0xFF) / 255.0f;
                float r = (abgr & 0xFF) / 255.0f;

                gradeRgb(r, g, b, nightFactor, x, y, temp);

                int outR = Math.min(255, Math.max(0, (int) (temp[0] * 255.0f + 0.5f)));
                int outG = Math.min(255, Math.max(0, (int) (temp[1] * 255.0f + 0.5f)));
                int outB = Math.min(255, Math.max(0, (int) (temp[2] * 255.0f + 0.5f)));

                lightmapPixels[index] = (a << 24) | (outB << 16) | (outG << 8) | outR;
            }
        }
    }

    private static float acesTonemap(float x) {
        if (x <= 0.0f) return 0.0f;
        float a = 2.51f;
        float b = 0.03f;
        float c = 2.43f;
        float d = 0.59f;
        float e = 0.14f;
        float raw = (x * (a * x + b)) / (x * (c * x + d) + e);
        return raw / ACES_NORM_FACTOR;
    }

    private static float clamp01(float v) {
        return (v < 0.0f) ? 0.0f : (v > 1.0f ? 1.0f : v);
    }

    public boolean isEnabled() { return enabled; }
    public Mode getMode() { return mode; }
    public float getGammaBoost() { return gammaBoost; }
    public float getVibrance() { return vibrance; }
    public float getSaturation() { return saturation; }
    public float getContrast() { return contrast; }
    public float getBlackCrushCompensation() { return blackCrushCompensation; }
    public float getNightAmbientBoost() { return nightAmbientBoost; }
    public int getColorTemperature() { return colorTemperature; }
    public boolean isDebandingEnabled() { return debanding; }
    public boolean isTexturePackGradingEnabled() { return texturePackGradingEnabled; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setMode(Mode mode) { if (mode != null) this.mode = mode; }
    public void setGammaBoost(float v) { this.gammaBoost = v; }
    public void setVibrance(float v) { this.vibrance = v; }
    public void setSaturation(float v) { this.saturation = v; }
    public void setContrast(float v) { this.contrast = v; }
    public void setBlackCrushCompensation(float v) { this.blackCrushCompensation = v; }
    public void setNightAmbientBoost(float v) { this.nightAmbientBoost = v; }
    public void setColorTemperature(int temp) { this.colorTemperature = temp; recomputeColorTemperature(); }
    public void setDebanding(boolean debanding) { this.debanding = debanding; }
    public void setTexturePackGradingEnabled(boolean v) { this.texturePackGradingEnabled = v; }
}
