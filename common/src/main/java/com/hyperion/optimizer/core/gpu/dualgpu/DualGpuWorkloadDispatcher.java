package com.hyperion.optimizer.core.gpu.dualgpu;

public enum DualGpuWorkloadDispatcher {
    OFF("Отключено (Одна видеокарта)", "Используется только одна основная видеокарта"),
    AUTO_BALANCED("Авто-балансировка (Рекомендуется)", "dGPU: 3D мир и чанки; iGPU (Vega 8 / Intel): 2D интерфейс, асинхронный свет и частицы"),
    DEDICATED_IGPU_HUD_LIGHT("Встройка для интерфейса и света", "Жесткий оффлоад FBO интерфейса и Starlight света на iGPU"),
    CUSTOM("Пользовательский режим", "Ручная настройка распределения задач между видеокартами");

    private final String displayName;
    private final String description;

    DualGpuWorkloadDispatcher(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
