package it.hydr4.oraxennature.gui.base;

import net.kyori.adventure.text.Component;
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
    protected final Map<Integer, GuiItem> items;

    public AbstractGui(int size, Component title) {
        this.inventory = Bukkit.createInventory(null, size, title);
        this.items = new HashMap<>();
    }

    protected void setItem(int slot, GuiItem item) {
        items.put(slot, item);
        inventory.setItem(slot, item.getItemStack());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        GuiItem item = items.get(event.getRawSlot());
        if (item != null) {
            item.onClick(event);
        }
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        // Default implementation
    }

    @Override
    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public void close(Player player) {
        player.closeInventory();
    }
}
