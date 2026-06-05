package com.hbau.taihang;

public class GameSettings {
    private Difficulty difficulty = Difficulty.NORMAL;
    private boolean soundEnabled = true;
    private boolean bgmEnabled = true;
    private float soundVolume = 0.8f;
    private float bgmVolume = 0.5f;
    private double enemySpeedMultiplier = 1.0;
    private double towerCostMultiplier = 1.0;

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) {
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
    }

    public boolean isSoundEnabled() { return soundEnabled; }
    public void setSoundEnabled(boolean soundEnabled) { this.soundEnabled = soundEnabled; }
    
    public boolean isBgmEnabled() { return bgmEnabled; }
    public void setBgmEnabled(boolean bgmEnabled) { this.bgmEnabled = bgmEnabled; }
    
    public float getSoundVolume() { return soundVolume; }
    public void setSoundVolume(float soundVolume) { this.soundVolume = clamp(soundVolume, 0.0f, 1.0f); }
    
    public float getBgmVolume() { return bgmVolume; }
    public void setBgmVolume(float bgmVolume) { this.bgmVolume = clamp(bgmVolume, 0.0f, 1.0f); }

    public double getEnemySpeedMultiplier() { return enemySpeedMultiplier; }
    public void setEnemySpeedMultiplier(double v) { this.enemySpeedMultiplier = clamp(v, 0.5, 3.0); }

    public double getTowerCostMultiplier() { return towerCostMultiplier; }
    public void setTowerCostMultiplier(double v) { this.towerCostMultiplier = clamp(v, 0.5, 3.0); }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
    
    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}

