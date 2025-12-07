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
        
        IonScoreboard board = IonScoreboard.builder()
            .title("<gradient:gold:yellow><bold>Ion Template")
            .line(15, "<white>{player}")
            .line(14, "")
            .line(13, "<gold>Balance:")
            .line(12, "<yellow>  ${balance}")
            .line(11, "")
            .line(10, "<aqua>Stats:")
            .line(9, "<white>  Kills: <green>{kills}")
            .line(8, "<white>  Deaths: <red>{deaths}")
            .line(7, "<white>  K/D: <yellow>{kdr}")
            .line(6, "")
            .line(5, "<gray>Online: <white>{online}")
            .line(4, "")
            .line(3, "<gradient:blue:purple>play.server.com")
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
