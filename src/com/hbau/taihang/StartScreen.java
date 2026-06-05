package com.hbau.taihang;

import javax.swing.*;
import java.awt.*;

public class StartScreen extends JPanel {
    private final Image background = ScreenAssets.loadOrCreateBackground(1280, 720);

    public StartScreen(Runnable onStart, Runnable onSettings, Runnable onHelp, Runnable onAtlas, Runnable onQuit) {
        setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(10, 18, 10, 18);

        JPanel card = UiTheme.cardPanel(560, 560);

        // 全新艺术字标题 - 最可靠的方式
        JPanel titlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                String text = "太行卫士：农大护田战";
                Font font = FontLibrary.titleFont(36f);
                g2.setFont(font);
                FontMetrics fm = g2.getFontMetrics();
                
                int w = getWidth();
                int h = getHeight();
                int x = (w - fm.stringWidth(text)) / 2;
                int y = fm.getAscent() + (h - fm.getHeight()) / 2;

                // 先画深轮廓（更粗）
                g2.setColor(new Color(0x0D47A1));
                g2.drawString(text, x - 1, y);
                g2.drawString(text, x + 1, y);
                g2.drawString(text, x, y - 1);
                g2.drawString(text, x, y + 1);
                
                // 主文字亮蓝色
                g2.setColor(new Color(0x2196F3));
                g2.drawString(text, x, y);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                Font font = FontLibrary.titleFont(36f);
                FontMetrics fm = getFontMetrics(font);
                return new Dimension(fm.stringWidth("太行卫士：农大护田战") + 40, fm.getHeight() + 30);
            }
        };
        titlePanel.setOpaque(false);
        titlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titlePanel);

        card.add(Box.createVerticalStrut(12));

        JLabel subtitle = UiTheme.subtitleLabel("打字获得能量，购买科技助农设备，守护太行果园", 18f);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle);

        card.add(Box.createVerticalStrut(20));

        // 功能按钮行
        JPanel funcRow = new JPanel();
        funcRow.setOpaque(false);
        funcRow.setLayout(new FlowLayout(FlowLayout.CENTER, 16, 0));
        
        JButton helpButton = UiTheme.secondaryButton("帮助", UiTheme.BTN_SIZE_SMALL);
        helpButton.addActionListener(e -> onHelp.run());
        funcRow.add(helpButton);
        
        JButton atlasButton = UiTheme.purpleButton("图鉴", UiTheme.BTN_SIZE_SMALL);
        atlasButton.addActionListener(e -> onAtlas.run());
        funcRow.add(atlasButton);
        
        card.add(funcRow);

        card.add(Box.createVerticalStrut(18));

        // 主操作区
        JButton startButton = UiTheme.primaryButton("开始游戏", UiTheme.BTN_SIZE_LARGE);
        startButton.addActionListener(e -> onStart.run());
        card.add(startButton);

        card.add(Box.createVerticalStrut(12));

        JButton settingsButton = UiTheme.secondaryButton("设置", UiTheme.BTN_SIZE_LARGE);
        settingsButton.addActionListener(e -> onSettings.run());
        card.add(settingsButton);

        card.add(Box.createVerticalStrut(12));

        JButton quitButton = UiTheme.orangeButton("退出", UiTheme.BTN_SIZE_LARGE);
        quitButton.addActionListener(e -> onQuit.run());
        card.add(quitButton);

        add(card, gc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setColor(new Color(255, 255, 255, 70));
            g2.fillRoundRect(20, 20, getWidth() - 40, getHeight() - 40, 28, 28);
        } finally {
            g2.dispose();
        }
    }
}
