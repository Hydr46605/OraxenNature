package it.hydr4.oraxennature.gui;

import it.hydr4.oraxennature.OraxenNature;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Set;

public class PacksEditorGui extends PaginatedGui {

    public PacksEditorGui(OraxenNature plugin) {
        super(plugin, 54, "§8Packs Editor", 45); // 45 items per page (5 rows)
    }

    @Override
    protected void loadAllItems() {
        allItems.clear();
        File packsFolder = new File(plugin.getDataFolder(), "packs");
        if (packsFolder.exists() && packsFolder.isDirectory()) {
            File[] packFiles = packsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (packFiles != null) {
                for (File packFile : packFiles) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(packFile);
                    String packName = config.getString("pack_info.name", packFile.getName());
                    ItemStack item = new ItemStack(Material.BOOK);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName("§b" + packName);
                        item.setItemMeta(meta);
                    }
                    allItems.add(new Button(item, event -> {
                        Player player = (Player) event.getWhoClicked();
                        plugin.getGuiManager().openGui(player, new PackDetailGui(plugin, packFile.getName()));
                    }));
                }
            }
        }
    }
}