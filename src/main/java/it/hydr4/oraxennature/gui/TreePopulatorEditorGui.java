package it.hydr4.oraxennature.gui;

import it.hydr4.oraxennature.OraxenNature;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.YamlConfiguration;

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
                // TODO: Replace with an Oraxen block icon for better visual representation
                ItemStack item = new ItemStack(Material.STONE);
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
    }
}