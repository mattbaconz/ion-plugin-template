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
                com.ionapi.api.util.MessageBuilder.of("<red>⚠ <gray>No stats found!").send(player);
                return;
            }

            PlayerData data = opt.get();
            long hours = TimeUnit.MILLISECONDS.toHours(data.getPlayTime());
            long minutes = TimeUnit.MILLISECONDS.toMinutes(data.getPlayTime()) % 60;

            ItemStack killsItem = new IonItemBuilder(Material.DIAMOND_SWORD)
                    .name("<gradient:#55FF55:#00AA00><bold>⚔ Kills</gradient>")
                    .lore("", "<gray>» <white>Total: <green>" + data.getKills())
                    .flags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES)
                    .build();

            ItemStack deathsItem = new IonItemBuilder(Material.SKELETON_SKULL)
                    .name("<gradient:#FF5555:#AA0000><bold>☠ Deaths</gradient>")
                    .lore("", "<gray>» <white>Total: <red>" + data.getDeaths())
                    .build();

            ItemStack kdrItem = new IonItemBuilder(Material.GOLDEN_SWORD)
                    .name("<gradient:#FFFF55:#FFAA00><bold>➗ K/D Ratio</gradient>")
                    .lore("", "<gray>» <white>Ratio: <yellow>" + String.format("%.2f", data.getKDR()))
                    .flags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES)
                    .build();

            ItemStack timeItem = new IonItemBuilder(Material.CLOCK)
                    .name("<gradient:#55FFFF:#00AAAA><bold>⏱ Play Time</gradient>")
                    .lore("", "<gray>» <white>Total: <aqua>" + hours + "h " + minutes + "m")
                    .build();

            ItemStack backItem = new IonItemBuilder(Material.ARROW)
                    .name("<red><bold>❌ Back to Menu")
                    .lore("", "<gray>Click to return.")
                    .build();

            new IonGuiBuilder()
                    .title("<gradient:#55FFFF:#00AAAA><bold>✦ PLAYER STATS ✦</bold></gradient>")
                    .rows(3)
                    .allowTake(false) // Prevent item stealing
                    .allowPlace(false)
                    .allowDrag(false)
                    .fillBorderBuilder(new IonItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build())
                    .item(11, killsItem, null)
                    .item(13, deathsItem, null)
                    .item(15, kdrItem, null)
                    .item(22, timeItem, null)
                    .item(18, backItem, click -> {
                        new MainMenuGui(plugin).open(player);
                        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
                    })
                    .build()
                    .open(player);
        });
    }
}
