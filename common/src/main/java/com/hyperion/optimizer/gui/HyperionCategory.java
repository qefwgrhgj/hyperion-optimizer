package com.hyperion.optimizer.gui;

public enum HyperionCategory {
    GRAPHICS_SETTINGS("🖥️ 1. Настройки графики", "Настройки аппаратного GPU-Driven рендеринга, окклюзии, FBO буфера интерфейса, частиц, стабилизатора 350+ FPS, HDR и цветокоррекции"),
    GPU_VIDEO_SETTINGS("🎮 2. Настройки видеокарт", "Аппаратное ускорение AMD Radeon (Wavefront, VRAM Guard, UMA), Dual-GPU гибридный режим (dGPU+iGPU), выбор видеокарт и конвейеров"),
    CPU_PROCESSOR_SETTINGS("🧠 3. Настройки процессора", "Многоядерность и многопоточность: параллельный мешинг чанков на всех ядрах, тик мобов, асинхронный расчет света, SIMD и приоритеты потоков"),
    VOXEL_LOD_INFINITE("🌲 4. Дальний рендер и Voxel LOD", "Бесконечная дальность прорисовки 2048+ чанков, сжатие вокселей, атмосферный туман и адаптивный LOD"),
    WORLD_LIGHTING("🌍 5. Мир и Освещение", "Настройки асинхронного света, кэша дальних чанков и физики жидкостей"),
    PHYSICS_REDSTONE("⚡ 6. Физика и Механика", "Оптимизация редстоуна, спящих воронок, взрывов и поиска путей мобов"),
    ENTITIES_ANIMATIONS("👾 7. Сущности и Анимации", "Отсечение мобов за стенами, LOD анимаций, слияние сфер опыта и сундуки"),
    NETWORK_MEMORY_AUDIO("📡 8. Сеть, Память и Аудио", "Консолидация сетевых пакетов, быстрое математическое LUT-кэширование и асинхронный звук"),
    COLOR_CORRECTION("🎨 9. Цветокоррекция и HDR", "Устранение перетемнений (Anti-Black-Crush), тонемаппинг ACES Filmic, сочные цвета и устранение бандинга"),
    ADVANCED_TWEAKS("⚙️ 10. Тонкие системные твики", "Аппаратные лимиты VRAM, размеры батчей инстансинга, параметры коллизий и продвинутые оптимизации");

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
