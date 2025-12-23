package it.hydr4.oraxennature.gui.impl.editors;

import it.hydr4.oraxennature.OraxenNature;
import it.hydr4.oraxennature.gui.base.Button;
import it.hydr4.oraxennature.gui.base.PaginatedGui;
import it.hydr4.oraxennature.gui.impl.details.PackDetailGui;
import it.hydr4.oraxennature.utils.TextUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class PacksEditorGui extends PaginatedGui {

    private final OraxenNature plugin;

    public PacksEditorGui(OraxenNature plugin) {
        super(54, TextUtils.parse("<gradient:#f1c40f:#f39c12><bold>Packs Editor</bold></gradient>"), 36);
        this.plugin = plugin;
        loadItems();
        setupItems();
    }

    private void loadItems() {
        allItems.clear();
        File packsFolder = new File(plugin.getDataFolder(), "packs");
        if (packsFolder.exists() && packsFolder.isDirectory()) {
            File[] packFiles = packsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (packFiles != null) {
                for (File packFile : packFiles) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(packFile);
                    String packName = config.getString("pack_info.name", packFile.getName());
                    ItemStack item = new ItemStack(Material.CHEST);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.displayName(TextUtils.parse("<gold>" + packName));
                        item.setItemMeta(meta);
                    }
                    addGuiItem(new Button(item, event -> {
                        Player player = (Player) event.getWhoClicked();
                        plugin.getGuiManager().openGui(player, new PackDetailGui(plugin, packFile.getName()));
                    }));
                }
            }
        }
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