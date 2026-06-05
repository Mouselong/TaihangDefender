package com.hbau.taihang;

/**
 * Simple non-GUI smoke test for core logic.
 */
public class SmokeTest {
    public static void main(String... args) {
        WordManager wm = new WordManager();
        wm.loadAll();
        wm.pickNextWord();
        String word = wm.getCurrentWordEnglish();
        if (word == null || word.trim().isEmpty()) {
            throw new IllegalStateException("No word loaded");
        }
        String prefix = word.substring(0, 1);
        wm.updateInput(prefix);
        if (!wm.isPrefix(prefix)) {
            throw new IllegalStateException("Prefix match failed");
        }
        if (wm.matchesCurrentWord(prefix)) {
            throw new IllegalStateException("Prefix should not be full match");
        }
        wm.updateInput(word);
        if (!wm.matchesCurrentWord(word)) {
            throw new IllegalStateException("Full match failed");
        }

        EnergyManager energyManager = new EnergyManager();
        if (energyManager.getEnergy() != 100) {
            throw new IllegalStateException("Initial energy failed");
        }
        energyManager.addEnergy(10);
        if (energyManager.getEnergy() != 100) {
            throw new IllegalStateException("Energy cap failed");
        }
        if (!energyManager.spendEnergy(50) || energyManager.getEnergy() != 50) {
            throw new IllegalStateException("Energy spend failed");
        }

        TowerShop shop = new TowerShop();
        if (shop.getCost(TowerShop.TowerType.DRONE) != TowerShop.DRONE_TOWER_COST) {
            throw new IllegalStateException("Drone cost failed");
        }
        if (shop.getCost(TowerShop.TowerType.IRRIGATION) != TowerShop.IRRIGATION_TOWER_COST) {
            throw new IllegalStateException("Irrigation cost failed");
        }
        if (!shop.canBuy(energyManager, TowerShop.TowerType.IRRIGATION)) {
            throw new IllegalStateException("Shop affordability failed");
        }

        EnemyType testType = EnemyType.T1_EAST_ASIAN_LOCUST;
        LaneEnemy visualEnemy = new LaneEnemy();
        visualEnemy.init(900, 120, 32,
                testType.createHp(1),
                testType.createSpeed(1, 1.0),
                testType.getMainColor(),
                testType.createScoreValue(1),
                testType.getTierLabel(),
                testType);
        visualEnemy.setRenderSize(54);
        if (visualEnemy.getRenderSize() < 54 || visualEnemy.getRadius() <= 0) {
            throw new IllegalStateException("Enemy sprite sizing failed");
        }
        if (!(EnemyType.T1_EAST_ASIAN_LOCUST.createHp(1) > EnemyType.T2_CORN_BORER.createHp(1)
                && EnemyType.T2_CORN_BORER.createHp(1) > EnemyType.T3_CUTWORM.createHp(1)
                && EnemyType.T3_CUTWORM.createHp(1) > EnemyType.T4_WIREWORM.createHp(1))) {
            throw new IllegalStateException("Tier hp ordering failed");
        }
        if (!(EnemyType.T1_EAST_ASIAN_LOCUST.createSpeed(1, 1.0) < EnemyType.T2_CORN_BORER.createSpeed(1, 1.0)
                && EnemyType.T2_CORN_BORER.createSpeed(1, 1.0) < EnemyType.T3_CUTWORM.createSpeed(1, 1.0)
                && EnemyType.T3_CUTWORM.createSpeed(1, 1.0) < EnemyType.T4_WIREWORM.createSpeed(1, 1.0))) {
            throw new IllegalStateException("Tier speed ordering failed");
        }

        GamePanel panel = new GamePanel(wm);
        panel.setSize(1100, 680);
        GameEngine engine = new GameEngine(panel, wm, new WaveManager());
        if (engine.getEnergy() != 100) {
            throw new IllegalStateException("Engine initial energy failed");
        }

        // Undo flow: place two towers, then undo twice and verify energy/tower count restored.
        if (!engine.purchaseTowerAt(TowerShop.TowerType.DRONE, 120, 120)) {
            throw new IllegalStateException("First tower purchase failed");
        }
        if (!engine.purchaseTowerAt(TowerShop.TowerType.DRONE, 200, 120)) {
            throw new IllegalStateException("Second tower purchase failed");
        }
        if (engine.getPurchasedTowerCount() != 2) {
            throw new IllegalStateException("Tower count after purchases failed");
        }
        if (engine.getEnergy() != 0) {
            throw new IllegalStateException("Energy after purchases failed");
        }
        if (!engine.hasUndo()) {
            throw new IllegalStateException("Undo availability failed");
        }
        if (engine.undoLastPlacedTower() == null) {
            throw new IllegalStateException("First undo failed");
        }
        if (engine.getPurchasedTowerCount() != 1 || engine.getEnergy() != 50) {
            throw new IllegalStateException("State after first undo failed");
        }
        if (engine.undoLastPlacedTower() == null) {
            throw new IllegalStateException("Second undo failed");
        }
        if (engine.getPurchasedTowerCount() != 0 || engine.getEnergy() != 100) {
            throw new IllegalStateException("State after second undo failed");
        }

        System.out.println("SmokeTest OK: " + word);
    }
}

