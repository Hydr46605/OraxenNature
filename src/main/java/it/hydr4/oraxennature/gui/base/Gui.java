package it.hydr4.oraxennature.gui.base;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public interface Gui {

    Inventory getInventory();

    void onInventoryClick(InventoryClickEvent event);

    void onInventoryClose(InventoryCloseEvent event);

    void open(Player player);

    void close(Player player);

    void setupItems();

}
