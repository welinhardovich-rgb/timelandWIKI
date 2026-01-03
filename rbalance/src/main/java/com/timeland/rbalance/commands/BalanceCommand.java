package com.timeland.rbalance.commands;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.economy.BalanceManager;
import com.timeland.rbalance.economy.CurrencyType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BalanceCommand implements CommandExecutor {
    private final RBalancePlugin plugin;
    
    public BalanceCommand(RBalancePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭту команду может использовать только игрок!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showBalance(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "deposit":
            case "d":
                handleDeposit(player, args);
                break;
            case "withdraw":
            case "w":
                handleWithdraw(player, args);
                break;
            case "top":
            case "t":
                handleTop(player, args);
                break;
            case "pay":
            case "p":
                handlePay(player, args);
                break;
            case "history":
            case "h":
                handleHistory(player, args);
                break;
            default:
                showHelp(player);
        }
        
        return true;
    }
    
    private void showBalance(Player player) {
        BalanceManager manager = plugin.getBalanceManager();
        player.sendMessage("§6§lВаш баланс:");
        
        for (CurrencyType currency : CurrencyType.values()) {
            double balance = manager.getBalance(player, currency);
            if (balance > 0) {
                player.sendMessage("§e  " + currency.getDisplayName() + ": " + BalanceManager.formatAmount(balance, currency));
            }
        }
    }
    
    private void handleDeposit(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cИспользование: /bal deposit <ресурс> <кол-во>");
            return;
        }
        
        if (!plugin.getConfiguration().isTradeEnabled()) {
            player.sendMessage("§cДепозиты временно отключены!");
            return;
        }
        
        CurrencyType currency = CurrencyType.fromString(args[1]);
        if (currency == null) {
            player.sendMessage("§cНеверный тип валюты! Доступные: Iron (I), Gold (G), Diamond (D), Netherite (N), Emerald (E)");
            return;
        }
        
        if (!plugin.getConfiguration().isCurrencyEnabled(currency)) {
            player.sendMessage("§cЭта валюта отключена!");
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount < 0.1) {
                player.sendMessage("§cМинимальная сумма: 0.1");
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§cНеверное количество!");
            return;
        }
        
        plugin.getBalanceManager().deposit(player, currency, amount);
    }
    
    private void handleWithdraw(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cИспользование: /bal withdraw <ресурс> <кол-во>");
            return;
        }
        
        CurrencyType currency = CurrencyType.fromString(args[1]);
        if (currency == null) {
            player.sendMessage("§cНеверный тип валюты!");
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount < 0.1) {
                player.sendMessage("§cМинимальная сумма: 0.1");
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§cНеверное количество!");
            return;
        }
        
        plugin.getBalanceManager().withdraw(player, currency, amount);
    }
    
    private void handleTop(Player player, String[] args) {
        CurrencyType currency;
        if (args.length > 1) {
            currency = CurrencyType.fromString(args[1]);
            if (currency == null) {
                player.sendMessage("§cНеверный тип валюты!");
                return;
            }
        } else {
            currency = CurrencyType.IRON;
        }
        
        player.sendMessage("§6§lТоп-10 по " + currency.getDisplayName() + ":");
        
        List<Map.Entry<UUID, Double>> topBalances = plugin.getBalanceManager().getTopBalances(currency, 10);
        
        int position = 1;
        for (Map.Entry<UUID, Double> entry : topBalances) {
            String playerName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            if (playerName != null) {
                player.sendMessage("§e" + position + ". " + playerName + ": " + 
                        BalanceManager.formatAmount(entry.getValue(), currency));
                position++;
            }
        }
        
        if (position == 1) {
            player.sendMessage("§cНет данных для отображения!");
        }
    }
    
    private void handlePay(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§cИспользование: /bal pay <ник> <ресурс> <кол-во>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cИгрок не онлайн!");
            return;
        }
        
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§cВы не можете перевести деньги себе!");
            return;
        }
        
        CurrencyType currency = CurrencyType.fromString(args[2]);
        if (currency == null) {
            player.sendMessage("§cНеверный тип валюты!");
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[3]);
            if (amount < 0.1) {
                player.sendMessage("§cМинимальная сумма: 0.1");
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§cНеверное количество!");
            return;
        }
        
        plugin.getBalanceManager().transfer(player, target, currency, amount);
    }
    
    private void handleHistory(Player player, String[] args) {
        UUID targetId = player.getUniqueId();
        
        if (args.length > 1 && args[1].equals("other") && player.hasPermission("rbalance.history.other")) {
            String targetName = args[2];
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                player.sendMessage("§cИгрок не найден!");
                return;
            }
            targetId = target.getUniqueId();
        }
        
        List<String> history = plugin.getRBLogger().getHistory(targetId);
        
        if (history.isEmpty()) {
            player.sendMessage("§cИстория пуста!");
            return;
        }
        
        player.sendMessage("§6§lИстория операций (последние 10):");
        
        int startIndex = Math.max(0, history.size() - 10);
        for (int i = startIndex; i < history.size(); i++) {
            player.sendMessage("§e" + history.get(i));
        }
        
        if (history.size() > 10) {
            player.sendMessage("§7... и еще " + (history.size() - 10) + " записей");
        }
    }
    
    private void showHelp(Player player) {
        player.sendMessage("§6§lКоманды R-Balance:");
        player.sendMessage("§e/bal §f- показать ваш баланс");
        player.sendMessage("§e/bal deposit <ресурс> <кол-во> §f- внести ресурсы");
        player.sendMessage("§e/bal withdraw <ресурс> <кол-во> §f- вывести ресурсы");
        player.sendMessage("§e/bal top <ресурс> §f- топ-10 игроков");
        player.sendMessage("§e/bal pay <ник> <ресурс> <кол-во> §f- перевести деньги");
        player.sendMessage("§e/bal history §f- посмотреть историю");
        player.sendMessage("§6Ресурсы: §7Iron(I), Gold(G), Diamond(D), Netherite(N), Emerald(E)");
    }
}