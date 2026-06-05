package com.hbau.taihang;

import java.awt.*;

/**
 * PvZ-style lane enemy: moves from right to left in one fixed lane.
 * Supports object pooling for performance optimization.
 */
public class LaneEnemy extends Enemy {
    private double goalX;
    private EnemyType type;

    public LaneEnemy() {
        super();
    }

    public LaneEnemy(double startX, double laneY, double goalX, int hp, double speed, Color color, int scoreValue, String tag) {
        this(startX, laneY, goalX, hp, speed, color, scoreValue, tag, null);
    }

    public LaneEnemy(double startX, double laneY, double goalX, int hp, double speed, Color color, int scoreValue, String tag, EnemyType type) {
        super(startX, laneY, hp);
        this.goalX = goalX;
        this.speed = speed;
        this.color = color;
        this.scoreValue = scoreValue;
        this.type = type != null ? type : EnemyType.T1_EAST_ASIAN_LOCUST;
        this.renderSize = 40;
        this.radius = 20;
        setRankName(this.type.getTierName(), this.type.getAccentColor());
        this.sprite = type != null
                ? ScreenAssets.createEnemySprite(this.type)
                : ScreenAssets.createEnemySprite(color, tag);
    }

    /**
     * 初始化/重置敌人，用于对象池
     */
    public void init(double startX, double laneY, double goalX, int hp, double speed, Color color, int scoreValue, String tag, EnemyType type) {
        this.x = startX;
        this.y = laneY;
        this.goalX = goalX;
        this.hp = hp;
        this.speed = speed;
        this.color = color;
        this.scoreValue = scoreValue;
        this.type = type != null ? type : EnemyType.T1_EAST_ASIAN_LOCUST;
        this.renderSize = 40;
        this.radius = 20;
        this.reachedGoal = false;
        this.slowMultiplier = 1.0;
        this.slowedUntilMs = 0L;
        setRankName(this.type.getTierName(), this.type.getAccentColor());
        this.sprite = type != null
                ? ScreenAssets.createEnemySprite(this.type)
                : ScreenAssets.createEnemySprite(color, tag);
        this.active = true;
    }

    @Override
    public void reset() {
        super.reset();
        this.goalX = 0;
        this.type = null;
    }

    @Override
    public void setRenderSize(int size) {
        int adjusted = (int) Math.round(size * type.getVisualScale());
        super.setRenderSize(adjusted);
    }

    public EnemyType getType() {
        return type;
    }

    @Override
    public void update() {
        if (!active) return;
        if (reachedGoal || isDead()) {
            return;
        }
        x -= getCurrentSpeed();
        if (x <= goalX) {
            reachedGoal = true;
        }
    }
}

