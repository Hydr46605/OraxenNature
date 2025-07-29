package it.hydr4.oraxennature.gui;

import it.hydr4.oraxennature.OraxenNature;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;

public class PackDetailGui extends AbstractGui {

    private final String packName;
    private final OraxenNature plugin;

    public PackDetailGui(OraxenNature plugin, String packName) {
        super(plugin, 54, "§8Pack Details: " + packName);
        this.plugin = plugin;
        this.packName = packName;
        loadItems();
    }

    private void loadItems() {
        File packFile = new File(plugin.getDataFolder(), "packs/" + packName);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(packFile);

        // Pack Info Item
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§b" + config.getString("pack_info.name", packName));
            infoMeta.setLore(java.util.Arrays.asList(
                    "§7Version: §f" + config.getString("pack_info.version", "N/A"),
                    "§7Author: §f" + config.getString("pack_info.author", "N/A"),
                    "§7Description: §f" + config.getString("pack_info.description", "N/A")
            ));
            infoItem.setItemMeta(infoMeta);
        }
        setItem(4, new DisplayItem(infoItem));

        // Enable/Disable Button
        boolean isEnabled = config.getBoolean("pack_info.enabled", false);
        ItemStack toggleButton = new ItemStack(isEnabled ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta toggleMeta = toggleButton.getItemMeta();
        if (toggleMeta != null) {
            toggleMeta.setDisplayName(isEnabled ? "§aEnabled" : "§cDisabled");
            toggleButton.setItemMeta(toggleMeta);
        }
        setItem(22, new Button(toggleButton, event -> {
            config.set("pack_info.enabled", !isEnabled);
            try {
                config.save(packFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            loadItems(); // Reload the GUI to reflect the change
        }));

        // Back Button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§cBack");
            backButton.setItemMeta(backMeta);
        }
        setItem(49, new Button(backButton, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new PacksEditorGui(plugin));
        }));
    }

    @Override
    public void open(Player player) {
        player.openInventory(inventory);
    }
}