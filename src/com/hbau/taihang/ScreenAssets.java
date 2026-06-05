package com.hbau.taihang;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ScreenAssets {
    private ScreenAssets() {}

    private static final Map<String, ImageIcon> ICON_CACHE = new ConcurrentHashMap<>();
    private static final Map<EnemyType, BufferedImage> ENEMY_SPRITE_CACHE = new ConcurrentHashMap<>();

    public static Image loadOrCreateBackground(int width, int height) {
        BufferedImage img = new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 渐变天空 - 从淡蓝到浅绿
            GradientPaint sky = new GradientPaint(0, 0, new Color(0x87CEEB), 0, height * 0.4f, new Color(0xB8E6B8));
            g.setPaint(sky);
            g.fillRect(0, 0, width, height);
            
            // 添加云朵装饰
            g.setColor(new Color(255, 255, 255, 180));
            drawCloud(g, 80, 60, 60);
            drawCloud(g, 300, 40, 45);
            drawCloud(g, 550, 70, 55);
            drawCloud(g, 750, 50, 40);
            drawCloud(g, 950, 65, 50);
            
            // 太行山背景 - 多层次
            g.setColor(new Color(0x8FBC8F));
            int[] mt1x = {0, width/6, width/3, width/2, width*2/3, width, width};
            int[] mt1y = {height/3, height/4, height/3+20, height/4+10, height/3, height/3+30, height};
            g.fillPolygon(mt1x, mt1y, mt1x.length);
            
            g.setColor(new Color(0x6B8E6B));
            int[] mt2x = {0, width/4, width/2, width*3/4, width, width};
            int[] mt2y = {height/3+40, height/3+20, height/3+50, height/3+30, height/3+60, height};
            g.fillPolygon(mt2x, mt2y, mt2x.length);
            
            // 麦田/农田 - 金黄色渐变
            GradientPaint field = new GradientPaint(0, height/2, new Color(0xF5DEB3), 0, height, new Color(0xDEB887));
            g.setPaint(field);
            g.fillRect(0, height/2, width, height/2);
            
            // 添加麦穗纹理
            g.setColor(new Color(0xDAA520, true));
            for (int row = 0; row < 8; row++) {
                int y = height/2 + 15 + row * 35;
                for (int col = 0; col < width/40 + 1; col++) {
                    int x = 20 + col * 40;
                    // 麦穗
                    g.fillOval(x, y, 12, 6);
                    g.fillOval(x, y-5, 10, 5);
                    g.fillOval(x, y-9, 8, 4);
                    // 麦秆
                    g.setStroke(new BasicStroke(2f));
                    g.setColor(new Color(0x9ACD32));
                    g.drawLine(x+6, y+6, x+6, y+25);
                    g.setColor(new Color(0xDAA520, true));
                }
            }
            
            // 果园树丛 - 不同颜色
            drawTree(g, 50, height/2 - 20, new Color(0x228B22));
            drawTree(g, 150, height/2 - 15, new Color(0x32CD32));
            drawTree(g, 250, height/2 - 25, new Color(0x2E8B57));
            drawTree(g, 400, height/2 - 18, new Color(0x3CB371));
            drawTree(g, 600, height/2 - 22, new Color(0x228B22));
            drawTree(g, 800, height/2 - 20, new Color(0x32CD32));
            drawTree(g, 950, height/2 - 15, new Color(0x2E8B57));
            
            // 农大校园建筑剪影 - 更精美的风格
            g.setColor(new Color(0xF5F5F5, true));
            // 主楼
            g.fillRoundRect(width - 220, 80, 150, 130, 10, 10);
            g.setColor(new Color(0x607D8B));
            g.drawRoundRect(width - 220, 80, 150, 130, 10, 10);
            // 窗户
            g.setColor(new Color(0x90CAF9));
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 5; col++) {
                    g.fillRect(width - 200 + col * 28, 95 + row * 28, 18, 18);
                }
            }
            // 钟楼
            g.setColor(new Color(0xF5F5F5, true));
            g.fillRect(width - 100, 60, 60, 150);
            g.setColor(new Color(0x607D8B));
            g.drawRect(width - 100, 60, 60, 150);
            // 钟楼顶部
            g.fillPolygon(new int[]{width - 100, width - 70, width - 40}, 
                         new int[]{60, 40, 60}, 3);
            // 钟面
            g.setColor(new Color(0xFFF9C4));
            g.fillOval(width - 88, 80, 36, 36);
            g.setColor(new Color(0x607D8B));
            g.drawOval(width - 88, 80, 36, 36);
            
            // 校名装饰
            g.setFont(FontLibrary.titleFont(16f));
            g.setColor(new Color(0x1B5E20));
            String schoolName = "河北农业大学";
            int nameWidth = g.getFontMetrics().stringWidth(schoolName);
            g.drawString(schoolName, width - 160 - nameWidth/2, 180);
            
            // 道路/小路
            g.setColor(new Color(0x8B7355));
            g.fillRect(0, height * 2/3, width, 25);
            g.setColor(new Color(0xA0826D));
            g.fillRect(0, height * 2/3 + 3, width, 8);
            
            // 装饰性篱笆
            g.setColor(new Color(0x8B4513));
            for (int i = 0; i < width/50 + 1; i++) {
                int x = i * 50;
                g.fillRect(x, height/2 + 5, 6, 30);
                g.fillRect(x + 20, height/2 + 10, 6, 25);
            }
            g.fillRect(0, height/2 + 8, width, 4);
            g.fillRect(0, height/2 + 20, width, 4);
            
        } finally {
            g.dispose();
        }
        return img;
    }
    
    private static void drawCloud(Graphics2D g, int x, int y, int size) {
        g.fillOval(x, y, size, (int)(size * 0.6));
        g.fillOval(x + (int)(size * 0.3), y - (int)(size * 0.2), (int)(size * 0.7), (int)(size * 0.5));
        g.fillOval(x + (int)(size * 0.6), y, (int)(size * 0.5), (int)(size * 0.4));
    }
    
    private static void drawTree(Graphics2D g, int x, int y, Color color) {
        // 树干
        g.setColor(new Color(0x8B4513));
        g.fillRect(x - 3, y, 6, 25);
        // 树冠
        g.setColor(color);
        g.fillOval(x - 15, y - 20, 30, 25);
        g.fillOval(x - 12, y - 28, 24, 20);
        g.fillOval(x - 8, y - 35, 16, 15);
    }

    public static ImageIcon createEnemyIcon(Color body, String label) {
        String key = "enemy:" + body.getRGB() + ":" + label;
        return ICON_CACHE.computeIfAbsent(key, ignored -> {
            BufferedImage img = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(0, 0, 0, 0));
                g.fillRect(0, 0, 48, 48);
                g.setColor(new Color(0, 0, 0, 60));
                g.fillOval(12, 22, 22, 8);
                g.setColor(body);
                g.fillOval(8, 14, 28, 18);
                g.setColor(body.darker());
                g.fillOval(14, 8, 16, 14);
                g.setColor(body.brighter());
                g.fillOval(22, 10, 6, 6);
                g.setColor(new Color(60, 40, 20));
                g.drawString(label, 12, 42);
            } finally {
                g.dispose();
            }
            return new ImageIcon(img);
        });
    }

    public static BufferedImage createEnemySprite(EnemyType type) {
        if (type == null) {
            type = EnemyType.T1_EAST_ASIAN_LOCUST;
        }
        return ENEMY_SPRITE_CACHE.computeIfAbsent(type, ScreenAssets::buildEnemySprite);
    }

    public static BufferedImage createEnemySprite(Color body, String label) {
        BufferedImage img = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setColor(new Color(0, 0, 0, 0));
            g.fillRect(0, 0, 128, 128);
            g.setColor(new Color(0, 0, 0, 50));
            g.fillOval(36, 86, 56, 14);
            g.setColor(body);
            g.fillOval(40, 46, 48, 30);
            g.fillOval(50, 28, 28, 22);
            g.setColor(body.darker());
            g.fillOval(56, 40, 16, 34);
            g.setColor(body.brighter());
            g.fillOval(64, 34, 8, 8);
            g.setColor(new Color(60, 40, 20));
            g.setFont(FontLibrary.bodyFont(16f));
            g.drawString(label == null ? "虫" : label, 46, 108);
        } finally {
            g.dispose();
        }
        return img;
    }

    private static BufferedImage buildEnemySprite(EnemyType type) {
        BufferedImage img = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setColor(new Color(0, 0, 0, 0));
            g.fillRect(0, 0, 128, 128);
            drawShadow(g);
            if (type == EnemyType.T1_EAST_ASIAN_LOCUST) {
                drawLocust(g, type);
            } else if (type == EnemyType.T2_CORN_BORER) {
                drawCornBorer(g, type);
            } else if (type == EnemyType.T2_DIAMONDBACK_MOTH) {
                drawDiamondbackMoth(g, type);
            } else if (type == EnemyType.T3_CUTWORM) {
                drawCutworm(g, type);
            } else if (type == EnemyType.T3_PEACH_APHID) {
                drawPeachAphid(g, type);
            } else {
                drawWireworm(g, type);
            }
            drawTierBadge(g, type);
        } finally {
            g.dispose();
        }
        return img;
    }

    private static void drawShadow(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 45));
        g.fillOval(32, 86, 64, 18);
    }

    private static void drawTierBadge(Graphics2D g, EnemyType type) {
        g.setFont(FontLibrary.bodyFont(15f));
        g.setStroke(new BasicStroke(1.4f));
        g.setColor(new Color(255, 255, 255, 215));
        g.fillRoundRect(82, 8, 38, 24, 10, 10);
        g.setColor(type.getAccentColor());
        g.drawRoundRect(82, 8, 38, 24, 10, 10);
        g.setColor(type.getDetailColor());
        g.drawString(type.getTierLabel(), 90, 26);
    }

    private static void drawLocust(Graphics2D g, EnemyType type) {
        g.setColor(new Color(0, 0, 0, 55));
        g.fillOval(34, 58, 58, 16);
        g.setColor(type.getHighlightColor());
        g.fillOval(42, 26, 44, 28);
        g.fillOval(35, 44, 36, 24);
        g.setColor(type.getMainColor());
        g.fillOval(48, 34, 32, 46);
        g.fillOval(52, 18, 24, 22);
        g.setColor(type.getAccentColor());
        g.fillOval(57, 50, 10, 22);
        g.fillOval(61, 24, 8, 14);
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(54, 54, 28, 78);
        g.drawLine(76, 54, 100, 80);
        g.drawLine(58, 36, 30, 30);
        g.drawLine(70, 36, 96, 30);
        g.setColor(type.getDetailColor());
        g.drawLine(58, 18, 52, 8);
        g.drawLine(70, 18, 78, 8);
    }

    private static void drawCornBorer(Graphics2D g, EnemyType type) {
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(30, 62, 66, 14);
        g.setColor(type.getHighlightColor());
        g.fillOval(28, 36, 28, 18);
        g.fillOval(72, 36, 28, 18);
        g.setColor(type.getAccentColor());
        g.fillPolygon(new int[]{22, 50, 40, 14}, new int[]{34, 26, 64, 52}, 4);
        g.fillPolygon(new int[]{86, 114, 94, 74}, new int[]{34, 52, 64, 26}, 4);
        g.setColor(type.getMainColor());
        g.fillRoundRect(55, 30, 18, 52, 10, 10);
        g.fillOval(52, 20, 24, 22);
        g.setColor(type.getDetailColor());
        g.fillOval(61, 24, 4, 4);
        g.drawLine(60, 20, 50, 10);
        g.drawLine(68, 20, 78, 10);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(64, 36, 64, 66);
        g.drawLine(54, 44, 74, 44);
        g.drawLine(54, 54, 74, 54);
    }

    private static void drawDiamondbackMoth(Graphics2D g, EnemyType type) {
        g.setColor(new Color(0, 0, 0, 45));
        g.fillOval(34, 60, 60, 14);
        g.setColor(type.getHighlightColor());
        g.fillPolygon(new int[]{34, 56, 48, 22}, new int[]{36, 22, 56, 48}, 4);
        g.fillPolygon(new int[]{94, 72, 80, 106}, new int[]{36, 22, 56, 48}, 4);
        g.setColor(type.getAccentColor());
        g.fillPolygon(new int[]{30, 54, 46, 18}, new int[]{40, 28, 60, 50}, 4);
        g.fillPolygon(new int[]{98, 74, 82, 110}, new int[]{40, 28, 60, 50}, 4);
        g.setColor(type.getMainColor());
        g.fillRoundRect(58, 28, 12, 50, 8, 8);
        g.fillOval(54, 20, 20, 18);
        g.setColor(type.getDetailColor());
        g.drawLine(60, 20, 52, 10);
        g.drawLine(68, 20, 76, 10);
        g.drawLine(60, 54, 46, 76);
        g.drawLine(68, 54, 82, 76);
        g.setStroke(new BasicStroke(2f));
        g.drawOval(45, 34, 12, 12);
        g.drawOval(71, 34, 12, 12);
    }

    private static void drawCutworm(Graphics2D g, EnemyType type) {
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(28, 64, 74, 14);
        g.setColor(type.getMainColor());
        g.fillRoundRect(24, 42, 76, 30, 18, 18);
        for (int i = 0; i < 5; i++) {
            int x = 30 + i * 14;
            g.setColor(i % 2 == 0 ? type.getAccentColor() : type.getHighlightColor());
            g.fillOval(x, 44, 18, 26);
        }
        g.setColor(type.getDetailColor());
        g.fillOval(22, 44, 24, 20);
        g.fillOval(86, 44, 16, 18);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(94, 48, 108, 40);
        g.drawLine(94, 54, 110, 54);
        g.drawLine(24, 50, 12, 42);
        g.drawLine(24, 58, 10, 60);
        g.drawArc(32, 42, 56, 30, 180, 180);
    }

    private static void drawPeachAphid(Graphics2D g, EnemyType type) {
        g.setColor(new Color(0, 0, 0, 45));
        g.fillOval(34, 62, 56, 14);
        g.setColor(type.getHighlightColor());
        g.fillOval(48, 34, 26, 30);
        g.fillOval(52, 22, 18, 20);
        g.setColor(type.getMainColor());
        g.fillOval(50, 30, 24, 38);
        g.fillOval(54, 18, 16, 14);
        g.setColor(type.getAccentColor());
        g.fillOval(76, 24, 18, 28);
        g.fillOval(38, 24, 18, 28);
        g.setStroke(new BasicStroke(2f));
        g.setColor(type.getDetailColor());
        g.drawLine(58, 18, 50, 8);
        g.drawLine(68, 18, 76, 8);
        g.drawLine(52, 56, 42, 74);
        g.drawLine(70, 56, 82, 74);
        g.drawLine(58, 44, 42, 48);
        g.drawLine(68, 44, 84, 48);
        g.fillOval(57, 28, 4, 4);
    }

    private static void drawWireworm(Graphics2D g, EnemyType type) {
        g.setColor(new Color(0, 0, 0, 55));
        g.fillOval(28, 66, 76, 12);
        g.setColor(type.getMainColor());
        g.fillRoundRect(24, 40, 72, 22, 12, 12);
        for (int i = 0; i < 6; i++) {
            int x = 28 + i * 12;
            g.setColor(i % 2 == 0 ? type.getAccentColor() : type.getHighlightColor());
            g.fillOval(x, 41, 14, 20);
        }
        g.setColor(type.getDetailColor());
        g.fillOval(20, 40, 20, 18);
        g.fillOval(90, 42, 14, 14);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(20, 48, 10, 42);
        g.drawLine(20, 54, 8, 56);
        g.drawLine(92, 48, 110, 42);
        g.drawLine(92, 54, 110, 58);
        g.setColor(new Color(255, 255, 255, 70));
        g.drawArc(34, 44, 46, 12, 0, 180);
    }

    public static ImageIcon createTowerIcon(Color main, String label) {
        String key = "tower:" + main.getRGB() + ":" + label;
        return ICON_CACHE.computeIfAbsent(key, ignored -> {
            BufferedImage img = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(0, 0, 0, 0));
                g.fillRect(0, 0, 48, 48);
                g.setColor(new Color(0, 0, 0, 60));
                g.fillOval(10, 30, 26, 6);
                g.setColor(main);
                g.fillRoundRect(10, 18, 28, 14, 8, 8);
                g.setColor(main.darker());
                g.fillRoundRect(14, 10, 20, 12, 8, 8);
                g.setColor(new Color(0xFFFFFF));
                g.fillRect(18, 6, 12, 4);
                
                // 不再在塔图标上绘制文字标签，只显示塔下方的完整名称
            } finally {
                g.dispose();
            }
            return new ImageIcon(img);
        });
    }
}


