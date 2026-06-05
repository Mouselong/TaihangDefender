package com.hbau.taihang;

import java.awt.*;

/**
 * New tower type representing a pesticide sprayer: higher damage, moderate range.
 */
public class PesticideTower extends Tower {
    public PesticideTower(int x, int y) {
        super(x, y);
        this.attackPower = 18;
        this.attackCooldownMs = 1400;
        this.range = 240;
        this.typeName = "pesticide";
        this.displayName = "农药塔";
        this.icon = ScreenAssets.createTowerIcon(new java.awt.Color(0x8E24AA), "药");
    }

    @Override
    public Bullet attack(Enemy target) {
        markAttacked();
        return new Bullet(x, y, target, attackPower);
    }
}