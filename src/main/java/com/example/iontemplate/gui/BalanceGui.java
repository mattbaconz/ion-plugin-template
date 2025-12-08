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

import java.math.BigDecimal;

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
                .title("<gold><bold>Balance: " + IonEconomy.format(balance))
                .rows(3)
                .item(13, balanceItem, null)
                .item(11, payItem, click -> {
                    player.closeInventory();
                    MessageBuilder.of("<yellow>Use: /pay <player> <amount>").send(player);
                })
                .item(15, topBalances, click -> {
                    showTopBalances(player);
                })
                .item(18, backItem, click -> {
                    new MainMenuGui(plugin).open(player);
                })
                .build()
                .open(player);
        });
    }
    
    private void showTopBalances(Player player) {
        // Get top 5 richest players
        ItemStack[] topItems = new ItemStack[5];
        int[] slot = {10, 11, 12, 13, 14};
        
        Bukkit.getOnlinePlayers().stream()
            .limit(5)
            .forEach(p -> {
                IonEconomy.getBalance(p.getUniqueId()).thenAccept(bal -> {
                    // This is simplified - in production you'd query the database
                });
            });
        
        ItemStack backItem = new IonItemBuilder(Material.ARROW)
            .name("<gray>Back")
            .build();
        
        new IonGuiBuilder()
            .title("<aqua><bold>Top Balances")
            .rows(3)
            .item(18, backItem, click -> {
                open(player);
            })
            .build()
            .open(player);
    }
}
