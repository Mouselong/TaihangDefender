package com.hbau.taihang;

/**
 * 子弹对象池，减少创建销毁开销
 */
public class BulletPool extends ObjectPool<Bullet> {

    public BulletPool() {
        super(20, 100); // 初始20个，最大100个
    }

    @Override
    protected Bullet create() {
        return new Bullet();
    }

    @Override
    protected void reset(Bullet obj) {
        obj.reset();
    }

    /**
     * 从对象池获取并初始化子弹
     */
    public Bullet acquire(int x, int y, Enemy target, int damage) {
        Bullet bullet = acquire();
        bullet.init(x, y, target, damage);
        return bullet;
    }
}
