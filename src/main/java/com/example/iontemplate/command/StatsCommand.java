package com.example.iontemplate.command;

import com.example.iontemplate.data.PlayerData;
import com.ionapi.api.util.MessageBuilder;
import com.ionapi.database.IonDatabase;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class StatsCommand implements CommandExecutor {
    
    private final IonDatabase database;
    
    public StatsCommand(IonDatabase database) {
        this.database = database;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        
        database.findAsync(PlayerData.class, player.getUniqueId()).thenAccept(opt -> {
            if (opt.isEmpty()) {
                MessageBuilder.of("<red>No stats found!").send(player);
                return;
            }
            
            PlayerData data = opt.get();
            long hours = TimeUnit.MILLISECONDS.toHours(data.getPlayTime());
            long minutes = TimeUnit.MILLISECONDS.toMinutes(data.getPlayTime()) % 60;
            
            MessageBuilder.of("<gold><bold>=== Your Stats ===").send(player);
            MessageBuilder.of("<yellow>Kills: <white>" + data.getKills()).send(player);
            MessageBuilder.of("<yellow>Deaths: <white>" + data.getDeaths()).send(player);
            MessageBuilder.of("<yellow>K/D Ratio: <white>" + String.format("%.2f", data.getKDR())).send(player);
            MessageBuilder.of("<yellow>Play Time: <white>" + hours + "h " + minutes + "m").send(player);
            MessageBuilder.of("<gold><bold>==================").send(player);
        });
        
        return true;
    }
}
