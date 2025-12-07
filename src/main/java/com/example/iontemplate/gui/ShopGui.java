package com.example.iontemplate.gui;

import com.ionapi.api.util.MessageBuilder;
import com.ionapi.economy.IonEconomy;
import com.ionapi.gui.IonGuiBuilder;
import com.ionapi.item.IonItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ShopGui {
    
    private final ConfigurationSection shopConfig;
    
    public ShopGui(ConfigurationSection shopConfig) {
        this.shopConfig = shopConfig;
    }
    
    public void open(Player player) {
        List<ShopItem> items = loadShopItems();
        
        IonGuiBuilder builder = new IonGuiBuilder()
            .title("<gold><bold>Shop Menu")
            .rows(3)
            .fillBorderBuilder(IonItem.of(Material.GRAY_STAINED_GLASS_PANE, " "));
        
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < Math.min(items.size(), slots.length); i++) {
            ShopItem shopItem = items.get(i);
            ItemStack displayItem = IonItem.builder(shopItem.material())
                .name(shopItem.name())
                .lore(
                    "",
                    "<gray>Price: <gold>$" + shopItem.price(),
                    "",
                    "<yellow>Click to purchase!"
                )
                .build();
            
            builder.item(slots[i], displayItem, click -> {
                purchaseItem(click.getPlayer(), shopItem);
            });
        }
        
        builder.build().open(player);
    }
    
    private void purchaseItem(Player player, ShopItem item) {
        IonEconomy.has(player.getUniqueId(), item.price()).thenAccept(hasEnough -> {
            if (!hasEnough) {
                MessageBuilder.of("<red>Not enough money! Need <gold>$" + item.price()).send(player);
                return;
            }
            
            IonEconomy.transaction(player.getUniqueId())
                .withdraw(BigDecimal.valueOf(item.price()))
                .reason("Shop purchase: " + item.material().name())
                .commit()
                .thenAccept(result -> {
                    if (result.isSuccess()) {
                        ItemStack purchased = IonItem.builder(item.material())
                            .name(item.name())
                            .build();
                        player.getInventory().addItem(purchased);
                        MessageBuilder.of("<green>Purchased! New balance: <gold>$" + IonEconomy.format(result.getNewBalance()))
                            .send(player);
                    }
                });
        });
    }
    
    private List<ShopItem> loadShopItems() {
        List<ShopItem> items = new ArrayList<>();
        if (shopConfig == null) return items;
        
        for (String key : shopConfig.getKeys(false)) {
            ConfigurationSection section = shopConfig.getConfigurationSection(key);
            if (section == null) continue;
            
            Material material = Material.valueOf(section.getString("material", "STONE"));
            double price = section.getDouble("price", 100);
            String name = section.getString("name", "<white>" + material.name());
            
            items.add(new ShopItem(material, price, name));
        }
        return items;
    }
    
    private record ShopItem(Material material, double price, String name) {}
}
