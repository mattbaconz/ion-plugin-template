package com.example.iontemplate.gui;

import com.example.iontemplate.IonTemplatePlugin;
import com.example.iontemplate.data.Warp;
import com.ionapi.api.util.MessageBuilder;
import com.ionapi.gui.IonGuiBuilder;
import com.ionapi.item.IonItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class WarpGui {
    
    private final IonTemplatePlugin plugin;
    
    public WarpGui(IonTemplatePlugin plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        plugin.getDatabase().findAllAsync(Warp.class).thenAccept(warps -> {
            IonGuiBuilder builder = new IonGuiBuilder()
                .title("<aqua><bold>Warps")
                .rows(3);
            
            int slot = 10;
            for (Warp warp : warps) {
                if (slot > 16) break;
                
                ItemStack warpItem = new IonItemBuilder(Material.ENDER_PEARL)
                    .name("<aqua><bold>" + warp.getName())
                    .lore("<gray>World: <white>" + warp.getWorld())
                    .lore("<gray>X: <white>" + (int)warp.getX() + " <gray>Y: <white>" + (int)warp.getY() + " <gray>Z: <white>" + (int)warp.getZ())
                    .lore("")
                    .lore("<yellow>Click to teleport!")
                    .build();
                
                builder.item(slot++, warpItem, click -> {
                    Location loc = warp.toLocation();
                    if (loc != null) {
                        player.teleport(loc);
                        player.closeInventory();
                        MessageBuilder.of("<green>Teleported to <aqua>" + warp.getName() + "<green>!").send(player);
                    } else {
                        MessageBuilder.of("<red>Warp world not found!").send(player);
                    }
                });
            }
            
            ItemStack createItem = new IonItemBuilder(Material.NETHER_STAR)
                .name("<green><bold>Create Warp")
                .lore("<gray>Use: /warp set <name>")
                .build();
            
            ItemStack backItem = new IonItemBuilder(Material.ARROW)
                .name("<gray>Back to Menu")
                .build();
            
            builder.item(22, createItem, click -> {
                player.closeInventory();
                MessageBuilder.of("<yellow>Use: /warp set <name>").send(player);
            });
            
            builder.item(18, backItem, click -> {
                new MainMenuGui(plugin).open(player);
            });
            
            builder.build().open(player);
        });
    }
}
