package com.timeland.rbalance.economy;

import org.bukkit.Material;

public enum CurrencyType {
    IRON("Iron", "I", Material.IRON_INGOT),
    GOLD("Gold", "G", Material.GOLD_INGOT),
    DIAMOND("Diamond", "D", Material.DIAMOND),
    NETHERITE("Netherite", "N", Material.NETHERITE_INGOT),
    EMERALD("Emerald", "E", Material.EMERALD);

    private final String displayName;
    private final String symbol;
    private final Material material;

    CurrencyType(String displayName, String symbol, Material material) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.material = material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public Material getMaterial() {
        return material;
    }

    public static CurrencyType fromString(String name) {
        for (CurrencyType type : values()) {
            if (type.name().equalsIgnoreCase(name) || type.getDisplayName().equalsIgnoreCase(name) || 
                type.getSymbol().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public double getNuggetValue() {
        switch (this) {
            case IRON: return 0.1;
            case GOLD: return 0.1;
            case EMERALD: return 0.1;
            case DIAMOND: return 0.1;
            case NETHERITE: return 0.1;
            default: return 0.1;
        }
    }

    public Material getNuggetMaterial() {
        switch (this) {
            case IRON: return Material.IRON_NUGGET;
            case GOLD: return Material.GOLD_NUGGET;
            default: return null;
        }
    }
}