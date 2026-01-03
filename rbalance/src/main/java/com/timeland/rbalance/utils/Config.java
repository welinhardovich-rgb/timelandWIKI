package com.timeland.rbalance.utils;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.economy.CurrencyType;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {
    private final RBalancePlugin plugin;
    
    public Config(RBalancePlugin plugin) {
        this.plugin = plugin;
    }
    
    public double getDepositCommission(CurrencyType currency) {
        return plugin.getConfig().getDouble("economy.currencies." + currency.name().toLowerCase() + ".deposit-commission", 1.0);
    }
    
    public double getWithdrawCommission(CurrencyType currency) {
        return plugin.getConfig().getDouble("economy.currencies." + currency.name().toLowerCase() + ".withdraw-commission", 3.0);
    }
    
    public double getDailyDepositLimit(CurrencyType currency) {
        return plugin.getConfig().getDouble("economy.currencies." + currency.name().toLowerCase() + ".daily-limit", 1000.0);
    }
    
    public double getMinOperationAmount() {
        return plugin.getConfig().getDouble("economy.min-operation", 0.1);
    }
    
    public boolean isCurrencyEnabled(CurrencyType currency) {
        return plugin.getConfig().getBoolean("economy.currencies." + currency.name().toLowerCase() + ".enabled", true);
    }
    
    public boolean isTradeEnabled() {
        return plugin.getConfig().getBoolean("trade.enabled", true);
    }
    
    public boolean isChunkOwnerRequired() {
        return plugin.getConfig().getBoolean("trade.chunk-owner-required", true);
    }
    
    public int getMaxSignsPerPlayer() {
        return plugin.getConfig().getInt("trade.max-signs-per-player", 10);
    }
    
    public boolean isHistoryEnabled() {
        return plugin.getConfig().getBoolean("logging.history-enabled", true);
    }
    
    public int getMaxHistoryEntries() {
        return plugin.getConfig().getInt("logging.max-history-entries", 1000);
    }
    
    public double getMinTradeAmount() {
        return plugin.getConfig().getDouble("trade.min-trade-amount", 0.1);
    }
    
    public void reload() {
        plugin.reloadConfig();
    }
}