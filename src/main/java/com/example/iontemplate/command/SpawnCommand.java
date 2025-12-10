package com.example.iontemplate.command;

import com.ionapi.api.util.CooldownManager;
import com.ionapi.api.util.MessageBuilder;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class SpawnCommand implements CommandExecutor {

    private final CooldownManager cooldowns;
    private final Location spawnLocation;
    private final int cooldownSeconds;

    public SpawnCommand(CooldownManager cooldowns, Location spawnLocation, int cooldownSeconds) {
        this.cooldowns = cooldowns;
        this.spawnLocation = spawnLocation;
        this.cooldownSeconds = cooldownSeconds;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (cooldowns.isOnCooldown(player.getUniqueId())) {
            long remaining = cooldowns.getRemainingTime(player.getUniqueId(), TimeUnit.SECONDS);
            MessageBuilder.of("<red>⚠ <gray>Wait <red>" + remaining + "s <gray>before teleporting again!").send(player);
            return true;
        }

        player.teleport(spawnLocation);
        cooldowns.setCooldown(player.getUniqueId(), cooldownSeconds, TimeUnit.SECONDS);
        MessageBuilder.of("<gray>» <green>Teleported to spawn!").send(player);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        return true;
    }
}
