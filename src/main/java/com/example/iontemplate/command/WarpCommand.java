package com.example.iontemplate.command;

import com.example.iontemplate.data.Warp;
import com.ionapi.api.util.MessageBuilder;
import com.ionapi.database.IonDatabase;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WarpCommand implements CommandExecutor {
    
    private final IonDatabase database;
    
    public WarpCommand(IonDatabase database) {
        this.database = database;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        
        if (args.length == 0) {
            // Open warp GUI
            new com.example.iontemplate.gui.WarpGui(
                (com.example.iontemplate.IonTemplatePlugin) org.bukkit.Bukkit.getPluginManager().getPlugin("IonTemplatePlugin")
            ).open(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "list" -> {
                new com.example.iontemplate.gui.WarpGui(
                    (com.example.iontemplate.IonTemplatePlugin) org.bukkit.Bukkit.getPluginManager().getPlugin("IonTemplatePlugin")
                ).open(player);
            }
            case "set" -> {
                if (args.length < 2) {
                    MessageBuilder.of("<red>Usage: /warp set <name>").send(player);
                    return true;
                }
                setWarp(player, args[1]);
            }
            case "delete", "del" -> {
                if (args.length < 2) {
                    MessageBuilder.of("<red>Usage: /warp delete <name>").send(player);
                    return true;
                }
                deleteWarp(player, args[1]);
            }
            default -> teleportToWarp(player, args[0]);
        }
        
        return true;
    }
    
    private void setWarp(Player player, String name) {
        if (!player.hasPermission("iontemplate.warp.set")) {
            MessageBuilder.of("<red>No permission!").send(player);
            return;
        }
        
        database.findAsync(Warp.class, name).thenAccept(opt -> {
            if (opt.isPresent()) {
                MessageBuilder.of("<red>Warp '" + name + "' already exists!").send(player);
                return;
            }
            
            Warp warp = new Warp(name, player.getLocation(), player.getUniqueId());
            database.saveAsync(warp).thenRun(() -> {
                MessageBuilder.of("<green>✓ Warp '" + name + "' created!").send(player);
            });
        });
    }
    
    private void deleteWarp(Player player, String name) {
        if (!player.hasPermission("iontemplate.warp.delete")) {
            MessageBuilder.of("<red>No permission!").send(player);
            return;
        }
        
        database.findAsync(Warp.class, name).thenAccept(opt -> {
            if (opt.isEmpty()) {
                MessageBuilder.of("<red>Warp '" + name + "' not found!").send(player);
                return;
            }
            
            database.deleteAsync(opt.get()).thenRun(() -> {
                MessageBuilder.of("<green>✓ Warp '" + name + "' deleted!").send(player);
            });
        });
    }
    
    private void teleportToWarp(Player player, String name) {
        database.findAsync(Warp.class, name).thenAccept(opt -> {
            if (opt.isEmpty()) {
                MessageBuilder.of("<red>Warp '" + name + "' not found!").send(player);
                return;
            }
            
            Location location = opt.get().toLocation();
            if (location == null) {
                MessageBuilder.of("<red>Warp world not loaded!").send(player);
                return;
            }
            
            player.teleport(location);
            MessageBuilder.of("<green>✓ Teleported to " + name + "!").send(player);
        });
    }
}
