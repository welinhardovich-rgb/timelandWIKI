package com.timeland.rbalance.utils;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.economy.CurrencyType;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

public class Logger {
    private final RBalancePlugin plugin;
    private final File logFile;
    private final SimpleDateFormat dateFormat;
    private final Map<UUID, List<String>> playerHistory;
    private static final int MAX_HISTORY = 1000;
    
    public Logger(RBalancePlugin plugin) {
        this.plugin = plugin;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.logFile = new File(plugin.getDataFolder(), "transactions.log");
        this.playerHistory = new HashMap<>();
        
        if (!logFile.getParentFile().exists()) {
            logFile.getParentFile().mkdirs();
        }
    }
    
    public void logDeposit(Player player, CurrencyType currency, double amount, double finalAmount, double commission) {
        String message = String.format("[DEPOSIT] %s %s deposited %.2f %s → %.2f (comm: %.2f)",
                getTimestamp(), player.getName(), amount, currency.getSymbol(), 
                finalAmount, commission);
        log(message);
        addToHistory(player.getUniqueId(), message);
    }
    
    public void logWithdraw(Player player, CurrencyType currency, double amount, double finalAmount, double commission) {
        String message = String.format("[WITHDRAW] %s %s withdrew %.2f %s → %.2f (comm: %.2f)",
                getTimestamp(), player.getName(), amount, currency.getSymbol(), 
                finalAmount, commission);
        log(message);
        addToHistory(player.getUniqueId(), message);
    }
    
    public void logTransfer(Player from, Player to, CurrencyType currency, double amount, double targetAmount, double commission) {
        String message = String.format("[TRANSFER] %s %s → %s sent %.2f %s → %.2f (comm: %.2f)",
                getTimestamp(), from.getName(), to.getName(), amount, currency.getSymbol(), 
                targetAmount, commission);
        log(message);
        addToHistory(from.getUniqueId(), message);
        addToHistory(to.getUniqueId(), message);
    }
    
    public void logTrade(Player buyer, UUID sellerId, CurrencyType currency, double sellAmount, double buyAmount) {
        String sellerName = plugin.getServer().getOfflinePlayer(sellerId).getName();
        String message = String.format("[TRADE] %s %s bought %.2f %s for %.2f from %s",
                getTimestamp(), buyer.getName(), sellAmount, currency.getSymbol(), 
                buyAmount, sellerName != null ? sellerName : sellerId.toString());
        log(message);
        addToHistory(buyer.getUniqueId(), message);
        
        if (sellerName != null) {
            addToHistory(sellerId, message);
        }
    }
    
    public void log(String message) {
        if (plugin.getConfiguration().isHistoryEnabled()) {
            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write(message + System.lineSeparator());
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to write to log file: " + e.getMessage());
            }
        }
        
        if (plugin.getConfig().getBoolean("logging.log-to-console", true)) {
            plugin.getLogger().info(message);
        }
    }
    
    private void addToHistory(UUID playerId, String message) {
        if (!plugin.getConfiguration().isHistoryEnabled()) return;
        
        playerHistory.computeIfAbsent(playerId, k -> new ArrayList<>()).add(message);
        
        List<String> history = playerHistory.get(playerId);
        if (history.size() > plugin.getConfiguration().getMaxHistoryEntries()) {
            history.remove(0);
        }
    }
    
    public List<String> getHistory(UUID playerId) {
        return playerHistory.getOrDefault(playerId, new ArrayList<>());
    }
    
    private String getTimestamp() {
        return dateFormat.format(new Date());
    }
}