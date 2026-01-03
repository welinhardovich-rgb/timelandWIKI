package com.timeland.rbalance.trade;

import com.timeland.rbalance.economy.CurrencyType;
import org.bukkit.Location;
import java.util.UUID;

public class TradeSign {
    private final Location location;
    private final UUID owner;
    private final CurrencyType currency;
    private final double sellAmount;
    private final double buyAmount;
    private final boolean buyMode;
    
    public TradeSign(Location location, UUID owner, CurrencyType currency, double sellAmount, double buyAmount, boolean buyMode) {
        this.location = location;
        this.owner = owner;
        this.currency = currency;
        this.sellAmount = sellAmount;
        this.buyAmount = buyAmount;
        this.buyMode = buyMode;
    }
    
    public TradeSign(Location location, UUID owner, CurrencyType currency, double buyAmount, boolean buyMode) {
        this.location = location;
        this.owner = owner;
        this.currency = currency;
        this.sellAmount = 1.0;
        this.buyAmount = buyAmount;
        this.buyMode = buyMode;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    public CurrencyType getCurrency() {
        return currency;
    }
    
    public double getSellAmount() {
        return sellAmount;
    }
    
    public double getBuyAmount() {
        return buyAmount;
    }
    
    public boolean isBuyMode() {
        return buyMode;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TradeSign other = (TradeSign) obj;
        return location.equals(other.location);
    }
    
    @Override
    public int hashCode() {
        return location.hashCode();
    }
}