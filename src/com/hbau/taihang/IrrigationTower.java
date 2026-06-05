package com.hbau.taihang;

public class IrrigationTower extends Tower {
    public IrrigationTower(int x, int y) {
        super(x, y);
        this.attackPower = 6;
        this.attackCooldownMs = 1200;
        this.range = 220;
        this.typeName = "irrigation";
        this.displayName = "灌溉塔";
        this.icon = ScreenAssets.createTowerIcon(new java.awt.Color(0x43A047), "灌");
    }

    @Override
    public Bullet attack(Enemy target) {
        markAttacked();
        Bullet b = new Bullet(x, y, target, attackPower);
        b.setSpeed(6.0);
        return b;
    }
}