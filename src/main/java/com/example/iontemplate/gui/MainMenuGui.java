package com.example.iontemplate.gui;

import com.example.iontemplate.IonTemplatePlugin;
import com.ionapi.api.util.MessageBuilder;
import com.ionapi.economy.IonEconomy;
import com.ionapi.gui.IonGuiBuilder;
import com.ionapi.item.IonItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MainMenuGui {
    
    private final IonTemplatePlugin plugin;
    
    public MainMenuGui(IonTemplatePlugin plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        ItemStack statsItem = new IonItemBuilder(Material.BOOK)
            .name("<gold><bold>Your Stats")
            .lore("<gray>Click to view your statistics")
            .lore("")
            .lore("<yellow>Kills: <white>" + plugin.getPlayerKills(player.getUniqueId()))
            .lore("<yellow>Deaths: <white>" + plugin.getPlayerDeaths(player.getUniqueId()))
            .lore("<yellow>K/D: <white>" + String.format("%.2f", plugin.getPlayerKDR(player.getUniqueId())))
            .build();
        
        ItemStack balanceItem = new IonItemBuilder(Material.GOLD_INGOT)
            .name("<gold><bold>Balance")
            .lore("<gray>Click to view your balance")
            .lore("")
            .lore("<yellow>Current: <white>" + getBalance(player))
            .build();
        
        ItemStack shopItem = new IonItemBuilder(Material.EMERALD)
            .name("<green><bold>Shop")
            .lore("<gray>Click to open the shop")
            .build();
        
        ItemStack warpItem = new IonItemBuilder(Material.ENDER_PEARL)
            .name("<aqua><bold>Warps")
            .lore("<gray>Click to manage warps")
            .build();
        
        ItemStack leaderboardItem = new IonItemBuilder(Material.DIAMOND)
            .name("<blue><bold>Leaderboards")
            .lore("<gray>Click to view top players")
            .build();
        
        ItemStack scoreboardToggle = new IonItemBuilder(Material.PAINTING)
            .name("<yellow><bold>Toggle Scoreboard")
            .lore("<gray>Click to show/hide scoreboard")
            .build();
        
        new IonGuiBuilder()
            .title("<gold><bold>Main Menu")
            .rows(3)
            .item(10, statsItem, click -> {
                new StatsGui(plugin).open(player);
            })
            .item(12, balanceItem, click -> {
                new BalanceGui(plugin).open(player);
            })
            .item(14, shopItem, click -> {
                new ShopGui(plugin.getConfig().getConfigurationSection("shop")).open(player);
            })
            .item(16, warpItem, click -> {
                new WarpGui(plugin).open(player);
            })
            .item(20, leaderboardItem, click -> {
                new LeaderboardGui(plugin).open(player);
            })
            .item(24, scoreboardToggle, click -> {
                plugin.getScoreboardManager().toggleScoreboard(player);
                player.closeInventory();
                MessageBuilder.of("<green>Scoreboard toggled!").send(player);
            })
            .build()
            .open(player);
    }
    
    private String getBalance(Player player) {
        try {
            return IonEconomy.format(IonEconomy.getBalance(player.getUniqueId()).join());
        } catch (Exception e) {
            return "$0.00";
        }
    }
}
