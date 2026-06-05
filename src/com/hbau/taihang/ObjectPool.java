package com.hbau.taihang;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 通用对象池，用于减少频繁创建销毁对象的开销
 * @param <T> 对象类型
 */
public abstract class ObjectPool<T> {
    private final Queue<T> pool = new LinkedList<>();
    private final int maxSize;
    private final int initialSize;

    public ObjectPool(int initialSize, int maxSize) {
        this.initialSize = initialSize;
        this.maxSize = maxSize;
        initializePool();
    }

    /**
     * 初始化对象池
     */
    private void initializePool() {
        for (int i = 0; i < initialSize; i++) {
            pool.offer(create());
        }
    }

    /**
     * 创建新对象，由子类实现
     */
    protected abstract T create();

    /**
     * 重置对象，由子类实现
     */
    protected abstract void reset(T obj);

    /**
     * 从对象池中获取一个对象
     */
    public synchronized T acquire() {
        if (pool.isEmpty()) {
            return create();
        }
        return pool.poll();
    }

    /**
     * 将对象释放回对象池
     */
    public synchronized void release(T obj) {
        if (obj == null) {
            return;
        }
        reset(obj);
        if (pool.size() < maxSize) {
            pool.offer(obj);
        }
    }

    /**
     * 获取当前对象池大小
     */
    public int size() {
        return pool.size();
    }

    /**
     * 清空对象池
     */
    public synchronized void clear() {
        pool.clear();
    }
}
