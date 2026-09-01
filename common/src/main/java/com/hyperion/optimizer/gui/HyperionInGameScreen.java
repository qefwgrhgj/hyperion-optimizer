package com.hyperion.optimizer.gui;

import com.hyperion.optimizer.api.HyperionConfig;
import com.hyperion.optimizer.api.HyperionConfigStorage;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * ⚡ Hyperion Sovereign In-Game Configuration Screen.
 *
 * Renders 100% natively inside Minecraft's UI:
 * - 10 Category Navigation Tabs (Graphics, GPU, CPU, Voxel LOD, World/Light, Physics, Entities, Network, HDR, Tweaks)
 * - Interactive In-Game Widgets: Boolean Toggles, Multi-State Enum Cycles, Step Sliders
 * - Real-Time Dynamic Tooltips & Status Badges
 * - One-Click Hardware Performance Presets (Potato PC, Balanced, High-End, Extreme 350+ FPS)
 * - Zero Window Popping, Zero Focus Loss, Instant Hot-Reload
 */
public class HyperionInGameScreen extends Screen {
    private final Screen parent;
    private final HyperionScreenModel model;
    private HyperionCategory activeCategory = HyperionCategory.GRAPHICS_SETTINGS;
    private int pageIndex = 0;
    private static final int OPTIONS_PER_PAGE = 8;

    public HyperionInGameScreen(Screen parent) {
        super(Component.literal("⚡ Hyperion Optimizer v1.0.3"));
        this.parent = parent;
        this.model = new HyperionScreenModel();
    }

    public HyperionInGameScreen(Screen parent, HyperionScreenModel model) {
        super(Component.literal("⚡ Hyperion Optimizer v1.0.3"));
        this.parent = parent;
        this.model = model != null ? model : new HyperionScreenModel();
    }

    @Override
    protected void init() {
        super.init();
        rebuildScreen();
    }

    private void rebuildScreen() {
        this.clearWidgets();
        HyperionConfig config = model.getWorkingConfig();

        // 1. Top Category Tabs (2 rows of 5 tabs)
        HyperionCategory[] categories = HyperionCategory.values();
        int tabMargin = 10;
        int usableWidth = this.width - (tabMargin * 2);
        int tabWidth = (usableWidth - (4 * 4)) / 5;
        int tabHeight = 18;

        for (int i = 0; i < categories.length; i++) {
            HyperionCategory cat = categories[i];
            int row = i / 5;
            int col = i % 5;
            int x = tabMargin + col * (tabWidth + 4);
            int y = 10 + row * (tabHeight + 3);

            boolean isSelected = (cat == activeCategory);
            String prefix = isSelected ? "▶ " : "";
            String tabTitle = prefix + getShortCategoryName(cat);

            Button catBtn = Button.builder(Component.literal(tabTitle), btn -> {
                this.activeCategory = cat;
                this.pageIndex = 0;
                rebuildScreen();
            }).bounds(x, y, tabWidth, tabHeight)
              .tooltip(Tooltip.create(Component.literal(cat.getTitle())))
              .build();

            this.addRenderableWidget(catBtn);
        }

        // 2. Options Grid in the Center (2 columns of 4 rows = 8 options per page)
        List<HyperionOption<?>> options = HyperionOptionsRegistry.getOptionsByCategory(activeCategory);
        int totalPages = Math.max(1, (options.size() + OPTIONS_PER_PAGE - 1) / OPTIONS_PER_PAGE);
        if (pageIndex >= totalPages) pageIndex = totalPages - 1;
        if (pageIndex < 0) pageIndex = 0;

        int startIndex = pageIndex * OPTIONS_PER_PAGE;
        int endIndex = Math.min(startIndex + OPTIONS_PER_PAGE, options.size());

        int gridTop = 56;
        int optionWidth = 190;
        int optionHeight = 20;
        int colSpacing = 12;
        int rowSpacing = 6;
        int col1X = this.width / 2 - optionWidth - (colSpacing / 2);
        int col2X = this.width / 2 + (colSpacing / 2);

        for (int i = startIndex; i < endIndex; i++) {
            HyperionOption<?> opt = options.get(i);
            int itemIndex = i - startIndex;
            int row = itemIndex / 2;
            int col = itemIndex % 2;

            int optX = (col == 0) ? col1X : col2X;
            int optY = gridTop + row * (optionHeight + rowSpacing);

            Button optBtn = createOptionButton(opt, config, optX, optY, optionWidth, optionHeight);
            this.addRenderableWidget(optBtn);
        }

        // 3. Pagination Controls (if more than 1 page)
        if (totalPages > 1) {
            int navY = gridTop + (4 * (optionHeight + rowSpacing)) + 2;

            Button prevBtn = Button.builder(Component.literal("◀ Назад"), btn -> {
                if (pageIndex > 0) {
                    pageIndex--;
                    rebuildScreen();
                }
            }).bounds(this.width / 2 - 110, navY, 60, 18).build();
            prevBtn.active = (pageIndex > 0);
            this.addRenderableWidget(prevBtn);

            Button pageIndicator = Button.builder(Component.literal((pageIndex + 1) + " / " + totalPages), btn -> {})
                .bounds(this.width / 2 - 45, navY, 90, 18).build();
            pageIndicator.active = false;
            this.addRenderableWidget(pageIndicator);

            Button nextBtn = Button.builder(Component.literal("Вперед ▶"), btn -> {
                if (pageIndex < totalPages - 1) {
                    pageIndex++;
                    rebuildScreen();
                }
            }).bounds(this.width / 2 + 50, navY, 60, 18).build();
            nextBtn.active = (pageIndex < totalPages - 1);
            this.addRenderableWidget(nextBtn);
        }

        // 4. Bottom Presets Bar (y = this.height - 48)
        int presetY = this.height - 48;
        HyperionConfigStorage.Preset[] presets = HyperionConfigStorage.Preset.values();
        int presetBtnWidth = 92;
        int presetTotalWidth = presets.length * (presetBtnWidth + 4);
        int presetStartX = (this.width - presetTotalWidth) / 2;

        for (int i = 0; i < presets.length; i++) {
            HyperionConfigStorage.Preset preset = presets[i];
            int px = presetStartX + i * (presetBtnWidth + 4);

            Button pBtn = Button.builder(Component.literal(preset.getTitle().split(" ")[0]), btn -> {
                model.applyPreset(preset);
                rebuildScreen();
            }).bounds(px, presetY, presetBtnWidth, 18)
              .tooltip(Tooltip.create(Component.literal(preset.getTitle() + "\n" + preset.getDescription())))
              .build();

            this.addRenderableWidget(pBtn);
        }

        // 5. Bottom Action Bar (y = this.height - 26)
        int actionY = this.height - 26;
        int actionBtnWidth = 120;

        Button resetBtn = Button.builder(Component.literal("🔄 Сброс"), btn -> {
            model.resetToDefaults();
            rebuildScreen();
        }).bounds(this.width / 2 - actionBtnWidth - 8, actionY, actionBtnWidth, 20)
          .tooltip(Tooltip.create(Component.literal("Сбросить все параметры к значениям по умолчанию")))
          .build();
        this.addRenderableWidget(resetBtn);

        Button doneBtn = Button.builder(Component.literal("💾 Сохранить и закрыть"), btn -> {
            model.saveAndApply();
            onClose();
        }).bounds(this.width / 2 + 8, actionY, actionBtnWidth + 30, 20)
          .tooltip(Tooltip.create(Component.literal("Применить настройки и сохранить в config/hyperion-optimizer.json")))
          .build();
        this.addRenderableWidget(doneBtn);
    }

    @SuppressWarnings("unchecked")
    private Button createOptionButton(HyperionOption<?> opt, HyperionConfig config, int x, int y, int w, int h) {
        String tooltipText = opt.getTooltip() != null ? opt.getTooltip() : opt.getName();

        if (opt.getType() == HyperionOption.OptionType.BOOLEAN) {
            HyperionOption<Boolean> bOpt = (HyperionOption<Boolean>) opt;
            boolean val = bOpt.getValue(config);
            String label = truncate(opt.getName(), 18) + ": " + (val ? "§aВКЛ§r" : "§7ВЫКЛ§r");

            return Button.builder(Component.literal(label), btn -> {
                boolean next = !bOpt.getValue(config);
                bOpt.setValue(config, next);
                model.markDirty();
                btn.setMessage(Component.literal(truncate(opt.getName(), 18) + ": " + (next ? "§aВКЛ§r" : "§7ВЫКЛ§r")));
            }).bounds(x, y, w, h)
              .tooltip(Tooltip.create(Component.literal(tooltipText + "\n[Клик для переключения]")))
              .build();

        } else if (opt.getType() == HyperionOption.OptionType.CYCLE) {
            HyperionOption<String> cOpt = (HyperionOption<String>) opt;
            String currentVal = cOpt.getValue(config);
            String displayVal = getDisplayValue(cOpt, currentVal);
            String label = truncate(opt.getName(), 14) + ": §e" + truncate(displayVal, 10) + "§r";

            return Button.builder(Component.literal(label), btn -> {
                String[] vals = cOpt.getPossibleValues();
                if (vals != null && vals.length > 0) {
                    int currIdx = 0;
                    String curr = cOpt.getValue(config);
                    for (int j = 0; j < vals.length; j++) {
                        if (vals[j].equalsIgnoreCase(curr)) {
                            currIdx = j;
                            break;
                        }
                    }
                    int nextIdx = (currIdx + 1) % vals.length;
                    cOpt.setValue(config, vals[nextIdx]);
                    model.markDirty();
                    String nextDisp = getDisplayValue(cOpt, vals[nextIdx]);
                    btn.setMessage(Component.literal(truncate(opt.getName(), 14) + ": §e" + truncate(nextDisp, 10) + "§r"));
                }
            }).bounds(x, y, w, h)
              .tooltip(Tooltip.create(Component.literal(tooltipText + "\n[Клик для смены режима]")))
              .build();

        } else if (opt.getType() == HyperionOption.OptionType.INT_SLIDER) {
            HyperionOption<Integer> iOpt = (HyperionOption<Integer>) opt;
            int currentVal = iOpt.getValue(config);
            String label = truncate(opt.getName(), 16) + ": §b" + currentVal + "§r";

            return Button.builder(Component.literal(label), btn -> {
                int min = (int) iOpt.getMinValue();
                int max = (int) iOpt.getMaxValue();
                int step = (int) iOpt.getStep();
                if (step <= 0) step = 1;

                int nextVal = iOpt.getValue(config) + step;
                if (nextVal > max) nextVal = min;
                iOpt.setValue(config, nextVal);
                model.markDirty();
                btn.setMessage(Component.literal(truncate(opt.getName(), 16) + ": §b" + nextVal + "§r"));
            }).bounds(x, y, w, h)
              .tooltip(Tooltip.create(Component.literal(tooltipText + "\n[Диапазон: " + (int) iOpt.getMinValue() + ".." + (int) iOpt.getMaxValue() + "]")))
              .build();

        } else {
            HyperionOption<Double> dOpt = (HyperionOption<Double>) opt;
            double currentVal = dOpt.getValue(config);
            String label = truncate(opt.getName(), 16) + ": §b" + String.format("%.2f", currentVal) + "§r";

            return Button.builder(Component.literal(label), btn -> {
                double min = dOpt.getMinValue();
                double max = dOpt.getMaxValue();
                double step = dOpt.getStep();
                if (step <= 0) step = 0.1;

                double nextVal = dOpt.getValue(config) + step;
                if (nextVal > max + 0.001) nextVal = min;
                dOpt.setValue(config, nextVal);
                model.markDirty();
                btn.setMessage(Component.literal(truncate(opt.getName(), 16) + ": §b" + String.format("%.2f", nextVal) + "§r"));
            }).bounds(x, y, w, h)
              .tooltip(Tooltip.create(Component.literal(tooltipText + "\n[Диапазон: " + String.format("%.2f", dOpt.getMinValue()) + ".." + String.format("%.2f", dOpt.getMaxValue()) + "]")))
              .build();
        }
    }

    private static String getDisplayValue(HyperionOption<String> opt, String value) {
        String[] possible = opt.getPossibleValues();
        String[] display = opt.getDisplayNames();
        if (possible != null && display != null && possible.length == display.length) {
            for (int i = 0; i < possible.length; i++) {
                if (possible[i].equalsIgnoreCase(value)) {
                    return display[i];
                }
            }
        }
        return value != null ? value : "";
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 1) + "…";
    }

    private static String getShortCategoryName(HyperionCategory cat) {
        switch (cat) {
            case GRAPHICS_SETTINGS: return "🖥️ Графика";
            case GPU_VIDEO_SETTINGS: return "🎮 Видеокарта";
            case CPU_PROCESSOR_SETTINGS: return "🧠 Процессор";
            case VOXEL_LOD_INFINITE: return "🌲 Voxel LOD";
            case WORLD_LIGHTING: return "🌍 Мир/Свет";
            case PHYSICS_REDSTONE: return "⚡ Физика";
            case ENTITIES_ANIMATIONS: return "👾 Сущности";
            case NETWORK_MEMORY_AUDIO: return "📡 Память";
            case COLOR_CORRECTION: return "🎨 HDR/Цвет";
            case ADVANCED_TWEAKS: return "⚙️ Твики";
            default: return cat.name();
        }
    }

    public static void setMinecraftScreen(Object mc, Object screen) {
        if (mc == null) return;
        try {
            for (java.lang.reflect.Method m : mc.getClass().getMethods()) {
                if ((m.getName().equals("setScreen") || m.getName().equals("setScreenAndShow") || m.getName().equals("method_1507"))
                        && m.getParameterCount() == 1) {
                    m.invoke(mc, screen);
                    return;
                }
            }
        } catch (Throwable ignored) {}
    }

    public static Screen getCurrentScreen(Object mc) {
        if (mc == null) return null;
        try {
            for (java.lang.reflect.Field f : mc.getClass().getFields()) {
                if (Screen.class.isAssignableFrom(f.getType())) {
                    Object val = f.get(mc);
                    if (val instanceof Screen) return (Screen) val;
                }
            }
            for (java.lang.reflect.Field f : mc.getClass().getDeclaredFields()) {
                if (Screen.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    Object val = f.get(mc);
                    if (val instanceof Screen) return (Screen) val;
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            setMinecraftScreen(this.minecraft, this.parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
