# 🚀 Modrinth Submission & Publishing Guide (Hyperion Optimizer v1.0.3)

---

## 1. Project Metadata Settings (Параметры проекта на сайте)

При создании страницы проекта на [Modrinth.com](https://modrinth.com) заполните следующие поля:

* **Name:** `Hyperion Optimizer`
* **Slug / Project ID:** `hyperion_optimizer` (или `hyperion-optimizer`)
* **Project Type:** `Mod`
* **Summary (Краткое описание):** `High-Performance Sovereign Multi-Core Cross-Platform Minecraft Optimization Engine.`
* **License:** `MIT License`
* **Categories:** `Optimization`, `Utility`
* **Client Side:** **`Required`**
* **Server Side:** **`Optional`** *(Поддерживается и оптимизирует серверные тики, но клиенты могут заходить без мода)*
* **Source Repository:** `https://github.com/qefwgrhgj/hyperion-optimizer`
* **Issue Tracker:** `https://github.com/qefwgrhgj/hyperion-optimizer/issues`
* **Icon:** Загрузить файл `common/src/main/resources/icon.png` (384x384 PNG)
* **Gallery (Галерея):** Прикрепить 1–2 скриншота меню настроек мода (`Ctrl + Shift + 0` в игре).

---

## 2. Автоматическая публикация всех 26 версий через API (Рекомендуется)

Чтобы не загружать 26 файлов вручную и исключить ошибку смешивания версий в один релиз:

1. Создайте пустой проект на Modrinth и скопируйте его `slug` или `id`.
2. Получите персональный токен Modrinth: [Modrinth Settings -> Personal Access Tokens](https://modrinth.com/settings/pats) с правом `CREATE_VERSION`.
3. Запустите скрипт публикации:
   ```bash
   python modrinth_publisher.py --token <ВАШ_ТОКЕН> --project hyperion_optimizer --upload
   ```
4. Скрипт автоматически создаст 26 независимых релизов с привязкой конкретного JAR к своей версии игры и загрузчику.

---

## 3. Ручная публикация в веб-интерфейсе (Если загружаете через сайт)

> ⚠️ **ВАЖНО:** Не перетаскивайте все 26 файлов в один релиз! Создавайте отдельный релиз для каждой версии/загрузчика.

| Файл JAR | Загрузчик | Версии игры |
| :--- | :---: | :--- |
| `hyperion-optimizer-fabric-1.16.5-1.0.3.jar` | Fabric | `1.16.5` |
| `hyperion-optimizer-forge-1.16.5-1.0.3.jar` | Forge | `1.16.5` |
| `hyperion-optimizer-fabric-1.17.1-1.0.3.jar` | Fabric | `1.17`, `1.17.1` |
| `hyperion-optimizer-forge-1.17.1-1.0.3.jar` | Forge | `1.17`, `1.17.1` |
| `hyperion-optimizer-fabric-1.18.2-1.0.3.jar` | Fabric | `1.18`, `1.18.1`, `1.18.2` |
| `hyperion-optimizer-forge-1.18.2-1.0.3.jar` | Forge | `1.18.2` |
| `hyperion-optimizer-fabric-1.19.2-1.0.3.jar` | Fabric | `1.19`, `1.19.1`, `1.19.2` |
| `hyperion-optimizer-forge-1.19.2-1.0.3.jar` | Forge | `1.19.2` |
| `hyperion-optimizer-fabric-1.19.4-1.0.3.jar` | Fabric | `1.19.3`, `1.19.4` |
| `hyperion-optimizer-forge-1.19.4-1.0.3.jar` | Forge | `1.19.4` |
| `hyperion-optimizer-fabric-1.20.1-1.0.3.jar` | Fabric | `1.20`, `1.20.1` |
| `hyperion-optimizer-forge-1.20.1-1.0.3.jar` | Forge | `1.20`, `1.20.1` |
| `hyperion-optimizer-fabric-1.20.4-1.0.3.jar` | Fabric | `1.20.2`, `1.20.3`, `1.20.4` |
| `hyperion-optimizer-neoforge-1.20.4-1.0.3.jar` | NeoForge | `1.20.4` |
| `hyperion-optimizer-fabric-1.20.6-1.0.3.jar` | Fabric | `1.20.5`, `1.20.6` |
| `hyperion-optimizer-neoforge-1.20.6-1.0.3.jar` | NeoForge | `1.20.6` |
| `hyperion-optimizer-fabric-1.21.1-1.0.3.jar` | Fabric | `1.21`, `1.21.1` |
| `hyperion-optimizer-neoforge-1.21.1-1.0.3.jar` | NeoForge | `1.21.1` |
| `hyperion-optimizer-fabric-1.21.4-1.0.3.jar` | Fabric | `1.21.2`, `1.21.3`, `1.21.4` |
| `hyperion-optimizer-neoforge-1.21.4-1.0.3.jar` | NeoForge | `1.21.4` |
| `hyperion-optimizer-fabric-1.21.11-1.0.3.jar` | Fabric | `1.21.5` – `1.21.11` |
| `hyperion-optimizer-neoforge-1.21.11-1.0.3.jar` | NeoForge | `1.21.5` – `1.21.11` |
| `hyperion-optimizer-fabric-26.1-1.0.3.jar` | Fabric | `26.1` |
| `hyperion-optimizer-neoforge-26.1-1.0.3.jar` | NeoForge | `26.1` |
| `hyperion-optimizer-fabric-26.2-1.0.3.jar` | Fabric | `26.2` |
| `hyperion-optimizer-neoforge-26.2-1.0.3.jar` | NeoForge | `26.2` |
