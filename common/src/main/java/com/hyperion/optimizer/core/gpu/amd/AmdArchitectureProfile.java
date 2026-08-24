package com.hyperion.optimizer.core.gpu.amd;

public enum AmdArchitectureProfile {
    AUTO("Автоопределение AMD", "Автоматически определяет архитектуру установленной видеокарты AMD"),
    RADEON_RX500_POLARIS("AMD Radeon RX 500+ (Polaris)", "Оптимизации для RX 550, 560, 570, 580, 590: Wave64, Primitive Discard, Indirect Count"),
    RADEON_540_LEXA("AMD Radeon 540 / 540X (Lexa)", "Оптимизации для Radeon 540: Wave64, 2GB VRAM Budget Guard, пакеты 16K-32K"),
    RADEON_VEGA_8_APU("AMD Radeon(TM) Vega 8 Graphics (APU)", "Оптимизации для встроенной Vega 8: GCN 5.0, UMA Zero-Copy, FP16 Rapid Packed Math"),
    RDNA_MODERN("AMD Radeon RDNA 1/2/3/3.5 (RX 5000-8000)", "Оптимизации для RDNA: Wave32, Subgroup Ballot, Hardware Mesh Shaders"),
    GENERIC_FALLBACK("Стандартный режим (Fallback)", "Базовые расширения OpenGL 4.5 без фирменных инструкций AMD");

    private final String displayName;
    private final String description;

    AmdArchitectureProfile(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static AmdArchitectureProfile detectFromRenderer(String renderer) {
        if (renderer == null || renderer.trim().isEmpty()) {
            return GENERIC_FALLBACK;
        }
        String lower = renderer.toLowerCase();

        // 1. Check Vega 8 / APU
        if (lower.contains("vega 8") || lower.contains("vega8") || (lower.contains("vega") && lower.contains("graphics"))) {
            return RADEON_VEGA_8_APU;
        }

        // 2. Check Radeon 540 / 540X / 550X (Lexa)
        if (lower.contains("radeon 540") || lower.contains("radeon 540x") || lower.contains("radeon 550x") || lower.contains("lexa")) {
            return RADEON_540_LEXA;
        }

        // 3. Check RX 500 Series (Polaris)
        if (lower.contains("rx 580") || lower.contains("rx 570") || lower.contains("rx 590") ||
            lower.contains("rx 560") || lower.contains("rx 550") || lower.contains("polaris") ||
            lower.contains("ellesmere") || lower.contains("baffin")) {
            return RADEON_RX500_POLARIS;
        }

        // 4. Check RDNA Modern (RX 5000, 6000, 7000, 8000, 680M, 780M)
        if (lower.contains("rx 6") || lower.contains("rx 7") || lower.contains("rx 5") ||
            lower.contains("rx 8") || lower.contains("rdna") || lower.contains("780m") || lower.contains("680m")) {
            return RDNA_MODERN;
        }

        // 5. Generic AMD
        if (lower.contains("amd") || lower.contains("radeon")) {
            return RADEON_RX500_POLARIS;
        }

        return GENERIC_FALLBACK;
    }
}
