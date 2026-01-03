package com.timeland.rbalance.economy;

import com.timeland.rbalance.RBalancePlugin;

public class CommissionHandler {
    private final RBalancePlugin plugin;
    
    public CommissionHandler(RBalancePlugin plugin) {
        this.plugin = plugin;
    }
    
    public double calculateFee(double amount, CurrencyType currency, String operation) {
        double commissionRate = 0.0;
        
        switch (operation.toLowerCase()) {
            case "deposit":
                commissionRate = plugin.getConfiguration().getDepositCommission(currency);
                break;
            case "withdraw":
                commissionRate = plugin.getConfiguration().getWithdrawCommission(currency);
                break;
            case "transfer":
                commissionRate = 1.0;
                break;
            default:
                commissionRate = 0.0;
        }
        
        return (amount * commissionRate) / 100.0;
    }
    
    public double applyFee(double amount, CurrencyType currency, String operation) {
        double fee = calculateFee(amount, currency, operation);
        return amount - fee;
    }
}