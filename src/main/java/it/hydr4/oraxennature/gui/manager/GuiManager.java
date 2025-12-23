package it.hydr4.oraxennature.gui.manager;

import it.hydr4.oraxennature.OraxenNature;
import it.hydr4.oraxennature.gui.base.Gui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiManager implements Listener {

    private final OraxenNature plugin;
    private final Map<UUID, Gui> openGuis;

    public GuiManager(OraxenNature plugin) {
        this.plugin = plugin;
        this.openGuis = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openGui(Player player, Gui gui) {
        openGuis.put(player.getUniqueId(), gui);
        gui.setupItems();
        gui.open(player);
    }

    public void closeGui(Player player) {
        if (openGuis.containsKey(player.getUniqueId())) {
            openGuis.remove(player.getUniqueId()).close(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (openGuis.containsKey(player.getUniqueId())) {
            openGuis.get(player.getUniqueId()).onInventoryClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        if (openGuis.containsKey(player.getUniqueId())) {
            openGuis.get(player.getUniqueId()).onInventoryClose(event);
            openGuis.remove(player.getUniqueId());
        }
    }
}