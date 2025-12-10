package com.example.iontemplate.command;

import com.ionapi.api.util.MessageBuilder;
import com.ionapi.economy.IonEconomy;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EcoCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (!sender.hasPermission("iontemplate.eco.admin")) {
            MessageBuilder.of("<red>✗ <gray>You don't have permission!").send(sender);
            return true;
        }

        if (args.length < 3) {
            MessageBuilder.of("<yellow>Usage: /eco <give|take|set> <player> <amount>").send(sender);
            return true;
        }

        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            MessageBuilder.of("<red>✗ <gray>Player not found!").send(sender);
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                MessageBuilder.of("<red>✗ <gray>Amount must be positive!").send(sender);
                return true;
            }
        } catch (NumberFormatException e) {
            MessageBuilder.of("<red>✗ <gray>Invalid amount!").send(sender);
            return true;
        }

        BigDecimal bdAmount = BigDecimal.valueOf(amount);

        switch (action) {
            case "give" -> {
                IonEconomy.transaction(target.getUniqueId())
                        .deposit(bdAmount)
                        .reason("Admin give by " + sender.getName())
                        .commit()
                        .thenAccept(result -> {
                            if (result.isSuccess()) {
                                MessageBuilder
                                        .of("<green>✓ <gray>Gave <gold>$" + amount + " <gray>to <white>"
                                                + target.getName())
                                        .send(sender);
                                MessageBuilder
                                        .of("<green>✓ <gray>You received <gold>$" + amount + " <gray>from an admin!")
                                        .send(target);
                                target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                            } else {
                                MessageBuilder.of("<red>✗ <gray>Transaction failed!").send(sender);
                            }
                        });
            }
            case "take" -> {
                IonEconomy.transaction(target.getUniqueId())
                        .withdraw(bdAmount)
                        .reason("Admin take by " + sender.getName())
                        .commit()
                        .thenAccept(result -> {
                            if (result.isSuccess()) {
                                MessageBuilder
                                        .of("<green>✓ <gray>Took <gold>$" + amount + " <gray>from <white>"
                                                + target.getName())
                                        .send(sender);
                                MessageBuilder.of("<red>✗ <gray>An admin took <gold>$" + amount + " <gray>from you!")
                                        .send(target);
                            } else {
                                MessageBuilder.of("<red>✗ <gray>Insufficient funds or transaction failed!")
                                        .send(sender);
                            }
                        });
            }
            case "set" -> {
                IonEconomy.getProvider().setBalance(target.getUniqueId(), bdAmount)
                        .thenAccept(result -> {
                            if (result.isSuccess()) {
                                MessageBuilder
                                        .of("<green>✓ <gray>Set <white>" + target.getName()
                                                + "'s <gray>balance to <gold>$" + amount)
                                        .send(sender);
                                MessageBuilder
                                        .of("<yellow>⚠ <gray>Your balance was set to <gold>$" + amount
                                                + " <gray>by an admin!")
                                        .send(target);
                            } else {
                                MessageBuilder.of("<red>✗ <gray>Failed to set balance!").send(sender);
                            }
                        });
            }
            default -> {
                MessageBuilder.of("<yellow>Usage: /eco <give|take|set> <player> <amount>").send(sender);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
            @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("give");
            completions.add("take");
            completions.add("set");
        } else if (args.length == 2) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                completions.add(p.getName());
            }
        } else if (args.length == 3) {
            completions.add("100");
            completions.add("500");
            completions.add("1000");
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .toList();
    }
}
