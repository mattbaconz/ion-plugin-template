package com.example.iontemplate.command;

import com.ionapi.api.util.CooldownManager;
import com.ionapi.api.util.MessageBuilder;
import com.ionapi.economy.IonEconomy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public class PayCommand implements CommandExecutor {
    
    private final CooldownManager cooldowns;
    private final int cooldownSeconds;
    
    public PayCommand(CooldownManager cooldowns, int cooldownSeconds) {
        this.cooldowns = cooldowns;
        this.cooldownSeconds = cooldownSeconds;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        
        if (args.length < 2) {
            MessageBuilder.of("<red>Usage: /pay <player> <amount>").send(player);
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            MessageBuilder.of("<red>Player not found!").send(player);
            return true;
        }
        
        if (target.equals(player)) {
            MessageBuilder.of("<red>You can't pay yourself!").send(player);
            return true;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            MessageBuilder.of("<red>Invalid amount!").send(player);
            return true;
        }
        
        if (cooldowns.isOnCooldown(player.getUniqueId())) {
            long remaining = cooldowns.getRemainingTime(player.getUniqueId(), TimeUnit.SECONDS);
            MessageBuilder.of("<red>Wait " + remaining + "s before paying again!").send(player);
            return true;
        }
        
        IonEconomy.transfer(player.getUniqueId(), target.getUniqueId(), BigDecimal.valueOf(amount))
            .thenAccept(result -> {
                if (result.isSuccess()) {
                    cooldowns.setCooldown(player.getUniqueId(), cooldownSeconds, TimeUnit.SECONDS);
                    MessageBuilder.of("<green>Sent <gold>$" + amount + " <green>to " + target.getName() + "!")
                        .send(player);
                    MessageBuilder.of("<green>Received <gold>$" + amount + " <green>from " + player.getName() + "!")
                        .send(target);
                } else {
                    MessageBuilder.of("<red>Transaction failed! Insufficient funds.").send(player);
                }
            });
        
        return true;
    }
}
