package com.timeland.rbalance.protection;

import com.timeland.rbalance.RBalancePlugin;
import org.bukkit.event.Listener;

public class AbuseProtection implements Listener {
    private final RBalancePlugin plugin;
    
    public AbuseProtection(RBalancePlugin plugin) {
        this.plugin = plugin;
    }
}