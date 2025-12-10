package com.example.iontemplate.gui;

import com.example.iontemplate.IonTemplatePlugin;
import com.ionapi.api.util.MessageBuilder;
import com.ionapi.economy.IonEconomy;
import com.ionapi.gui.IonGuiBuilder;
import com.ionapi.item.IonItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MainMenuGui {

        private final IonTemplatePlugin plugin;

        public MainMenuGui(IonTemplatePlugin plugin) {
                this.plugin = plugin;
        }

        public void open(Player player) {
                // Fetch data asynchronously first
                IonEconomy.getBalance(player.getUniqueId()).thenAccept(balance -> {
                        String balanceFormatted = IonEconomy.format(balance);

                        // Switch back to thread-safe context for opening GUI
                        plugin.getScheduler().runAt(player, () -> {
                                buildAndOpen(player, balanceFormatted);
                        });
                }).exceptionally(ex -> {
                        plugin.getScheduler().runAt(player, () -> {
                                buildAndOpen(player, "$0.00");
                                plugin.getLogger().warning("Failed to load balance for menu: " + ex.getMessage());
                        });
                        return null;
                });
        }

        private void buildAndOpen(Player player, String balanceFormatted) {
                ItemStack statsItem = new IonItemBuilder(Material.BOOK)
                                .name("<gradient:#55FFFF:#00AAAA><bold>📊 Your Stats</gradient>")
                                .lore(
                                                "",
                                                "<gray>» <white>Kills: <green>"
                                                                + plugin.getPlayerKills(player.getUniqueId()),
                                                "<gray>» <white>Deaths: <red>"
                                                                + plugin.getPlayerDeaths(player.getUniqueId()),
                                                "<gray>» <white>K/D: <yellow>" + String.format("%.2f",
                                                                plugin.getPlayerKDR(player.getUniqueId())),
                                                "",
                                                "<yellow>Click to view full details!")
                                .build();

                ItemStack balanceItem = new IonItemBuilder(Material.GOLD_INGOT)
                                .name("<gradient:#FFD700:#FFA500><bold>💰 Balance</gradient>")
                                .lore(
                                                "",
                                                "<gray>» <white>Current: <gold>" + balanceFormatted,
                                                "",
                                                "<yellow>Click to view details!")
                                .build();

                ItemStack shopItem = new IonItemBuilder(Material.EMERALD)
                                .name("<gradient:#55FF55:#00AA00><bold>🛒 Shop</gradient>")
                                .lore(
                                                "",
                                                "<gray>Purchase items and upgrades.",
                                                "",
                                                "<yellow>Click to open!")
                                .build();

                ItemStack warpItem = new IonItemBuilder(Material.ENDER_PEARL)
                                .name("<gradient:#FF55FF:#AA00AA><bold>🌌 Warps</gradient>")
                                .lore(
                                                "",
                                                "<gray>Travel to different locations.",
                                                "",
                                                "<yellow>Click to manage!")
                                .build();

                ItemStack leaderboardItem = new IonItemBuilder(Material.DIAMOND)
                                .name("<gradient:#5555FF:#0000AA><bold>🏆 Leaderboards</gradient>")
                                .lore(
                                                "",
                                                "<gray>View the top players.",
                                                "",
                                                "<yellow>Click to view!")
                                .build();

                ItemStack scoreboardToggle = new IonItemBuilder(Material.PAINTING)
                                .name("<gradient:#FFFF55:#AAAA00><bold>📝 Scoreboard</gradient>")
                                .lore(
                                                "",
                                                "<gray>Toggle the sidebar display.",
                                                "",
                                                "<yellow>Click to toggle!")
                                .build();

                new IonGuiBuilder()
                                .title("<gradient:#FFD700:#FFA500><bold>✦ MAIN MENU ✦</bold></gradient>")
                                .rows(4)
                                .allowTake(false) // Prevent item stealing
                                .allowPlace(false)
                                .allowDrag(false)
                                .fillBorderBuilder(
                                                new IonItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build())
                                .item(10, statsItem, click -> {
                                        new StatsGui(plugin).open(player);
                                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                                })
                                .item(12, balanceItem, click -> {
                                        new BalanceGui(plugin).open(player);
                                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                                })
                                .item(14, shopItem, click -> {
                                        new ShopGui(plugin.getConfig().getConfigurationSection("shop")).open(player);
                                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                                })
                                .item(16, warpItem, click -> {
                                        new WarpGui(plugin).open(player);
                                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                                })
                                .item(22, leaderboardItem, click -> {
                                        new LeaderboardGui(plugin).open(player);
                                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                                })
                                .item(31, scoreboardToggle, click -> {
                                        plugin.getScoreboardManager().toggleScoreboard(player);
                                        player.closeInventory();
                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                                        MessageBuilder.of("<green>Scoreboard toggled!").send(player);
                                })
                                .build()
                                .open(player);
        }
}
