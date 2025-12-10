package com.example.iontemplate.gui;

import com.ionapi.api.util.MessageBuilder;
import com.ionapi.economy.IonEconomy;
import com.ionapi.gui.ConfirmationGui;
import com.ionapi.gui.IonGuiBuilder;
import com.ionapi.item.IonItem;
import com.ionapi.item.IonItemBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.ionapi.gui.GuiSound;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ShopGui {

    private final ConfigurationSection shopConfig;

    // Price threshold for confirmation dialog
    private static final double CONFIRMATION_THRESHOLD = 200.0;

    public ShopGui(ConfigurationSection shopConfig) {
        this.shopConfig = shopConfig;
    }

    public void open(Player player) {
        List<ShopItem> items = loadShopItems();

        IonGuiBuilder builder = new IonGuiBuilder()
                .title("<gradient:#55FF55:#00AA00><bold>✦ SHOP MENU ✦</bold></gradient>")
                .rows(5)
                .allowTake(false) // Prevent item stealing
                .allowPlace(false) // Prevent placing items
                .allowDrag(false) // Prevent dragging
                .fillBorderBuilder(new IonItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build());

        // Info item in center-bottom
        ItemStack infoItem = IonItem.builder(Material.BOOK)
                .name("<gradient:gold:yellow><bold>ℹ Shop Info</bold></gradient>")
                .lore(
                        "",
                        "<gray>» <white>Left-Click <gray>to purchase",
                        "<gray>» <yellow>Expensive items require confirmation",
                        "",
                        "<gray>Your balance: <gold>{balance}")
                .build();

        // Main shop item slots (3 rows of 7)
        int[] slots = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34 };

        for (int i = 0; i < Math.min(items.size(), slots.length); i++) {
            ShopItem shopItem = items.get(i);
            ItemStack displayItem = createShopDisplayItem(shopItem);

            builder.item(slots[i], displayItem, click -> {
                handlePurchase(click.getPlayer(), shopItem);
                click.getPlayer().playSound(click.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            });
        }

        // Add info item
        builder.item(40, infoItem, click -> {
            click.getPlayer().playSound(click.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
        });

        builder.build().open(player);
    }

    private ItemStack createShopDisplayItem(ShopItem item) {
        var builder = IonItem.builder(item.material())
                .name("<green><bold>" + item.name());

        // Add special visual enhancements based on item type
        if (item.material() == Material.PLAYER_HEAD && item.skullTexture() != null) {
            builder.skullTexture(item.skullTexture());
        } else if (isLeatherArmor(item.material()) && item.color() != null) {
            builder.color(item.color());
        } else if (isPotionItem(item.material()) && item.potionColor() != null) {
            builder.potionColor(item.potionColor());
        }

        // Price indicator styling
        String priceColor = item.price() >= CONFIRMATION_THRESHOLD ? "<red>" : "<gold>";
        String priceIndicator = item.price() >= CONFIRMATION_THRESHOLD ? "⚠ " : "";

        builder.lore(
                "",
                "<gray>» <white>Price: " + priceColor + priceIndicator + "$" + String.format("%.2f", item.price()),
                "",
                item.price() >= CONFIRMATION_THRESHOLD
                        ? "<yellow>Click to confirm purchase!"
                        : "<gradient:#FFFF55:#FFAA00>Left-Click to purchase!</gradient>");

        return builder.build();
    }

    private void handlePurchase(Player player, ShopItem item) {
        // For expensive items, show confirmation dialog (v1.3.0 feature!)
        if (item.price() >= CONFIRMATION_THRESHOLD) {
            showConfirmationDialog(player, item);
        } else {
            // Direct purchase for cheaper items
            purchaseItem(player, item);
        }
    }

    private void showConfirmationDialog(Player player, ShopItem item) {
        ConfirmationGui.create()
                .title("<red><bold>⚠ Confirm Purchase</bold></red>")
                .message("<gray>Buy <white>" + item.name() + " <gray>for <gold>$" + String.format("%.2f", item.price())
                        + "<gray>?")
                .rows(3)
                .confirmItem(IonItem.builder(Material.GREEN_WOOL)
                        .name("<green><bold>✓ Confirm Purchase")
                        .lore(
                                "",
                                "<gray>Click to buy for <gold>$" + String.format("%.2f", item.price()))
                        .build())
                .cancelItem(IonItem.builder(Material.RED_WOOL)
                        .name("<red><bold>✗ Cancel")
                        .lore(
                                "",
                                "<gray>Click to go back")
                        .build())
                .fillerItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
                .onConfirm(p -> {
                    purchaseItem(p, item);
                })
                .onCancel(p -> {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                    MessageBuilder.of("<yellow>Purchase cancelled.").send(p);
                    // Re-open shop
                    open(p);
                })
                .confirmSound(new GuiSound(Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f))
                .cancelSound(new GuiSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f))
                .open(player);
    }

    private void purchaseItem(Player player, ShopItem item) {
        IonEconomy.has(player.getUniqueId(), item.price()).thenAccept(hasEnough -> {
            if (!hasEnough) {
                MessageBuilder.of("<red>✗ <gray>Not enough money! Need <gold>$" + String.format("%.2f", item.price()))
                        .send(player);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }

            IonEconomy.transaction(player.getUniqueId())
                    .withdraw(BigDecimal.valueOf(item.price()))
                    .reason("Shop purchase: " + item.name())
                    .commit()
                    .thenAccept(result -> {
                        if (result.isSuccess()) {
                            // Build the actual item to give
                            ItemStack purchased = buildPurchasedItem(item);
                            player.getInventory().addItem(purchased);
                            MessageBuilder
                                    .of("<green>✓ <gray>Purchased <white>" + item.name() + "<gray>! New balance: <gold>"
                                            + IonEconomy.format(result.getNewBalance()))
                                    .send(player);
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
                        } else {
                            MessageBuilder.of("<red>✗ <gray>Transaction failed: " + result.getErrorMessage())
                                    .send(player);
                        }
                    });
        });
    }

    private ItemStack buildPurchasedItem(ShopItem item) {
        var builder = IonItem.builder(item.material())
                .name(item.name());

        // Apply special properties (v1.3.0 features)
        if (item.material() == Material.PLAYER_HEAD && item.skullTexture() != null) {
            builder.skullTexture(item.skullTexture());
        } else if (isLeatherArmor(item.material()) && item.color() != null) {
            builder.color(item.color());
        } else if (isPotionItem(item.material())) {
            if (item.potionColor() != null) {
                builder.potionColor(item.potionColor());
            }
            if (item.potionEffect() != null) {
                builder.potionEffect(item.potionEffect(), item.potionDuration(), item.potionAmplifier());
            }
        }

        return builder.build();
    }

    private boolean isLeatherArmor(Material material) {
        return material == Material.LEATHER_HELMET ||
                material == Material.LEATHER_CHESTPLATE ||
                material == Material.LEATHER_LEGGINGS ||
                material == Material.LEATHER_BOOTS;
    }

    private boolean isPotionItem(Material material) {
        return material == Material.POTION ||
                material == Material.SPLASH_POTION ||
                material == Material.LINGERING_POTION;
    }

    private List<ShopItem> loadShopItems() {
        List<ShopItem> items = new ArrayList<>();
        if (shopConfig == null) {
            // Default items if no config (showcasing v1.3.0 features)
            items.add(new ShopItem(Material.DIAMOND_SWORD, 500, "<gradient:red:blue>Legendary Sword", null, null, null,
                    null, 0, 0));
            items.add(new ShopItem(Material.GOLDEN_APPLE, 100, "<gold>Golden Apple", null, null, null, null, 0, 0));
            items.add(new ShopItem(Material.ENDER_PEARL, 50, "<dark_purple>Ender Pearl", null, null, null, null, 0, 0));
            // v1.3.0 feature items
            items.add(new ShopItem(Material.LEATHER_CHESTPLATE, 150, "<red>Blood Armor", null, Color.fromRGB(139, 0, 0),
                    null, null, 0, 0));
            items.add(new ShopItem(Material.POTION, 75, "<aqua>Speed Potion", null, null, Color.AQUA,
                    PotionEffectType.SPEED, 600, 1));
            items.add(new ShopItem(Material.PLAYER_HEAD, 250, "<gradient:gold:yellow>Golden Crown",
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjk2M2Q0MzA3ZGFmMzI0NDMxNjQyZjk3ZmQ0YzBlMzU1NzNiN2VlNGQyZjE2OGJkMGM4ZjFiNTQ0NjkyOGI1MCJ9fX0=",
                    null, null, null, 0, 0));
            return items;
        }

        for (String key : shopConfig.getKeys(false)) {
            ConfigurationSection section = shopConfig.getConfigurationSection(key);
            if (section == null)
                continue;

            Material material = Material.valueOf(section.getString("material", "STONE"));
            double price = section.getDouble("price", 100);
            String name = section.getString("name", "<white>" + material.name());
            String skullTexture = section.getString("skull-texture", null);

            // Parse color
            Color color = null;
            if (section.contains("color")) {
                String colorStr = section.getString("color", "");
                if (colorStr.startsWith("#")) {
                    color = Color.fromRGB(Integer.parseInt(colorStr.substring(1), 16));
                }
            }

            // Parse potion
            Color potionColor = null;
            PotionEffectType potionEffect = null;
            int potionDuration = 0;
            int potionAmplifier = 0;
            if (section.contains("potion")) {
                ConfigurationSection potionSection = section.getConfigurationSection("potion");
                if (potionSection != null) {
                    String effectName = potionSection.getString("effect", "");
                    if (!effectName.isEmpty()) {
                        potionEffect = PotionEffectType.getByName(effectName);
                    }
                    potionDuration = potionSection.getInt("duration", 600);
                    potionAmplifier = potionSection.getInt("amplifier", 0);
                    String colorStr = potionSection.getString("color", "");
                    if (colorStr.startsWith("#")) {
                        potionColor = Color.fromRGB(Integer.parseInt(colorStr.substring(1), 16));
                    }
                }
            }

            items.add(new ShopItem(material, price, name, skullTexture, color, potionColor, potionEffect,
                    potionDuration, potionAmplifier));
        }
        return items;
    }

    private record ShopItem(
            Material material,
            double price,
            String name,
            String skullTexture,
            Color color,
            Color potionColor,
            PotionEffectType potionEffect,
            int potionDuration,
            int potionAmplifier) {
    }
}
