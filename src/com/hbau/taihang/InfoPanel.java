package com.hbau.taihang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class InfoPanel extends JPanel {
    private final JLabel lblWord = liveLabel("当前词汇: -", UiTheme.ACCENT, true);
    private final JLabel lblWave = liveLabel("波次: 第 0 波", UiTheme.BODY, false);
    private final JLabel lblScore = liveLabel("分数: 0", UiTheme.BODY, false);
    private final JLabel lblLives = liveLabel("生命: 0", UiTheme.BODY, false);
    private final JLabel lblEnergy = liveLabel("能量: 0", UiTheme.BODY, false);
    private final JLabel lblTowers = liveLabel("已购塔: 0", UiTheme.BODY, false);
    private final JLabel lblTowerType = liveLabel("塔型: -", UiTheme.BODY, false);
    private final JLabel lblTowerRole = liveLabel("定位: -", UiTheme.BODY, false);
    private final JLabel lblTowerFeature = liveLabel("特性: -", UiTheme.BODY, false);
    private final JLabel lblTowerCost = liveLabel("价格: 0", UiTheme.BODY, false);
    private final JLabel lblStatus = liveLabel("状态: 等待开始", UiTheme.BODY, false);

    private GameEngine engine;
    private Timer timer;
    private PropertyChangeListener engineListener;

    public InfoPanel() {
        setPreferredSize(new Dimension(280, 0));
        setBackground(new Color(0xF2FAF3));
        setBorder(new EmptyBorder(14, 14, 14, 14));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(UiTheme.infoCard("战场信息",
                lblWord,
                lblWave,
                lblScore,
                lblLives,
                lblEnergy,
                lblTowers));
        add(Box.createVerticalStrut(12));
        add(UiTheme.infoCard("当前选择",
                lblTowerType,
                lblTowerRole,
                lblTowerFeature,
                lblTowerCost));
        add(Box.createVerticalStrut(12));
        add(UiTheme.infoCard("提示",
                lblStatus));
    }

    public void setEngine(GameEngine engine) {
        // unregister previous listener if any
        if (this.engine != null && engineListener != null) {
            this.engine.removePropertyChangeListener(engineListener);
        }
        this.engine = engine;
        // create a listener to update the displayed current word immediately
        engineListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (GameEngine.PROP_CURRENT_WORD.equals(evt.getPropertyName())) {
                    SwingUtilities.invokeLater(() -> updateWordLabel());
                }
            }
        };
        if (this.engine != null) {
            this.engine.addPropertyChangeListener(engineListener);
        }
        if (timer == null) {
            timer = new Timer(250, e -> updateInfo());
            timer.start();
        }
        updateInfo();
    }

    private void updateWordLabel() {
        if (engine == null) {
            lblWord.setText(wrapLine("当前词汇: -"));
            return;
        }
        String english = engine.getCurrentWord();
        String chinese = engine.getCurrentWordChinese();
        if (chinese != null && !chinese.trim().isEmpty()) {
            // 直接设置HTML，不经过wrapLine转义
            lblWord.setText("<html><div style='width:220px;'>" +
                    "词汇: <span style='color:#1B5E20;font-weight:bold;'>" + safeText(english) + "</span><br/>" +
                    "释义: <span style='color:#4CAF50;'>" + safeText(chinese) + "</span>" +
                    "</div></html>");
        } else {
            lblWord.setText(wrapLine("当前词汇: " + safeText(english)));
        }
    }

    private JLabel liveLabel(String text, Color color, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(bold ? FontLibrary.titleFont(15f) : FontLibrary.bodyFont(15f));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void updateInfo() {
        if (engine == null) {
            return;
        }
        updateWordLabel();
        lblWave.setText("波次: 第 " + engine.getWave() + " 波");
        lblScore.setText("分数: " + engine.getScore());
        lblLives.setText("生命: " + engine.getLives());
        lblEnergy.setText("能量: " + engine.getEnergy() + "/" + engine.getMaxEnergy());
        lblTowers.setText("已购塔: " + engine.getPurchasedTowerCount());
        lblTowerType.setText("塔型: " + engine.getTowerShop().getDisplayName(engine.getSelectedTowerType()));
        lblTowerRole.setText("定位: " + engine.getTowerShop().getRoleHint(engine.getSelectedTowerType()));
        lblTowerFeature.setText("特性: " + engine.getTowerShop().getFeatureText(engine.getSelectedTowerType()));
        lblTowerCost.setText("价格: " + engine.getSelectedTowerCost());
        lblStatus.setText(wrapLine("状态: " + safeText(engine.getStatusMessage())));
    }

    private String safeText(String text) {
        return (text == null || text.trim().isEmpty()) ? "-" : text;
    }

    private String wrapLine(String text) {
        String safe = safeText(text)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
        // HTML width helps long status/word lines wrap instead of truncating.
        return "<html><div style='width:220px;'>" + safe + "</div></html>";
    }
}
