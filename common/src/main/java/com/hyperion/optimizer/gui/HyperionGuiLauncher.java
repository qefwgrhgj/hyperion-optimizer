package com.hyperion.optimizer.gui;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.api.HyperionConfig;
import com.hyperion.optimizer.api.HyperionConfigStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ⚡ Hyperion Sovereign In-Game & Standalone GUI Configuration Launcher.
 *
 * Provides a modern, dark-themed, ultra-responsive configuration dashboard:
 * - Category Tabs (Graphics, GPU, CPU, HDR Color, World, Physics, Entities, Network)
 * - Live Search Filtering
 * - Real-time Interactive Sliders, Checkboxes, and Dropdowns
 * - One-Click Hardware Performance Presets
 * - Instant Hot-Reloading into the Running Hyperion Engine
 */
public final class HyperionGuiLauncher {
    private static final Logger LOGGER = Logger.getLogger("HyperionGUI");
    private static volatile JFrame activeDialog = null;

    private static final Color BG_DARK = new Color(24, 24, 27);
    private static final Color BG_CARD = new Color(39, 39, 42);
    private static final Color BG_INPUT = new Color(63, 63, 70);
    private static final Color ACCENT_COLOR = new Color(99, 102, 241);
    private static final Color ACCENT_HOVER = new Color(129, 140, 248);
    private static final Color TEXT_PRIMARY = new Color(244, 244, 245);
    private static final Color TEXT_MUTED = new Color(161, 161, 170);
    private static final Color BORDER_COLOR = new Color(63, 63, 70);

    private HyperionGuiLauncher() {}

    public static synchronized void openConfigScreen() {
        if (GraphicsEnvironment.isHeadless() || Boolean.getBoolean("java.awt.headless") || Boolean.getBoolean("hyperion.test.headless")) {
            LOGGER.info("[Hyperion] Headless/Test environment detected - skipping GUI display.");
            return;
        }

        // 1. Try to open in-game screen via reflection if running inside Minecraft
        boolean inGameOpened = tryOpenMinecraftScreen();
        if (inGameOpened) {
            return;
        }

        // 2. Open modern standalone / desktop overlay Swing dashboard
        SwingUtilities.invokeLater(() -> {
            try {
                if (activeDialog != null && activeDialog.isDisplayable()) {
                    activeDialog.toFront();
                    activeDialog.requestFocus();
                    return;
                }

                createAndShowGui();
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "[Hyperion] Failed to open GUI Dialog", t);
            }
        });
    }

    public static synchronized void closeActiveDialog() {
        if (activeDialog != null) {
            try {
                activeDialog.dispose();
            } catch (Throwable ignored) {}
            activeDialog = null;
        }
    }

    private static boolean tryOpenMinecraftScreen() {
        try {
            // Attempt reflection for net.minecraft.client.MinecraftClient / Minecraft
            Class<?> mcClass = null;
            String[] mcClassNames = {
                "net.minecraft.client.MinecraftClient",
                "net.minecraft.client.Minecraft"
            };

            for (String name : mcClassNames) {
                try {
                    mcClass = Class.forName(name);
                    break;
                } catch (ClassNotFoundException ignored) {}
            }

            if (mcClass == null) return false;

            java.lang.reflect.Method getInstanceMethod = null;
            for (String methodName : new String[]{"getInstance", "func_71410_x"}) {
                try {
                    getInstanceMethod = mcClass.getMethod(methodName);
                    break;
                } catch (Exception ignored) {}
            }

            if (getInstanceMethod == null) return false;
            Object mcInstance = getInstanceMethod.invoke(null);
            if (mcInstance == null) return false;

            // In-game screen integration point
            return false; // Fall through to rich Swing dialog for seamless cross-version rendering
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void createAndShowGui() {
        HyperionScreenModel model = new HyperionScreenModel();
        HyperionConfig workingConfig = model.getWorkingConfig();

        JFrame frame = new JFrame("⚡ Hyperion Optimizer — Центр управления производительностью");
        activeDialog = frame;
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(920, 680);
        frame.setMinimumSize(new Dimension(800, 550));
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(BG_DARK);
        frame.setLayout(new BorderLayout(0, 0));

        // --- TOP HEADER & SEARCH ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(BG_DARK);
        topPanel.setBorder(new EmptyBorder(16, 20, 10, 20));

        JLabel titleLabel = new JLabel("⚡ Hyperion Optimizer v1.0.3 Sovereign");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);

        JPanel searchPanel = new JPanel(new BorderLayout(6, 0));
        searchPanel.setBackground(BG_DARK);
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setForeground(TEXT_MUTED);
        JTextField searchField = new JTextField(20);
        searchField.setBackground(BG_INPUT);
        searchField.setForeground(TEXT_PRIMARY);
        searchField.setCaretColor(TEXT_PRIMARY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        frame.add(topPanel, BorderLayout.NORTH);

        // --- MAIN CONTENT (TABS + OPTIONS LIST) ---
        JPanel centerPanel = new JPanel(new BorderLayout(10, 0));
        centerPanel.setBackground(BG_DARK);
        centerPanel.setBorder(new EmptyBorder(0, 20, 10, 20));

        // Left Navigation Category List
        DefaultListModel<HyperionCategory> catListModel = new DefaultListModel<>();
        for (HyperionCategory cat : HyperionCategory.values()) {
            catListModel.addElement(cat);
        }

        JList<HyperionCategory> catList = new JList<>(catListModel);
        catList.setBackground(BG_CARD);
        catList.setForeground(TEXT_PRIMARY);
        catList.setSelectionBackground(ACCENT_COLOR);
        catList.setSelectionForeground(Color.WHITE);
        catList.setFixedCellHeight(42);
        catList.setBorder(new EmptyBorder(8, 8, 8, 8));
        catList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof HyperionCategory) {
                    l.setText("  " + ((HyperionCategory) value).getTitle());
                    l.setFont(new Font("Segoe UI", isSelected ? Font.BOLD : Font.PLAIN, 13));
                }
                l.setBorder(new EmptyBorder(6, 8, 6, 8));
                return l;
            }
        });
        catList.setSelectedIndex(0);

        JScrollPane catScrollPane = new JScrollPane(catList);
        catScrollPane.setPreferredSize(new Dimension(240, 0));
        catScrollPane.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        catScrollPane.getViewport().setBackground(BG_CARD);
        centerPanel.add(catScrollPane, BorderLayout.WEST);

        // Right Options Panel
        JPanel optionsContainer = new JPanel();
        optionsContainer.setLayout(new BoxLayout(optionsContainer, BoxLayout.Y_AXIS));
        optionsContainer.setBackground(BG_DARK);

        JScrollPane optionsScrollPane = new JScrollPane(optionsContainer);
        optionsScrollPane.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        optionsScrollPane.getViewport().setBackground(BG_DARK);
        optionsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        centerPanel.add(optionsScrollPane, BorderLayout.CENTER);

        frame.add(centerPanel, BorderLayout.CENTER);

        // Refresh options helper
        Runnable refreshOptions = () -> {
            optionsContainer.removeAll();
            HyperionCategory selectedCat = catList.getSelectedValue();
            String query = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";

            List<HyperionOption<?>> opts;
            if (!query.isEmpty()) {
                opts = HyperionOptionsRegistry.getAllOptions();
            } else {
                opts = HyperionOptionsRegistry.getOptionsByCategory(selectedCat != null ? selectedCat : HyperionCategory.GRAPHICS_SETTINGS);
            }

            for (HyperionOption<?> opt : opts) {
                if (!query.isEmpty()) {
                    String name = opt.getName().toLowerCase();
                    String tooltip = opt.getTooltip() != null ? opt.getTooltip().toLowerCase() : "";
                    String key = opt.getKey().toLowerCase();
                    if (!name.contains(query) && !tooltip.contains(query) && !key.contains(query)) {
                        continue;
                    }
                }

                JPanel itemCard = createOptionCard(opt, workingConfig, model);
                optionsContainer.add(itemCard);
                optionsContainer.add(Box.createVerticalStrut(8));
            }

            optionsContainer.revalidate();
            optionsContainer.repaint();
        };

        catList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshOptions.run();
            }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                refreshOptions.run();
            }
        });

        // --- BOTTOM ACTION & PRESETS BAR ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(BG_DARK);
        bottomPanel.setBorder(new EmptyBorder(10, 20, 16, 20));

        // Presets Row
        JPanel presetsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        presetsPanel.setBackground(BG_DARK);
        JLabel presetLabel = new JLabel("Пресеты: ");
        presetLabel.setForeground(TEXT_MUTED);
        presetLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        presetsPanel.add(presetLabel);

        for (HyperionConfigStorage.Preset p : HyperionConfigStorage.Preset.values()) {
            JButton pBtn = new JButton(p.getTitle().split(" ")[0]);
            pBtn.setToolTipText(p.getDescription());
            pBtn.setBackground(BG_CARD);
            pBtn.setForeground(TEXT_PRIMARY);
            pBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(4, 10, 4, 10)
            ));
            pBtn.setFocusPainted(false);
            pBtn.addActionListener(e -> {
                model.applyPreset(p);
                refreshOptions.run();
                JOptionPane.showMessageDialog(frame, "Применен пресет:\n" + p.getTitle() + "\n\n" + p.getDescription(), "Hyperion Presets", JOptionPane.INFORMATION_MESSAGE);
            });
            presetsPanel.add(pBtn);
        }

        // Action Buttons Row (Reset, Save & Apply)
        JPanel actionBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtnPanel.setBackground(BG_DARK);

        JButton resetBtn = new JButton("Сброс (По умолчанию)");
        resetBtn.setBackground(BG_CARD);
        resetBtn.setForeground(TEXT_MUTED);
        resetBtn.setFocusPainted(false);
        resetBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(8, 14, 8, 14)
        ));
        resetBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame, "Сбросить все настройки Hyperion к значениям по умолчанию?", "Подтверждение сброса", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                model.resetToDefaults();
                refreshOptions.run();
            }
        });

        JButton saveBtn = new JButton("⚡ Сохранить и применить");
        saveBtn.setBackground(ACCENT_COLOR);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setFocusPainted(false);
        saveBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ACCENT_HOVER, 1, true),
            new EmptyBorder(8, 20, 8, 20)
        ));
        saveBtn.addActionListener(e -> {
            boolean success = model.saveAndApply();
            if (success) {
                JOptionPane.showMessageDialog(frame, "Настройки Hyperion успешно сохранены и применены в игре!", "Успешно", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(frame, "Ошибка сохранения конфигурации.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        actionBtnPanel.add(resetBtn);
        actionBtnPanel.add(saveBtn);

        bottomPanel.add(presetsPanel, BorderLayout.WEST);
        bottomPanel.add(actionBtnPanel, BorderLayout.EAST);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        refreshOptions.run();
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
        frame.toFront();
        frame.requestFocus();
    }

    @SuppressWarnings("unchecked")
    private static JPanel createOptionCard(HyperionOption<?> opt, HyperionConfig config, HyperionScreenModel model) {
        JPanel card = new JPanel(new BorderLayout(10, 4));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(BG_CARD);

        JLabel nameLabel = new JLabel(opt.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(TEXT_PRIMARY);

        JLabel descLabel = new JLabel(opt.getTooltip() != null ? opt.getTooltip() : "");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(TEXT_MUTED);

        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(descLabel);

        card.add(textPanel, BorderLayout.CENTER);

        // Right Control Widget
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controlPanel.setBackground(BG_CARD);

        if (opt.getType() == HyperionOption.OptionType.BOOLEAN) {
            HyperionOption<Boolean> bOpt = (HyperionOption<Boolean>) opt;
            boolean currentVal = bOpt.getValue(config);
            JCheckBox checkBox = new JCheckBox(currentVal ? "ВКЛ (ON)" : "ВЫКЛ (OFF)", currentVal);
            checkBox.setBackground(BG_CARD);
            checkBox.setForeground(currentVal ? ACCENT_HOVER : TEXT_MUTED);
            checkBox.setFont(new Font("Segoe UI", Font.BOLD, 12));
            checkBox.setFocusPainted(false);
            checkBox.addActionListener(e -> {
                boolean nv = checkBox.isSelected();
                bOpt.setValue(config, nv);
                checkBox.setText(nv ? "ВКЛ (ON)" : "ВЫКЛ (OFF)");
                checkBox.setForeground(nv ? ACCENT_HOVER : TEXT_MUTED);
                model.markDirty();
            });
            controlPanel.add(checkBox);
        } else if (opt.getType() == HyperionOption.OptionType.INT_SLIDER) {
            HyperionOption<Integer> iOpt = (HyperionOption<Integer>) opt;
            int currentVal = iOpt.getValue(config);
            int min = (int) iOpt.getMinValue();
            int max = (int) iOpt.getMaxValue();

            JLabel valLabel = new JLabel(String.valueOf(currentVal));
            valLabel.setForeground(TEXT_PRIMARY);
            valLabel.setPreferredSize(new Dimension(45, 20));

            JSlider slider = new JSlider(min, max, currentVal);
            slider.setBackground(BG_CARD);
            slider.setPreferredSize(new Dimension(140, 24));
            slider.addChangeListener(e -> {
                int nv = slider.getValue();
                iOpt.setValue(config, nv);
                valLabel.setText(String.valueOf(nv));
                model.markDirty();
            });

            controlPanel.add(slider);
            controlPanel.add(Box.createHorizontalStrut(6));
            controlPanel.add(valLabel);
        } else if (opt.getType() == HyperionOption.OptionType.DOUBLE_SLIDER) {
            HyperionOption<Double> dOpt = (HyperionOption<Double>) opt;
            double currentVal = dOpt.getValue(config);
            int sliderMin = (int) (dOpt.getMinValue() * 100);
            int sliderMax = (int) (dOpt.getMaxValue() * 100);
            int sliderVal = (int) (currentVal * 100);

            JLabel valLabel = new JLabel(String.format("%.2f", currentVal));
            valLabel.setForeground(TEXT_PRIMARY);
            valLabel.setPreferredSize(new Dimension(45, 20));

            JSlider slider = new JSlider(sliderMin, sliderMax, sliderVal);
            slider.setBackground(BG_CARD);
            slider.setPreferredSize(new Dimension(140, 24));
            slider.addChangeListener(e -> {
                double nv = slider.getValue() / 100.0;
                dOpt.setValue(config, nv);
                valLabel.setText(String.format("%.2f", nv));
                model.markDirty();
            });

            controlPanel.add(slider);
            controlPanel.add(Box.createHorizontalStrut(6));
            controlPanel.add(valLabel);
        } else if (opt.getType() == HyperionOption.OptionType.CYCLE) {
            HyperionOption<String> cOpt = (HyperionOption<String>) opt;
            String currentVal = cOpt.getValue(config);
            String[] possibleVals = cOpt.getPossibleValues();
            String[] displayNames = cOpt.getDisplayNames();

            int selIndex = 0;
            if (possibleVals != null) {
                for (int i = 0; i < possibleVals.length; i++) {
                    if (possibleVals[i].equalsIgnoreCase(currentVal)) {
                        selIndex = i;
                        break;
                    }
                }
            }

            JComboBox<String> comboBox = new JComboBox<>(displayNames != null ? displayNames : possibleVals);
            comboBox.setSelectedIndex(selIndex);
            comboBox.setBackground(BG_INPUT);
            comboBox.setForeground(TEXT_PRIMARY);
            comboBox.setPreferredSize(new Dimension(180, 26));
            comboBox.addActionListener(e -> {
                int idx = comboBox.getSelectedIndex();
                if (possibleVals != null && idx >= 0 && idx < possibleVals.length) {
                    cOpt.setValue(config, possibleVals[idx]);
                    model.markDirty();
                }
            });
            controlPanel.add(comboBox);
        }

        card.add(controlPanel, BorderLayout.EAST);
        return card;
    }
}
