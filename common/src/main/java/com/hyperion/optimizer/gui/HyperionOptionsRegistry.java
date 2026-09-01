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
        register(HyperionOption.createBoolean(
            "enableTexturePackColorCorrection",
            "Цветокоррекция текстур-паков (Resource Pack Color Grading)",
            "Применяет HDR тонемаппинг, сочность и устранение бандинга прямо к текстурам и спрайтам ресурс-пака",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.enableTexturePackColorCorrection,
            (c, v) -> c.enableTexturePackColorCorrection = v,
            false
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
            "NATURAL_BALANCED"
        ));
        register(HyperionOption.createBoolean(
            "enableChunkLod",
            "LOD геометрии чанков (Level of Detail)",
            "Упрощает полигональную сетку блоков для чанков дальше 16 блоков от игрока (+40-70% FPS)",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.enableChunkLod,
            (c, v) -> c.enableChunkLod = v,
            true
        ));
        register(HyperionOption.createDoubleSlider(
            "chunkLodDistanceBlocks",
            "Дистанция включения LOD (Блоки)",
            "Расстояние от камеры, начиная с которого включается упрощение геометрии",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.chunkLodDistanceBlocks,
            (c, v) -> c.chunkLodDistanceBlocks = v,
            16.0, 8.0, 64.0, 4.0
        ));
        register(HyperionOption.createBoolean(
            "enableAggressiveFaceCulling",
            "Агрессивный Culling скрытых граней",
            "Отсекает внутренние невидимые грани блоков до передачи геометрии на видеокарту",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.enableAggressiveFaceCulling,
            (c, v) -> c.enableAggressiveFaceCulling = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableGpuBlockInstancing",
            "GPU Instancing / Batching блоков",
            "Объединяет вызовы отрисовки одинаковых блоков в один пакет для разгрузки шины ОЗУ/PCIe",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.enableGpuBlockInstancing,
            (c, v) -> c.enableGpuBlockInstancing = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableVoxelLodEngine",
            "Воксельный LOD рендеринг (2048+ чанков)",
            "Отрисовывает горизонт ландшафта до 2048+ чанков через облегченные воксельные пирамиды Mip-уровней",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.enableVoxelLodEngine,
            (c, v) -> c.enableVoxelLodEngine = v,
            true
        ));
        register(HyperionOption.createIntSlider(
            "voxelMaxRenderDistanceChunks",
            "Дальность воксельного горизонта (Чанки)",
            "Максимальная дистанция прорисовки воксельного LOD (от 32 до 2048+ чанков)",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.voxelMaxRenderDistanceChunks,
            (c, v) -> c.voxelMaxRenderDistanceChunks = v,
            2048, 64, 4096, 64
        ));
        register(HyperionOption.createBoolean(
            "enableVoxelHorizonBlending",
            "Плавный переход горизонта (Horizon Blend)",
            "Устраняет стыки и резкие границы между обычными чанками и дальними вокселями",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.enableVoxelHorizonBlending,
            (c, v) -> c.enableVoxelHorizonBlending = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableVoxelAtmosphericFog",
            "Атмосферный туман горизонта",
            "Мягкое сглаживание атмосферной дымки на сверхдальних дистанциях горизонта",
            HyperionCategory.GRAPHICS_SETTINGS,
            c -> c.enableVoxelAtmosphericFog,
            (c, v) -> c.enableVoxelAtmosphericFog = v,
            true
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
        register(HyperionOption.createCycle(
            "gpuVendorProfile",
            "Архитектурный профиль GPU",
            "Оптимизация под связки NVIDIA+Intel Optimus, Apple Silicon (M1/M2/M3/M4) и AMD",
            HyperionCategory.GPU_VIDEO_SETTINGS,
            c -> c.gpuVendorProfile,
            (c, v) -> c.gpuVendorProfile = v,
            new String[]{"AUTO", "AMD_RADEON_HYBRID", "NVIDIA_INTEL_OPTIMUS", "APPLE_SILICON_M_SERIES", "INTEL_ARC_DEDICATED", "GENERIC_UNIVERSAL"},
            new String[]{"Auto Detect (Авто-детекция)", "AMD Radeon + Vega APU", "NVIDIA + Intel (Optimus)", "Apple Silicon M-Серия (UMA TBDR)", "Intel Arc Dedicated", "Универсальный OpenGL"},
            "AUTO"
        ));
        register(HyperionOption.createBoolean(
            "enableDualGpuSyncLock",
            "Sync Lock (Защита от Wait-Loop)",
            "Предотвращает 100% загрузку CPU в активных циклах ожидания между видеокартами",
            HyperionCategory.GPU_VIDEO_SETTINGS,
            c -> c.enableDualGpuSyncLock,
            (c, v) -> c.enableDualGpuSyncLock = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableDualGpuThermalFallback",
            "Аварийный откат (Auto-Fallback)",
            "Автоматический сброс тяжелых задач на один адаптер при росте frametime / перегреве",
            HyperionCategory.GPU_VIDEO_SETTINGS,
            c -> c.enableDualGpuThermalFallback,
            (c, v) -> c.enableDualGpuThermalFallback = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableGpuResetCrashGuard",
            "Crash Guard (GPU Reset / TDR)",
            "Перехватывает сбросы графического драйвера и переключает рендер без краша игры",
            HyperionCategory.GPU_VIDEO_SETTINGS,
            c -> c.enableGpuResetCrashGuard,
            (c, v) -> c.enableGpuResetCrashGuard = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableBackgroundFpsCap",
            "Ограничитель в фоновом режиме (Alt+Tab)",
            "Жестко режет FPS до 15-30 при свертывании игры, защищая GPU от перегрева",
            HyperionCategory.GPU_VIDEO_SETTINGS,
            c -> c.enableBackgroundFpsCap,
            (c, v) -> c.enableBackgroundFpsCap = v,
            true
        ));
        register(HyperionOption.createIntSlider(
            "backgroundMaxFramerate",
            "Лимит FPS в фоне (Alt+Tab)",
            "Максимальная частота кадров при свернутом окне",
            HyperionCategory.GPU_VIDEO_SETTINGS,
            c -> c.backgroundMaxFramerate,
            (c, v) -> c.backgroundMaxFramerate = v,
            20, 10, 60, 5
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
            "Асинхронный 64-битный свет",
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
            "Локальный кэш дальнего мира",
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
            "Слияние сфер опыта",
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
            "Консолидация сетевых пакетов",
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
        register(HyperionOption.createBoolean(
            "enableTexturePackColorCorrection",
            "Цветокоррекция текстур-паков (Resource Pack Color Grading)",
            "Применяет HDR тонемаппинг, сочность и устранение бандинга прямо к текстурам и спрайтам ресурс-пака",
            HyperionCategory.COLOR_CORRECTION,
            c -> c.enableTexturePackColorCorrection,
            (c, v) -> c.enableTexturePackColorCorrection = v,
            false
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
            "NATURAL_BALANCED"
        ));
        register(HyperionOption.createDoubleSlider(
            "colorVibrance",
            "Сочность цветов (Vibrance Boost)",
            "Интеллектуальное усиление пастельных и ненасыщенных цветов",
            HyperionCategory.COLOR_CORRECTION,
            c -> c.colorVibrance,
            (c, v) -> c.colorVibrance = v,
            1.00, 0.5, 2.0, 0.05
        ));
        register(HyperionOption.createDoubleSlider(
            "colorSaturation",
            "Насыщенность цветов (Saturation)",
            "Общий множитель насыщенности цветового пространства",
            HyperionCategory.COLOR_CORRECTION,
            c -> c.colorSaturation,
            (c, v) -> c.colorSaturation = v,
            1.00, 0.5, 2.0, 0.05
        ));
        register(HyperionOption.createDoubleSlider(
            "colorContrast",
            "Контрастность изображения",
            "Кривая контрастности света и тени",
            HyperionCategory.COLOR_CORRECTION,
            c -> c.colorContrast,
            (c, v) -> c.colorContrast = v,
            1.00, 0.5, 1.5, 0.02
        ));
        register(HyperionOption.createDoubleSlider(
            "colorBlackCrushCompensation",
            "Компенсация черного (Anti-Black-Crush)",
            "Осветляет глухие черные тени в пещерах и ночью без потери контрастности",
            HyperionCategory.COLOR_CORRECTION,
            c -> c.colorBlackCrushCompensation,
            (c, v) -> c.colorBlackCrushCompensation = v,
            0.12, 0.0, 0.30, 0.01
        ));
        register(HyperionOption.createDoubleSlider(
            "colorNightAmbientBoost",
            "Осветление ночного мира (Night Visibility)",
            "Мягкое рассеянное ночное освещение для комфортной игры без факелов",
            HyperionCategory.COLOR_CORRECTION,
            c -> c.colorNightAmbientBoost,
            (c, v) -> c.colorNightAmbientBoost = v,
            0.12, 0.0, 0.50, 0.05
        ));
        register(HyperionOption.createIntSlider(
            "colorTemperature",
            "Цветовая температура (Кельвин)",
            "Баланс белого: от теплого лампового (3500K) до холодного арктического (9000K)",
            HyperionCategory.COLOR_CORRECTION,
            c -> c.colorTemperature,
            (c, v) -> c.colorTemperature = v,
            6500, 3000, 10000, 250
        ));
        register(HyperionOption.createBoolean(
            "enableTexturePackColorCorrection",
            "Цветокоррекция текстур-паков",
            "Применение тонемаппинга и баланса белого напрямую к альфа-каналам кастомных текстур",
            HyperionCategory.COLOR_CORRECTION,
            c -> c.enableTexturePackColorCorrection,
            (c, v) -> c.enableTexturePackColorCorrection = v,
            false
        ));

        // =========================================================================
        // 4. VOXEL LOD & INFINITE DISTANCE (Дальний рендер и Voxel LOD)
        // =========================================================================
        register(HyperionOption.createBoolean(
            "enableVoxelLodEngine",
            "Движок воксельного LOD (2048+)",
            "Позволяет рендерить горизонт до 2048+ чанков без потери FPS через октарное воксельное сжатие",
            HyperionCategory.VOXEL_LOD_INFINITE,
            c -> c.enableVoxelLodEngine,
            (c, v) -> c.enableVoxelLodEngine = v,
            true
        ));
        register(HyperionOption.createIntSlider(
            "voxelMaxRenderDistanceChunks",
            "Максимальная дальность LOD (Чанки)",
            "Предельная дистанция отрисовки воксельного горизонта",
            HyperionCategory.VOXEL_LOD_INFINITE,
            c -> c.voxelMaxRenderDistanceChunks,
            (c, v) -> c.voxelMaxRenderDistanceChunks = v,
            2048, 64, 4096, 64
        ));
        register(HyperionOption.createBoolean(
            "enableVoxelHorizonBlending",
            "Плавное смешивание горизонта",
            "Сглаживает переход между реальными чанками и дальними LOD секциями",
            HyperionCategory.VOXEL_LOD_INFINITE,
            c -> c.enableVoxelHorizonBlending,
            (c, v) -> c.enableVoxelHorizonBlending = v,
            true
        ));
        register(HyperionOption.createDoubleSlider(
            "voxelBlendStartChunks",
            "Начало смешивания LOD (Чанки)",
            "Расстояние, с которого начинается переход в упрощенный воксельный меш",
            HyperionCategory.VOXEL_LOD_INFINITE,
            c -> c.voxelBlendStartChunks,
            (c, v) -> c.voxelBlendStartChunks = v,
            12.0, 4.0, 32.0, 1.0
        ));
        register(HyperionOption.createDoubleSlider(
            "voxelBlendEndChunks",
            "Конец смешивания LOD (Чанки)",
            "Расстояние полного перехода в воксельную структуру",
            HyperionCategory.VOXEL_LOD_INFINITE,
            c -> c.voxelBlendEndChunks,
            (c, v) -> c.voxelBlendEndChunks = v,
            24.0, 8.0, 64.0, 2.0
        ));
        register(HyperionOption.createBoolean(
            "enableVoxelAtmosphericFog",
            "Атмосферный туман горизонта",
            "Физически корректный экспоненциальный туман для естественного погружения",
            HyperionCategory.VOXEL_LOD_INFINITE,
            c -> c.enableVoxelAtmosphericFog,
            (c, v) -> c.enableVoxelAtmosphericFog = v,
            true
        ));
        register(HyperionOption.createCycle(
            "voxelStorageCompression",
            "Метод сжатия вокселей в памяти",
            "Алгоритм упаковки воксельных данных в ОЗУ (RLE, Fast LZ4)",
            HyperionCategory.VOXEL_LOD_INFINITE,
            c -> c.voxelStorageCompression,
            (c, v) -> c.voxelStorageCompression = v,
            new String[]{"RLE_PALETTE", "FAST_LZ4", "UNCOMPRESSED"},
            new String[]{"RLE Палитра (Рекомендуется)", "Fast LZ4 Сжатие", "Без сжатия (Макс. скорость)"},
            "RLE_PALETTE"
        ));

        // =========================================================================
        // 10. ADVANCED TWEAKS (Тонкие системные твики)
        // =========================================================================
        register(HyperionOption.createBoolean(
            "enableGpuResetCrashGuard",
            "Защита от сбоев GPU (Crash Guard)",
            "Перехват TDR и ошибок сброса драйвера видеокарты без вылета игры",
            HyperionCategory.ADVANCED_TWEAKS,
            c -> c.enableGpuResetCrashGuard,
            (c, v) -> c.enableGpuResetCrashGuard = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableGpuBlockInstancing",
            "Аппаратный GPU-инстансинг блоков",
            "Отрисовка одинаковых моделей блоков за один вызов Draw Instanced",
            HyperionCategory.ADVANCED_TWEAKS,
            c -> c.enableGpuBlockInstancing,
            (c, v) -> c.enableGpuBlockInstancing = v,
            true
        ));
        register(HyperionOption.createIntSlider(
            "maxInstancesPerBatch",
            "Размер батча инстансинга",
            "Количество инстансов на один буфер",
            HyperionCategory.ADVANCED_TWEAKS,
            c -> c.maxInstancesPerBatch,
            (c, v) -> c.maxInstancesPerBatch = v,
            16384, 1024, 65536, 1024
        ));
        register(HyperionOption.createBoolean(
            "enableAggressiveFaceCulling",
            "Агрессивное отсечение граней",
            "Удаление внутренних скрытых полигонов между соседними блоками",
            HyperionCategory.ADVANCED_TWEAKS,
            c -> c.enableAggressiveFaceCulling,
            (c, v) -> c.enableAggressiveFaceCulling = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableInternalCavityCulling",
            "Отсечение полостей (Cavity Culling)",
            "Не строит геометрию для изолированных воздушных карманов под землей",
            HyperionCategory.ADVANCED_TWEAKS,
            c -> c.enableInternalCavityCulling,
            (c, v) -> c.enableInternalCavityCulling = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableHdTextureOptimization",
            "Оптимизация HD текстур-паков",
            "Специальный конвейер сжатия и фильтрации для текстур 64x-512x",
            HyperionCategory.ADVANCED_TWEAKS,
            c -> c.enableHdTextureOptimization,
            (c, v) -> c.enableHdTextureOptimization = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableFastCloudEngine",
            "Быстрый движок облаков (Fast Clouds)",
            "Высокоскоростной рендеринг облаков с переиспользованием меша",
            HyperionCategory.ADVANCED_TWEAKS,
            c -> c.enableFastCloudEngine,
            (c, v) -> c.enableFastCloudEngine = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableSmartLeavesCulling",
            "Умная листва (Smart Leaves)",
            "Отсечение невидимой листвы внутри крон деревьев (+15-30% FPS в лесах)",
            HyperionCategory.ADVANCED_TWEAKS,
            c -> c.enableSmartLeavesCulling,
            (c, v) -> c.enableSmartLeavesCulling = v,
            true
        ));
        register(HyperionOption.createBoolean(
            "enableFabulousGraphicsOptimization",
            "Оптимизация Fabulous! графики",
            "Ускорение послойной сортировки полупрозрачных слоев воды и стекла",
            HyperionCategory.ADVANCED_TWEAKS,
            c -> c.enableFabulousGraphicsOptimization,
            (c, v) -> c.enableFabulousGraphicsOptimization = v,
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
