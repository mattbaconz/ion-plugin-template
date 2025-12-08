package com.example.iontemplate.command;

import com.example.iontemplate.gui.MainMenuGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MenuCommand implements CommandExecutor {
    
    private final MainMenuGui mainMenu;
    
    public MenuCommand(MainMenuGui mainMenu) {
        this.mainMenu = mainMenu;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        
        mainMenu.open(player);
        return true;
    }
}
