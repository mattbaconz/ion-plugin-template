package com.example.iontemplate.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GuiProtectionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null)
            return;

        Inventory topInventory = event.getView().getTopInventory();

        // Check if the open inventory (top) is one of our protected GUIs
        if (isProtectedGui(topInventory)) {

            // If clicking in the top inventory (the GUI itself)
            if (event.getClickedInventory().equals(topInventory)) {

                // Always cancel the event if it hasn't been cancelled yet
                // This prevents taking items out
                if (!event.isCancelled()) {
                    event.setCancelled(true);
                }

                // Double check for "collect to cursor" which can grab items even if clicked
                // slot is empty sometimes?
                // Or if clicking with item on cursor.
                // Just setting cancelled(true) should handle it.
            }
            // If clicking in bottom inventory (player inventory)
            else if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
                // Prevent moving items into the GUI via shift-click
                if (event.isShiftClick()) {
                    if (!event.isCancelled()) {
                        event.setCancelled(true);
                    }
                }

                // Prevent "collect to cursor" (double click) from pulling items from the GUI
                if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                    if (!event.isCancelled()) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (isProtectedGui(event.getInventory())) {
            // Passively allow dragging in player inventory (bottom) if it doesn't affect
            // top?
            // Drag event involves raw slots.
            // If any of the new slots are in the top inventory, cancel.

            boolean involvesTop = false;
            int topSize = event.getInventory().getSize();
            for (int slot : event.getRawSlots()) {
                if (slot < topSize) {
                    involvesTop = true;
                    break;
                }
            }

            if (involvesTop) {
                if (!event.isCancelled()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean isProtectedGui(Inventory inventory) {
        if (inventory == null)
            return false;

        InventoryHolder holder = inventory.getHolder();
        if (holder != null) {
            String className = holder.getClass().getName();
            // Check if the holder belongs to IonAPI or our plugin's GUIs
            return className.startsWith("com.ionapi.gui") ||
                    className.startsWith("com.example.iontemplate.gui");
        }

        // Fallback: If holder is null (some GUIs might do this), we could check title
        // but let's stick to holder for now.
        // Most library GUIs use a custom holder.
        return false;
    }
}
