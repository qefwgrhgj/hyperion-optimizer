package com.hyperion.optimizer.gui;

import com.hyperion.optimizer.api.HyperionConfig;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class HyperionOption<T> {
    public enum OptionType {
        BOOLEAN,
        INT_SLIDER,
        DOUBLE_SLIDER,
        CYCLE
    }

    private final String key;
    private final String name;
    private final String tooltip;
    private final HyperionCategory category;
    private final OptionType type;
    private final Function<HyperionConfig, T> getter;
    private final BiConsumer<HyperionConfig, T> setter;
    private final T defaultValue;
    private final double minValue;
    private final double maxValue;
    private final double step;
    private final String[] possibleValues;
    private final String[] displayNames;

    public HyperionOption(
            String key,
            String name,
            String tooltip,
            HyperionCategory category,
            OptionType type,
            Function<HyperionConfig, T> getter,
            BiConsumer<HyperionConfig, T> setter,
            T defaultValue,
            double minValue,
            double maxValue,
            double step,
            String[] possibleValues,
            String[] displayNames) {
        this.key = key;
        this.name = name;
        this.tooltip = tooltip;
        this.category = category;
        this.type = type;
        this.getter = getter;
        this.setter = setter;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
        this.possibleValues = possibleValues;
        this.displayNames = displayNames;
    }

    public static HyperionOption<Boolean> createBoolean(
            String key,
            String name,
            String tooltip,
            HyperionCategory category,
            Function<HyperionConfig, Boolean> getter,
            BiConsumer<HyperionConfig, Boolean> setter,
            boolean def) {
        return new HyperionOption<>(key, name, tooltip, category, OptionType.BOOLEAN, getter, setter, def, 0, 1, 1, null, null);
    }

    public static HyperionOption<Integer> createIntSlider(
            String key,
            String name,
            String tooltip,
            HyperionCategory category,
            Function<HyperionConfig, Integer> getter,
            BiConsumer<HyperionConfig, Integer> setter,
            int def,
            int min,
            int max,
            int step) {
        return new HyperionOption<>(key, name, tooltip, category, OptionType.INT_SLIDER, getter, setter, def, min, max, step, null, null);
    }

    public static HyperionOption<Double> createDoubleSlider(
            String key,
            String name,
            String tooltip,
            HyperionCategory category,
            Function<HyperionConfig, Double> getter,
            BiConsumer<HyperionConfig, Double> setter,
            double def,
            double min,
            double max,
            double step) {
        return new HyperionOption<>(key, name, tooltip, category, OptionType.DOUBLE_SLIDER, getter, setter, def, min, max, step, null, null);
    }

    public static HyperionOption<String> createCycle(
            String key,
            String name,
            String tooltip,
            HyperionCategory category,
            Function<HyperionConfig, String> getter,
            BiConsumer<HyperionConfig, String> setter,
            String[] possibleValues,
            String[] displayNames,
            String def) {
        return new HyperionOption<>(key, name, tooltip, category, OptionType.CYCLE, getter, setter, def, 0, possibleValues != null ? possibleValues.length - 1 : 0, 1, possibleValues, displayNames);
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getTooltip() {
        return tooltip;
    }

    public HyperionCategory getCategory() {
        return category;
    }

    public OptionType getType() {
        return type;
    }

    public T getValue(HyperionConfig config) {
        return getter.apply(config);
    }

    public void setValue(HyperionConfig config, T value) {
        setter.accept(config, value);
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getStep() {
        return step;
    }

    public String[] getPossibleValues() {
        return possibleValues;
    }

    public String[] getDisplayNames() {
        return displayNames;
    }
}
