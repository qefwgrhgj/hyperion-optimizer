package com.hyperion.optimizer.gui;

public enum HyperionCategory {
    GRAPHICS_SETTINGS("🖥️ 1. Настройки графики", "Настройки аппаратного GPU-Driven рендеринга, окклюзии, FBO буфера интерфейса, частиц, стабилизатора 350+ FPS, HDR и цветокоррекции"),
    GPU_VIDEO_SETTINGS("🎮 2. Настройки видеокарт", "Аппаратное ускорение AMD Radeon (Wavefront, VRAM Guard, UMA), Dual-GPU гибридный режим (dGPU+iGPU), выбор видеокарт и конвейеров"),
    CPU_PROCESSOR_SETTINGS("🧠 3. Настройки процессора", "Многоядерность и многопоточность: параллельный мешинг чанков на всех ядрах, тик мобов, асинхронный Starlight свет, SIMD и приоритеты потоков"),
    WORLD_LIGHTING("🌍 Мир и Освещение", "Настройки асинхронного света Starlight/Phosphor, кэша чанков Bobby и физики жидкостей"),
    PHYSICS_REDSTONE("⚡ Физика и Механика", "Оптимизация редстоуна, спящих воронок, взрывов и поиска путей мобов"),
    ENTITIES_ANIMATIONS("👾 Сущности и Анимации", "Отсечение мобов за стенами, LOD анимаций, слияние сфер опыта и сундуки"),
    NETWORK_MEMORY_AUDIO("📡 Сеть, Память и Аудио", "Консолидация сетевых пакетов, быстрое математическое LUT-кэширование и асинхронный звук"),
    COLOR_CORRECTION("🎨 Цветокоррекция и HDR", "Устранение перетемнений (Anti-Black-Crush), тонемаппинг ACES Filmic, сочные цвета и устранение бандинга");

    private final String title;
    private final String description;

    HyperionCategory(String title, String description) {
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
