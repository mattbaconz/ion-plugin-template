package com.example.iontemplate.manager;

import com.example.iontemplate.IonTemplatePlugin;
import com.ionapi.economy.IonEconomy;
import com.ionapi.ui.IonScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ScoreboardManager {

    private final IonTemplatePlugin plugin;
    private final Map<UUID, IonScoreboard> scoreboards = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> enabled = new ConcurrentHashMap<>();
    private final Map<UUID, String> balanceCache = new ConcurrentHashMap<>();
    private final Map<UUID, CachedStats> statsCache = new ConcurrentHashMap<>();

    public ScoreboardManager(IonTemplatePlugin plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    public void showScoreboard(Player player) {
        UUID uuid = player.getUniqueId();

        if (!enabled.getOrDefault(uuid, true)) {
            return;
        }

        // Don't recreate if already exists and showing
        if (scoreboards.containsKey(uuid)) {
            return;
        }

        // Pre-cache balance before showing
        updateBalanceCache(uuid);

        IonScoreboard board = IonScoreboard.builder()
                .title("<gradient:#FFD700:#FFA500><bold> ⚡ ION TEMPLATE ⚡ </bold></gradient>")
                .line(0, "<gray>━━━━━━━━━━━━━━━━━━")
                .line(1, "<gray>» <white><bold>Player")
                .line(2, " <gray>│ <aqua>{player}")
                .line(3, "")
                .line(4, "<gray>» <gold><bold>Rank")
                .line(5, " <gray>│ <yellow>★ Member")
                .line(6, "")
                .line(7, "<gray>» <light_purple><bold>Stats")
                .line(8, " <gray>│ <white>Kills: <green>{kills}")
                .line(9, " <gray>│ <white>Deaths: <red>{deaths}")
                .line(10, " <gray>│ <white>K/D: <yellow>{kdr}")
                .line(11, "")
                .line(12, "<gray>» <green><bold>Balance")
                .line(13, " <gray>│ <gold>{balance}")
                .line(14, "")
                // Animated line using v1.3.0 feature!
                .animatedLine(15, 40L, // 40 ticks = 2 seconds per frame
                        "<gradient:gold:yellow>✦ Welcome to the Server! ✦</gradient>",
                        "<gradient:aqua:blue>✦ play.yourserver.com ✦</gradient>",
                        "<gradient:green:lime>✦ Online: {online} Players ✦</gradient>",
                        "<gradient:light_purple:pink>✦ Have Fun! ✦</gradient>")
                .placeholder("player", Player::getName)
                .placeholder("balance", p -> balanceCache.getOrDefault(p.getUniqueId(), "$0.00"))
                .placeholder("kills", p -> String.valueOf(plugin.getPlayerKills(p.getUniqueId())))
                .placeholder("deaths", p -> String.valueOf(plugin.getPlayerDeaths(p.getUniqueId())))
                .placeholder("kdr", p -> String.format("%.2f", plugin.getPlayerKDR(p.getUniqueId())))
                .placeholder("online", p -> String.valueOf(Bukkit.getOnlinePlayers().size()))
                .build();

        board.show(player);
        scoreboards.put(uuid, board);

        // Cache initial stats
        statsCache.put(uuid, new CachedStats(
                plugin.getPlayerKills(uuid),
                plugin.getPlayerDeaths(uuid),
                balanceCache.getOrDefault(uuid, "$0.00")));
    }

    public void hideScoreboard(Player player) {
        UUID uuid = player.getUniqueId();
        IonScoreboard board = scoreboards.remove(uuid);
        if (board != null) {
            board.hide(player);
        }
        statsCache.remove(uuid);
        balanceCache.remove(uuid);
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
        // Update every 5 seconds to reduce flashing
        // Only update if stats have actually changed
        plugin.getScheduler().runTimer(() -> {
            scoreboards.forEach((uuid, board) -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) {
                    return;
                }

                // Get current values
                int kills = plugin.getPlayerKills(uuid);
                int deaths = plugin.getPlayerDeaths(uuid);
                String balance = balanceCache.getOrDefault(uuid, "$0.00");

                // Check if anything changed
                CachedStats cached = statsCache.get(uuid);
                if (cached != null &&
                        cached.kills == kills &&
                        cached.deaths == deaths &&
                        cached.balance.equals(balance)) {
                    // Nothing changed, skip update
                    return;
                }

                // Update cache
                statsCache.put(uuid, new CachedStats(kills, deaths, balance));

                // Only update the scoreboard if values changed
                board.update(player);
            });

            // Refresh balance cache for all players
            for (UUID uuid : scoreboards.keySet()) {
                updateBalanceCache(uuid);
            }
        }, 5, 5, TimeUnit.SECONDS); // Update every 5 seconds
    }

    private void updateBalanceCache(UUID uuid) {
        IonEconomy.getBalance(uuid).thenAccept(balance -> {
            balanceCache.put(uuid, IonEconomy.format(balance));
        });
    }

    public void cleanup() {
        scoreboards.clear();
        enabled.clear();
        balanceCache.clear();
        statsCache.clear();
    }

    // Simple cache record
    private record CachedStats(int kills, int deaths, String balance) {
    }
}
