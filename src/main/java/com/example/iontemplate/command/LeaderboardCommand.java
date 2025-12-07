package com.example.iontemplate.command;

import com.example.iontemplate.data.PlayerData;
import com.ionapi.api.util.MessageBuilder;
import com.ionapi.database.IonDatabase;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public class LeaderboardCommand implements CommandExecutor {
    
    private final IonDatabase database;
    
    public LeaderboardCommand(IonDatabase database) {
        this.database = database;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        
        String type = args.length > 0 ? args[0].toLowerCase() : "kills";
        
        switch (type) {
            case "kills" -> showKillsLeaderboard(sender);
            case "deaths" -> showDeathsLeaderboard(sender);
            case "kdr", "kd" -> showKDRLeaderboard(sender);
            default -> {
                MessageBuilder.of("<red>Usage: /leaderboard <kills|deaths|kdr>").send(sender);
            }
        }
        
        return true;
    }
    
    private void showKillsLeaderboard(CommandSender sender) {
        database.findAllAsync(PlayerData.class).thenAccept(players -> {
            if (players.isEmpty()) {
                MessageBuilder.of("<gray>No data available yet!").send(sender);
                return;
            }
            
            List<PlayerData> sorted = players.stream()
                .sorted(Comparator.comparingInt(PlayerData::getKills).reversed())
                .limit(10)
                .toList();
            
            MessageBuilder.of("<gold><bold>═══ Top Kills ═══").send(sender);
            for (int i = 0; i < sorted.size(); i++) {
                PlayerData data = sorted.get(i);
                MessageBuilder.of("<yellow>#" + (i + 1) + ". <white>" + data.getName() + 
                    " <gray>- <green>" + data.getKills()).send(sender);
            }
            MessageBuilder.of("<gold><bold>═════════════════").send(sender);
        });
    }
    
    private void showDeathsLeaderboard(CommandSender sender) {
        database.findAllAsync(PlayerData.class).thenAccept(players -> {
            if (players.isEmpty()) {
                MessageBuilder.of("<gray>No data available yet!").send(sender);
                return;
            }
            
            List<PlayerData> sorted = players.stream()
                .sorted(Comparator.comparingInt(PlayerData::getDeaths).reversed())
                .limit(10)
                .toList();
            
            MessageBuilder.of("<gold><bold>═══ Most Deaths ═══").send(sender);
            for (int i = 0; i < sorted.size(); i++) {
                PlayerData data = sorted.get(i);
                MessageBuilder.of("<yellow>#" + (i + 1) + ". <white>" + data.getName() + 
                    " <gray>- <red>" + data.getDeaths()).send(sender);
            }
            MessageBuilder.of("<gold><bold>══════════════════").send(sender);
        });
    }
    
    private void showKDRLeaderboard(CommandSender sender) {
        database.findAllAsync(PlayerData.class).thenAccept(players -> {
            if (players.isEmpty()) {
                MessageBuilder.of("<gray>No data available yet!").send(sender);
                return;
            }
            
            List<PlayerData> sorted = players.stream()
                .filter(p -> p.getDeaths() > 0) // Only players with deaths
                .sorted(Comparator.comparingDouble(PlayerData::getKDR).reversed())
                .limit(10)
                .toList();
            
            MessageBuilder.of("<gold><bold>═══ Top K/D Ratio ═══").send(sender);
            for (int i = 0; i < sorted.size(); i++) {
                PlayerData data = sorted.get(i);
                MessageBuilder.of("<yellow>#" + (i + 1) + ". <white>" + data.getName() + 
                    " <gray>- <aqua>" + String.format("%.2f", data.getKDR())).send(sender);
            }
            MessageBuilder.of("<gold><bold>═════════════════════").send(sender);
        });
    }
}
