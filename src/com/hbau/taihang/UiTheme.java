package com.hbau.taihang;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

public final class UiTheme {
    // 主色系统 - 优化色彩
    public static final Color PRIMARY_GREEN = new Color(0x27AE60);     // 主色：绿色
    public static final Color PRIMARY_BLUE = new Color(0x3498DB);      // 蓝色
    public static final Color PRIMARY_PURPLE = new Color(0x9B59B6);    // 紫色
    public static final Color PRIMARY_ORANGE = new Color(0xE67E22);    // 橙色
    public static final Color PRIMARY_GRAY = new Color(0x7F8C8D);      // 灰色

    // 中性色
    public static final Color CARD_BG = new Color(255, 255, 255, 245);
    public static final Color CARD_BORDER = new Color(0xD7E6D7);
    public static final Color TITLE = new Color(0x1B5E20);
    public static final Color SUBTITLE = new Color(0x1565C0);
    public static final Color BODY = new Color(0x263238);
    public static final Color ACCENT = new Color(0x0D47A1);
    public static final Color BUTTON_TEXT = Color.WHITE;
    public static final Color BUTTON_DISABLED_BG = new Color(0x9E9E9E);

    // 按钮标准化尺寸
    public static final Dimension BTN_SIZE_SMALL = new Dimension(140, 40);
    public static final Dimension BTN_SIZE_MEDIUM = new Dimension(200, 46);
    public static final Dimension BTN_SIZE_LARGE = new Dimension(260, 54);

    private UiTheme() {}

    public static JPanel cardPanel(int width, int height) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // 更柔和的多层阴影
                    g2.setColor(new Color(0, 0, 0, 15));
                    g2.fillRoundRect(6, 6, getWidth() - 6, getHeight() - 6, 24, 24);
                    g2.setColor(new Color(0, 0, 0, 10));
                    g2.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 24, 24);
                } finally {
                    g2.dispose();
                }
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBackground(CARD_BG);
        panel.setBorder(new EmptyBorder(28, 36, 28, 36));
        panel.setPreferredSize(new Dimension(width, height));
        return panel;
    }

    public static JPanel infoCard(String title, JComponent... items) {
        JPanel panel = new JPanel();
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel head = titleLabel(title, 17f);
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(head);
        panel.add(Box.createVerticalStrut(8));
        for (int i = 0; i < items.length; i++) {
            panel.add(items[i]);
            if (i < items.length - 1) {
                panel.add(Box.createVerticalStrut(6));
            }
        }
        return panel;
    }

    public static JLabel titleLabel(String text, float size) {
        JLabel label = new JLabel(text);
        label.setFont(FontLibrary.titleFont(size));
        label.setForeground(new Color(0x1976D2));
        return label;
    }

    public static JLabel artTitleLabel(String text, float size) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                Font font = FontLibrary.titleFont(size);
                g2.setFont(font);
                FontMetrics fm = g2.getFontMetrics();
                
                int w = getWidth();
                int h = getHeight();
                int x = (w - fm.stringWidth(getText())) / 2;
                int y = fm.getAscent() + (h - fm.getHeight()) / 2;

                // 更强的阴影
                g2.setColor(new Color(0, 0, 0, 80));
                g2.drawString(getText(), x + 3, y + 3);

                // 更粗的描边
                g2.setColor(new Color(0x0D47A1));
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dy = -2; dy <= 2; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2.drawString(getText(), x + dx, y + dy);
                        }
                    }
                }

                // 更亮的渐变填充
                GradientPaint gradient = new GradientPaint(0, 0, new Color(0x1976D2), 0, h, new Color(0x64B5F6));
                g2.setPaint(gradient);
                g2.drawString(getText(), x, y);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                Font font = FontLibrary.titleFont(size);
                FontMetrics fm = getFontMetrics(font);
                return new Dimension(fm.stringWidth(getText()) + 30, fm.getHeight() + 20);
            }
        };
        return label;
    }

    public static JLabel subtitleLabel(String text, float size) {
        JLabel label = new JLabel(text);
        label.setFont(FontLibrary.bodyFont(size));
        label.setForeground(new Color(0x2C3E50));
        return label;
    }

    public static JLabel bodyLabel(String text, float size) {
        JLabel label = new JLabel(text);
        label.setFont(FontLibrary.bodyFont(size));
        label.setForeground(BODY);
        return label;
    }

    public static JLabel accentLabel(String text, float size) {
        JLabel label = new JLabel(text);
        label.setFont(FontLibrary.titleFont(size));
        label.setForeground(ACCENT);
        return label;
    }

    public static JLabel leftLabel(String text, float size, Color color, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(bold ? FontLibrary.titleFont(size) : FontLibrary.bodyFont(size));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public static void styleButton(JButton button, Color bg, Color fg, Dimension size, boolean titleFont) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(titleFont ? FontLibrary.titleFont(20f) : FontLibrary.bodyFont(18f));
        button.setForeground(fg);
        button.setBackground(bg);
        button.setUI(new BasicButtonUI());
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setFocusPainted(false);
        button.setMaximumSize(size);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setBorder(BorderFactory.createEmptyBorder(12, 28, 12, 28));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果 - 更柔和
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            private Color originalBg;
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                originalBg = button.getBackground();
                // 更柔和的亮泽效果
                button.setBackground(blendColors(originalBg, Color.WHITE, 0.85f));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (originalBg != null) {
                    button.setBackground(originalBg);
                }
            }
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (originalBg != null) {
                    button.setBackground(blendColors(originalBg, Color.BLACK, 0.85f));
                }
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (originalBg != null) {
                    if (button.getModel().isRollover()) {
                        button.setBackground(blendColors(originalBg, Color.WHITE, 0.85f));
                    } else {
                        button.setBackground(originalBg);
                    }
                }
            }
        });
    }

    // 辅助方法：混合两种颜色
    private static Color blendColors(Color c1, Color c2, float ratio) {
        int r = (int)(c1.getRed() * ratio + c2.getRed() * (1 - ratio));
        int g = (int)(c1.getGreen() * ratio + c2.getGreen() * (1 - ratio));
        int b = (int)(c1.getBlue() * ratio + c2.getBlue() * (1 - ratio));
        return new Color(r, g, b);
    }

    public static JButton button(String text, Color bg, Color fg, Dimension size, boolean titleFont) {
        JButton button = new JButton(text);
        styleButton(button, bg, fg, size, titleFont);
        return button;
    }

    // 常用按钮工厂方法
    public static JButton primaryButton(String text, Dimension size) {
        return button(text, PRIMARY_GREEN, BUTTON_TEXT, size, true);
    }

    public static JButton secondaryButton(String text, Dimension size) {
        return button(text, PRIMARY_BLUE, BUTTON_TEXT, size, false);
    }

    public static JButton purpleButton(String text, Dimension size) {
        return button(text, PRIMARY_PURPLE, BUTTON_TEXT, size, false);
    }

    public static JButton orangeButton(String text, Dimension size) {
        return button(text, PRIMARY_ORANGE, BUTTON_TEXT, size, true);
    }

    public static JButton grayButton(String text, Dimension size) {
        return button(text, PRIMARY_GRAY, BUTTON_TEXT, size, false);
    }
}
