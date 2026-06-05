package com.hbau.taihang;

public class DroneTower extends Tower {
    public DroneTower(int x, int y) {
        super(x, y);
        this.attackPower = 12;
        this.attackCooldownMs = 900;
        this.range = 260;
        this.typeName = "drone";
        this.displayName = "无人机塔";
        this.icon = ScreenAssets.createTowerIcon(new java.awt.Color(0x1E88E5), "机");
    }

    @Override
    public Bullet attack(Enemy target) {
        markAttacked();
        return new Bullet(x, y, target, attackPower);
    }
}