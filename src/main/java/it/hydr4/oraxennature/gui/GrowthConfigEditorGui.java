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

    private final String packName;

    public GrowthConfigEditorGui(OraxenNature plugin) {
        this(plugin, null);
    }

    public GrowthConfigEditorGui(OraxenNature plugin, String packName) {
        super(plugin, 54, "§8Growth Config Editor" + (packName != null ? " for " + packName : ""), 45); // 45 items per page (5 rows)
        this.packName = packName;
    }

    @Override
    protected void loadAllItems() {
        allItems.clear();
        YamlConfiguration config;

        if (packName != null) {
            File packFile = new File(plugin.getDataFolder(), "packs/" + packName);
            config = YamlConfiguration.loadConfiguration(packFile);
            if (config.isConfigurationSection("growth_configs")) {
                Set<String> growthConfigKeys = config.getConfigurationSection("growth_configs").getKeys(false);
                for (String key : growthConfigKeys) {
                    ItemStack item = new ItemStack(Material.BONE_MEAL); // Placeholder
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
        } else {
            File growthConfigFile = new File(plugin.getDataFolder(), "growth_config.yml");
            config = YamlConfiguration.loadConfiguration(growthConfigFile);

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
