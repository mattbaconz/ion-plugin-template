package com.example.iontemplate.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StatsCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        
        new com.example.iontemplate.gui.StatsGui(
            (com.example.iontemplate.IonTemplatePlugin) org.bukkit.Bukkit.getPluginManager().getPlugin("IonTemplatePlugin")
        ).open(player);
        
        return true;
    }
}
