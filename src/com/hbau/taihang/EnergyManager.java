package com.hbau.taihang;

/**
 * Manages player energy. Starts at 100 and caps at 100.
 */
public class EnergyManager {
    private int maxEnergy;
    private int energy;

    public EnergyManager() {
        this(100, 100);
    }

    public EnergyManager(int initialEnergy, int maxEnergy) {
        this.maxEnergy = Math.max(0, maxEnergy);
        this.energy = clamp(initialEnergy);
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public void setMaxEnergy(int newMax) {
        this.maxEnergy = Math.max(0, Math.max(newMax, energy));
    }

    public boolean canAfford(int cost) {
        return cost >= 0 && energy >= cost;
    }

    public int addEnergy(int amount) {
        if (amount <= 0) {
            return energy;
        }
        energy = clamp(energy + amount);
        return energy;
    }

    public boolean spendEnergy(int cost) {
        if (!canAfford(cost)) {
            return false;
        }
        energy -= cost;
        return true;
    }

    public void setEnergy(int energy) {
        this.energy = clamp(energy);
    }

    private int clamp(int value) {
        if (value < 0) return 0;
        return Math.min(value, maxEnergy);
    }
}