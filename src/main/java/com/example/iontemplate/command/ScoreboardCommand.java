package com.example.iontemplate.command;

import com.example.iontemplate.manager.ScoreboardManager;
import com.ionapi.api.util.MessageBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ScoreboardCommand implements CommandExecutor {
    
    private final ScoreboardManager scoreboardManager;
    
    public ScoreboardCommand(ScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        
        scoreboardManager.toggleScoreboard(player);
        MessageBuilder.of("<green>Scoreboard toggled!").send(player);
        return true;
    }
}
