package com.timeland.rbalance.economy;

import com.timeland.rbalance.RBalancePlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BalanceManager implements Listener {
    private final RBalancePlugin plugin;
    private final Map<UUID, PlayerBalance> playerBalances;
    private final Map<CurrencyType, Double> serverBalances;
    private final CommissionHandler commissionHandler;
    private final File dataFile;
    private static final long DAY_IN_MS = 24 * 60 * 60 * 1000;

    public BalanceManager(RBalancePlugin plugin) {
        this.plugin = plugin;
        this.playerBalances = new ConcurrentHashMap<>();
        this.serverBalances = new HashMap<>();
        this.commissionHandler = new CommissionHandler(plugin);
        this.dataFile = new File(plugin.getDataFolder(), "balance.yml");
        
        for (CurrencyType type : CurrencyType.values()) {
            serverBalances.put(type, 0.0);
        }
        
        loadAllData();
        checkDailyReset();
    }

    public PlayerBalance getPlayerBalance(Player player) {
        return getPlayerBalance(player.getUniqueId());
    }

    public PlayerBalance getPlayerBalance(UUID playerId) {
        return playerBalances.computeIfAbsent(playerId, PlayerBalance::new);
    }

    public double getBalance(Player player, CurrencyType currency) {
        return getPlayerBalance(player).getBalance(currency);
    }

    public boolean deposit(Player player, CurrencyType currency, double amount) {
        if (amount <= 0) return false;
        
        PlayerBalance balance = getPlayerBalance(player);
        double maxLimit = plugin.getConfiguration().getDailyDepositLimit(currency);
        double currentDaily = balance.getDailyDeposit(currency);
        
        if (currentDaily + amount > maxLimit) {
            player.sendMessage("§cВы достигли дневного лимита депозита для " + currency.getDisplayName());
            return false;
        }

        Material material = currency.getMaterial();
        String correctName = getExpectedResourceName(material);
        if (!removeFromInventory(player, material, correctName)) return false;

        double commission = commissionHandler.calculateFee(amount, currency, "deposit");
        double finalAmount = amount - commission;
        
        balance.deposit(currency, finalAmount);
        balance.addDailyDeposit(currency, amount);
        serverBalances.put(currency, serverBalances.get(currency) + commission);
        
        plugin.getRBLogger().logDeposit(player, currency, amount, finalAmount, commission);
        player.sendMessage("§aДепозит: " + formatAmount(amount, currency) + " (комиссия: " + formatAmount(commission, currency) + ")");
        
        return true;
    }

    public boolean withdraw(Player player, CurrencyType currency, double amount) {
        if (amount <= 0) return false;
        
        PlayerBalance balance = getPlayerBalance(player);
        if (!balance.hasSufficient(currency, amount)) {
            player.sendMessage("§cНедостаточно средств!");
            return false;
        }

        double commission = commissionHandler.calculateFee(amount, currency, "withdraw");
        double finalAmount = amount - commission;
        
        if (!balance.withdraw(currency, amount)) return false;
        
        if (!addToInventory(player, currency.getMaterial(), finalAmount)) {
            balance.deposit(currency, amount);
            return false;
        }
        
        serverBalances.put(currency, serverBalances.get(currency) + commission);
        
        plugin.getRBLogger().logWithdraw(player, currency, amount, finalAmount, commission);
        player.sendMessage("§aВывод: " + formatAmount(finalAmount, currency) + " (комиссия: " + formatAmount(commission, currency) + ")");
        
        return true;
    }

    public boolean transfer(Player from, Player to, CurrencyType currency, double amount) {
        if (amount <= 0) return false;
        
        PlayerBalance fromBalance = getPlayerBalance(from);
        if (!fromBalance.hasSufficient(currency, amount)) {
            from.sendMessage("§cНедостаточно средств!");
            return false;
        }

        double commission = amount * 0.01;
        double targetAmount = amount - commission;
        
        PlayerBalance toBalance = getPlayerBalance(to);
        if (!fromBalance.withdraw(currency, amount)) return false;
        
        toBalance.deposit(currency, targetAmount);
        serverBalances.put(currency, serverBalances.get(currency) + commission);
        
        plugin.getRBLogger().logTransfer(from, to, currency, amount, targetAmount, commission);
        
        from.sendMessage("§aПеревод: " + formatAmount(amount, currency) + " игроку " + to.getName() + " (комиссия: 1%)");
        to.sendMessage("§aПолучено: " + formatAmount(targetAmount, currency) + " от " + from.getName());
        
        return true;
    }

    public List<Map.Entry<UUID, Double>> getTopBalances(CurrencyType currency, int limit) {
        return playerBalances.entrySet().stream()
                .map(entry -> {
                    UUID playerId = entry.getKey();
                    double balance = entry.getValue().getBalance(currency);
                    return new AbstractMap.SimpleEntry<>(playerId, balance);
                })
                .filter(entry -> entry.getValue() > 0)
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void processTradeSign(Player buyer, UUID sellerId, CurrencyType currency, double sellAmount, double buyAmount) {
        PlayerBalance sellerBalance = getPlayerBalance(sellerId);
        PlayerBalance buyerBalance = getPlayerBalance(buyer);
        
        if (!sellerBalance.hasSufficient(currency, sellAmount)) {
            buyer.sendMessage("§cУ продавца недостаточно средств!");
            return;
        }
        
        if (!buyerBalance.hasSufficient(currency, buyAmount)) {
            buyer.sendMessage("§cУ вас недостаточно средств!");
            return;
        }
        
        sellerBalance.withdraw(currency, sellAmount);
        buyerBalance.deposit(currency, sellAmount);
        buyerBalance.withdraw(currency, buyAmount);
        sellerBalance.deposit(currency, buyAmount);
        
        plugin.getRBLogger().logTrade(buyer, sellerId, currency, sellAmount, buyAmount);
        buyer.sendMessage("§aСделка совершена!");
    }

    public void saveAllData() {
        YamlConfiguration config = new YamlConfiguration();
        
        for (Map.Entry<UUID, PlayerBalance> entry : playerBalances.entrySet()) {
            config.getConfigurationSection("players").set(entry.getKey().toString(), entry.getValue().serialize());
        }
        
        for (Map.Entry<CurrencyType, Double> entry : serverBalances.entrySet()) {
            config.set("server." + entry.getKey().name(), entry.getValue());
        }
        
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save balance data: " + e.getMessage());
        }
    }

    private void loadAllData() {
        if (!dataFile.exists()) return;
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        
        ConfigurationSection playersSection = config.getConfigurationSection("players");
        if (playersSection != null) {
            for (String playerId : playersSection.getKeys(false)) {
                try {
                    PlayerBalance balance = new PlayerBalance(UUID.fromString(playerId));
                    ConfigurationSection playerData = playersSection.getConfigurationSection(playerId);
                    if (playerData != null) {
                        balance.deserialize(playerData.getValues(false));
                        playerBalances.put(balance.getPlayerId(), balance);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load balance for player " + playerId);
                }
            }
        }
        
        ConfigurationSection serverSection = config.getConfigurationSection("server");
        if (serverSection != null) {
            for (CurrencyType currency : CurrencyType.values()) {
                serverBalances.put(currency, serverSection.getDouble(currency.name(), 0.0));
            }
        }
    }

    private void checkDailyReset() {
        for (PlayerBalance balance : playerBalances.values()) {
            long lastReset = balance.getLastDepositReset();
            if (System.currentTimeMillis() - lastReset > DAY_IN_MS) {
                balance.resetDailyDeposits();
            }
        }
    }

    private boolean removeFromInventory(Player player, Material material, String resourceName) {
        if (!player.getInventory().contains(material)) {
            player.sendMessage("§cУ вас нет " + resourceName);
            return false;
        }
        player.getInventory().removeItemAnySlot(new ItemStack(material));
        return true;
    }

    private boolean addToInventory(Player player, Material material, double amount) {
        try {
            int intAmount = (int) Math.floor(amount);
            if (intAmount > 0) {
                ItemStack item = new ItemStack(material, intAmount);
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                if (!leftover.isEmpty()) {
                    player.sendMessage("§cНедостаточно места в инвентаре!");
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to give item to player: " + e.getMessage());
        }
        return true;
    }

    private String getExpectedResourceName(Material material) {
        switch (material) {
            case IRON_INGOT: return "железного слитка";
            case GOLD_INGOT: return "золотого слитка";
            case DIAMOND: return "алмаза";
            case NETHERITE_INGOT: return "незеритового слитка";
            case EMERALD: return "изумруда";
            default: return material.name().toLowerCase();
        }
    }

    public static String formatAmount(double amount, CurrencyType currency) {
        return String.format("%.2f %s", amount, currency.getSymbol());
    }
}