package com.hbau.taihang;

/**
 * Simple shop for buying towers with energy.
 */
public class TowerShop {
    public static final int DRONE_TOWER_COST = 50;
    public static final int IRRIGATION_TOWER_COST = 30;
    public static final int PESTICIDE_TOWER_COST = 70;

    public enum TowerType {
        DRONE,
        IRRIGATION
        , PESTICIDE
    }

    public String getDisplayName(TowerType type) {
        if (type == null) {
            return "??";
        }
        if (type == TowerType.DRONE) {
            return "无人机塔";
        } else if (type == TowerType.IRRIGATION) {
            return "灌溉塔";
        } else {
            return "农药塔";
        }
    }

    public String getRoleHint(TowerType type) {
        if (type == null) {
            return "-";
        }
        if (type == TowerType.DRONE) {
            return "远程点杀";
        } else if (type == TowerType.IRRIGATION) {
            return "控场减速";
        } else {
            return "溅射清群";
        }
    }

    public String getFeatureText(TowerType type) {
        if (type == null) {
            return "-";
        }
        if (type == TowerType.DRONE) {
            return "优先锁定前排害虫";
        } else if (type == TowerType.IRRIGATION) {
            return "命中后可减速周围敌人";
        } else {
            return "命中后对附近敌人造成溅射";
        }
    }

    public String getStatsText(TowerType type) {
        if (type == null) {
            return "-";
        }
        if (type == TowerType.DRONE) {
            return "攻:12 / 程:160 / 速:0.9s";
        } else if (type == TowerType.IRRIGATION) {
            return "攻:4 / 程:120 / 速:1.2s";
        } else {
            return "攻:8 / 程:140 / 速:1.4s";
        }
    }

    public String getTooltipText(TowerType type) {
        if (type == null) {
            return "";
        }
        return getDisplayName(type) + " | " + getRoleHint(type) + " | " + getFeatureText(type) + " | " + getStatsText(type)
                + " | 价格:" + getCost(type);
    }

    public boolean canBuy(EnergyManager energyManager, TowerType type) {
        if (energyManager == null || type == null) {
            return false;
        }
        return energyManager.getEnergy() >= getCost(type);
    }

    public int getCost(TowerType type) {
        if (type == null) {
            return Integer.MAX_VALUE;
        }
        if (type == TowerType.DRONE) {
            return DRONE_TOWER_COST;
        } else if (type == TowerType.IRRIGATION) {
            return IRRIGATION_TOWER_COST;
        } else {
            return PESTICIDE_TOWER_COST;
        }
    }

    public Tower create(TowerType type, int x, int y) {
        return createTower(type, x, y);
    }

    public Tower createTower(TowerType type, int x, int y) {
        if (type == null) {
            return null;
        }
        if (type == TowerType.DRONE) {
            return new DroneTower(x, y);
        } else if (type == TowerType.IRRIGATION) {
            return new IrrigationTower(x, y);
        } else {
            return new PesticideTower(x, y);
        }
    }
}