package com.hbau.taihang;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Enemy {
    protected double x, y;
    protected int hp;
    protected int radius = 12;
    protected double speed = 1.0;
    protected Color color = Color.RED;
    protected int scoreValue = 10;
    protected double slowMultiplier = 1.0;
    protected long slowedUntilMs = 0L;
    protected int renderSize = 40;
    protected String rankName = "";
    protected Color rankColor = new Color(0x263238);

    protected boolean reachedGoal = false;
    protected BufferedImage sprite;
    protected boolean active = false; // 对象池使用：是否激活状态

    public Enemy() {
        // 无参构造函数，用于对象池
        this.active = false;
    }

    public Enemy(double x, double y, int hp) {
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.active = true;
    }

    /**
     * 重置敌人，用于对象池回收
     */
    public void reset() {
        this.x = 0;
        this.y = 0;
        this.hp = 0;
        this.radius = 12;
        this.speed = 1.0;
        this.color = Color.RED;
        this.scoreValue = 10;
        this.slowMultiplier = 1.0;
        this.slowedUntilMs = 0L;
        this.renderSize = 40;
        this.rankName = "";
        this.rankColor = new Color(0x263238);
        this.reachedGoal = false;
        this.sprite = null;
        this.active = false;
    }

    public boolean isActive() { return active; }
    
    public void setActive(boolean active) { this.active = active; }

    public void update() {
        if (!active) return;
        // default enemy has no movement; lane enemy overrides this.
    }

    public void update(double deltaTime) {
        if (!active) return;
        // deltaTime overload for frame-based updates
        update();
    }

    /**
     * 应用减速效果
     * @param multiplier 减速倍率（0.25-1.0），值越小减速越强
     * @param durationMs 持续时间（毫秒）
     */
    public void applySlow(double multiplier, long durationMs) {
        if (durationMs <= 0) {
            return;
        }
        double m = Math.max(0.25, Math.min(1.0, multiplier));
        // Only apply stronger slow (smaller multiplier) or when current slow has expired.
        if (System.currentTimeMillis() > slowedUntilMs && m < slowMultiplier) {
            slowMultiplier = m;
        }
        slowedUntilMs = Math.max(slowedUntilMs, System.currentTimeMillis() + durationMs);
    }

    protected double getCurrentSpeed() {
        if (System.currentTimeMillis() > slowedUntilMs) {
            slowMultiplier = 1.0;
            return speed;
        }
        return speed * slowMultiplier;
    }

    public boolean isDead() { return hp <= 0; }

    public boolean hasReachedGoal() { return reachedGoal; }

    public void damage(int d) { hp -= d; }

    public int getScoreValue() { return scoreValue; }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getRadius() { return radius; }

    public void setRenderSize(int size) {
        renderSize = Math.max(18, size);
        radius = Math.max(10, renderSize / 2);
    }

    public int getRenderSize() { return renderSize; }

    public int getHp() { return hp; }
    public double getSpeed() { return speed; }
    public double getSlowMultiplier() { return slowMultiplier; }
    public long getSlowedUntilMs() { return slowedUntilMs; }
    public String getRankName() { return rankName; }

    protected void setRankName(String rankName, Color rankColor) {
        this.rankName = rankName == null ? "" : rankName;
        if (rankColor != null) {
            this.rankColor = rankColor;
        }
    }

    public void draw(Graphics2D g2) {
        if (!active) return;
        if (sprite != null) {
            int size = renderSize;
            g2.drawImage(sprite, (int) x - size / 2, (int) y - size / 2, size, size, null);
        } else {
            g2.setColor(color);
            g2.fillOval((int)x - radius, (int)y - radius, radius * 2, radius * 2);
        }
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        g2.drawString(String.valueOf(hp), (int)x - 6, (int)y - radius - 4);

        if (!rankName.trim().isEmpty()) {
            Font old = g2.getFont();
            g2.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(rankName);
            int tx = (int) x - tw / 2;
            int ty = (int) y - radius - 18;
            g2.setColor(new Color(255, 255, 255, 220));
            g2.fillRoundRect(tx - 6, ty - fm.getAscent(), tw + 12, fm.getHeight() + 2, 8, 8);
            g2.setColor(rankColor);
            g2.drawRoundRect(tx - 6, ty - fm.getAscent(), tw + 12, fm.getHeight() + 2, 8, 8);
            g2.drawString(rankName, tx, ty);
            g2.setFont(old);
        }
    }
}