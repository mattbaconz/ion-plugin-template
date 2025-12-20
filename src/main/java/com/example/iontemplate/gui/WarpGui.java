package com.example.iontemplate.gui;

import com.example.iontemplate.IonTemplatePlugin;
import com.example.iontemplate.data.Warp;
import com.ionapi.api.util.MessageBuilder;
import com.ionapi.gui.ConfirmationGui;
import com.ionapi.gui.IonGuiBuilder;
import com.ionapi.item.IonItem;
import com.ionapi.item.IonItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.ionapi.gui.GuiSound;

import java.util.List;

public class WarpGui {

    private final IonTemplatePlugin plugin;

    public WarpGui(IonTemplatePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        plugin.getDatabase().findAllAsync(Warp.class).thenAccept(warps -> {
            plugin.getScheduler().runAt(player, () -> buildAndOpen(player, warps));
        });
    }

    private void buildAndOpen(Player player, List<Warp> warps) {
        IonGuiBuilder builder = new IonGuiBuilder()
                .title("<gradient:#FF55FF:#AA00AA><bold>✦ WARPS ✦</bold></gradient>")
                .rows(4)
                .allowTake(false) // Prevent item stealing
                .allowPlace(false)
                .allowDrag(false)
                .fillBorderBuilder(new IonItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build());

        int[] slots = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25 };
        int index = 0;

        for (Warp warp : warps) {
            if (index >= slots.length)
                break;

            int slot = slots[index++];

            ItemStack warpItem = IonItem.builder(Material.ENDER_PEARL)
                    .name("<gradient:light_purple:aqua><bold>" + warp.getName() + "</bold></gradient>")
                    .lore(
                            "",
                            "<gray>» <white>World: <aqua>" + warp.getWorld(),
                            "<gray>» <white>Position: <yellow>" + (int) warp.getX() + ", " + (int) warp.getY() + ", "
                                    + (int) warp.getZ(),
                            "",
                            "<green>Left-Click <gray>to teleport",
                            player.hasPermission("iontemplate.warp.delete")
                                    ? "<red>Right-Click <gray>to delete"
                                    : "")
                    .glow()
                    .build();

            builder.item(slot, warpItem, click -> {
                if (click.isRightClick() && player.hasPermission("iontemplate.warp.delete")) {
                    // Show confirmation dialog for deletion
                    showDeleteConfirmation(player, warp);
                } else {
                    // Teleport
                    Location loc = warp.toLocation();
                    if (loc != null) {
                        player.closeInventory();
                        plugin.getScheduler().runAt(player, () -> {
                            player.teleport(loc);
                            MessageBuilder
                                    .of("<green>✓ <gray>Teleported to <light_purple>" + warp.getName() + "<gray>!")
                                    .send(player);
                            player.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                        });
                    } else {
                        MessageBuilder.of("<red>✗ <gray>Warp world not found!").send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    }
                }
            });
        }

        // Create warp button (only if has permission)
        if (player.hasPermission("iontemplate.warp.set")) {
            ItemStack createItem = IonItem.builder(Material.NETHER_STAR)
                    .name("<gradient:green:lime><bold>➕ Create Warp</bold></gradient>")
                    .lore(
                            "",
                            "<gray>Use the command:",
                            "<yellow>/warp set <name>",
                            "",
                            "<gray>Creates a warp at your location")
                    .glow()
                    .build();

            builder.item(31, createItem, click -> {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                MessageBuilder.of("<yellow>ℹ <gray>Use: <white>/warp set <name>").send(player);
            });
        }

        // Back button
        ItemStack backItem = IonItem.builder(Material.ARROW)
                .name("<gray>« Back to Menu")
                .build();

        builder.item(27, backItem, click -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new MainMenuGui(plugin).open(player);
        });

        // Info item
        ItemStack infoItem = IonItem.builder(Material.BOOK)
                .name("<gradient:gold:yellow><bold>ℹ Warp Info</bold></gradient>")
                .lore(
                        "",
                        "<gray>» <white>Total warps: <aqua>" + warps.size(),
                        "",
                        "<green>Left-Click <gray>to teleport",
                        "<red>Right-Click <gray>to delete (admin)")
                .build();

        builder.item(35, infoItem, click -> {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
        });

        builder.build().open(player);
    }

    private void showDeleteConfirmation(Player player, Warp warp) {
        // ConfirmationGui for dangerous action!
        ConfirmationGui.create()
                .title("<red><bold>⚠ Delete Warp?</bold></red>")
                .message("<gray>Delete warp <light_purple>" + warp.getName() + "<gray>? This cannot be undone!")
                .rows(3)
                .confirmItem(IonItem.builder(Material.RED_WOOL)
                        .name("<red><bold>✓ Delete Warp")
                        .lore(
                                "",
                                "<gray>This action is <red>irreversible</red>!")
                        .build())
                .cancelItem(IonItem.builder(Material.GREEN_WOOL)
                        .name("<green><bold>✗ Keep Warp")
                        .lore(
                                "",
                                "<gray>Return to the warp menu")
                        .build())
                .fillerItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
                .onConfirm(p -> {
                    plugin.getDatabase().deleteAsync(warp).thenRun(() -> {
                        plugin.getScheduler().runAt(p, () -> {
                            MessageBuilder.of("<green>✓ <gray>Warp <light_purple>" + warp.getName() + " <gray>deleted!")
                                    .send(p);
                            p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 0.5f);
                            // Re-open warp menu
                            open(p);
                        });
                    });
                })
                .onCancel(p -> {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                    MessageBuilder.of("<yellow>Deletion cancelled.").send(p);
                    // Re-open warp menu
                    open(p);
                })
                .confirmSound(new GuiSound(Sound.ENTITY_BLAZE_DEATH, 1.0f, 0.5f))
                .cancelSound(new GuiSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f))
                .danger() // Red styling for destructive action
                .open(player);
    }
}
