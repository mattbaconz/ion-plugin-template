package com.example.iontemplate.command;

import com.ionapi.api.util.MessageBuilder;
import com.ionapi.economy.IonEconomy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BalanceCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        
        UUID targetUuid;
        String targetName;
        
        if (args.length > 0) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                MessageBuilder.of("<red>Player not found!").send(sender);
                return true;
            }
            targetUuid = target.getUniqueId();
            targetName = target.getName();
        } else if (sender instanceof Player player) {
            targetUuid = player.getUniqueId();
            targetName = player.getName();
        } else {
            sender.sendMessage("Usage: /balance <player>");
            return true;
        }
        
        IonEconomy.getBalance(targetUuid).thenAccept(balance -> {
            if (sender instanceof Player && targetUuid.equals(((Player) sender).getUniqueId())) {
                MessageBuilder.of("<gold>Your balance: <yellow>$" + IonEconomy.format(balance))
                    .send(sender);
            } else {
                MessageBuilder.of("<gold>" + targetName + "'s balance: <yellow>$" + IonEconomy.format(balance))
                    .send(sender);
            }
        });
        
        return true;
    }
}
