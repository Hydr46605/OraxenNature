package it.hydr4.oraxennature.gui;

import it.hydr4.oraxennature.OraxenNature;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EditorGui extends AbstractGui {

    private final OraxenNature plugin;

    public EditorGui(OraxenNature plugin) {
        super(27, "§8OraxenNature Editor"); // 27 slots (3 rows) for simplicity
        this.plugin = plugin;
        setupItems();
    }

    @Override
    protected void setupItems() {
        // Block Populator Button
        ItemStack blockPopulatorItem = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta blockPopulatorMeta = blockPopulatorItem.getItemMeta();
        if (blockPopulatorMeta != null) {
            blockPopulatorMeta.setDisplayName("§aBlock Populator");
            blockPopulatorItem.setItemMeta(blockPopulatorMeta);
        }
        setItem(10, new Button(blockPopulatorItem, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new BlockPopulatorEditorGui(plugin));
        }));

        // Tree Populator Button
        ItemStack treePopulatorItem = new ItemStack(Material.OAK_SAPLING);
        ItemMeta treePopulatorMeta = treePopulatorItem.getItemMeta();
        if (treePopulatorMeta != null) {
            treePopulatorMeta.setDisplayName("§aTree Populator");
            treePopulatorItem.setItemMeta(treePopulatorMeta);
        }
        setItem(13, new Button(treePopulatorItem, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new TreePopulatorEditorGui(plugin));
        }));

        // Growth Config Button
        ItemStack growthConfigItem = new ItemStack(Material.BONE_MEAL);
        ItemMeta growthConfigMeta = growthConfigItem.getItemMeta();
        if (growthConfigMeta != null) {
            growthConfigMeta.setDisplayName("§aGrowth Config");
            growthConfigItem.setItemMeta(growthConfigMeta);
        }
        setItem(16, new Button(growthConfigItem, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new GrowthConfigEditorGui(plugin));
        }));
    }
}