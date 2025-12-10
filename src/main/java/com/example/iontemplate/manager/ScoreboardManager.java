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

    private static final int SCOREBOARD_WIDTH = 30; // Fixed width for stability

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
                .line(0, pad("<gray>━━━━━━━━━━━━━━━━━━"))
                .line(1, pad("<gray>» <white><bold>Player"))
                .line(2, pad(" <gray>│ <aqua>{player}"))
                .line(3, pad(""))
                .line(4, pad("<gray>» <gold><bold>Rank"))
                .line(5, pad(" <gray>│ <yellow>★ Member"))
                .line(6, pad(""))
                .line(7, pad("<gray>» <light_purple><bold>Stats"))
                .line(8, pad(" <gray>│ <white>Kills: <green>{kills}"))
                .line(9, pad(" <gray>│ <white>Deaths: <red>{deaths}"))
                .line(10, pad(" <gray>│ <white>K/D: <yellow>{kdr}"))
                .line(11, pad(""))
                .line(12, pad("<gray>» <green><bold>Balance"))
                .line(13, pad(" <gray>│ <gold>{balance}"))
                .line(14, pad(""))
                // Animated line using legacy colors for stability and padding
                .animatedLine(15, 40L, // 40 ticks = 2 seconds per frame
                        pad("<yellow>✦ Welcome to the Server! ✦"),
                        pad("<aqua>✦ play.yourserver.com ✦"),
                        pad("<green>✦ Online: {online} Players ✦"),
                        pad("<light_purple>✦ Have Fun! ✦"))
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

    private String pad(String text) {
        // Simple padding to prevent jitter
        // Note: This is an estimation since we can't measure font width accurately
        // here,
        // but consistent trailing spaces usually helps sidebar stability.
        if (text.length() >= SCOREBOARD_WIDTH) {
            return text;
        }
        return text + " ".repeat(SCOREBOARD_WIDTH - text.length());
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
        }, 5, 20, TimeUnit.SECONDS); // Update every 1 second (20 ticks) for smoother feel, relying on internal check
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
