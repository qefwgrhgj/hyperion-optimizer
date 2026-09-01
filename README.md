# ⚡ Hyperion Optimizer (v1.0.3 Sovereign Release Edition)

**Hyperion Optimizer** (`hyperion-optimizer`) is a high-performance, cross-platform modular optimization engine for Minecraft (**1.16.5 – 1.21.11**, **26.1 – 26.2**, including 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1, 26.2). It combines GPU-Driven compute rendering, multi-core CPU work-stealing, offscreen 2D HUD caching, advanced server-side physics, network packet consolidation, and asynchronous lighting.

---

## 🌐 Language / Язык
* [English Description](#-english-overview)
* [Русское описание](#-русское-описание)

---

## 🌟 English Overview

### 🚀 Key Optimization Pillars

1. **Multi-Core CPU Thread Orchestrator:**
   * **Parallel Chunk Mesher:** Chunk polygonal mesh generation offloaded from main thread across CPU cores via Work-Stealing `ForkJoinPool` (`ParallelChunkMesher`).
   * **LOD (Level of Detail):** Dynamic geometry simplification for distant terrain blocks (`ChunkLodManager`).
   * **Parallel Entity Physics:** Multi-threaded entity movement, spatial collisions, and AI tick processing (`MultiCoreEntityPhysicsEngine`).
   * **Async World Task Dispatcher:** Background fluid updates and world light calculations (`AsyncWorldTickDispatcher`) with CPU core affinity governance (`CpuCoreAffinityGovernor`).
2. **GPU-Driven Compute Culling & Multi-Vendor Engine:**
   * **GPU Instancing / Batching:** Batched rendering for identical block models via SSBO/UBO, alleviating memory and PCIe bus load (`GpuInstancingEngine`).
   * **Aggressive Face Culling:** Pre-GPU elimination of occluded chunk clusters and hidden internal faces (`AggressiveFaceCuller`).
   * **Vendor Profiles:** Dedicated hardware paths for AMD Radeon, NVIDIA GeForce, Intel Arc, and Apple Silicon.
   * **Thermal Power Guard (Alt+Tab):** Dynamic frame limiter (15–30 FPS) when game is minimized to reduce GPU load and temperature (`GpuThermalPowerGuard`).
3. **Voxel LOD Infinite Distance Horizon (2048+ Chunks):**
   * **5-Level Hierarchical Mip Pyramid:** Downsamples distant terrain into hierarchical voxel clusters (Mip 0 to Mip 4) for instant horizon rendering up to 2048+ chunks (`VoxelHierarchicalMipTree`).
   * **Compact RLE / Palette Compression:** Stores distant horizons with minimal RAM overhead (`VoxelSectionStorage`).
   * **GPU Multi-Draw Indirect Voxel Pipeline:** Dispatches distant voxels in persistent GPU buffer arena draw dispatches (`VoxelLodRenderer`).
   * **Horizon Fog Blender:** Smooth blending between local chunks and distant voxels with atmospheric fog (`VoxelHorizonBlender`).
4. **Frame Pacing & Resilience:**
   * **Thread Synchronization:** Eliminates busy wait-loops with micro-parking locks (`DualGpuSyncLock`).
   * **Auto-Fallback & Safe Guard:** Graceful workload shedding and GPU buffer crash recovery (`GpuResetCrashGuard`).
5. **Decoupled HUD FBO Cache (F1-Mode):**
   * Offscreen 2D HUD rendering with event-driven repainting. Composed over world frames in a single draw pass.
6. **Server Physics, Hoppers & Redstone:**
   * Event-driven sleeping hoppers (`Sleeping Hoppers`), fast Bresenham raycast explosions, and single-pass topological redstone solver.
   * Pathfinding circuit breaker preventing tick lag from trapped mobs (`PathfindingCircuitBreaker`).
7. **Entity Optimization & Lighting:**
   * Experience orb clustering (`ExperienceOrbMerger`), static chest baking (`StaticChestMeshBaker`), and 64-bit asynchronous lighting (`AsyncBitsetLightEngine`).
8. **Particle Acceleration & Micro-Optimizations:**
   * Batched direct VBO particle rendering with depth culling (`AdvancedParticleEngine`).
   * Biome blend cache, dirty lightmap caching, and zero-allocation F3 debug overlay string buffers.

### 🛡️ Shader & Mod Compatibility Matrix

| Mod / Technology | Compatibility Status | Notes |
| :--- | :---: | :--- |
| **Iris Shaders / Oculus** | ✅ **Fully Compatible** | Automatic pass coordination: Hyperion detects composite passes and safely coordinates decoupled HUD buffers without artifacting. |
| **Sodium / Embeddium** | ✅ **Fully Compatible** | Hyperion seamlessly harmonizes with Sodium/Embeddium mesh builders and avoids duplicate meshing passes. |
| **Indium** | ✅ **Fully Compatible** | Full support for Fabric Rendering API (FRAPI) quad pipelines. |
| **FerriteCore / ModernFix** | ✅ **Fully Compatible** | Complementary memory reductions without data structures conflict. |
| **Entity Culling / More Culling** | ✅ **Fully Compatible** | Hyperion's depth culler coordinates with entity bounding-box culling. |
| **OptiFine** | ⚠️ **Partial Support** | Supported, but recommend disabling OptiFine's internal "Fast Render" to let Hyperion's modern GPU batching take priority. |
| **Dedicated Servers (Headless)** | ✅ **100% Compatible** | Client-only rendering systems are automatically isolated on headless servers, keeping server TPS optimizations active. |

### 📦 Modpack Permission Policy
* **Can I include Hyperion Optimizer in my modpack?**
  * **YES!** You are completely free to include Hyperion Optimizer in any modpack (public, private, community, or commercial) hosted on **Modrinth**, **CurseForge**, FTB, or custom third-party launchers. No prior written permission is required. Attribution via GitHub link is appreciated!

### ⌨️ In-Game Configuration Hotkey
* Open settings menu anytime via: **`Ctrl + Shift + 0`** (Control + Shift + 0).

### 🖥️ Settings Dashboard (10 Categories)
1. **🖥️ Graphics Settings:** GPU-driven rendering, Hi-Z occlusion, block instancing, chunk LOD, HUD FBO cache, and frame pacing.
2. **🎮 Video Card / GPU:** Architecture profiles (AMD Radeon, NVIDIA, Intel, Apple Silicon), threading mode, and buffer safe guards.
3. **🧠 Processor / CPU:** Parallel chunk mesher, thread allocation modes, multi-core entity physics, and task dispatcher.
4. **🌲 Distance & Voxel LOD:** Horizon rendering up to 2048+ chunks, Mip levels, RLE compression, and distance fog.
5. **🌍 World & Lighting:** Async 64-bit lighting, client world cache, and fluid dynamic updates.
6. **⚡ Physics & Redstone:** Fast redstone engine, sleeping hoppers, fast explosions, and pathfinding breaker.
7. **👾 Entities & Animations:** Entity depth culling, animation LOD, experience orb merging, and static chest meshes.
8. **📡 Network, Memory & Audio:** Packet flush consolidation, mathematical fast LUTs, and async sound engine.
9. **🎨 Color Correction & HDR:** ACES Filmic tonemapping, anti-black-crush compensation, vibrant grading, and debanding.
10. **⚙️ Advanced Tweaks:** VRAM memory limits, batch allocation sizes, collision parameters, and advanced profiling.

---

## 🇷🇺 Русское описание

### 🌟 Ключевые столпы оптимизации

1. **Многоядерность и многопоточность CPU (Multi-Core Engine):**
   * **Асинхронный генератор мешей:** Генерация полигональных сеток чанков вынесена из главного потока на свободные ядра процессора через Work-Stealing `ForkJoinPool` (`ParallelChunkMesher`).
   * **LOD (Level of Detail):** Динамическое упрощение геометрии и сетки блоков для дальних дистанций (`ChunkLodManager`).
   * **Параллельный тик сущностей:** Многопоточный расчет перемещения, коллизий и AI мобов (`MultiCoreEntityPhysicsEngine`).
   * **Асинхронный диспетчер задач мира:** Фоновые расчеты жидкостей и света (`AsyncWorldTickDispatcher`) с регулятором приоритетов (`CpuCoreAffinityGovernor`).
2. **GPU-Driven Compute Culling, Instancing & Multi-Vendor Engine:**
   * **GPU Instancing / Batching:** Объединение вызовов отрисовки одинаковых блоков в один пакет через SSBO/UBO (`GpuInstancingEngine`).
   * **Агрессивный Culling:** Отсечение скрытых чанков и внутренних невидимых граней блоков (`AggressiveFaceCuller`).
   * **Поддержка вендоров:** Архитектурные профили под AMD Radeon, NVIDIA, Intel и Apple Silicon.
   * **Ограничитель в фоне (Alt+Tab):** Ограничение FPS до 15–30 при свертывании игры (`GpuThermalPowerGuard`).
3. **Воксельный LOD рендеринг горизонта (2048+ чанков):**
   * **Иерархическая Mip-пирамида:** 5 уровней воксельных Mip-сеток для прорисовки дистанций до $2048+$ чанков (`VoxelHierarchicalMipTree`).
   * **Компактное RLE/Palette сжатие:** Сжатие секций чанков с минимальным оверхедом RAM (`VoxelSectionStorage`).
   * **GPU Multi-Draw Indirect Voxel Pipeline:** Отрисовка дальних вокселей за один Indirect вызов (`VoxelLodRenderer`).
   * **Плавный переход горизонта (Horizon Blender):** Сглаживание границы с атмосферным туманом (`VoxelHorizonBlender`).
4. **Стабилизация времени кадра и аварийный откат:**
   * **Синхронизация потоков (Sync Lock):** Устранение wait-loop циклов (`DualGpuSyncLock`).
   * **Crash Guard:** Защита от вылетов при сбоях буферов (`GpuResetCrashGuard`).
5. **Decoupled HUD FBO Cache (F1-Mode):**
   * Рендеринг 2D-интерфейса в отдельный оффскрин-буфер с обновлением по событиям.
6. **Высокопроизводительная физика, коллизии и AI:**
   * Спящие воронки (`Sleeping Hoppers`), константный кэш коллизий и 1-проходный редстоун.
   * Предохранитель поиска путей для застрявших мобов (`PathfindingCircuitBreaker`).
7. **Слияние сфер опыта, асинхронный свет и кэш чанков:**
   * Мгновенное слияние сфер опыта (`ExperienceOrbMerger`), запекание сундуков (`StaticChestMeshBaker`) и асинхронный 64-битный свет (`AsyncBitsetLightEngine`).
8. **Пакетный GPU-конвейер частиц:**
   * Пакетный рендеринг частиц через единый VBO-буфер за один Draw Call (`AdvancedParticleEngine`).
9. **Микро-оптимизации рендеринга и интерфейса:**
   * Кэширование карты освещения (Lightmap), кэш биомов и устранение аллокаций строк F3.
10. **Цветокоррекция и HDR:**
    * Тонемаппинг ACES Filmic, компенсация перетемнений (Anti-Black-Crush) и сочные цвета.

### 🛡️ Совместимость с шейдерами и модами

| Мод / Технология | Статус совместимости | Примечания |
| :--- | :---: | :--- |
| **Iris Shaders / Oculus** | ✅ **Полная совместимость** | Автоматическая координация проходов рендеринга и буферов постобработки. |
| **Sodium / Embeddium** | ✅ **Полная совместимость** | Согласованная работа с внешними генераторами геометрии чанков без конфликтов. |
| **Indium** | ✅ **Полная совместимость** | Поддержка конвейера Fabric Rendering API (FRAPI). |
| **FerriteCore / ModernFix** | ✅ **Полная совместимость** | Взаимное дополнение оптимизаций оперативной памяти. |
| **Entity Culling** | ✅ **Полная совместимость** | Совместное отсечение геометрии сущностей. |
| **OptiFine** | ⚠️ **Частичная** | Рекомендуется отключить «Быстрый рендер» в OptiFine в пользу прямого GPU-инстансинга Hyperion. |
| **Выделенные серверы** | ✅ **100% Совместимость** | Клиентский код рендеринга изолирован, серверные оптимизации тика и физики активны. |

### 📦 Политика использования в модпаках
* **Можно ли использовать Hyperion Optimizer в сборках и модпаках?**
  * **ДА!** Вы имеете полное право включать Hyperion Optimizer в любые публичные, приватные и авторские модпаки на платформах **Modrinth**, **CurseForge**, FTB и сторонних лаунчерах. Никаких предварительных согласований не требуется.

### ⌨️ Горячая клавиша меню настроек
* Меню настроек открывается по нажатию: **`Ctrl + Shift + 0`** (Control + Shift + 0).

### 🖥️ Меню настроек мода (10 Категорий)
1. **🖥️ 1. Настройки графики:** Аппаратный GPU-Driven рендеринг, Hi-Z окклюзия, GPU Instancing / Batching, LOD геометрии чанков, FBO-кэш интерфейса и стабилизатор FPS.
2. **🎮 2. Настройки видеокарт:** Архитектурные профили GPU (AMD Radeon, NVIDIA, Intel Arc, Apple Silicon), многопоточный режим и Safe Guard.
3. **🧠 3. Настройки процессора:** Асинхронный генератор мешей, ForkJoin mesher на свободных ядрах, параллельный тик сущностей и диспетчер мира.
4. **🌲 4. Дальний рендер и Voxel LOD:** Отрисовка горизонта до 2048+ чанков, 5-уровневые Mip-пирамиды, RLE-сжатие и атмосферный туман.
5. **🌍 5. Мир и Освещение:** Асинхронный 64-битный расчет света, локальный кэш дальних чанков и быстрая физика жидкостей.
6. **⚡ 6. Физика и Механика:** 1-проходный расчет редстоуна, спящие воронки, быстрые лучи взрывов и предохранитель поиска путей мобов.
7. **👾 7. Сущности и Анимации:** Отсечение сущностей за стенами, LOD анимаций, слияние сфер опыта и запекание статичных сундуков.
8. **📡 8. Сеть, Память и Аудио:** Консолидация сетевых TCP-пакетов, быстрое математическое кэширование и асинхронный аудио-движок.
9. **🎨 9. Цветокоррекция и HDR:** Тонемаппинг ACES Filmic, компенсация перетемнений (Anti-Black-Crush), сочные цвета и устранение бандинга.
10. **⚙️ 10. Тонкие системные твики:** Лимиты VRAM, размеры батчей инстансинга, параметры коллизий и тонкая настройка движка.

---

## 📂 Version & Module Structure / Структура модулей

```text
├── common/               # Core engine, multithreading, GPU pipeline, GUI
├── fabric-1.16.5 / forge-1.16.5
├── fabric-1.17.1 / forge-1.17.1    # Minecraft 1.17, 1.17.1
├── fabric-1.18.2 / forge-1.18.2    # Minecraft 1.18, 1.18.1, 1.18.2
├── fabric-1.19.2 / forge-1.19.2    # Minecraft 1.19, 1.19.1, 1.19.2
├── fabric-1.19.4 / forge-1.19.4    # Minecraft 1.19.3, 1.19.4
├── fabric-1.20.1 / forge-1.20.1    # Minecraft 1.20, 1.20.1
├── fabric-1.20.4 / neoforge-1.20.4 # Minecraft 1.20.2, 1.20.3, 1.20.4
├── fabric-1.20.6 / neoforge-1.20.6 # Minecraft 1.20.5, 1.20.6
├── fabric-1.21.1 / neoforge-1.21.1 # Minecraft 1.21, 1.21.1
├── fabric-1.21.4 / neoforge-1.21.4 # Minecraft 1.21.2, 1.21.3, 1.21.4
├── fabric-1.21.11/ neoforge-1.21.11# Minecraft 1.21.5 – 1.21.11
├── fabric-26.1   / neoforge-26.1   # Minecraft 26.1
└── fabric-26.2   / neoforge-26.2   # Minecraft 26.2
```

---

## 🧪 Build & Verification / Сборка и верификация

* Build all 26 packages: `python build_all.py`
* Verify via Gradle Wrapper: `./gradlew build`
* Source Code: [GitHub Repository](https://github.com/qefwgrhgj/hyperion-optimizer)
* Issue Tracker: [GitHub Issues](https://github.com/qefwgrhgj/hyperion-optimizer/issues)
* License: MIT License (included in repository and embedded in all binary JAR artifacts).
