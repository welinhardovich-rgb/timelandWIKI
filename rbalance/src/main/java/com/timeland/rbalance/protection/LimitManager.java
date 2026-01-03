package com.timeland.rbalance.protection;

import com.timeland.rbalance.RBalancePlugin;
import org.bukkit.event.Listener;

public class LimitManager implements Listener {
    private final RBalancePlugin plugin;
    
    public LimitManager(RBalancePlugin plugin) {
        this.plugin = plugin;
    }
}