package com.example.iontemplate.gui;

import com.example.iontemplate.IonTemplatePlugin;
import com.ionapi.api.util.MessageBuilder;
import com.ionapi.economy.IonEconomy;
import com.ionapi.gui.IonGuiBuilder;
import com.ionapi.item.IonItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BalanceGui {

    private final IonTemplatePlugin plugin;

    public BalanceGui(IonTemplatePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        IonEconomy.getBalance(player.getUniqueId()).thenAccept(balance -> {
            ItemStack balanceItem = new IonItemBuilder(Material.GOLD_BLOCK)
                    .name("<gold><bold>Your Balance")
                    .lore("<gray>Current: <yellow>" + IonEconomy.format(balance))
                    .build();

            ItemStack payItem = new IonItemBuilder(Material.EMERALD)
                    .name("<green><bold>Pay Player")
                    .lore("<gray>Click to pay another player")
                    .lore("<gray>Use: /pay <player> <amount>")
                    .build();

            ItemStack topBalances = new IonItemBuilder(Material.DIAMOND)
                    .name("<aqua><bold>Top Balances")
                    .lore("<gray>Richest players on the server")
                    .build();

            ItemStack backItem = new IonItemBuilder(Material.ARROW)
                    .name("<gray>Back to Menu")
                    .build();

            new IonGuiBuilder()
                    .title("<gradient:#FFD700:#FFA500><bold>✦ Balance: " + IonEconomy.format(balance)
                            + " ✦</bold></gradient>")
                    .rows(3)
                    .allowTake(false) // Prevent item stealing
                    .allowPlace(false)
                    .allowDrag(false)
                    .fillBorderBuilder(new IonItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build())
                    .item(13, balanceItem, null)
                    .item(11, payItem, click -> {
                        player.closeInventory();
                        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
                        MessageBuilder.of("<yellow>ℹ <gray>Use: <white>/pay <player> <amount>").send(player);
                    })
                    .item(15, topBalances, click -> {
                        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
                        showTopBalances(player);
                    })
                    .item(18, backItem, click -> {
                        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
                        new MainMenuGui(plugin).open(player);
                    })
                    .build()
                    .open(player);
        });
    }

    private void showTopBalances(Player player) {
        // Get top 5 richest players
        // Placeholder logic for now - in production you'd query the database

        ItemStack backItem = new IonItemBuilder(Material.ARROW)
                .name("<gray>Back")
                .build();

        new IonGuiBuilder()
                .title("<gradient:aqua:blue><bold>✦ Top Balances ✦</bold></gradient>")
                .rows(3)
                .allowTake(false)
                .allowPlace(false)
                .allowDrag(false)
                .fillBorderBuilder(new IonItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build())
                .item(18, backItem, click -> {
                    player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
                    open(player);
                })
                .build()
                .open(player);
    }
}
