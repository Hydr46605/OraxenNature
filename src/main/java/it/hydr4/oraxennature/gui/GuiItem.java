package it.hydr4.oraxennature.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public interface GuiItem {

    ItemStack getItemStack();

    void onClick(InventoryClickEvent event);

}