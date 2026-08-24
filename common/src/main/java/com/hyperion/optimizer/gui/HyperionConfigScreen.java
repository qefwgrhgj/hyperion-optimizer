package com.hyperion.optimizer.gui;

import java.util.ArrayList;
import java.util.List;

/**
 * HyperionConfigScreen
 * Interactive In-Game Configuration Screen Controller.
 * Provides unified cross-platform UI logic for tabs:
 * 1. 🖥️ Настройки графики (Graphics Settings)
 * 2. 🎮 Настройки видеокарт (GPU & Video Card Settings)
 * 3. 🧠 Настройки процессора (CPU & Multithreading Settings)
 */
public class HyperionConfigScreen {
    private final HyperionScreenModel model;
    private String searchQuery = "";
    private int selectedOptionIndex = 0;
    private int scrollOffset = 0;
    private final int maxVisibleOptions = 10;

    public HyperionConfigScreen() {
        this.model = new HyperionScreenModel();
    }

    public HyperionConfigScreen(HyperionScreenModel model) {
        this.model = model != null ? model : new HyperionScreenModel();
    }

    public HyperionScreenModel getModel() {
        return model;
    }

    public void selectCategory(HyperionCategory category) {
        if (category != null) {
            model.setActiveCategory(category);
            this.selectedOptionIndex = 0;
            this.scrollOffset = 0;
        }
    }

    public void selectGraphicsTab() {
        selectCategory(HyperionCategory.GRAPHICS_SETTINGS);
    }

    public void selectGpuTab() {
        selectCategory(HyperionCategory.GPU_VIDEO_SETTINGS);
    }

    public void selectCpuTab() {
        selectCategory(HyperionCategory.CPU_PROCESSOR_SETTINGS);
    }

    public List<HyperionOption<?>> getFilteredOptions() {
        List<HyperionOption<?>> options = model.getCurrentOptions();
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return options;
        }

        String q = searchQuery.toLowerCase().trim();
        List<HyperionOption<?>> filtered = new ArrayList<>();
        for (HyperionOption<?> opt : options) {
            if (opt.getName().toLowerCase().contains(q) || opt.getTooltip().toLowerCase().contains(q) || opt.getKey().toLowerCase().contains(q)) {
                filtered.add(opt);
            }
        }
        return filtered;
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query != null ? query : "";
        this.selectedOptionIndex = 0;
        this.scrollOffset = 0;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public int getSelectedOptionIndex() {
        return selectedOptionIndex;
    }

    public void setSelectedOptionIndex(int index) {
        List<HyperionOption<?>> opts = getFilteredOptions();
        if (!opts.isEmpty()) {
            this.selectedOptionIndex = Math.max(0, Math.min(opts.size() - 1, index));
            if (selectedOptionIndex < scrollOffset) {
                scrollOffset = selectedOptionIndex;
            } else if (selectedOptionIndex >= scrollOffset + maxVisibleOptions) {
                scrollOffset = selectedOptionIndex - maxVisibleOptions + 1;
            }
        }
    }

    public void scroll(int delta) {
        List<HyperionOption<?>> opts = getFilteredOptions();
        int maxOffset = Math.max(0, opts.size() - maxVisibleOptions);
        this.scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset + delta));
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public int getMaxVisibleOptions() {
        return maxVisibleOptions;
    }

    public void toggleOrCycleSelectedOption() {
        List<HyperionOption<?>> opts = getFilteredOptions();
        if (selectedOptionIndex >= 0 && selectedOptionIndex < opts.size()) {
            HyperionOption<?> opt = opts.get(selectedOptionIndex);
            toggleOption(opt);
        }
    }

    @SuppressWarnings("unchecked")
    public void toggleOption(HyperionOption<?> opt) {
        if (opt == null) return;
        if (opt.getType() == HyperionOption.OptionType.BOOLEAN) {
            HyperionOption<Boolean> bOpt = (HyperionOption<Boolean>) opt;
            boolean current = bOpt.getValue(model.getWorkingConfig());
            bOpt.setValue(model.getWorkingConfig(), !current);
            model.markDirty();
            model.setStatusMessage("Изменено: " + opt.getName() + " -> " + (!current ? "ВКЛ" : "ВЫКЛ"));
        } else if (opt.getType() == HyperionOption.OptionType.CYCLE) {
            HyperionOption<String> cOpt = (HyperionOption<String>) opt;
            String current = cOpt.getValue(model.getWorkingConfig());
            String[] vals = cOpt.getPossibleValues();
            if (vals != null && vals.length > 0) {
                int currIdx = 0;
                for (int i = 0; i < vals.length; i++) {
                    if (vals[i].equalsIgnoreCase(current)) {
                        currIdx = i;
                        break;
                    }
                }
                int nextIdx = (currIdx + 1) % vals.length;
                cOpt.setValue(model.getWorkingConfig(), vals[nextIdx]);
                model.markDirty();
                model.setStatusMessage("Изменено: " + opt.getName() + " -> " + vals[nextIdx]);
            }
        }
    }
}
