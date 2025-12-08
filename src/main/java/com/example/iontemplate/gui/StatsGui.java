package com.example.iontemplate.gui;

import com.example.iontemplate.IonTemplatePlugin;
import com.example.iontemplate.data.PlayerData;
import com.ionapi.gui.IonGuiBuilder;
import com.ionapi.item.IonItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

public class StatsGui {
    
    private final IonTemplatePlugin plugin;
    
    public StatsGui(IonTemplatePlugin plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        plugin.getDatabase().findAsync(PlayerData.class, player.getUniqueId()).thenAccept(opt -> {
            if (opt.isEmpty()) {
                player.sendMessage("§cNo stats found!");
                return;
            }
            
            PlayerData data = opt.get();
            long hours = TimeUnit.MILLISECONDS.toHours(data.getPlayTime());
            long minutes = TimeUnit.MILLISECONDS.toMinutes(data.getPlayTime()) % 60;
            
            ItemStack killsItem = new IonItemBuilder(Material.DIAMOND_SWORD)
                .name("<green><bold>Kills")
                .lore("<gray>Total kills: <white>" + data.getKills())
                .build();
            
            ItemStack deathsItem = new IonItemBuilder(Material.SKELETON_SKULL)
                .name("<red><bold>Deaths")
                .lore("<gray>Total deaths: <white>" + data.getDeaths())
                .build();
            
            ItemStack kdrItem = new IonItemBuilder(Material.GOLDEN_SWORD)
                .name("<yellow><bold>K/D Ratio")
                .lore("<gray>Ratio: <white>" + String.format("%.2f", data.getKDR()))
                .build();
            
            ItemStack timeItem = new IonItemBuilder(Material.CLOCK)
                .name("<aqua><bold>Play Time")
                .lore("<gray>Total: <white>" + hours + "h " + minutes + "m")
                .build();
            
            ItemStack backItem = new IonItemBuilder(Material.ARROW)
                .name("<gray>Back to Menu")
                .build();
            
            new IonGuiBuilder()
                .title("<gold><bold>" + player.getName() + "'s Stats")
                .rows(3)
                .item(11, killsItem, null)
                .item(13, deathsItem, null)
                .item(15, kdrItem, null)
                .item(22, timeItem, null)
                .item(18, backItem, click -> {
                    new MainMenuGui(plugin).open(player);
                })
                .build()
                .open(player);
        });
    }
}
