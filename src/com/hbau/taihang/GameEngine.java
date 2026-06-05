package com.hbau.taihang;

import javax.swing.*;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class GameEngine {
    public static final String PROP_CURRENT_WORD = "currentWord";

    private final GamePanel panel;
    private final WordManager wordManager;
    private final WaveManager waveManager;
    private final EnergyManager energyManager;
    private final TowerShop towerShop;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Tower> towers = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final BulletPool bulletPool = new BulletPool();
    private final EnemyPool enemyPool = new EnemyPool();
    private int laneCount = 5;
    private int laneTop = 90;
    private int laneBottom = 520;
    private int gridLeft = 60;
    private int gridRight = 860;
    private int laneCols = 9;
    private int[] laneCenters = new int[] {130, 210, 290, 370, 450};

    private Timer timer;
    private boolean paused = false;
    private int score = 0;
    private int lives = 10;
    private boolean gameOver = false;
    private TowerShop.TowerType selectedTowerType = TowerShop.TowerType.DRONE;
    private String statusMessage = "\u62a4\u7530\u9632\u7ebf\uff1a\u8f93\u5165\u8bcd\u6c47\u83b7\u80fd\u91cf\uff0c\u5728\u9053\u8def\u4e0a\u653e\u7f6e\u5854\u724c";
    private long statusMessageAt = System.currentTimeMillis();
    private Difficulty difficulty = Difficulty.NORMAL;
    private boolean soundEnabled = true;
    private double towerCostMultiplier = 1.0;
    private static final long UNDO_WINDOW_MS = 3000L;
    private final List<UndoRecord> undoStack = new ArrayList<>();
    private final Random random = new Random();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private boolean awaitingPerkChoice = false;
    private long waveCompleteAt = 0L;
    private static final long PERK_DELAY_MS = 2000L;
    private final List<PerkChoice> perkChoices = new ArrayList<>();
    private double towerDamageMultiplier = 1.0;
    private int wordEnergyBonus = 0;
    private long tempBuffUntilMs = 0L;
    private double towerRangeMultiplier = 1.0;
    private double enemySpeedNerf = 1.0;
    private int maxEnergyBonus = 0;
    private int towerFireRateBonus = 0;
    private int scoreMultiplierBonus = 0;
    private int energyDrainBonus = 0;
    private double critChanceBonus = 0.0;
    private boolean poisonAuraActive = false;
    private long poisonAuraUntilMs = 0L;
    private boolean superShotActive = false;
    private static final int PERK_CHOICES_PER_WAVE = 3;

    public enum PerkType {
        TOWER_UPGRADE, WORD_BONUS, TEMP_BUFF, TOWER_RANGE, ENEMY_WEAKNESS,
        ENERGY_BOOST, HEAL_REPAIR, LIGHTNING_STRIKE, FIRE_RATE, SCORE_BONUS,
        FREE_TOWER, SHIELD_BARRIER, INSTANT_ENERGY, CRIT_BOOST, EXPERIENCE_BOOST,
        GLOBAL_SLOW, MAX_HEALTH_UP, TOWER_REFUND, POISON_AURA, DEFENSE_WALL,
        ENERGY_DRAIN, SUPER_SHOT
    }

    public static final class PerkChoice {
        private final PerkType type;
        private final String title;
        private final String description;
        public PerkChoice(PerkType type, String title, String description) {
            this.type = type; this.title = title; this.description = description;
        }
        public PerkType getType() { return type; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }

    private static class UndoRecord {
        final Tower tower;
        final TowerShop.TowerType towerType;
        final long placedAt;
        final int cost;
        UndoRecord(Tower tower, TowerShop.TowerType towerType, long placedAt, int cost) {
            this.tower = tower; this.towerType = towerType;
            this.placedAt = placedAt; this.cost = cost;
        }
    }

    public GameEngine(GamePanel panel, WordManager wordManager, WaveManager waveManager) {
        this.panel = panel;
        this.wordManager = wordManager;
        this.waveManager = waveManager;
        this.energyManager = new EnergyManager();
        this.towerShop = new TowerShop();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) { pcs.addPropertyChangeListener(l); }
    public void removePropertyChangeListener(PropertyChangeListener l) { pcs.removePropertyChangeListener(l); }

    public void start() {
        enemies.clear(); towers.clear(); bullets.clear(); undoStack.clear();
        selectedTowerType = TowerShop.TowerType.DRONE;
        score = 0; lives = difficulty.getInitialLives(); gameOver = false;
        awaitingPerkChoice = false; waveCompleteAt = 0L; perkChoices.clear();
        statusMessage = "\u62a4\u7530\u9632\u7ebf\uff1a\u8f93\u5165\u8bcd\u6c47\u83b7\u80fd\u91cf\uff0c\u5728\u9053\u8def\u4e0a\u653e\u7f6e\u5854\u724c";
        statusMessageAt = System.currentTimeMillis();
        towerDamageMultiplier = 1.0; wordEnergyBonus = 0; tempBuffUntilMs = 0L;
        towerRangeMultiplier = 1.0; enemySpeedNerf = 1.0; maxEnergyBonus = 0;
        towerFireRateBonus = 0; scoreMultiplierBonus = 0; energyDrainBonus = 0;
        wordManager.loadAll();
        String oldWord = wordManager.getCurrentWordEnglish();
        wordManager.pickNextWord();
        fireCurrentWordChanged(oldWord, wordManager.getCurrentWordEnglish());
        waveManager.setDifficulty(difficulty);
        waveManager.primeSpawnClock();
        if (timer != null) { timer.stop(); }
        timer = new Timer(16, e -> update());
        paused = false;
        timer.start();
        SoundManager.getInstance().playBGM();
    }

    public void stop() {
        if (timer != null) { timer.stop(); timer = null; }
        paused = false;
        SoundManager.getInstance().stopBGM();
    }

    public void pause() { paused = true; if (timer != null) timer.stop(); }
    public void resume() { paused = false; if (timer != null && !timer.isRunning()) timer.start(); }

    private void update() {
        if (paused || gameOver) return;
        if (awaitingPerkChoice) { panel.repaint(); return; }
        Enemy spawned = waveManager.updateAndMaybeSpawn();
        if (spawned != null) { spawned.setRenderSize(getAdaptiveEnemySize()); enemies.add(spawned); }

        if (waveCompleteAt > 0) {
            if (System.currentTimeMillis() >= waveCompleteAt + PERK_DELAY_MS) {
                awaitingPerkChoice = true;
                rollPerkChoices();
                setStatusMessage("\u7b2c " + waveManager.getWave() + " \u6ce2\u5b8c\u6210\uff1a\u8bf7\u9009\u62e9 1 \u4e2a\u589e\u76ca\u540e\u7ee7\u7eed");
                panel.resetTypingField();
                waveCompleteAt = 0L;
            } else {
                setStatusMessage("\u7b2c " + waveManager.getWave() + " \u6ce2\u5b8c\u6210\uff01\u51c6\u5907\u589e\u76ca\u9009\u62e9...");
            }
            panel.repaint();
            return;
        }

        if (waveManager.isWaveComplete(enemies.size())) {
            waveCompleteAt = System.currentTimeMillis();
            setStatusMessage("\u7b2c " + waveManager.getWave() + " \u6ce2\u5b8c\u6210\uff01\u51c6\u5907\u589e\u76ca\u9009\u62e9...");
            SoundManager.getInstance().playSound(SoundManager.SoundEffect.WAVE_COMPLETE);
            panel.repaint();
            return;
        }
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            e.update(1.0 / 60.0);
            if (poisonAuraActive && System.currentTimeMillis() <= poisonAuraUntilMs) { e.damage(2); }
            if (enemySpeedNerf < 1.0 && System.currentTimeMillis() > e.getSlowedUntilMs()) { e.applySlow(enemySpeedNerf, 5000L); }
            if (e.isDead()) {
                int extraScore = scoreMultiplierBonus > 0 ? e.getScoreValue() * scoreMultiplierBonus / 100 : 0;
                score += e.getScoreValue() + extraScore;
                if (energyDrainBonus > 0) { energyManager.addEnergy(energyDrainBonus); }
                SoundManager.getInstance().playSound(SoundManager.SoundEffect.ENEMY_DIE);
                it.remove(); continue;
            }
            if (e.hasReachedGoal()) { lives--; it.remove(); if (lives <= 0) { gameOver = true; } continue; }
        }
        for (Iterator<Bullet> bit = bullets.iterator(); bit.hasNext();) {
            Bullet b = bit.next();
            b.update();
            if (b.isExpired()) { bit.remove(); continue; }
            for (Enemy e : enemies) {
                if (Math.hypot(b.getX() - e.getX(), b.getY() - e.getY()) <= 20) {
                    int damage = (int) Math.round(b.getDamage() * getCurrentAttackMultiplier());
                    if (b.isCrit()) {
                        damage *= 2;
                    }
                    if (superShotActive) {
                        damage *= 2;
                        superShotActive = false;
                    }
                    e.damage(damage);
                    bit.remove();
                    break;
                }
            }
        }
        for (Tower t : towers) {
            if (!t.canAttack()) continue;
            Enemy target = findTargetForTower(t);
            if (target != null) {
                Bullet bullet = t.attack(target);
                if (bullet != null) { 
                    bullet.scaleDamage(getCurrentAttackMultiplier());
                    if (critChanceBonus > 0 && random.nextDouble() < critChanceBonus) {
                        bullet.setCrit(true);
                    }
                    bullets.add(bullet); 
                    SoundManager.getInstance().playSound(SoundManager.SoundEffect.TOWER_SHOOT); 
                }
                applyTowerSpecialEffects(t, target);
            }
        }
        panel.repaint();
    }

    public boolean purchaseTowerAt(TowerShop.TowerType type, int x, int y) {
        if (type == null) return false;
        Point2D.Double pt = getSnappedPlacementPoint(x, y);
        int cost = (int) Math.round(towerShop.getCost(type) * towerCostMultiplier);
        if (energyManager.getEnergy() < cost) { setStatusMessage("\u80fd\u91cf\u4e0d\u8db3\uff0c\u5148\u8f93\u5165\u5355\u8bcd\u6512\u80fd\u91cf"); Toolkit.getDefaultToolkit().beep(); return false; }
        if (!isValidPlacement((int)pt.x, (int)pt.y)) { setStatusMessage("\u5f53\u524d\u4f4d\u7f6e\u4e0d\u80fd\u653e\u5854\uff0c\u8bf7\u6362\u4e00\u4e2a\u7a7a\u5730"); Toolkit.getDefaultToolkit().beep(); return false; }
        Tower tower = towerShop.create(type, (int)pt.x, (int)pt.y);
        tower.boostRange(towerRangeMultiplier);
        energyManager.spendEnergy(cost);
        towers.add(tower);
        undoStack.add(new UndoRecord(tower, type, System.currentTimeMillis(), cost));
        setStatusMessage("\u653e\u7f6e " + toDisplayName(type) + "\uff0c\u6d88\u8017\u80fd\u91cf" + cost);
        SoundManager.getInstance().playSound(SoundManager.SoundEffect.PLACE_TOWER);
        return true;
    }

    public Point2D.Double getSnappedPlacementPoint(int x, int y) { return new Point2D.Double(snapToLaneColCenter(x), snapToNearestLane(y)); }
    public boolean canPlaceTowerAt(int x, int y) { return isValidPlacement(x, y); }
    public boolean isValidPlacement(int x, int y) {
        for (Tower t : towers) { if (Math.hypot(t.getX() - x, t.getY() - y) <= 45) return false; }
        return x >= gridLeft && x <= gridRight && y >= laneTop && y <= laneBottom;
    }

    public int getPurchasedTowerCount() { return towers.size(); }
    public boolean hasUndo() { return !undoStack.isEmpty() && System.currentTimeMillis() - undoStack.get(undoStack.size()-1).placedAt <= UNDO_WINDOW_MS; }

    public Tower undoLastPlacedTower() {
        long now = System.currentTimeMillis();
        for (int i = undoStack.size() - 1; i >= 0; i--) {
            UndoRecord rec = undoStack.get(i);
            if (now - rec.placedAt <= UNDO_WINDOW_MS && towers.contains(rec.tower)) {
                towers.remove(rec.tower);
                energyManager.addEnergy(rec.cost);
                undoStack.remove(i);
                setStatusMessage("\u5df2\u64a4\u9500 " + toDisplayName(rec.towerType) + "\uff0c\u8fd4\u8fd8\u80fd\u91cf" + rec.cost);
                return rec.tower;
            }
        }
        return null;
    }

    public void applySettings(GameSettings settings) {
        if (settings == null) return;
        this.difficulty = settings.getDifficulty();
        this.soundEnabled = settings.isSoundEnabled();
        this.towerCostMultiplier = settings.getTowerCostMultiplier();
        this.enemySpeedNerf = settings.getEnemySpeedMultiplier();
        waveManager.setDifficulty(difficulty);
        waveManager.setExtraSpeedMultiplier(enemySpeedNerf);
        SoundManager.getInstance().setSoundEnabled(settings.isSoundEnabled());
        SoundManager.getInstance().setBgmEnabled(settings.isBgmEnabled());
        SoundManager.getInstance().setSoundVolume(settings.getSoundVolume());
        SoundManager.getInstance().setBgmVolume(settings.getBgmVolume());
    }

    public TowerShop getTowerShop() { return towerShop; }

    private Enemy findTargetForTower(Tower tower) {
        if (tower == null) return null;
        Enemy best = null; double bestX = Double.MAX_VALUE;
        for (Enemy e : enemies) { if (Math.hypot(e.getX() - tower.getX(), e.getY() - tower.getY()) <= tower.getRange() && e.getX() < bestX) { bestX = e.getX(); best = e; } }
        return best;
    }

    private Enemy findNearestEnemy(int x, int y, double range) {
        Enemy best = null; double min = Double.MAX_VALUE;
        for (Enemy e : enemies) { double d = Math.hypot(e.getX() - x, e.getY() - y); if (d <= range && d < min) { min = d; best = e; } }
        return best;
    }

    public void onWordTyped(String word) {
        int gain = 10 + wordEnergyBonus;
        energyManager.addEnergy(gain);
        score += 5;
        setStatusMessage("\u8f93\u5165\u6210\u529f\uff1a\u80fd\u91cf+" + gain + "\uff0c\u5f53\u524d" + getEnergy() + "/" + getMaxEnergy());
        String oldWord = wordManager.getCurrentWordEnglish();
        wordManager.pickNextWord();
        fireCurrentWordChanged(oldWord, wordManager.getCurrentWordEnglish());
        SoundManager.getInstance().playSound(SoundManager.SoundEffect.WORD_SUCCESS);
    }

    public List<Enemy> getEnemies() { return enemies; }
    public List<Tower> getTowers() { return towers; }
    public List<Bullet> getBullets() { return bullets; }
    public int getWave() { return waveManager.getWave(); }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public int getEnergy() { return energyManager.getEnergy(); }
    public int getMaxEnergy() { return energyManager.getMaxEnergy() + maxEnergyBonus; }
    public String getStatusMessage() { return statusMessage; }
    public long getStatusMessageAt() { return statusMessageAt; }
    public Difficulty getDifficulty() { return difficulty; }
    public int getSelectedTowerCost() { return (int)Math.round(towerShop.getCost(selectedTowerType) * towerCostMultiplier); }
    public boolean isGameOver() { return gameOver; }
    public String getCurrentWord() { return wordManager.getCurrentWordEnglish(); }
    public String getCurrentWordChinese() { return wordManager.getCurrentWordChinese(); }
    public WordEntry getCurrentWordEntry() { return wordManager.getCurrentWordEntry(); }
    public int[] getLaneCenters() { return laneCenters.clone(); }
    public int getLaneTop() { return laneTop; }
    public int getLaneBottom() { return laneBottom; }
    public int getGridLeft() { return gridLeft; }
    public int getGridRight() { return gridRight; }
    public TowerShop.TowerType getSelectedTowerType() { return selectedTowerType; }
    public void setSelectedTowerType(TowerShop.TowerType t) { selectedTowerType = t; }
    public void setDifficulty(Difficulty d) { if (d != null) { difficulty = d; } }
    public void setSoundEnabled(boolean v) { soundEnabled = v; }
    public void setEnemySpeedMultiplier(double v) { waveManager.setExtraSpeedMultiplier(v); }
    public void setTowerCostMultiplier(double v) { towerCostMultiplier = Math.max(0.5, Math.min(3.0, v)); }
    public boolean isPaused() { return paused; }
    public void setPaused(boolean v) { paused = v; }
    public boolean isAwaitingPerkChoice() { return awaitingPerkChoice; }
    public List<PerkChoice> getPerkChoices() { return perkChoices; }

    public boolean choosePerk(int index) {
        if (!awaitingPerkChoice || index < 0 || index >= perkChoices.size()) return false;
        PerkChoice choice = perkChoices.get(index);
        applyPerk(choice.getType());
        awaitingPerkChoice = false; perkChoices.clear(); waveCompleteAt = 0L;
        SoundManager.getInstance().playSound(SoundManager.SoundEffect.PERK_CHOICE);
        SoundManager.getInstance().playSound(SoundManager.SoundEffect.WAVE_START);
        waveManager.advanceWave();
        setStatusMessage("\u7b2c " + waveManager.getWave() + " \u6ce2\u5f00\u59cb\uff01\u589e\u76ca\u751f\u6548\uff01");
        return true;
    }

    public void setStatusMessage(String msg) { this.statusMessage = msg; this.statusMessageAt = System.currentTimeMillis(); }
    private void updateEnemyVisuals() { int size = getAdaptiveEnemySize(); for (Enemy enemy : enemies) enemy.setRenderSize(size); }
    private int getAdaptiveEnemySize() { return Math.max(28, Math.min(72, (int) Math.round(Math.min(getLaneHeight() * 0.78, getColWidth() * 0.82)))); }
    private double getLaneHeight() { return Math.max(40, (laneBottom - laneTop) / (double) laneCount); }
    private double getColWidth() { return Math.max(48, (gridRight - gridLeft) / (double) laneCols); }
    private int snapToLaneColCenter(int x) { int colW = (int) getColWidth(); int col = Math.max(0, Math.min(laneCols - 1, (x - gridLeft) / colW)); return gridLeft + col * colW + colW / 2; }
    private int snapToNearestLane(int y) { double best = Double.MAX_VALUE; int idx = 0; for (int i = 0; i < laneCenters.length; i++) { double d = Math.abs(laneCenters[i] - y); if (d < best) { best = d; idx = i; } } return laneCenters[idx]; }
    private String toDisplayName(TowerShop.TowerType type) { if (type == null) return "\u5854"; return towerShop.getDisplayName(type); }

    private void applyTowerSpecialEffects(Tower tower, Enemy target) {
        if (target == null) return;
        if (tower instanceof IrrigationTower) {
            double slow = isTempBuffActive() ? 0.60 : 0.72;
            long durationMs = isTempBuffActive() ? 2800L : 2200L;
            for (Enemy enemy : enemies) { if (Math.hypot(enemy.getX() - target.getX(), enemy.getY() - target.getY()) <= 95) enemy.applySlow(slow, durationMs); }
        }
        if (tower instanceof PesticideTower) {
            int splash = isTempBuffActive() ? 8 : 5;
            for (Enemy enemy : enemies) { if (enemy != target && Math.hypot(enemy.getX() - target.getX(), enemy.getY() - target.getY()) <= 70) enemy.damage(splash); }
        }
    }

    private void rollPerkChoices() {
        perkChoices.clear();
        List<PerkType> pool = new ArrayList<>();
        pool.add(PerkType.TOWER_UPGRADE); pool.add(PerkType.WORD_BONUS); pool.add(PerkType.TEMP_BUFF);
        pool.add(PerkType.TOWER_RANGE); pool.add(PerkType.ENEMY_WEAKNESS); pool.add(PerkType.ENERGY_BOOST);
        pool.add(PerkType.HEAL_REPAIR); pool.add(PerkType.LIGHTNING_STRIKE); pool.add(PerkType.FIRE_RATE);
        pool.add(PerkType.SCORE_BONUS); pool.add(PerkType.FREE_TOWER); pool.add(PerkType.SHIELD_BARRIER);
        pool.add(PerkType.INSTANT_ENERGY); pool.add(PerkType.CRIT_BOOST); pool.add(PerkType.EXPERIENCE_BOOST);
        pool.add(PerkType.GLOBAL_SLOW); pool.add(PerkType.MAX_HEALTH_UP); pool.add(PerkType.TOWER_REFUND);
        pool.add(PerkType.POISON_AURA); pool.add(PerkType.DEFENSE_WALL); pool.add(PerkType.ENERGY_DRAIN);
        pool.add(PerkType.SUPER_SHOT);
        Collections.shuffle(pool, random);
        for (int i = 0; i < PERK_CHOICES_PER_WAVE && i < pool.size(); i++) { perkChoices.add(createPerkChoice(pool.get(i))); }
    }

    private PerkChoice createPerkChoice(PerkType type) {
        switch (type) {
            case TOWER_UPGRADE:  return new PerkChoice(type, "\u667a\u6167\u704c\u6e89", "\u5168\u5854\u4f24\u5bb3\u6c38\u4e45 +15%\uff08\u53ef\u53e0\u52a0\uff09");
            case WORD_BONUS:     return new PerkChoice(type, "\u5b66\u5b50\u52e4\u5b66", "\u6bcf\u6b21\u6b63\u786e\u8f93\u5165\u989d\u5916\u80fd\u91cf +3\uff08\u53ef\u53e0\u52a0\uff09");
            case TEMP_BUFF:      return new PerkChoice(type, "\u79d1\u6280\u5174\u519c", "20 \u79d2\u5185\u5168\u5854\u4f24\u5bb3\u63d0\u9ad8\u4e14\u704c\u6e89\u51cf\u901f\u66f4\u5f3a");
            case TOWER_RANGE:    return new PerkChoice(type, "\u9065\u611f\u76d1\u6d4b", "\u5168\u5854\u5c04\u7a0b\u6c38\u4e45 +25%\uff08\u53ef\u53e0\u52a0\uff09");
            case ENEMY_WEAKNESS: return new PerkChoice(type, "\u866b\u5bb3\u9884\u8b66", "\u5f53\u524d\u573a\u4e0a\u654c\u4eba\u79fb\u901f -20%\uff08\u6301\u7eed 10 \u79d2\uff09");
            case ENERGY_BOOST:   return new PerkChoice(type, "\u571f\u58e4\u80a5\u529b", "\u80fd\u91cf\u4e0a\u9650\u6c38\u4e45 +20\uff08\u53ef\u53e0\u52a0\uff09");
            case HEAL_REPAIR:    return new PerkChoice(type, "\u751f\u6001\u4fee\u590d", "\u7acb\u5373\u6062\u590d 2 \u70b9\u751f\u547d\u503c");
            case LIGHTNING_STRIKE: return new PerkChoice(type, "\u96f7\u7535\u9632\u62a4", "\u5bf9\u573a\u4e0a\u6240\u6709\u654c\u4eba\u9020\u6210 15 \u70b9\u4f24\u5bb3");
            case FIRE_RATE:      return new PerkChoice(type, "\u673a\u68b0\u5347\u7ea7", "\u5168\u5854\u653b\u51fb\u901f\u5ea6\u6c38\u4e45 +15%\uff08\u53ef\u53e0\u52a0\uff09");
            case SCORE_BONUS:    return new PerkChoice(type, "\u4e30\u6536\u5728\u671b", "\u51fb\u6740\u5f97\u5206\u6c38\u4e45 +50%\uff08\u53ef\u53e0\u52a0\uff09");
            case FREE_TOWER:     return new PerkChoice(type, "\u6821\u53cb\u6350\u8d60", "\u968f\u673a\u83b7\u5f97 1 \u5ea7\u514d\u8d39\u5854\uff08\u65e0\u4eba\u673a/\u704c\u6e89/\u519c\u836f\uff09");
            case SHIELD_BARRIER: return new PerkChoice(type, "\u62a4\u7530\u5c4f\u969c", "8 \u79d2\u5185\u5bb3\u866b\u65e0\u6cd5\u62b5\u8fbe\u679c\u56ed\u533a\uff08\u51bb\u7ed3\u79fb\u901f\uff09");
            case INSTANT_ENERGY:   return new PerkChoice(type, "\u519c\u5927\u80fd\u91cf\u7ad9", "\u7acb\u5373\u83b7\u5f97 30 \u70b9\u80fd\u91cf");
            case CRIT_BOOST:       return new PerkChoice(type, "\u690d\u4fdd\u5f3a\u5316", "\u5168\u5854\u66b4\u51fb\u7387\u6c38\u4e45 +10%\uff08\u53ef\u53e0\u52a0\uff09");
            case EXPERIENCE_BOOST: return new PerkChoice(type, "\u5b66\u672f\u6fc0\u52b1", "\u4e0b\u6b21\u6b63\u786e\u8f93\u5165\u83b7\u5f97\u53cc\u500d\u80fd\u91cf");
            case GLOBAL_SLOW:      return new PerkChoice(type, "\u5bd2\u6f6e\u6765\u88ad", "\u4e0b\u4e00\u6ce2\u6240\u6709\u654c\u4eba\u79fb\u901f -15%");
            case MAX_HEALTH_UP:    return new PerkChoice(type, "\u6c83\u571f\u589e\u80a5", "\u6700\u5927\u751f\u547d\u503c\u6c38\u4e45 +1\uff08\u53ef\u53e0\u52a0\uff09");
            case TOWER_REFUND:     return new PerkChoice(type, "\u8bbe\u5907\u56de\u6536", "\u8fd4\u8fd8\u6700\u8fd1\u4e00\u5ea7\u5854\u82b1\u8d39\u80fd\u91cf\u7684 80%");
            case POISON_AURA:      return new PerkChoice(type, "\u751f\u7269\u9632\u6cbb", "15 \u79d2\u5185\u654c\u4eba\u6bcf\u79d2\u53d7\u5230 2 \u70b9\u4f24\u5bb3");
            case DEFENSE_WALL:     return new PerkChoice(type, "\u56f4\u680f\u52a0\u56fa", "\u5728\u679c\u56ed\u524d\u653e\u7f6e\u4e00\u9053\u4e34\u65f6\u9632\u62a4\u5899");
            case ENERGY_DRAIN:     return new PerkChoice(type, "\u5149\u5408\u4f5c\u7528", "\u51fb\u6740\u654c\u4eba\u989d\u5916\u83b7\u5f97 2 \u70b9\u80fd\u91cf\uff08\u53ef\u53e0\u52a0\uff09");
            case SUPER_SHOT:       return new PerkChoice(type, "\u7cbe\u51c6\u55b7\u65bd", "\u4e0b\u4e00\u6b21\u5854\u653b\u51fb\u4f24\u5bb3\u7ffb\u500d");
            default: return new PerkChoice(type, "\u672a\u77e5\u589e\u76ca", "");
        }
    }

    private void applyPerk(PerkType type) {
        if (type == null) return;
        switch (type) {
            case TOWER_UPGRADE:  towerDamageMultiplier += 0.15; break;
            case WORD_BONUS:     wordEnergyBonus += 3; break;
            case TEMP_BUFF:      tempBuffUntilMs = System.currentTimeMillis() + 20000L; break;
            case TOWER_RANGE:    towerRangeMultiplier += 0.25; break;
            case ENEMY_WEAKNESS: for (Enemy e : enemies) e.applySlow(0.80, 10000L); break;
            case ENERGY_BOOST:   maxEnergyBonus += 20; energyManager.setMaxEnergy(getMaxEnergy()); break;
            case HEAL_REPAIR:    lives = Math.min(lives + 2, difficulty.getInitialLives()); break;
            case LIGHTNING_STRIKE: for (Enemy e : enemies) e.damage(15); break;
            case FIRE_RATE:      towerFireRateBonus += 15; break;
            case SCORE_BONUS:    scoreMultiplierBonus += 50; break;
            case FREE_TOWER: {
                TowerShop.TowerType[] types = {TowerShop.TowerType.DRONE, TowerShop.TowerType.IRRIGATION, TowerShop.TowerType.PESTICIDE};
                TowerShop.TowerType rt = types[random.nextInt(3)];
                int freeX = gridLeft + 60 + random.nextInt(gridRight - gridLeft - 120);
                int freeY = laneCenters[random.nextInt(laneCenters.length)];
                int attempts = 0;
                while (!isValidPlacement(freeX, freeY) && attempts < 50) { freeX = gridLeft + 60 + random.nextInt(gridRight - gridLeft - 120); freeY = laneCenters[random.nextInt(laneCenters.length)]; attempts++; }
                if (isValidPlacement(freeX, freeY)) { Tower t = towerShop.create(rt, freeX, freeY); t.boostRange(towerRangeMultiplier); towers.add(t); setStatusMessage("\u514d\u8d39\u83b7\u5f97 " + towerShop.getDisplayName(rt) + "\uff01"); }
                else { energyManager.addEnergy(50); setStatusMessage("\u65e0\u7a7a\u4f4d\u653e\u7f6e\u514d\u8d39\u5854\uff0c\u8865\u507f\u80fd\u91cf+50"); }
                break;
            }
            case SHIELD_BARRIER: { for (Enemy e : enemies) e.applySlow(0.01, 8000L); break; }
            case INSTANT_ENERGY:   energyManager.addEnergy(30); break;
            case CRIT_BOOST:       critChanceBonus += 0.10; break;
            case EXPERIENCE_BOOST: wordEnergyBonus += 10; break;
            case GLOBAL_SLOW:      enemySpeedNerf = Math.max(0.5, enemySpeedNerf - 0.15); waveManager.setExtraSpeedMultiplier(enemySpeedNerf); break;
            case MAX_HEALTH_UP:    lives++; break;
            case TOWER_REFUND: {
                if (!undoStack.isEmpty()) { UndoRecord rec = undoStack.get(undoStack.size() - 1); if (towers.contains(rec.tower)) { int refund = (int) Math.round(rec.cost * 0.8); energyManager.addEnergy(refund); setStatusMessage("\u56de\u6536\u80fd\u91cf +" + refund); } }
                break;
            }
            case POISON_AURA:      poisonAuraActive = true; poisonAuraUntilMs = System.currentTimeMillis() + 15000L; break;
            case DEFENSE_WALL:     energyManager.addEnergy(40); setStatusMessage("\u9632\u62a4\u5899\u5df2\u90e8\u7f72\uff0c\u83b7\u5f97\u80fd\u91cf +40"); break;
            case ENERGY_DRAIN:     energyDrainBonus += 2; break;
            case SUPER_SHOT:       superShotActive = true; break;
        }
    }

    private boolean isTempBuffActive() { return System.currentTimeMillis() <= tempBuffUntilMs; }
    private double getCurrentAttackMultiplier() { return towerDamageMultiplier * (isTempBuffActive() ? 1.25 : 1.0); }
    private void fireCurrentWordChanged(String oldWord, String newWord) { pcs.firePropertyChange(PROP_CURRENT_WORD, oldWord, newWord); }

    public int getTowerCost(TowerShop.TowerType type) {
        return (int) Math.round(towerShop.getCost(type) * towerCostMultiplier);
    }
    public long getUndoTimeRemainingMs() {
        long now = System.currentTimeMillis();
        long best = 0L;
        for (UndoRecord rec : undoStack) {
            if (towers.contains(rec.tower)) {
                long remaining = UNDO_WINDOW_MS - (now - rec.placedAt);
                if (remaining > best) best = remaining;
            }
        }
        return Math.max(0, best);
    }
    public long getUndoWindowMs() { return UNDO_WINDOW_MS; }

}
