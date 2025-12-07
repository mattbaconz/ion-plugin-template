package com.example.iontemplate.manager;

import com.ionapi.ui.IonBossBar;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages boss bars for players.
 * Demonstrates IonBossBar API usage.
 */
public class BossBarManager {
    
    private final Plugin plugin;
    private final Map<UUID, IonBossBar> activeBars = new HashMap<>();
    
    public BossBarManager(Plugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Shows a welcome boss bar to a player.
     */
    public void showWelcomeBar(Player player) {
        IonBossBar bar = IonBossBar.builder()
            .name("welcome-" + player.getUniqueId())
            .title("<gradient:gold:yellow>Welcome, {player}!</gradient>")
            .color(BossBar.Color.YELLOW)
            .style(BossBar.Overlay.PROGRESS)
            .progress(1.0f)
            .placeholder("player", p -> p.getName())
            .build();
        
        bar.show(player);
        activeBars.put(player.getUniqueId(), bar);
        
        // Auto-hide after 5 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                hideBar(player);
            }
        }.runTaskLater(plugin, 100L);
    }
    
    /**
     * Shows a progress bar (e.g., for events, downloads, etc.)
     */
    public void showProgressBar(Player player, String title, int durationSeconds) {
        IonBossBar bar = IonBossBar.builder()
            .name("progress-" + player.getUniqueId())
            .title(title)
            .color(BossBar.Color.GREEN)
            .style(BossBar.Overlay.NOTCHED_10)
            .progress(1.0f)
            .build();
        
        bar.show(player);
        activeBars.put(player.getUniqueId(), bar);
        
        // Animate progress
        new BukkitRunnable() {
            int ticks = 0;
            final int totalTicks = durationSeconds * 20;
            
            @Override
            public void run() {
                if (ticks >= totalTicks) {
                    bar.setTitle("<green>Complete!");
                    bar.setColor(BossBar.Color.GREEN);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            hideBar(player);
                        }
                    }.runTaskLater(plugin, 40L);
                    cancel();
                    return;
                }
                
                float progress = 1.0f - ((float) ticks / totalTicks);
                bar.setProgress(progress);
                
                // Change color based on progress
                if (progress < 0.3f) {
                    bar.setColor(BossBar.Color.RED);
                } else if (progress < 0.6f) {
                    bar.setColor(BossBar.Color.YELLOW);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Shows a health bar for a boss fight or similar.
     */
    public void showHealthBar(Player player, String bossName, double currentHealth, double maxHealth) {
        IonBossBar bar = IonBossBar.builder()
            .name("health-" + player.getUniqueId())
            .title("<red>❤ " + bossName + " <white>{health}/{max}")
            .color(BossBar.Color.RED)
            .style(BossBar.Overlay.NOTCHED_20)
            .progress((float) (currentHealth / maxHealth))
            .placeholder("health", p -> String.valueOf((int) currentHealth))
            .placeholder("max", p -> String.valueOf((int) maxHealth))
            .build();
        
        bar.show(player);
        activeBars.put(player.getUniqueId(), bar);
    }
    
    /**
     * Updates an existing health bar.
     */
    public void updateHealthBar(Player player, double currentHealth, double maxHealth) {
        IonBossBar bar = activeBars.get(player.getUniqueId());
        if (bar != null) {
            bar.setProgress((float) (currentHealth / maxHealth));
            bar.update(player);
        }
    }
    
    /**
     * Hides the active bar for a player.
     */
    public void hideBar(Player player) {
        IonBossBar bar = activeBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.hide(player);
        }
    }
    
    /**
     * Cleans up all active bars.
     */
    public void cleanup() {
        activeBars.values().forEach(bar -> {
            // Boss bars are automatically cleaned up when hidden
        });
        activeBars.clear();
    }
}
