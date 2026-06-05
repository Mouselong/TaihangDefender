package com.hbau.taihang;

import java.util.Random;

/**
 * Minimal wave manager: spawns simple enemies. This is a stub for expansion.
 */
public class WaveManager {
    private final Random rnd = new Random();
    private Difficulty difficulty = Difficulty.NORMAL;
    private int wave = 1;
    private int spawnedInWave = 0;
    private long lastSpawnMs = 0;
    private int waveSize = difficulty.getInitialWaveSize();
    private int spawnIntervalMs = difficulty.getSpawnIntervalMs();
    private int[] laneCenters = new int[] {130, 210, 290, 370, 450};
    private int spawnX = 900;
    private int goalX = 32;
    private double extraSpeedMultiplier = 1.0;

    public int getWave() { return wave; }

    public void setDifficulty(Difficulty difficulty) {
        if (difficulty != null) {
            this.difficulty = difficulty;
            recalculateWaveParameters();
        }
    }

    public void setExtraSpeedMultiplier(double v) {
        this.extraSpeedMultiplier = Math.max(0.5, Math.min(3.0, v));
    }


    public Enemy updateAndMaybeSpawn() {
        long now = System.currentTimeMillis();
        if (spawnedInWave >= waveSize) return null;
        if (now - lastSpawnMs < spawnIntervalMs) return null;
        lastSpawnMs = now;
        spawnedInWave++;
        return createEnemyForWave(wave);
    }

    public void configureLanes(int[] laneCenters, int spawnX, int goalX) {
        if (laneCenters != null && laneCenters.length > 0) {
            this.laneCenters = laneCenters.clone();
        }
        this.spawnX = spawnX;
        this.goalX = goalX;
    }

    private static final long PREP_TIME_MS = 3000;

    public void primeSpawnClock() {
        lastSpawnMs = System.currentTimeMillis() + PREP_TIME_MS;
    }

    public boolean isWaveComplete(int remainingEnemies) {
        return spawnedInWave >= waveSize && remainingEnemies == 0;
    }

    public void advanceWave() {
        wave++;
        spawnedInWave = 0;
        lastSpawnMs = System.currentTimeMillis() + PREP_TIME_MS;
        recalculateWaveParameters();
    }

    private void recalculateWaveParameters() {
        waveSize = difficulty.getInitialWaveSize() + Math.max(0, wave - 1) * 2;
        spawnIntervalMs = Math.max(420, difficulty.getSpawnIntervalMs() - Math.max(0, wave - 1) * 18);
    }

    private Enemy createEnemyForWave(int wave) {
        int laneY = laneCenters[rnd.nextInt(laneCenters.length)];
        double speedFactor = difficulty.getEnemySpeedMultiplier();
        EnemyType type = pickEnemyTypeForWave(wave);
        double speed = type.createSpeed(wave, speedFactor) * extraSpeedMultiplier;
        return new LaneEnemy(spawnX, laneY, goalX,
                type.createHp(wave),
                speed,
                type.getMainColor(),
                type.createScoreValue(wave),
                type.getTierLabel(),
                type);
    }

    private EnemyType pickEnemyTypeForWave(int wave) {
        if (wave <= 2) {
            return EnemyType.T4_WIREWORM;
        }
        if (wave <= 4) {
            return rnd.nextInt(4) == 0
                    ? (rnd.nextBoolean() ? EnemyType.T3_CUTWORM : EnemyType.T3_PEACH_APHID)
                    : EnemyType.T4_WIREWORM;
        }
        if (wave <= 7) {
            int roll = rnd.nextInt(6);
            if (roll == 0) {
                return EnemyType.T4_WIREWORM;
            } else if (roll == 1 || roll == 2) {
                return rnd.nextBoolean() ? EnemyType.T3_CUTWORM : EnemyType.T3_PEACH_APHID;
            } else {
                return rnd.nextBoolean() ? EnemyType.T2_CORN_BORER : EnemyType.T2_DIAMONDBACK_MOTH;
            }
        }
        if (wave <= 10) {
            int roll = rnd.nextInt(10);
            if (roll == 0) {
                return EnemyType.T4_WIREWORM;
            } else if (roll >= 1 && roll <= 3) {
                return rnd.nextBoolean() ? EnemyType.T3_CUTWORM : EnemyType.T3_PEACH_APHID;
            } else if (roll >= 4 && roll <= 7) {
                return rnd.nextBoolean() ? EnemyType.T2_CORN_BORER : EnemyType.T2_DIAMONDBACK_MOTH;
            } else {
                return EnemyType.T1_EAST_ASIAN_LOCUST;
            }
        }
        int roll = rnd.nextInt(12);
        if (roll <= 3) {
            return EnemyType.T1_EAST_ASIAN_LOCUST;
        } else if (roll >= 4 && roll <= 7) {
            return rnd.nextBoolean() ? EnemyType.T2_CORN_BORER : EnemyType.T2_DIAMONDBACK_MOTH;
        } else if (roll >= 8 && roll <= 10) {
            return rnd.nextBoolean() ? EnemyType.T3_CUTWORM : EnemyType.T3_PEACH_APHID;
        } else {
            return EnemyType.T4_WIREWORM;
        }
    }
}
