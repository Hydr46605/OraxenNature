package it.hydr4.oraxennature.gui.impl.editors;

import it.hydr4.oraxennature.OraxenNature;
import it.hydr4.oraxennature.gui.base.Button;
import it.hydr4.oraxennature.gui.base.PaginatedGui;
import it.hydr4.oraxennature.gui.impl.details.BlockPopulatorDetailGui;
import it.hydr4.oraxennature.utils.TextUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.YamlConfiguration;
import io.th0rgal.oraxen.api.OraxenItems;

import java.io.File;
import java.util.Set;

public class BlockPopulatorEditorGui extends PaginatedGui {

    private final OraxenNature plugin;
    private final String packName;

    public BlockPopulatorEditorGui(OraxenNature plugin) {
        this(plugin, null);
    }

    public BlockPopulatorEditorGui(OraxenNature plugin, String packName) {
        super(54, TextUtils.parse("<gradient:#3498db:#2980b9><bold>Block Populator Editor</bold></gradient>" + (packName != null ? " <gray>(" + packName + ")" : "")), 36);
        this.plugin = plugin;
        this.packName = packName;
        loadItems();
        setupItems();
    }

    private void loadItems() {
        allItems.clear();
        YamlConfiguration config;

        if (packName != null) {
            File packFile = new File(plugin.getDataFolder(), "packs/" + packName);
            config = YamlConfiguration.loadConfiguration(packFile);
            if (config.isConfigurationSection("blocks")) {
                Set<String> blockKeys = config.getConfigurationSection("blocks").getKeys(false);
                for (String key : blockKeys) {
                    addBlockItem(config, key);
                }
            }
        } else {
            config = (YamlConfiguration) plugin.getBlockPopulatorConfig();
            if (config.isConfigurationSection("blocks")) {
                Set<String> blockKeys = config.getConfigurationSection("blocks").getKeys(false);
                for (String key : blockKeys) {
                    addBlockItem(config, key);
                }
            }
        }
    }

    private void addBlockItem(YamlConfiguration config, String key) {
        String oraxenId = config.getString("blocks." + key + ".oraxen_id");
        ItemStack item;
        if (oraxenId != null && OraxenItems.getItemById(oraxenId) != null) {
            item = OraxenItems.getItemById(oraxenId).build();
        } else {
            item = new ItemStack(Material.STONE);
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(TextUtils.parse("<aqua>" + key));
            item.setItemMeta(meta);
        }
        addGuiItem(new Button(item, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new BlockPopulatorDetailGui(plugin, key));
        }));
    }

    @Override
    public void setupItems() {
        super.setupItems();
        fillBorder();

        // Back Button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(TextUtils.parse("<red>Back"));
            backButton.setItemMeta(backMeta);
        }
        setItem(45, new Button(backButton, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new EditorGui(plugin));
        }));

        // Create New Button
        ItemStack createButton = new ItemStack(Material.ANVIL);
        ItemMeta createMeta = createButton.getItemMeta();
        if (createMeta != null) {
            createMeta.displayName(TextUtils.parse("<green>Create New Populator"));
            createButton.setItemMeta(createMeta);
        }
        setItem(49, new Button(createButton, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new BlockPopulatorDetailGui(plugin, "__NEW__"));
        }));
    }

    private void fillBorder() {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        if (meta != null) {
            meta.displayName(TextUtils.parse(" "));
            border.setItemMeta(meta);
        }
        it.hydr4.oraxennature.gui.base.DisplayItem displayItem = new it.hydr4.oraxennature.gui.base.DisplayItem(border);
        for (int i = 36; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                setItem(i, displayItem);
            }
        }
    }
}