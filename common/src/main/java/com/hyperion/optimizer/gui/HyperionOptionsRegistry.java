package com.hyperion.optimizer.gui;

import com.hyperion.optimizer.api.HyperionConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class HyperionOptionsRegistry {
    private static final List<HyperionOption<?>> ALL_OPTIONS = new ArrayList<>();
    private static final Map<HyperionCategory, List<HyperionOption<?>>> CATEGORY_MAP = new EnumMap<>(HyperionCategory.class);

    static {
        for (HyperionCategory cat : HyperionCategory.values()) {
            CATEGORY_MAP.put(cat, new ArrayList<>());
        }

        // =========================================================================
        // 1. GRAPHICS SETTINGS (Настройки графики)
        // =========================================================================
        register(HyperionOption.createBoolean(
            "enableGpuDrivenRenderer",
            "Аппаратный GPU-Рендеринг (Amdium)",
            "Расчет видимости чанков на GPU через Compute Shaders (+100-300% FPS на AMD/Intel/Nvidia)",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.enableGpuDrivenRenderer,
            (c, v) -> c.enableGpuDrivenRenderer = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableHiZOcclusionCulling",
            "Иерархическая Z-Окклюзия (Hi-Z)",
            "Аппаратное отсечение невидимой геометрии пещер и скрытых чанков",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.enableHiZOcclusionCulling,
            (c, v) -> c.enableHiZOcclusionCulling = v,
            true
        ));
        register(HyperionOption.createIntSlider(
            "maxGpuIndirectDrawBatchSize",
            "Размер пакета GPU Indirect Draw",
            "Количество команд отрисовки, передаваемых за один Draw Call",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.maxGpuIndirectDrawBatchSize,
            (c, v) -> c.maxGpuIndirectDrawBatchSize = v,
            65536, 16384, 131072, 16384
        ));
        register(HyperionOption.createBoolean(
            "enableDecoupledHud",
            "Кэш интерфейса в FBO (F1 Mode)",
            "Рендеринг интерфейса в отдельный буфер с оптимизацией перерисовки (+25-50% FPS)",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.enableDecoupledHud,
            (c, v) -> c.enableDecoupledHud = v,
            true
        ));
        register(HyperionOption.createIntSlider(
            "hudTargetFramerate",
            "Частота обновления интерфейса (FPS)",
            "Целевой FPS для отрисовки статических элементов HUD",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.hudTargetFramerate,
            (c, v) -> c.hudTargetFramerate = v,
            60, 30, 144, 15
        ));
        register(HyperionOption.createBoolean(
            "enableFastParticleEngine",
            "Лимитер частиц на блок",
            "Предотвращает падение FPS от спама частиц лавы, зелий и редстоуна",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.enableFastParticleEngine,
            (c, v) -> c.enableFastParticleEngine = v,
            true
        ));
        register(HyperionOption.createIntSlider(
            "maxParticlesPerBlockPerSecond",
            "Макс. частиц на блок/сек",
            "Ограничение генерации частиц на один воксель",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.maxParticlesPerBlockPerSecond,
            (c, v) -> c.maxParticlesPerBlockPerSecond = v,
            5, 1, 30, 1
        ));
        register(HyperionOption.createBoolean(
            "enableFpsStabilizer",
            "Стабилизатор FPS (350 FPS Frame Pacing)",
            "Устраняет падение FPS с 350 до 60 при входе в прогруженные чанки, динамически бюджетируя нагрузку",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.enableFpsStabilizer,
            (c, v) -> c.enableFpsStabilizer = v,
            true
        ));
        register(HyperionOption.createIntSlider(
            "targetFramerate",
            "Целевой стабильный FPS",
            "Целевая частота кадров для динамического распределителя нагрузки",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.targetFramerate,
            (c, v) -> c.targetFramerate = v,
            350, 60, 500, 25
        ));
        register(HyperionOption.createIntSlider(
            "maxChunkUploadsPerFrame",
            "Лимит загрузки мешей чанков / кадр",
            "Предотвращает зависания конвейера OpenGL при перемещении в загруженные чанки",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.maxChunkUploadsPerFrame,
            (c, v) -> c.maxChunkUploadsPerFrame = v,
            3, 1, 10, 1
        ));
        register(HyperionOption.createBoolean(
            "enableAggressiveCaveCulling",
            "Агрессивное отсечение пещер и невидимых блоков",
            "Мгновенно сбрасывает подземные чанк-секции без прямой видимости (+40-80% FPS в нагруженных чанках)",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.enableAggressiveCaveCulling,
            (c, v) -> c.enableAggressiveCaveCulling = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableBlockEntityDistanceCulling",
            "Дистанционное отсечение сундуков и плиток",
            "Не нагружает рендер сотнями статичных сундуков и печек на расстоянии",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.enableBlockEntityDistanceCulling,
            (c, v) -> c.enableBlockEntityDistanceCulling = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableColorCorrection",
            "Цветокоррекция и HDR Tone Mapping",
            "Кинематографический тонемаппинг ACES, насыщение и устранение перетемнений теней",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.enableColorCorrection,
            (c, v) -> c.enableColorCorrection = v,
            true
        ));
        register(HyperionOption.createCycle(
            "colorGradingMode",
            "Профиль цветокоррекции",
            "Цветовая палитра рендера мира",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.colorGradingMode,
            (c, v) -> c.colorGradingMode = v,
            new String[]{"VIBRANT_HDR", "NIGHT_VISION_CLEAR", "CINEMATIC_FILMIC", "NATURAL_BALANCED", "CUSTOM"},
            new String[]{"Vibrant HDR (Яркий)", "Clear Night (Ночной обзор)", "Cinematic Filmic (Кино)", "Natural Balanced (Натуральный)", "Custom (Пользовательский)"},
            "VIBRANT_HDR"
        ));
        register(HyperionOption.createDoubleSlider(
            "colorBlackCrushCompensation",
            "Компенсация черного (Anti-Black-Crush)",
            "Осветляет глухие черные тени в пещерах и ночью без потери контрастности",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.colorBlackCrushCompensation,
            (c, v) -> c.colorBlackCrushCompensation = v,
            0.08, 0.0, 0.30, 0.01
        ));

        // =========================================================================
        // 2. VIDEO CARD / GPU SETTINGS (Настройки видеокарт)
        // =========================================================================
        register(HyperionOption.createBoolean(
            "enableAmdHardwareAcceleration",
            "Аппаратное ускорение AMD («Amdium»)",
            "Включает специализированный GPU-Driven конвейер для AMD Radeon RX 500+, 540, Vega 8 и RDNA",
            HyperionCategory.GPU_VIDEO_SETTINGS,
            c -> c.enableAmdHardwareAcceleration,
            (c, v) -> c.enableAmdHardwareAcceleration = v,
            true
        ));
        register(HyperionOption.createCycle(
            "amdArchitectureProfile",
            "Архитектура видеокарты AMD",
            "Оптимизация под микроархитектуру GPU (Wavefront 64/32, Primitive Discard)",
            HyperionCategory.GPU_VIDEO_SETTINGS,
            c -> c.amdArchitectureProfile,
            (c, v) -> c.amdArchitectureProfile = v,
            new String[]{"AUTO", "RADEON_RX500_POLARIS", "RADEON_540_LEXA", "RADEON_VEGA_8_APU", "RDNA_MODERN"},
            new String[]{"Auto Detect (Авто)", "RX 400/500 Polaris", "Radeon 540/550 Lexa", "Vega 8 / APU Ryzen", "RDNA 1/2/3/4"},
            "AUTO"
        ));
        register(HyperionOption.createBoolean(
            "enableAmdPrimitiveDiscard",
            "Primitive Discard Accelerator (RX 500 / 540)",
            "Аппаратное отсечение пустых полигонов на уровне Compute Unit в Polaris/Vega",
            HyperionCategory.GPU_VIDEO_SETTINGS,
            c -> c.enableAmdPrimitiveDiscard,
            (c, v) -> c.enableAmdPrimitiveDiscard = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableAmdMultiDrawIndirectCount",
            "GL_ARB_indirect_parameters (MultiDraw Count)",
            "Позволяет видеокарте AMD динамически определять количество отрисовываемых чанков прямо из VRAM",
            HyperionCategory.GPU_VIDEO_SETTINGS,
            c -> c.enableAmdMultiDrawIndirectCount,
            (c, v) -> c.enableAmdMultiDrawIndirectCount = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableAmdPersistentCoherentBuffers",
            "Когерентная память VRAM (Persistent Buffers)",
            "Потоковая передача геометрии в видеокарту без синхронизационных задержек CPU",
            HyperionCategory.GPU_VIDEO_SETTINGS,
            c -> c.enableAmdPersistentCoherentBuffers,
            (c, v) -> c.enableAmdPersistentCoherentBuffers = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableAmd2GbVramGuard",
            "Защита VRAM 2GB (Radeon 540 Budget Guard)",
            "Интеллектуальное сжатие буферов и предотвращение вылетов при дальности 32-64 чанка",
            HyperionCategory.GPU_VIDEO_SETTINGS,
            c -> c.enableAmd2GbVramGuard,
            (c, v) -> c.enableAmd2GbVramGuard = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableAmdUmaZeroCopy",
            "UMA Zero-Copy Memory (Vega 8 APU)",
            "Прямой доступ к системной памяти без задержек PCIe для встроенных видеокарт Ryzen",
            HyperionCategory.GPU_VIDEO_SETTINGS,
            c -> c.enableAmdUmaZeroCopy,
            (c, v) -> c.enableAmdUmaZeroCopy = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableDualGpuSupport",
            "Режим двух видеокарт (Dual-GPU)",
            "Одновременное использование дискретной (Radeon 540/RX 580) и встроенной (Vega 8) видеокарт",
            HyperionCategory.GPU_VIDEO_SETTINGS,
            c -> c.enableDualGpuSupport,
            (c, v) -> c.enableDualGpuSupport = v,
            true
        ));
        register(HyperionOption.createCycle(
            "dualGpuMode",
            "Режим распределения Dual-GPU",
            "Распределение нагрузки между дискретной и встроенной видеокартами",
            HyperionCategory.GPU_VIDEO_SETTINGS,
            c -> c.dualGpuMode,
            (c, v) -> c.dualGpuMode = v,
            new String[]{"OFF", "AUTO_BALANCED", "DEDICATED_IGPU_HUD_LIGHT", "CUSTOM"},
            new String[]{"Выключено", "Автобаланс (dGPU мир + iGPU UI/Light)", "Выделенный iGPU для HUD и света", "Пользовательский"},
            "AUTO_BALANCED"
        ));
        register(HyperionOption.createBoolean(
            "enableSecondaryGpuHudOffload",
            "Оффлоад интерфейса (HUD) на встройку",
            "Рендерит 2D интерфейс на встроенной графике Vega 8, освобождая дискретную карту",
            HyperionCategory.GPU_VIDEO_SETTINGS,
            c -> c.enableSecondaryGpuHudOffload,
            (c, v) -> c.enableSecondaryGpuHudOffload = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableSecondaryGpuLightOffload",
            "Оффлоад расчета света на встройку",
            "Выполняет Compute-шейдеры света на iGPU параллельно с основным рендерингом мира",
            HyperionCategory.GPU_VIDEO_SETTINGS,
            c -> c.enableSecondaryGpuLightOffload,
            (c, v) -> c.enableSecondaryGpuLightOffload = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableSecondaryGpuParticleOffload",
            "Оффлоад частиц на встройку",
            "Вычисляет физику и анимацию частиц на встроенной видеокарте",
            HyperionCategory.GPU_VIDEO_SETTINGS,
            c -> c.enableSecondaryGpuParticleOffload,
            (c, v) -> c.enableSecondaryGpuParticleOffload = v,
            true
        ));

        // =========================================================================
        // 3. CPU & MULTITHREADING SETTINGS (Настройки процессора)
        // =========================================================================
        register(HyperionOption.createBoolean(
            "enableCpuMultithreading",
            "Многопоточное ядро CPU (Multi-Core Engine)",
            "Задействует все ядра процессора для мешинга чанков, тика сущностей и физики",
            HyperionCategory.CPU_PROCESSOR_SETTINGS,
            c -> c.enableCpuMultithreading,
            (c, v) -> c.enableCpuMultithreading = v,
            true
        ));
        register(HyperionOption.createCycle(
            "cpuThreadAllocationMode",
            "Режим распределения потоков CPU",
            "Стратегия назначения ядер и воркеров",
            HyperionCategory.CPU_PROCESSOR_SETTINGS,
            c -> c.cpuThreadAllocationMode,
            (c, v) -> c.cpuThreadAllocationMode = v,
            new String[]{"AUTO_DETECT_CORES", "ALL_CORES", "BALANCED_N_MINUS_1", "CUSTOM"},
            new String[]{"Авто-детекция (Рекомендуется)", "Все доступные ядра (Макс. FPS)", "Сбалансированный (N-1 ядро)", "Пользовательский лимит"},
            "AUTO_DETECT_CORES"
        ));
        register(HyperionOption.createIntSlider(
            "customCpuCoreCount",
            "Количество потоков воркеров (Custom)",
            "Количество рабочих потоков при выборе пользовательского режима",
            HyperionCategory.CPU_PROCESSOR_SETTINGS,
            c -> c.customCpuCoreCount,
            (c, v) -> c.customCpuCoreCount = v,
            Math.max(2, Runtime.getRuntime().availableProcessors()), 1, 64, 1
        ));
        register(HyperionOption.createBoolean(
            "enableParallelChunkMeshing",
            "Параллельный мешинг чанков (ForkJoin Mesher)",
            "Многопоточное построение геометрии чанков на свободных ядрах процессора",
            HyperionCategory.CPU_PROCESSOR_SETTINGS,
            c -> c.enableParallelChunkMeshing,
            (c, v) -> c.enableParallelChunkMeshing = v,
            true
        ));
        register(HyperionOption.createIntSlider(
            "parallelChunkMesherThreads",
            "Потоки построения чанков",
            "Количество выделенных потоков для генерации воксельных мешей",
            HyperionCategory.CPU_PROCESSOR_SETTINGS,
            c -> c.parallelChunkMesherThreads,
            (c, v) -> c.parallelChunkMesherThreads = v,
            Math.max(2, (Runtime.getRuntime().availableProcessors() * 5) / 8), 1, 32, 1
        ));
        register(HyperionOption.createBoolean(
            "enableMultiCoreEntityPhysics",
            "Параллельный тик и физика сущностей",
            "Многоядерная обработка перемещения, коллизий и AI мобов без блокировки главного потока",
            HyperionCategory.CPU_PROCESSOR_SETTINGS,
            c -> c.enableMultiCoreEntityPhysics,
            (c, v) -> c.enableMultiCoreEntityPhysics = v,
            true
        ));
        register(HyperionOption.createIntSlider(
            "entityPhysicsBatchSize",
            "Размер пакета сущностей на ядро",
            "Количество мобов, передаваемых на один воркер-поток",
            HyperionCategory.CPU_PROCESSOR_SETTINGS,
            c -> c.entityPhysicsBatchSize,
            (c, v) -> c.entityPhysicsBatchSize = v,
            64, 16, 256, 16
        ));
        register(HyperionOption.createBoolean(
            "enableAsyncWorldTickDispatcher",
            "Асинхронный диспетчер задач мира",
            "Фоновая обработка жидкостей, редстоун-цепей и света",
            HyperionCategory.CPU_PROCESSOR_SETTINGS,
            c -> c.enableAsyncWorldTickDispatcher,
            (c, v) -> c.enableAsyncWorldTickDispatcher = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableCpuCoreAffinity",
            "Губернатор приоритетов потоков (CPU Affinity)",
            "Повышает приоритет потока рендеринга и звука, снижая задержки ввода (Input Lag)",
            HyperionCategory.CPU_PROCESSOR_SETTINGS,
            c -> c.enableCpuCoreAffinity,
            (c, v) -> c.enableCpuCoreAffinity = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableSimdVectorAcceleration",
            "Векторизация SIMD и Fast Math LUT",
            "Быстрые тригонометрические таблицы и векторные инструкции для расчетов углов и коллизий",
            HyperionCategory.CPU_PROCESSOR_SETTINGS,
            c -> c.enableSimdVectorAcceleration,
            (c, v) -> c.enableSimdVectorAcceleration = v,
            true
        ));

        // =========================================================================
        // 4. WORLD & LIGHTING
        // =========================================================================
        register(HyperionOption.createBoolean(
            "enableHybridLightEngine",
            "Гибридный свет (Starlight + Phosphor)",
            "Асинхронный 64-битный расчет света в многопоточном пуле с L1/L2 кэшированием",
            HyperionCategory.WORLD_LIGHTING,
            c -> c.enableHybridLightEngine,
            (c, v) -> c.enableHybridLightEngine = v,
            true
        ));
        register(HyperionOption.createIntSlider(
            "lightWorkerThreads",
            "Потоки расчета света",
            "Количество фоновых потоков для мгновенного обновления света",
            HyperionCategory.WORLD_LIGHTING,
            c -> c.lightWorkerThreads,
            (c, v) -> c.lightWorkerThreads = v,
            Math.max(1, Runtime.getRuntime().availableProcessors() - 1), 1, 16, 1
        ));
        register(HyperionOption.createBoolean(
            "enableClientWorldCache",
            "Дальний кэш мира (Bobby World Cache)",
            "Сохраняет чанки на клиенте для дальности прорисовки до 64+ чанков на серверах",
            HyperionCategory.WORLD_LIGHTING,
            c -> c.enableClientWorldCache,
            (c, v) -> c.enableClientWorldCache = v,
            true
        ));
        register(HyperionOption.createIntSlider(
            "clientMaxViewDistance",
            "Дальность кэша (Чанки)",
            "Максимальная дальность прорисовки фейковых чанков на клиенте",
            HyperionCategory.WORLD_LIGHTING,
            c -> c.clientMaxViewDistance,
            (c, v) -> c.clientMaxViewDistance = v,
            32, 12, 64, 4
        ));

        // =========================================================================
        // 5. PHYSICS & REDSTONE
        // =========================================================================
        register(HyperionOption.createBoolean(
            "enableFastRedstoneEngine",
            "Топологический редстоун (Alternate Current)",
            "1-проходный направленный граф распространения сигнала без рекурсивного лага",
            HyperionCategory.PHYSICS_REDSTONE,
            c -> c.enableFastRedstoneEngine,
            (c, v) -> c.enableFastRedstoneEngine = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableSleepingHoppers",
            "Спящие воронки (Sleeping Hoppers)",
            "Усыпляет пустые и заблокированные воронки до появления предметов",
            HyperionCategory.PHYSICS_REDSTONE,
            c -> c.enableSleepingHoppers,
            (c, v) -> c.enableSleepingHoppers = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enablePathfindingCircuitBreaker",
            "Предохранитель поиска путей (AI Circuit Breaker)",
            "Ограничивает бесконечный поиск путей для мобов в тупиках, снижая серверный лаг",
            HyperionCategory.PHYSICS_REDSTONE,
            c -> c.enablePathfindingCircuitBreaker,
            (c, v) -> c.enablePathfindingCircuitBreaker = v,
            true
        ));

        // =========================================================================
        // 6. ENTITIES & ANIMATIONS
        // =========================================================================
        register(HyperionOption.createBoolean(
            "enableEntityDepthCulling",
            "Отсечение скрытых мобов за стенами (Entity Culling)",
            "Не рендерит мобов за непрозрачными стенами и в пещерах",
            HyperionCategory.ENTITIES_ANIMATIONS,
            c -> c.enableEntityDepthCulling,
            (c, v) -> c.enableEntityDepthCulling = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableExperienceOrbClumping",
            "Слияние сфер опыта (Clumps)",
            "Мгновенно объединяет сотни сфер опыта в один кластер",
            HyperionCategory.ENTITIES_ANIMATIONS,
            c -> c.enableExperienceOrbClumping,
            (c, v) -> c.enableExperienceOrbClumping = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableStaticFastChests",
            "Быстрые статичные сундуки (Fast Chests)",
            "Запекает сундуки в меш чанка вместо отрисовки через BlockEntityRenderer",
            HyperionCategory.ENTITIES_ANIMATIONS,
            c -> c.enableStaticFastChests,
            (c, v) -> c.enableStaticFastChests = v,
            true
        ));

        // =========================================================================
        // 7. NETWORK, MEMORY & AUDIO
        // =========================================================================
        register(HyperionOption.createBoolean(
            "enablePacketFlushConsolidation",
            "Консолидация сетевых пакетов (Krypton)",
            "Объединяет мелкие пакеты TCP для устранения микрофризов в мультиплеере",
            HyperionCategory.NETWORK_MEMORY_AUDIO,
            c -> c.enablePacketFlushConsolidation,
            (c, v) -> c.enablePacketFlushConsolidation = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableAsyncAudio",
            "Асинхронный звуковой движок",
            "Переносит позиционирование звуков OpenAL в отдельный независимый поток",
            HyperionCategory.NETWORK_MEMORY_AUDIO,
            c -> c.enableAsyncAudio,
            (c, v) -> c.enableAsyncAudio = v,
            true
        ));

        // =========================================================================
        // 8. COLOR CORRECTION
        // =========================================================================
        register(HyperionOption.createBoolean(
            "enableColorCorrection",
            "Цветокоррекция и HDR Tone Mapping",
            "Кинематографический тонемаппинг ACES, насыщение и устранение перетемнений теней",
            HyperionCategory.COLOR_CORRECTION,
            c -> c.enableColorCorrection,
            (c, v) -> c.enableColorCorrection = v,
            true
        ));
        register(HyperionOption.createCycle(
            "colorGradingMode",
            "Профиль цветокоррекции",
            "Цветовая палитра рендера мира",
            HyperionCategory.COLOR_CORRECTION,
            c -> c.colorGradingMode,
            (c, v) -> c.colorGradingMode = v,
            new String[]{"VIBRANT_HDR", "NIGHT_VISION_CLEAR", "CINEMATIC_FILMIC", "NATURAL_BALANCED", "CUSTOM"},
            new String[]{"Vibrant HDR (Яркий)", "Clear Night (Ночной обзор)", "Cinematic Filmic (Кино)", "Natural Balanced (Натуральный)", "Custom (Пользовательский)"},
            "VIBRANT_HDR"
        ));
        register(HyperionOption.createDoubleSlider(
            "colorVibrance",
            "Сочность цветов (Vibrance Boost)",
            "Интеллектуальное усиление пастельных и ненасыщенных цветов",
            HyperionCategory.COLOR_CORRECTION,
            c -> c.colorVibrance,
            (c, v) -> c.colorVibrance = v,
            1.15, 0.8, 2.0, 0.05
        ));
        register(HyperionOption.createDoubleSlider(
            "colorSaturation",
            "Насыщенность цветов (Saturation)",
            "Общий множитель насыщенности цветового пространства",
            HyperionCategory.COLOR_CORRECTION,
            c -> c.colorSaturation,
            (c, v) -> c.colorSaturation = v,
            1.05, 0.5, 2.0, 0.05
        ));
        register(HyperionOption.createDoubleSlider(
            "colorContrast",
            "Контрастность изображения",
            "Кривая контрастности света и тени",
            HyperionCategory.COLOR_CORRECTION,
            c -> c.colorContrast,
            (c, v) -> c.colorContrast = v,
            1.02, 0.8, 1.5, 0.02
        ));
        register(HyperionOption.createDoubleSlider(
            "colorBlackCrushCompensation",
            "Компенсация черного (Anti-Black-Crush)",
            "Осветляет глухие черные тени в пещерах и ночью без потери контрастности",
            HyperionCategory.COLOR_CORRECTION,
            c -> c.colorBlackCrushCompensation,
            (c, v) -> c.colorBlackCrushCompensation = v,
            0.08, 0.0, 0.30, 0.01
        ));
        register(HyperionOption.createDoubleSlider(
            "colorNightAmbientBoost",
            "Осветление ночного мира (Night Visibility)",
            "Мягкое рассеянное ночное освещение для комфортной игры без факелов",
            HyperionCategory.COLOR_CORRECTION,
            c -> c.colorNightAmbientBoost,
            (c, v) -> c.colorNightAmbientBoost = v,
            0.10, 0.0, 0.50, 0.05
        ));
        register(HyperionOption.createBoolean(
            "enableColorDebanding",
            "Устранение цветового бандинга (Debanding)",
            "Сглаживает ступенчатые градиенты неба и темных пещер с помощью дизеринга",
            HyperionCategory.COLOR_CORRECTION,
            c -> c.enableColorDebanding,
            (c, v) -> c.enableColorDebanding = v,
            true
        ));
    }

    private static void register(HyperionOption<?> option) {
        ALL_OPTIONS.add(option);
        List<HyperionOption<?>> categoryList = CATEGORY_MAP.get(option.getCategory());
        if (categoryList != null) {
            categoryList.add(option);
        }
    }

    public static List<HyperionOption<?>> getAllOptions() {
        return Collections.unmodifiableList(ALL_OPTIONS);
    }

    public static List<HyperionOption<?>> getOptionsByCategory(HyperionCategory category) {
        List<HyperionOption<?>> list = CATEGORY_MAP.get(category);
        return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
    }
}
