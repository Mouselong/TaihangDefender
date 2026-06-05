package com.hbau.taihang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsScreen extends JPanel {

    private final Image background = ScreenAssets.loadOrCreateBackground(1280, 720);

    public SettingsScreen(GameSettings settings, Runnable onBack, Runnable onSave) {
        setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.insets = new Insets(10, 18, 10, 18);

        JPanel card = UiTheme.cardPanel(720, 900);
        card.setBackground(new Color(255, 255, 255, 242));

        JLabel title = UiTheme.titleLabel("设置", 34f);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = UiTheme.subtitleLabel("调整难度、音效和游戏参数", 18f);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(20));

        // ---- ALL CONTROLS CENTERED ----

        JLabel diffLabel = UiTheme.accentLabel("难度选择：", 18f);
        diffLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(diffLabel);
        card.add(Box.createVerticalStrut(6));

        JComboBox<Difficulty> difficultyBox = new JComboBox<>(Difficulty.values());
        difficultyBox.setSelectedItem(settings.getDifficulty());
        difficultyBox.setFont(FontLibrary.bodyFont(16f));
        difficultyBox.setMaximumSize(new Dimension(360, 34));
        difficultyBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(difficultyBox);
        card.add(Box.createVerticalStrut(18));

        // 音效开关
        JCheckBox soundBox = new JCheckBox("开启音效");
        soundBox.setSelected(settings.isSoundEnabled());
        soundBox.setFont(FontLibrary.bodyFont(17f));
        soundBox.setOpaque(false);
        soundBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(soundBox);
        card.add(Box.createVerticalStrut(14));

        // 音效音量
        JLabel soundVolLabel = new JLabel(String.format("音效音量：%.0f%%", settings.getSoundVolume() * 100));
        soundVolLabel.setFont(FontLibrary.bodyFont(17f));
        soundVolLabel.setForeground(UiTheme.BODY);
        soundVolLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(soundVolLabel);
        card.add(Box.createVerticalStrut(4));

        JSlider soundVolSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, (int)(settings.getSoundVolume() * 100));
        soundVolSlider.setOpaque(false);
        soundVolSlider.setMajorTickSpacing(25);
        soundVolSlider.setMinorTickSpacing(10);
        soundVolSlider.setPaintTicks(true);
        soundVolSlider.setPaintLabels(true);
        soundVolSlider.setFont(FontLibrary.bodyFont(12f));
        soundVolSlider.setForeground(UiTheme.BODY);
        soundVolSlider.setMaximumSize(new Dimension(400, 50));
        soundVolSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        soundVolSlider.addChangeListener(e -> {
            float val = soundVolSlider.getValue() / 100f;
            soundVolLabel.setText(String.format("音效音量：%.0f%%", val * 100));
        });
        card.add(soundVolSlider);
        card.add(Box.createVerticalStrut(18));

        // BGM开关
        JCheckBox bgmBox = new JCheckBox("开启背景音乐");
        bgmBox.setSelected(settings.isBgmEnabled());
        bgmBox.setFont(FontLibrary.bodyFont(17f));
        bgmBox.setOpaque(false);
        bgmBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(bgmBox);
        card.add(Box.createVerticalStrut(14));

        // BGM音量
        JLabel bgmVolLabel = new JLabel(String.format("背景音乐音量：%.0f%%", settings.getBgmVolume() * 100));
        bgmVolLabel.setFont(FontLibrary.bodyFont(17f));
        bgmVolLabel.setForeground(UiTheme.BODY);
        bgmVolLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(bgmVolLabel);
        card.add(Box.createVerticalStrut(4));

        JSlider bgmVolSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, (int)(settings.getBgmVolume() * 100));
        bgmVolSlider.setOpaque(false);
        bgmVolSlider.setMajorTickSpacing(25);
        bgmVolSlider.setMinorTickSpacing(10);
        bgmVolSlider.setPaintTicks(true);
        bgmVolSlider.setPaintLabels(true);
        bgmVolSlider.setFont(FontLibrary.bodyFont(12f));
        bgmVolSlider.setForeground(UiTheme.BODY);
        bgmVolSlider.setMaximumSize(new Dimension(400, 50));
        bgmVolSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        bgmVolSlider.addChangeListener(e -> {
            float val = bgmVolSlider.getValue() / 100f;
            bgmVolLabel.setText(String.format("背景音乐音量：%.0f%%", val * 100));
        });
        card.add(bgmVolSlider);
        card.add(Box.createVerticalStrut(18));

        JLabel speedLabel = new JLabel(String.format("敌人移动速度倍率：%.1fx", settings.getEnemySpeedMultiplier()));
        speedLabel.setFont(FontLibrary.bodyFont(17f));
        speedLabel.setForeground(UiTheme.BODY);
        speedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(speedLabel);
        card.add(Box.createVerticalStrut(4));

        JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 50, 300, (int)(settings.getEnemySpeedMultiplier() * 100));
        speedSlider.setOpaque(false);
        speedSlider.setMajorTickSpacing(50);
        speedSlider.setMinorTickSpacing(25);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setFont(FontLibrary.bodyFont(12f));
        speedSlider.setForeground(UiTheme.BODY);
        speedSlider.setMaximumSize(new Dimension(480, 50));
        speedSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        speedSlider.addChangeListener(e -> {
            double val = speedSlider.getValue() / 100.0;
            speedLabel.setText(String.format("敌人移动速度倍率：%.1fx", val));
        });
        card.add(speedSlider);
        card.add(Box.createVerticalStrut(20));

        JLabel costLabel = new JLabel(String.format("塔价格倍率：%.1fx", settings.getTowerCostMultiplier()));
        costLabel.setFont(FontLibrary.bodyFont(17f));
        costLabel.setForeground(UiTheme.BODY);
        costLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(costLabel);
        card.add(Box.createVerticalStrut(4));

        JSlider costSlider = new JSlider(JSlider.HORIZONTAL, 50, 300, (int)(settings.getTowerCostMultiplier() * 100));
        costSlider.setOpaque(false);
        costSlider.setMajorTickSpacing(50);
        costSlider.setMinorTickSpacing(25);
        costSlider.setPaintTicks(true);
        costSlider.setPaintLabels(true);
        costSlider.setFont(FontLibrary.bodyFont(12f));
        costSlider.setForeground(UiTheme.BODY);
        costSlider.setMaximumSize(new Dimension(480, 50));
        costSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        costSlider.addChangeListener(e -> {
            double val = costSlider.getValue() / 100.0;
            costLabel.setText(String.format("塔价格倍率：%.1fx", val));
        });
        card.add(costSlider);
        card.add(Box.createVerticalStrut(20));

        JPanel tipsPanel = new JPanel();
        tipsPanel.setLayout(new BoxLayout(tipsPanel, BoxLayout.Y_AXIS));
        tipsPanel.setOpaque(true);
        tipsPanel.setBackground(new Color(0xF8FBF8));
        tipsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tipsPanel.setMaximumSize(new Dimension(620, 180));
        tipsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xC8E6C9), 2, true),
                new EmptyBorder(12, 18, 12, 18)));
        addTipLine(tipsPanel, "难度会影响敌人的速度、刷怪间隔和生命值。");
        addTipLine(tipsPanel, "简单模式：敌人速度较慢，适合新手玩家练习；普通模式：标准难度，推荐默认使用。");
        addTipLine(tipsPanel, "调整速度倍率可以改变游戏节奏，数值越高敌人移动越快。");
        addTipLine(tipsPanel, "塔价格倍率会影响购买塔所需的能量，建议根据难度适当调整。");
        addTipLine(tipsPanel, "设置会在点击保存后立即生效，下次游戏启动时自动加载。");
        card.add(tipsPanel);
        card.add(Box.createVerticalStrut(20));

        JButton saveButton = UiTheme.primaryButton("保存设置", UiTheme.BTN_SIZE_MEDIUM);
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveButton.addActionListener(e -> {
            settings.setDifficulty((Difficulty) difficultyBox.getSelectedItem());
            settings.setSoundEnabled(soundBox.isSelected());
            settings.setSoundVolume(soundVolSlider.getValue() / 100f);
            settings.setBgmEnabled(bgmBox.isSelected());
            settings.setBgmVolume(bgmVolSlider.getValue() / 100f);
            settings.setEnemySpeedMultiplier(speedSlider.getValue() / 100.0);
            settings.setTowerCostMultiplier(costSlider.getValue() / 100.0);
            onSave.run();
        });

        JButton backButton = UiTheme.grayButton("返回主菜单", UiTheme.BTN_SIZE_MEDIUM);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> onBack.run());

        JPanel buttonRow = new JPanel();
        buttonRow.setLayout(new FlowLayout(FlowLayout.CENTER, 16, 0));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonRow.setMaximumSize(new Dimension(720, 50));
        buttonRow.add(saveButton);
        buttonRow.add(backButton);
        card.add(buttonRow);

        add(card, gc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setColor(new Color(255, 255, 255, 60));
            g2.fillRoundRect(20, 20, getWidth() - 40, getHeight() - 40, 28, 28);
        } finally {
            g2.dispose();
        }
    }

    private void addTipLine(JPanel panel, String text) {
        JLabel label = new JLabel("<html><div style='width:560px;'>" + text + "</div></html>");
        label.setFont(FontLibrary.bodyFont(14f));
        label.setForeground(new Color(0x37474F));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
    }
}
