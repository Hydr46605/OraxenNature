package it.hydr4.oraxennature.gui;

import it.hydr4.oraxennature.OraxenNature;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.YamlConfiguration;
import io.th0rgal.oraxen.api.OraxenItems;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class BlockPopulatorEditorGui extends PaginatedGui {

    public BlockPopulatorEditorGui(OraxenNature plugin) {
        super(plugin, 54, "§8Block Populator Editor", 45); // 45 items per page (5 rows)
    }

    @Override
    protected void loadAllItems() {
        allItems.clear();
        File blockPopulatorFile = new File(plugin.getDataFolder(), "block_populator.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(blockPopulatorFile);

        if (config.isConfigurationSection("blocks")) {
            Set<String> blockKeys = config.getConfigurationSection("blocks").getKeys(false);
            for (String key : blockKeys) {
                // Get the oraxen_id from the block's configuration
                String oraxenId = config.getString("blocks." + key + ".oraxen_id");
                ItemStack item;
                if (oraxenId != null && OraxenItems.getItemById(oraxenId) != null) {
                    item = OraxenItems.getItemById(oraxenId).build();
                } else {
                    // Default to stone if no Oraxen ID or Oraxen item not found
                    item = new ItemStack(Material.STONE);
                }
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§b" + key);
                    item.setItemMeta(meta);
                }
                allItems.add(new Button(item, event -> {
                    Player player = (Player) event.getWhoClicked();
                    plugin.getGuiManager().openGui(player, new BlockPopulatorDetailGui(plugin, key));
                }));
            }
        }
    }

    @Override
    protected void setupPaginationButtons() {
        super.setupPaginationButtons(); // Call super to retain existing pagination buttons

        // Back Button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§cBack");
            backButton.setItemMeta(backMeta);
        }
        setItem(inventory.getSize() - 9, new Button(backButton, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new EditorGui(plugin));
        }));

        // Create New Block Populator Button
        ItemStack createNewButton = new ItemStack(Material.ANVIL);
        ItemMeta createNewMeta = createNewButton.getItemMeta();
        if (createNewMeta != null) {
            createNewMeta.setDisplayName("§aCreate New Block Populator");
            createNewButton.setItemMeta(createNewMeta);
        }
        setItem(inventory.getSize() - 7, new Button(createNewButton, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new BlockPopulatorDetailGui(plugin, "__NEW__"));
        }));

        // Packs Editor Button
        ItemStack packsButton = new ItemStack(Material.CHEST);
        ItemMeta packsMeta = packsButton.getItemMeta();
        if (packsMeta != null) {
            packsMeta.setDisplayName("§6Packs Editor");
            packsButton.setItemMeta(packsMeta);
        }
        setItem(inventory.getSize() - 1, new Button(packsButton, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new PacksEditorGui(plugin));
        }));
    }
}