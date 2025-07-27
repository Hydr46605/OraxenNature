package it.hydr4.oraxennature.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractGui implements Gui {

    protected final Inventory inventory;
    protected final Map<Integer, GuiItem> guiItems;

    public AbstractGui(int size, String title) {
        this.inventory = Bukkit.createInventory(null, size, title);
        this.guiItems = new HashMap<>();
    }

    protected void setItem(int slot, GuiItem guiItem) {
        inventory.setItem(slot, guiItem.getItemStack());
        guiItems.put(slot, guiItem);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true); // By default, cancel all clicks in our GUIs
        GuiItem clickedItem = guiItems.get(event.getRawSlot());
        if (clickedItem != null) {
            clickedItem.onClick(event);
        }
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        // Default implementation: do nothing
    }

    @Override
    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public void close(Player player) {
        player.closeInventory();
    }

    public abstract void setupItems();
}