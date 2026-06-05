package com.hbau.taihang;

import java.awt.*;

/**
 * 敌人对象池，减少创建销毁开销
 */
public class EnemyPool extends ObjectPool<LaneEnemy> {

    public EnemyPool() {
        super(10, 50); // 初始10个，最大50个
    }

    @Override
    protected LaneEnemy create() {
        return new LaneEnemy();
    }

    @Override
    protected void reset(LaneEnemy obj) {
        obj.reset();
    }

    /**
     * 从对象池获取并初始化敌人
     */
    public LaneEnemy acquire(double startX, double laneY, double goalX, int hp, double speed, Color color, int scoreValue, String tag, EnemyType type) {
        LaneEnemy enemy = acquire();
        enemy.init(startX, laneY, goalX, hp, speed, color, scoreValue, tag, type);
        return enemy;
    }
}
