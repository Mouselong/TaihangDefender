package com.hbau.taihang;

import java.awt.Color;

public enum EnemyType {
    T1_EAST_ASIAN_LOCUST("T1", "东亚飞蝗", "东亚飞蝗", 1, 64, 0.62, 28, 1.18,
            new Color(0xC79A3C), new Color(0x8D6E2F), new Color(0xF4D35E), new Color(0x5D4037)),
    T2_CORN_BORER("T2", "玉米螟", "玉米螟", 2, 44, 0.78, 20, 1.08,
            new Color(0xF4A261), new Color(0xC97B2B), new Color(0xFFE0B2), new Color(0x5D4037)),
    T2_DIAMONDBACK_MOTH("T2", "小菜蛾", "小菜蛾", 2, 40, 0.84, 20, 1.06,
            new Color(0x9E9E9E), new Color(0x616161), new Color(0xECEFF1), new Color(0x455A64)),
    T3_CUTWORM("T3", "地老虎", "地老虎", 3, 30, 0.96, 14, 0.98,
            new Color(0x8D6E63), new Color(0x5D4037), new Color(0xD7CCC8), new Color(0x3E2723)),
    T3_PEACH_APHID("T3", "桃蚜", "桃蚜", 3, 28, 1.00, 14, 0.96,
            new Color(0x7CB342), new Color(0x33691E), new Color(0xDCEDC8), new Color(0x1B5E20)),
    T4_WIREWORM("T4", "金针虫", "金针虫", 4, 22, 1.12, 10, 0.88,
            new Color(0xC9A227), new Color(0x8D6E0A), new Color(0xFFF59D), new Color(0x5D4037));

    private final String tierLabel;
    private final String tierName;
    private final String displayName;
    private final int tier;
    private final int baseHp;
    private final double baseSpeed;
    private final int scoreValue;
    private final double visualScale;
    private final Color mainColor;
    private final Color accentColor;
    private final Color highlightColor;
    private final Color detailColor;

    EnemyType(String tierLabel, String tierName, String displayName, int tier, int baseHp, double baseSpeed, int scoreValue,
              double visualScale, Color mainColor, Color accentColor, Color highlightColor, Color detailColor) {
        this.tierLabel = tierLabel;
        this.tierName = tierName;
        this.displayName = displayName;
        this.tier = tier;
        this.baseHp = baseHp;
        this.baseSpeed = baseSpeed;
        this.scoreValue = scoreValue;
        this.visualScale = visualScale;
        this.mainColor = mainColor;
        this.accentColor = accentColor;
        this.highlightColor = highlightColor;
        this.detailColor = detailColor;
    }

    public String getTierLabel() { return tierLabel; }
    public String getTierName() { return tierName; }
    public String getDisplayName() { return displayName; }
    public int getTier() { return tier; }
    public int getBaseHp() { return baseHp; }
    public double getBaseSpeed() { return baseSpeed; }
    public int getScoreValue() { return scoreValue; }
    public Color getMainColor() { return mainColor; }
    public Color getAccentColor() { return accentColor; }
    public Color getHighlightColor() { return highlightColor; }
    public Color getDetailColor() { return detailColor; }
    public double getVisualScale() { return visualScale; }

    public int createHp(int wave) {
        int waveBonus = Math.max(0, wave - 1) * (5 - tier);
        return baseHp + waveBonus;
    }

    public double createSpeed(int wave, double difficultyMultiplier) {
        double waveBonus = 1.0 + Math.min(0.30, Math.max(0, wave - 1) * 0.015);
        return baseSpeed * difficultyMultiplier * waveBonus;
    }

    public int createScoreValue(int wave) {
        return scoreValue + Math.max(0, wave - 1) * (5 - tier);
    }
}

