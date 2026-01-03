package com.timeland.rbalance.commands;

import com.timeland.rbalance.RBalancePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TradeCommand implements CommandExecutor {
    private final RBalancePlugin plugin;
    
    public TradeCommand(RBalancePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfiguration().isTradeEnabled()) {
            sender.sendMessage("§cТорговля временно отключена!");
            return true;
        }
        
        if (args.length == 0) {
            showTradeHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "help":
            case "h":
                showTradeHelp(sender);
                break;
            default:
                sender.sendMessage("§cНеизвестная команда! Используйте /tradecmd help");
        }
        
        return true;
    }
    
    private void showTradeHelp(CommandSender sender) {
        sender.sendMessage("§6§lСистема АФК-торговли:");
        sender.sendMessage("§eКак создать торговую табличку:");
        sender.sendMessage("§71. §fПоставьте табличку");
        sender.sendMessage("§72. §fНапишите на ней:");
        sender.sendMessage("§7   [Trade]");
        sender.sendMessage("§7   <Ваш ник>");
        sender.sendMessage("§7   <Ресурс>x<Кол-во>");
        sender.sendMessage("§7   S:<цена>B /<цена>P");
        sender.sendMessage("§fПример:");
        sender.sendMessage("§7   [Trade]");
        sender.sendMessage("§7   Notch");
        sender.sendMessage("§7   Iron x10");
        sender.sendMessage("§7   S:4I / B:2I");
        sender.sendMessage("§e§oНажмите ПКМ для совершения сделки!");
    }
}