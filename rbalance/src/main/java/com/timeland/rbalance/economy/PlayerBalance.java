package com.timeland.rbalance.economy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerBalance {
    private final UUID playerId;
    private final Map<CurrencyType, Double> balances;
    private long lastDepositReset;
    private final Map<CurrencyType, Double> dailyDeposits;

    public PlayerBalance(UUID playerId) {
        this.playerId = playerId;
        this.balances = new HashMap<>();
        for (CurrencyType type : CurrencyType.values()) {
            balances.put(type, 0.0);
        }
        this.lastDepositReset = System.currentTimeMillis();
        this.dailyDeposits = new HashMap<>();
        for (CurrencyType type : CurrencyType.values()) {
            dailyDeposits.put(type, 0.0);
        }
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public double getBalance(CurrencyType currency) {
        return balances.getOrDefault(currency, 0.0);
    }

    public void setBalance(CurrencyType currency, double amount) {
        balances.put(currency, Math.max(0, amount));
    }

    public boolean deposit(CurrencyType currency, double amount) {
        if (amount < 0) return false;
        double current = getBalance(currency);
        balances.put(currency, current + amount);
        return true;
    }

    public boolean withdraw(CurrencyType currency, double amount) {
        if (amount < 0) return false;
        double current = getBalance(currency);
        if (current >= amount) {
            balances.put(currency, current - amount);
            return true;
        }
        return false;
    }

    public boolean hasSufficient(CurrencyType currency, double amount) {
        return getBalance(currency) >= amount;
    }

    public Map<CurrencyType, Double> getAllBalances() {
        return new HashMap<>(balances);
    }

    public void resetDailyDeposits() {
        for (CurrencyType type : dailyDeposits.keySet()) {
            dailyDeposits.put(type, 0.0);
        }
        lastDepositReset = System.currentTimeMillis();
    }

    public double getDailyDeposit(CurrencyType currency) {
        return dailyDeposits.getOrDefault(currency, 0.0);
    }

    public void addDailyDeposit(CurrencyType currency, double amount) {
        double current = getDailyDeposit(currency);
        dailyDeposits.put(currency, current + amount);
    }

    public long getLastDepositReset() {
        return lastDepositReset;
    }

    public void setLastDepositReset(long timestamp) {
        this.lastDepositReset = timestamp;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        Map<String, Double> balancesData = new HashMap<>();
        for (Map.Entry<CurrencyType, Double> entry : balances.entrySet()) {
            balancesData.put(entry.getKey().name(), entry.getValue());
        }
        data.put("balances", balancesData);
        
        Map<String, Double> dailyData = new HashMap<>();
        for (Map.Entry<CurrencyType, Double> entry : dailyDeposits.entrySet()) {
            dailyData.put(entry.getKey().name(), entry.getValue());
        }
        data.put("dailyDeposits", dailyData);
        data.put("lastDepositReset", lastDepositReset);
        
        return data;
    }

    public void deserialize(Map<String, Object> data) {
        @SuppressWarnings("unchecked")
        Map<String, Double> balancesData = (Map<String, Double>) data.get("balances");
        if (balancesData != null) {
            for (Map.Entry<String, Double> entry : balancesData.entrySet()) {
                try {
                    CurrencyType currency = CurrencyType.valueOf(entry.getKey());
                    balances.put(currency, entry.getValue());
                } catch (IllegalArgumentException ignored) {}
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, Double> dailyData = (Map<String, Double>) data.get("dailyDeposits");
        if (dailyData != null) {
            for (Map.Entry<String, Double> entry : dailyData.entrySet()) {
                try {
                    CurrencyType currency = CurrencyType.valueOf(entry.getKey());
                    dailyDeposits.put(currency, entry.getValue());
                } catch (IllegalArgumentException ignored) {}
            }
        }

        if (data.containsKey("lastDepositReset")) {
            lastDepositReset = (Long) data.get("lastDepositReset");
        }
    }
}