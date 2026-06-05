package com.hbau.taihang;

import java.awt.*;

/**
 * Simple homing bullet that moves toward a target enemy.
 * Supports object pooling for performance optimization.
 */
public class Bullet {
    private double x, y;
    private Enemy target;
    private int damage;
    private double speed = 4.0;
    private boolean expired = false;
    private boolean active = false; // 对象池使用：是否激活状态
    private boolean crit = false; // 是否暴击

    public Bullet() {
        // 无参构造函数，用于对象池
        this.active = false;
    }

    public Bullet(int x, int y, Enemy target, int damage) {
        init(x, y, target, damage);
    }

    /**
     * 初始化/重置子弹，用于对象池
     */
    public void init(int x, int y, Enemy target, int damage) {
        this.x = x;
        this.y = y;
        this.target = target;
        this.damage = damage;
        this.speed = 4.0;
        this.expired = false;
        this.active = true;
        this.crit = false;
    }

    /**
     * 重置子弹，用于对象池回收
     */
    public void reset() {
        this.x = 0;
        this.y = 0;
        this.target = null;
        this.damage = 0;
        this.speed = 4.0;
        this.expired = false;
        this.active = false;
        this.crit = false;
    }

    public void setSpeed(double s) { this.speed = s; }

    public void scaleDamage(double multiplier) {
        if (multiplier <= 0) {
            return;
        }
        damage = Math.max(1, (int) Math.round(damage * multiplier));
    }

    public void update() {
        if (!active) return;
        if (target == null || target.isDead() || target.hasReachedGoal()) { expired = true; return; }
        double dx = target.getX() - x;
        double dy = target.getY() - y;
        double dist = Math.hypot(dx, dy);
        if (dist < 1) {
            hit();
            return;
        }
        double vx = dx / dist * speed;
        double vy = dy / dist * speed;
        x += vx;
        y += vy;

        // collision check
        if (Math.hypot(target.getX() - x, target.getY() - y) < target.getRadius() + 2) {
            hit();
        }
    }

    private void hit() {
        if (target != null) target.damage(damage);
        expired = true;
    }

    public boolean isExpired() { return expired; }
    
    public boolean isActive() { return active; }

    public boolean isCrit() { return crit; }
    public void setCrit(boolean crit) { this.crit = crit; }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getDamage() { return damage; }

    public void draw(Graphics2D g2) {
        if (!active) return;
        if (crit) {
            g2.setColor(new Color(0xFF5722));
            g2.fillOval((int)x - 5, (int)y - 5, 10, 10);
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 8));
            g2.drawString("!", (int)x - 2, (int)y + 3);
        } else {
            g2.setColor(Color.BLUE);
            g2.fillOval((int)x - 3, (int)y - 3, 6, 6);
        }
    }
}