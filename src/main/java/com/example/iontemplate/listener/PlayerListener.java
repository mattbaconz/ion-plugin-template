package com.example.iontemplate.listener;

import com.example.iontemplate.IonTemplatePlugin;
import com.example.iontemplate.data.PlayerData;
import com.ionapi.api.util.MessageBuilder;
import com.ionapi.api.util.Metrics;
import com.ionapi.api.util.RateLimiter;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {
    
    private final IonTemplatePlugin plugin;
    private final RateLimiter chatLimiter;
    private final Map<UUID, Long> sessionStart = new HashMap<>();
    
    public PlayerListener(IonTemplatePlugin plugin, RateLimiter chatLimiter) {
        this.plugin = plugin;
        this.chatLimiter = chatLimiter;
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        Metrics.increment("player.join");
        sessionStart.put(uuid, System.currentTimeMillis());
        
        // Load or create player data
        plugin.getDatabase().findAsync(PlayerData.class, uuid).thenAccept(opt -> {
            if (opt.isEmpty()) {
                PlayerData data = new PlayerData(uuid, player.getName());
                plugin.getDatabase().saveAsync(data);
                MessageBuilder.of("<gradient:gold:yellow>Welcome to the server, " + player.getName() + "!")
                    .send(player);
            } else {
                PlayerData data = opt.get();
                data.setName(player.getName());
                data.setLastLogin(System.currentTimeMillis());
                plugin.getDatabase().saveAsync(data);
                MessageBuilder.of("<green>Welcome back, " + player.getName() + "!")
                    .send(player);
            }
        });
        
        // Create economy account
        plugin.getEconomyProvider().createAccount(uuid);
        
        // Show scoreboard
        plugin.getScoreboardManager().showScoreboard(player);
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        Metrics.increment("player.quit");
        
        // Update play time
        Long start = sessionStart.remove(uuid);
        if (start != null) {
            long sessionTime = System.currentTimeMillis() - start;
            plugin.getDatabase().findAsync(PlayerData.class, uuid).thenAccept(opt -> {
                if (opt.isPresent()) {
                    PlayerData data = opt.get();
                    data.addPlayTime(sessionTime);
                    plugin.getDatabase().saveAsync(data);
                }
            });
        }
        
        // Hide scoreboard
        plugin.getScoreboardManager().hideScoreboard(player);
    }
    
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        // Update victim stats
        plugin.getDatabase().findAsync(PlayerData.class, victim.getUniqueId()).thenAccept(opt -> {
            if (opt.isPresent()) {
                PlayerData data = opt.get();
                data.addDeath();
                plugin.getDatabase().saveAsync(data);
            }
        });
        
        // Update killer stats
        if (killer != null) {
            Metrics.increment("player.kills");
            plugin.getDatabase().findAsync(PlayerData.class, killer.getUniqueId()).thenAccept(opt -> {
                if (opt.isPresent()) {
                    PlayerData data = opt.get();
                    data.addKill();
                    plugin.getDatabase().saveAsync(data);
                }
            });
        }
    }
    
    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        
        if (!chatLimiter.tryAcquire(player.getUniqueId())) {
            event.setCancelled(true);
            MessageBuilder.of("<red>Slow down! You're sending messages too fast.")
                .send(player);
        }
    }
}
