package com.example.iontemplate.gui;

import com.example.iontemplate.IonTemplatePlugin;
import com.example.iontemplate.data.PlayerData;
import com.ionapi.gui.IonGuiBuilder;
import com.ionapi.item.IonItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;

public class LeaderboardGui {

    private final IonTemplatePlugin plugin;

    public LeaderboardGui(IonTemplatePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        showKillsLeaderboard(player);
    }

    public void showKillsLeaderboard(Player player) {
        plugin.getDatabase().findAllAsync(PlayerData.class).thenAccept(allData -> {
            List<PlayerData> topKills = allData.stream()
                    .sorted(Comparator.comparingInt(PlayerData::getKills).reversed())
                    .limit(10)
                    .toList();

            IonGuiBuilder builder = new IonGuiBuilder()
                    .title("<gold><bold>Top Kills")
                    .rows(4)
                    .allowTake(false)
                    .allowPlace(false)
                    .allowDrag(false);

            int slot = 10;
            int rank = 1;
            for (PlayerData data : topKills) {
                if (slot > 16) {
                    slot = 19;
                }
                if (slot > 25)
                    break;

                Material material = switch (rank) {
                    case 1 -> Material.GOLD_BLOCK;
                    case 2 -> Material.IRON_BLOCK;
                    case 3 -> Material.COPPER_BLOCK;
                    default -> Material.STONE;
                };

                ItemStack item = new IonItemBuilder(material)
                        .name("<yellow>#" + rank + " <white>" + data.getName())
                        .lore("<gray>Kills: <green>" + data.getKills())
                        .lore("<gray>Deaths: <red>" + data.getDeaths())
                        .lore("<gray>K/D: <yellow>" + String.format("%.2f", data.getKDR()))
                        .build();

                builder.item(slot++, item, null);
                rank++;
            }

            // Category buttons
            ItemStack killsBtn = new IonItemBuilder(Material.DIAMOND_SWORD)
                    .name("<green><bold>Kills")
                    .lore("<gray>Currently viewing")
                    .build();

            ItemStack deathsBtn = new IonItemBuilder(Material.SKELETON_SKULL)
                    .name("<red><bold>Deaths")
                    .lore("<gray>Click to view")
                    .build();

            ItemStack kdrBtn = new IonItemBuilder(Material.GOLDEN_SWORD)
                    .name("<yellow><bold>K/D Ratio")
                    .lore("<gray>Click to view")
                    .build();

            ItemStack backItem = new IonItemBuilder(Material.ARROW)
                    .name("<gray>Back to Menu")
                    .build();

            builder.item(28, killsBtn, null);
            builder.item(30, deathsBtn, click -> showDeathsLeaderboard(player));
            builder.item(32, kdrBtn, click -> showKDRLeaderboard(player));
            builder.item(27, backItem, click -> new MainMenuGui(plugin).open(player));

            builder.build().open(player);
        });
    }

    public void showDeathsLeaderboard(Player player) {
        plugin.getDatabase().findAllAsync(PlayerData.class).thenAccept(allData -> {
            List<PlayerData> topDeaths = allData.stream()
                    .sorted(Comparator.comparingInt(PlayerData::getDeaths).reversed())
                    .limit(10)
                    .toList();

            IonGuiBuilder builder = new IonGuiBuilder()
                    .title("<red><bold>Top Deaths")
                    .rows(4)
                    .allowTake(false)
                    .allowPlace(false)
                    .allowDrag(false);

            int slot = 10;
            int rank = 1;
            for (PlayerData data : topDeaths) {
                if (slot > 16)
                    slot = 19;
                if (slot > 25)
                    break;

                ItemStack item = new IonItemBuilder(Material.SKELETON_SKULL)
                        .name("<yellow>#" + rank + " <white>" + data.getName())
                        .lore("<gray>Deaths: <red>" + data.getDeaths())
                        .lore("<gray>Kills: <green>" + data.getKills())
                        .build();

                builder.item(slot++, item, null);
                rank++;
            }

            ItemStack killsBtn = new IonItemBuilder(Material.DIAMOND_SWORD)
                    .name("<green><bold>Kills")
                    .lore("<gray>Click to view")
                    .build();

            ItemStack deathsBtn = new IonItemBuilder(Material.SKELETON_SKULL)
                    .name("<red><bold>Deaths")
                    .lore("<gray>Currently viewing")
                    .build();

            ItemStack kdrBtn = new IonItemBuilder(Material.GOLDEN_SWORD)
                    .name("<yellow><bold>K/D Ratio")
                    .lore("<gray>Click to view")
                    .build();

            ItemStack backItem = new IonItemBuilder(Material.ARROW)
                    .name("<gray>Back to Menu")
                    .build();

            builder.item(28, killsBtn, click -> showKillsLeaderboard(player));
            builder.item(30, deathsBtn, null);
            builder.item(32, kdrBtn, click -> showKDRLeaderboard(player));
            builder.item(27, backItem, click -> new MainMenuGui(plugin).open(player));

            builder.build().open(player);
        });
    }

    public void showKDRLeaderboard(Player player) {
        plugin.getDatabase().findAllAsync(PlayerData.class).thenAccept(allData -> {
            List<PlayerData> topKDR = allData.stream()
                    .filter(d -> d.getDeaths() > 0)
                    .sorted(Comparator.comparingDouble(PlayerData::getKDR).reversed())
                    .limit(10)
                    .toList();

            IonGuiBuilder builder = new IonGuiBuilder()
                    .title("<yellow><bold>Top K/D Ratio")
                    .rows(4)
                    .allowTake(false)
                    .allowPlace(false)
                    .allowDrag(false);

            int slot = 10;
            int rank = 1;
            for (PlayerData data : topKDR) {
                if (slot > 16)
                    slot = 19;
                if (slot > 25)
                    break;

                ItemStack item = new IonItemBuilder(Material.GOLDEN_SWORD)
                        .name("<yellow>#" + rank + " <white>" + data.getName())
                        .lore("<gray>K/D: <yellow>" + String.format("%.2f", data.getKDR()))
                        .lore("<gray>Kills: <green>" + data.getKills())
                        .lore("<gray>Deaths: <red>" + data.getDeaths())
                        .build();

                builder.item(slot++, item, null);
                rank++;
            }

            ItemStack killsBtn = new IonItemBuilder(Material.DIAMOND_SWORD)
                    .name("<green><bold>Kills")
                    .lore("<gray>Click to view")
                    .build();

            ItemStack deathsBtn = new IonItemBuilder(Material.SKELETON_SKULL)
                    .name("<red><bold>Deaths")
                    .lore("<gray>Click to view")
                    .build();

            ItemStack kdrBtn = new IonItemBuilder(Material.GOLDEN_SWORD)
                    .name("<yellow><bold>K/D Ratio")
                    .lore("<gray>Currently viewing")
                    .build();

            ItemStack backItem = new IonItemBuilder(Material.ARROW)
                    .name("<gray>Back to Menu")
                    .build();

            builder.item(28, killsBtn, click -> showKillsLeaderboard(player));
            builder.item(30, deathsBtn, click -> showDeathsLeaderboard(player));
            builder.item(32, kdrBtn, null);
            builder.item(27, backItem, click -> new MainMenuGui(plugin).open(player));

            builder.build().open(player);
        });
    }
}
