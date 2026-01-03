package com.timeland.rbalance;

import com.timeland.rbalance.commands.BalanceCommand;
import com.timeland.rbalance.commands.TradeCommand;
import com.timeland.rbalance.economy.BalanceManager;
import com.timeland.rbalance.protection.AbuseProtection;
import com.timeland.rbalance.protection.LimitManager;
import com.timeland.rbalance.trade.TradeManager;
import com.timeland.rbalance.utils.Config;
import com.timeland.rbalance.utils.Logger;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class RBalancePlugin extends JavaPlugin {
    private static RBalancePlugin instance;
    private BalanceManager balanceManager;
    private LimitManager limitManager;
    private TradeManager tradeManager;
    private AbuseProtection abuseProtection;
    private Config config;
    private Logger logger;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        config = new Config(this);
        logger = new Logger(this);
        
        balanceManager = new BalanceManager(this);
        limitManager = new LimitManager(this);
        tradeManager = new TradeManager(this);
        abuseProtection = new AbuseProtection(this);
        
        registerCommand("bal", new BalanceCommand(this));
        registerCommand("tradecmd", new TradeCommand(this));
        
        registerListener(balanceManager);
        registerListener(tradeManager);
        registerListener(abuseProtection);
        registerListener(limitManager);
        
        getLogger().info("RBalance plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (balanceManager != null) {
            balanceManager.saveAllData();
        }
        getLogger().info("RBalance plugin disabled!");
    }

    private void registerCommand(String name, CommandExecutor executor) {
        getCommand(name).setExecutor(executor);
    }

    private void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public static RBalancePlugin getInstance() {
        return instance;
    }

    public BalanceManager getBalanceManager() {
        return balanceManager;
    }

    public LimitManager getLimitManager() {
        return limitManager;
    }

    public TradeManager getTradeManager() {
        return tradeManager;
    }

    public AbuseProtection getAbuseProtection() {
        return abuseProtection;
    }

    public Config getConfiguration() {
        return config;
    }

    public Logger getRBLogger() {
        return logger;
    }
}