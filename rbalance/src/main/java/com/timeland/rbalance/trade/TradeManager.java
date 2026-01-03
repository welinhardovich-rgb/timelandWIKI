package com.timeland.rbalance.trade;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.economy.CurrencyType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TradeManager implements Listener {
    private final RBalancePlugin plugin;
    private final Map<Location, TradeSign> tradeSigns;
    private static final Pattern TRADE_PATTERN = Pattern.compile("\\[Trade\\]");
    private static final Pattern PRICE_PATTERN = Pattern.compile("S:(\\d+(?:\\.\\d+)?)([IGDNE])\\s*/\\s*B:(\\d+(?:\\.\\d+)?)([IGDNE])");
    
    public TradeManager(RBalancePlugin plugin) {
        this.plugin = plugin;
        this.tradeSigns = new HashMap<>();
    }
    
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        String line1 = event.getLine(0);
        if (line1 == null || !TRADE_PATTERN.matcher(line1).find()) {
            return;
        }
        
        Player player = event.getPlayer();
        
        if (!event.getPlayer().hasPermission("rbalance.trade.create")) {
            player.sendMessage("§cУ вас нет прав создавать торговые таблички!");
            return;
        }
        
        String line2 = event.getLine(1);
        String playerName = player.getName();
        if (line2 != null && !line2.isEmpty() && !line2.equals(playerName)) {
            player.sendMessage("§cВы можете создавать таблички только от своего имени!");
            return;
        }
        event.setLine(1, playerName);
        
        String line3 = event.getLine(2);
        String line4 = event.getLine(3);
        
        if (line3 == null || line4 == null || !line3.toLowerCase().contains("x") || !PRICE_PATTERN.matcher(line4).find()) {
            player.sendMessage("§cНеверный формат торговой таблички!");
            event.setCancelled(true);
            return;
        }
        
        String[] parts = line3.split("x");
        if (parts.length != 2) {
            player.sendMessage("§cНеверный формат строки 3! Используйте: <Ресурс>x<Кол-во>");
            event.setCancelled(true);
            return;
        }
        
        String resourceSymbol = parts[0].toUpperCase();
        try {
            double amount = Double.parseDouble(parts[1]);
            CurrencyType currency = CurrencyType.fromString(resourceSymbol);
            
            if (currency == null) {
                player.sendMessage("§cНеверный тип валюты!");
                event.setCancelled(true);
                return;
            }
            
            Matcher priceMatcher = PRICE_PATTERN.matcher(line4);
            if (priceMatcher.find()) {
                double sellAmount = Double.parseDouble(priceMatcher.group(1));
                String sellSymbol = priceMatcher.group(2);
                double buyAmount = Double.parseDouble(priceMatcher.group(3));
                CurrencyType priceCurrency = CurrencyType.fromString(sellSymbol);
                
                if (priceCurrency != null && currency == priceCurrency) {
                    Location location = event.getBlock().getLocation();
                    TradeSign tradeSign = new TradeSign(location, player.getUniqueId(), currency, sellAmount, buyAmount, false);
                    tradeSigns.put(location, tradeSign);
                    
                    player.sendMessage("§aТорговая табличка успешно создана!");
                    return;
                }
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§cНеверное количество!");
        }
        
        player.sendMessage("§cНе удалось создать торговую табличку. Проверьте формат.");
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT_CLICK")) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;
        
        if (block.getType() == Material.OAK_WALL_SIGN || block.getType() == Material.OAK_SIGN) {
            processSignInteraction(block, event.getPlayer());
        }
    }
    
    private void processSignInteraction(Block block, Player buyer) {
        Location location = block.getLocation();
        TradeSign tradeSign = tradeSigns.get(location);
        
        if (tradeSign == null) return;
        
        if (buyer.getUniqueId().equals(tradeSign.getOwner())) {
            buyer.sendMessage("§cВы не можете использовать свою собственную табличку!");
            return;
        }
        
        UUID sellerId = tradeSign.getOwner();
        if (!plugin.getBalanceManager().getPlayerBalance(sellerId).hasSufficient(tradeSign.getCurrency(), tradeSign.getSellAmount())) {
            buyer.sendMessage("§cУ продавца недостаточно средств для сделки!");
            return;
        }
        
        plugin.getBalanceManager().processTradeSign(buyer, sellerId, tradeSign.getCurrency(), 
                tradeSign.getSellAmount(), tradeSign.getBuyAmount());
    }
    
    @EventHandler
    public void onSignBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.OAK_WALL_SIGN || block.getType() == Material.OAK_SIGN) {
            TradeSign tradeSign = tradeSigns.get(block.getLocation());
            if (tradeSign != null) {
                Player breaker = event.getPlayer();
                if (breaker != null && !breaker.getUniqueId().equals(tradeSign.getOwner()) && !breaker.hasPermission("rbalance.trade.admin")) {
                    breaker.sendMessage("§cВы не можете ломать чужую торговую табличку!");
                    event.setCancelled(true);
                    return;
                }
                tradeSigns.remove(block.getLocation());
            }
        }
    }
}