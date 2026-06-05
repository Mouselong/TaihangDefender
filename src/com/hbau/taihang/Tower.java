package com.hbau.taihang;

import java.awt.*;
import javax.swing.ImageIcon;

public abstract class Tower {
    protected int x, y;
    protected int attackPower = 10;
    protected int range = 200;
    protected long lastAttackTime = 0;
    protected int attackCooldownMs = 800;
    protected ImageIcon icon;
    protected long placedAt = 0L;
    protected String typeName = "";
    protected String displayName = "";

    public Tower(int x, int y) {
        this.x = x; this.y = y;
    }

    public abstract Bullet attack(Enemy target);

    public void update() {
        // override if needed
    }

    public boolean canAttack() {
        return System.currentTimeMillis() - lastAttackTime >= attackCooldownMs;
    }

    public void markAttacked() { lastAttackTime = System.currentTimeMillis(); }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getRange() { return range; }

    public void boostRange(double multiplier) {
        if (multiplier > 0) {
            range = (int) Math.round(range * multiplier);
        }
    }

    public String getType() { return typeName; }
    public String getDisplayName() { return displayName; }
    
    public int getAttackPower() { return attackPower; }
    public int getAttackCooldownMs() { return attackCooldownMs; }

    public void draw(Graphics2D g2) {
        if (icon != null) {
            icon.paintIcon(null, g2, x - 16, y - 16);
        } else {
            g2.setColor(new Color(0x90CAF9));
            g2.fillRect(x - 12, y - 12, 24, 24);
        }
        
        // 绘制塔名称在塔下方
        if (displayName != null && !displayName.isEmpty()) {
            g2.setColor(new Color(0x1B5E20));
            g2.setFont(FontLibrary.bodyFont(12f));
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(displayName);
            g2.drawString(displayName, x - textWidth / 2, y + 30);
        }
        
        g2.setColor(new Color(0x90CAF9, true));
        g2.drawOval(x - range, y - range, range * 2, range * 2);
    }

    public void setPlacedAt(long when) { this.placedAt = when; }
    public long getPlacedAt() { return placedAt; }
}