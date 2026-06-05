package com.hbau.taihang;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class GameRenderer {
    private final GamePanel panel;
    private GameEngine engine;
    private Image backgroundImage;

    public GameRenderer(GamePanel panel) {
        this.panel = panel;
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
        if (engine != null) {
            backgroundImage = ScreenAssets.loadOrCreateBackground(Math.max(900, panel.getWidth()), Math.max(600, panel.getHeight()));
        }
    }

    public void draw(Graphics2D g2, Point hoverPoint) {
        if (backgroundImage == null) {
            backgroundImage = ScreenAssets.loadOrCreateBackground(panel.getWidth(), panel.getHeight());
        }
        g2.drawImage(backgroundImage, 0, 0, panel.getWidth(), panel.getHeight(), null);

        if (engine != null) {
            drawLanes(g2);
            drawPlacementHints(g2, hoverPoint);
            drawGhostTower(g2, hoverPoint);
            drawSelectedTowerRange(g2, hoverPoint);

            for (Enemy e : engine.getEnemies()) {
                e.draw(g2);
            }
            for (Bullet b : engine.getBullets()) {
                b.draw(g2);
            }
            long now = System.currentTimeMillis();
            for (Tower t : engine.getTowers()) {
                long placed = t.getPlacedAt();
                if (placed > 0 && now - placed < 500) {
                    double progress = (now - placed) / 500.0;
                    double scale = 0.5 + 0.5 * Math.min(1.0, progress);
                    // draw with scaling around tower center
                    AffineTransform old = g2.getTransform();
                    g2.translate(t.getX(), t.getY());
                    g2.scale(scale, scale);
                    g2.translate(-t.getX(), -t.getY());
                    t.draw(g2);
                    g2.setTransform(old);
                } else {
                    t.draw(g2);
                }
            }

            if (engine.isGameOver()) {
                g2.setColor(new Color(0, 0, 0, 160));
                g2.fillRect(0, 0, panel.getWidth(), panel.getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(36f));
                g2.drawString("Game Over", panel.getWidth() / 2 - 100, panel.getHeight() / 2);
            }
        }

        // Draw small agricultural watermark
        g2.setColor(new Color(0x1B5E20, true));
        g2.setFont(FontLibrary.bodyFont(12f));
        String wm = "农大护田 - 太行卫士";
        int wx = panel.getWidth() - g2.getFontMetrics().stringWidth(wm) - 12;
        g2.drawString(wm, Math.max(12, wx), 18);
    }


    private void drawLanes(Graphics2D g2) {
        int top = engine.getLaneTop();
        int bottom = engine.getLaneBottom();
        int left = engine.getGridLeft();
        int right = engine.getGridRight();
        int width = Math.max(1, right - left);
        int[] lanes = engine.getLaneCenters();
        
        // 游戏区域阴影
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillRoundRect(left - 18, top - 10, width + 36, bottom - top + 20, 20, 20);
        
        // 主游戏区域背景
        GradientPaint laneBg = new GradientPaint(0, top, new Color(255, 255, 255, 100), 
                                                 0, bottom, new Color(230, 245, 230, 90));
        g2.setPaint(laneBg);
        g2.fillRoundRect(left - 20, top - 12, width + 40, bottom - top + 24, 20, 20);
        
        // 边框
        g2.setColor(new Color(0x4CAF50));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(left - 20, top - 12, width + 40, bottom - top + 24, 20, 20);

        // 绘制各条路
        g2.setFont(FontLibrary.bodyFont(12f));
        for (int i = 0; i < lanes.length; i++) {
            int y = lanes[i];
            
            // 路背景
            g2.setColor(new Color(200, 230, 201, 100));
            g2.fillRect(left, y - 20, width, 40);
            
            // 路分隔线
            g2.setColor(new Color(0x81C784));
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(left, y, right, y);
            
            // 路的装饰圆点
            g2.setColor(new Color(0x2E7D32));
            g2.fillOval(left - 10, y - 6, 12, 12);
            g2.setColor(new Color(0xFFFFFF));
            g2.fillOval(left - 8, y - 4, 8, 8);
            
            // 路标
                g2.setColor(new Color(0x5D4037)); // 深棕色，提高可读性
                g2.setFont(FontLibrary.bodyFont(11f));
                g2.drawString("第" + (i + 1) + "路", left - 38, y + 4);
        }

        // 网格线 - 放置区域提示
        int cols = 9;
        int colW = Math.max(40, width / cols);
        g2.setColor(new Color(165, 213, 167, 150));
        g2.setStroke(new BasicStroke(1f));
        for (int i = 1; i < cols; i++) {
            int x = left + i * colW;
            g2.drawLine(x, top, x, bottom);
        }
        
        // 起点和终点装饰
        // 起点（虫害入口）- 敌人从右边来
        g2.setColor(new Color(0xE53935));
        g2.fillOval(right, top - 20, 30, 30);
        g2.setColor(Color.WHITE);
        g2.setFont(FontLibrary.titleFont(14f));
        g2.drawString("入", right + 8, top - 1);
        
        // 终点（果园防线）- 敌人往左边去
        g2.setColor(new Color(0x43A047));
        g2.fillOval(left - 30, top - 20, 30, 30);
        g2.setColor(Color.WHITE);
        g2.drawString("防", left - 22, top - 1);
        
        // 标签文字
        g2.setFont(FontLibrary.bodyFont(11f));
        g2.setColor(new Color(0x5D4037)); // 深棕色，与路标保持一致
        g2.drawString("虫害入口", right + 5, top + 20);
        g2.drawString("果园防线", left - 35, top + 20);
    }

    private void drawPlacementHints(Graphics2D g2, Point hoverPoint) {
        if (engine == null) return;
        if (engine.isAwaitingPerkChoice()) return;
        if (hoverPoint == null) return;
        Point2D.Double snapped = engine.getSnappedPlacementPoint(hoverPoint.x, hoverPoint.y);
        boolean canPlace = engine.canPlaceTowerAt((int) snapped.x, (int) snapped.y);
        TowerShop.TowerType selectedType = engine.getSelectedTowerType();
        if (selectedType == null) {
            selectedType = TowerShop.TowerType.DRONE;
        }
        String towerName;
        if (selectedType == TowerShop.TowerType.DRONE) {
            towerName = "无人机塔";
        } else if (selectedType == TowerShop.TowerType.IRRIGATION) {
            towerName = "灌溉塔";
        } else {
            towerName = "农药塔";
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        g2.setColor(canPlace ? new Color(0x1B5E20) : new Color(0xB71C1C));
        g2.fillOval((int) snapped.x - 16, (int) snapped.y - 16, 32, 32);
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setColor(canPlace ? new Color(0x1B5E20) : new Color(0xB71C1C));
        g2.drawOval((int) snapped.x - 16, (int) snapped.y - 16, 32, 32);
        g2.drawString(towerName + " - " + (canPlace ? "可放置" : "不可放置"), (int) snapped.x + 18, (int) snapped.y - 18);
    }

    private void drawGhostTower(Graphics2D g2, Point hoverPoint) {
        if (engine == null) return;
        if (engine.isAwaitingPerkChoice()) return;
        if (hoverPoint == null) return;
        Point2D.Double snapped = engine.getSnappedPlacementPoint(hoverPoint.x, hoverPoint.y);
        boolean canPlace = engine.canPlaceTowerAt((int) snapped.x, (int) snapped.y);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        g2.setColor(canPlace ? new Color(0x4CAF50) : new Color(0xEF5350));
        g2.fillRect((int) snapped.x - 12, (int) snapped.y - 12, 24, 24);
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setColor(Color.WHITE);
        g2.drawRect((int) snapped.x - 12, (int) snapped.y - 12, 24, 24);
    }

    private void drawSelectedTowerRange(Graphics2D g2, Point hoverPoint) {
        if (engine == null) return;
        if (engine.isAwaitingPerkChoice()) return;
        if (hoverPoint == null) return;
        TowerShop.TowerType sel = engine.getSelectedTowerType();
        if (sel == null) return;
        TowerShop shop = engine.getTowerShop();
        if (shop == null) return;
        Point2D.Double snapped = engine.getSnappedPlacementPoint(hoverPoint.x, hoverPoint.y);
        Tower tmp = shop.createTower(sel, (int) snapped.x, (int) snapped.y);
        if (tmp == null) return;
        int range = tmp.getRange();
        // draw translucent range circle to help player decide placement
        Composite prev = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
        g2.setColor(new Color(0x4CAF50));
        g2.fillOval((int) snapped.x - range, (int) snapped.y - range, range * 2, range * 2);
        g2.setComposite(prev);
        g2.setColor(new Color(0x4CAF50));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval((int) snapped.x - range, (int) snapped.y - range, range * 2, range * 2);
    }
}

