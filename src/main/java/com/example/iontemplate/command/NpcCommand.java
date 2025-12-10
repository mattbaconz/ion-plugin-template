package com.example.iontemplate.command;

import com.example.iontemplate.IonTemplatePlugin;
import com.ionapi.api.util.MessageBuilder;
import com.ionapi.npc.IonNPC;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NpcCommand implements CommandExecutor {

    private final IonTemplatePlugin plugin;

    public NpcCommand(IonTemplatePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (args.length < 2) {
            MessageBuilder.of("<red>Usage: /npc spawn <name> [skin]").send(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("spawn")) {
            String name = args[1].replace("&", "§");
            String skin = args.length > 2 ? args[2] : player.getName();

            IonNPC npc = IonNPC.builder(plugin)
                    .location(player.getLocation())
                    .name(name)
                    .skin(skin)
                    .lookAtPlayer(true)
                    .viewDistance(50)
                    .persistent(true)
                    .onClick(p -> {
                        MessageBuilder
                                .of("<gray>[" + name + "<gray>] <white>Hello there, <yellow>" + p.getName()
                                        + "<white>!")
                                .send(p);
                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
                        p.swingMainHand();
                    })
                    .build();

            npc.show(player);

            MessageBuilder.of("<green>✓ <gray>NPC <white>" + name + " <gray>spawned!")
                    .send(player);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }

        return true;
    }
}
