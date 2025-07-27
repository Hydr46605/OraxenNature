package it.hydr4.oraxennature.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class DisplayItem implements GuiItem {

    private final ItemStack itemStack;

    public DisplayItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true); // Prevent interaction with display items
    }
}