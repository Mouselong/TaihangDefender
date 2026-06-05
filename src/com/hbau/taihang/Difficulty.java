package com.hbau.taihang;

public enum Difficulty {
    EASY("简单", 1.0, 900, 6, 10),
    NORMAL("普通", 1.2, 800, 7, 12),
    HARD("困难", 1.45, 680, 8, 14);

    private final String displayName;
    private final double enemySpeedMultiplier;
    private final int spawnIntervalMs;
    private final int initialWaveSize;
    private final int initialLives;

    Difficulty(String displayName, double enemySpeedMultiplier, int spawnIntervalMs, int initialWaveSize, int initialLives) {
        this.displayName = displayName;
        this.enemySpeedMultiplier = enemySpeedMultiplier;
        this.spawnIntervalMs = spawnIntervalMs;
        this.initialWaveSize = initialWaveSize;
        this.initialLives = initialLives;
    }

    public String getDisplayName() { return displayName; }
    public double getEnemySpeedMultiplier() { return enemySpeedMultiplier; }
    public int getSpawnIntervalMs() { return spawnIntervalMs; }
    public int getInitialWaveSize() { return initialWaveSize; }
    public int getInitialLives() { return initialLives; }
    @Override
    public String toString() { return displayName; }
}

