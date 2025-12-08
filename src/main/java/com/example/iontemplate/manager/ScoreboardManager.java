package com.example.iontemplate.manager;

import com.example.iontemplate.IonTemplatePlugin;
import com.ionapi.economy.IonEconomy;
import com.ionapi.ui.IonScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {
    
    private final IonTemplatePlugin plugin;
    private final Map<UUID, IonScoreboard> scoreboards = new HashMap<>();
    private final Map<UUID, Boolean> enabled = new HashMap<>();
    
    public ScoreboardManager(IonTemplatePlugin plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }
    
    public void showScoreboard(Player player) {
        if (!enabled.getOrDefault(player.getUniqueId(), true)) return;
        
        // Don't recreate if already exists
        if (scoreboards.containsKey(player.getUniqueId())) {
            return;
        }
        
        IonScoreboard board = IonScoreboard.builder()
            .title("<gold><bold>Ion Template")
            .line(0, "<white>{player}")
            .line(1, "")
            .line(2, "<gold>Balance:")
            .line(3, "<yellow>  ${balance}")
            .line(4, "")
            .line(5, "<aqua>Stats:")
            .line(6, "<white>  Kills: <green>{kills}")
            .line(7, "<white>  Deaths: <red>{deaths}")
            .line(8, "<white>  K/D: <yellow>{kdr}")
            .line(9, "")
            .line(10, "<gray>Online: <white>{online}")
            .line(11, "")
            .line(12, "<blue>play.server.com")
            .placeholder("player", Player::getName)
            .placeholder("balance", p -> {
                try {
                    return IonEconomy.format(IonEconomy.getBalance(p.getUniqueId()).join());
                } catch (Exception e) {
                    return "0";
                }
            })
            .placeholder("kills", p -> String.valueOf(plugin.getPlayerKills(p.getUniqueId())))
            .placeholder("deaths", p -> String.valueOf(plugin.getPlayerDeaths(p.getUniqueId())))
            .placeholder("kdr", p -> String.format("%.2f", plugin.getPlayerKDR(p.getUniqueId())))
            .placeholder("online", p -> String.valueOf(Bukkit.getOnlinePlayers().size()))
            .build();
        
        board.show(player);
        scoreboards.put(player.getUniqueId(), board);
    }
    
    public void hideScoreboard(Player player) {
        IonScoreboard board = scoreboards.remove(player.getUniqueId());
        if (board != null) {
            board.hide(player);
        }
    }
    
    public void toggleScoreboard(Player player) {
        UUID uuid = player.getUniqueId();
        boolean isEnabled = enabled.getOrDefault(uuid, true);
        
        if (isEnabled) {
            hideScoreboard(player);
            enabled.put(uuid, false);
        } else {
            enabled.put(uuid, true);
            showScoreboard(player);
        }
    }
    
    private void startUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                scoreboards.forEach((uuid, board) -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        board.update(player);
                    }
                });
            }
        }.runTaskTimer(plugin, 20, 20);
    }
    
    public void cleanup() {
        scoreboards.clear();
        enabled.clear();
    }
}
