package it.hydr4.oraxennature.gui;

import it.hydr4.oraxennature.OraxenNature;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.YamlConfiguration;
import io.th0rgal.oraxen.api.OraxenItems;

import java.io.File;
import java.util.Set;

public class TreePopulatorEditorGui extends PaginatedGui {

    public TreePopulatorEditorGui(OraxenNature plugin) {
        super(plugin, 54, "§8Tree Populator Editor", 45); // 45 items per page (5 rows)
    }

    @Override
    protected void loadAllItems() {
        allItems.clear();
        File treePopulatorFile = new File(plugin.getDataFolder(), "tree_populator.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(treePopulatorFile);

        if (config.isConfigurationSection("trees")) {
            Set<String> treeKeys = config.getConfigurationSection("trees").getKeys(false);
            for (String key : treeKeys) {
                // Get the log_oraxen_id from the tree's configuration
                String logOraxenId = config.getString("trees." + key + ".log_oraxen_id");
                ItemStack item;
                if (logOraxenId != null && OraxenItems.getItemById(logOraxenId) != null) {
                    item = OraxenItems.getItemById(logOraxenId).build();
                } else {
                    // Default to oak log if no Oraxen ID or Oraxen item not found
                    item = new ItemStack(Material.OAK_LOG);
                }
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§b" + key);
                    item.setItemMeta(meta);
                }
                allItems.add(new Button(item, event -> {
                    Player player = (Player) event.getWhoClicked();
                    plugin.getGuiManager().openGui(player, new TreePopulatorDetailGui(plugin, key));
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

        // Create New Tree Populator Button
        ItemStack createNewButton = new ItemStack(Material.ANVIL);
        ItemMeta createNewMeta = createNewButton.getItemMeta();
        if (createNewMeta != null) {
            createNewMeta.setDisplayName("§aCreate New Tree Populator");
            createNewButton.setItemMeta(createNewMeta);
        }
        setItem(inventory.getSize() - 7, new Button(createNewButton, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new TreePopulatorDetailGui(plugin, "__NEW__"));
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