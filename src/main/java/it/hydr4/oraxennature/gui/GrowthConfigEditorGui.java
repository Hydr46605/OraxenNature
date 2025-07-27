package it.hydr4.oraxennature.gui;

import it.hydr4.oraxennature.OraxenNature;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Set;

public class GrowthConfigEditorGui extends PaginatedGui {

    public GrowthConfigEditorGui(OraxenNature plugin) {
        super(plugin, 54, "§8Growth Config Editor", 45); // 45 items per page (5 rows)
    }

    @Override
    protected void loadAllItems() {
        allItems.clear();
        File growthConfigFile = new File(plugin.getDataFolder(), "growth_config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(growthConfigFile);

        if (config.isConfigurationSection("growth_configs")) {
            Set<String> configKeys = config.getConfigurationSection("growth_configs").getKeys(false);
            for (String key : configKeys) {
                // TODO: Replace with an Oraxen block icon for better visual representation
                ItemStack item = new ItemStack(Material.STONE);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§b" + key);
                    item.setItemMeta(meta);
                }
                allItems.add(new Button(item, event -> {
                    Player player = (Player) event.getWhoClicked();
                    plugin.getGuiManager().openGui(player, new GrowthConfigDetailGui(plugin, key));
                }));
            }
        }
    }

    @Override
    protected void setupPaginationButtons() {
        super.setupPaginationButtons(); // Call super to retain existing pagination buttons

        // Create New Growth Config Button
        ItemStack createNewButton = new ItemStack(Material.ANVIL);
        ItemMeta createNewMeta = createNewButton.getItemMeta();
        if (createNewMeta != null) {
            createNewMeta.setDisplayName("§aCreate New Growth Config");
            createNewButton.setItemMeta(createNewMeta);
        }
        setItem(inventory.getSize() - 7, new Button(createNewButton, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new GrowthConfigDetailGui(plugin, "__NEW__"));
        }));
    }
}
