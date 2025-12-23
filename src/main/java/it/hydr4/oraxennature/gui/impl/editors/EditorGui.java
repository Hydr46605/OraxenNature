package it.hydr4.oraxennature.gui.impl.editors;

import it.hydr4.oraxennature.OraxenNature;
import it.hydr4.oraxennature.gui.base.AbstractGui;
import it.hydr4.oraxennature.gui.base.Button;
import it.hydr4.oraxennature.gui.impl.details.PackDetailGui;
import it.hydr4.oraxennature.utils.TextUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EditorGui extends AbstractGui {

    private final OraxenNature plugin;

    public EditorGui(OraxenNature plugin) {
        super(45, TextUtils.parse("<gradient:#2ecc71:#27ae60><bold>OraxenNature Editor</bold></gradient>"));
        this.plugin = plugin;
        setupItems();
    }

    @Override
    public void setupItems() {
        fillBorder();

        // Block Populator Button
        ItemStack blockPopulatorItem = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta blockPopulatorMeta = blockPopulatorItem.getItemMeta();
        if (blockPopulatorMeta != null) {
            blockPopulatorMeta.displayName(TextUtils.parse("<green>Block Populator"));
            blockPopulatorItem.setItemMeta(blockPopulatorMeta);
        }
        setItem(20, new Button(blockPopulatorItem, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new BlockPopulatorEditorGui(plugin));
        }));

        // Tree Populator Button
        ItemStack treePopulatorItem = new ItemStack(Material.OAK_SAPLING);
        ItemMeta treePopulatorMeta = treePopulatorItem.getItemMeta();
        if (treePopulatorMeta != null) {
            treePopulatorMeta.displayName(TextUtils.parse("<green>Tree Populator"));
            treePopulatorItem.setItemMeta(treePopulatorMeta);
        }
        setItem(22, new Button(treePopulatorItem, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new TreePopulatorEditorGui(plugin));
        }));

        // Growth Config Button
        ItemStack growthConfigItem = new ItemStack(Material.BONE_MEAL);
        ItemMeta growthConfigMeta = growthConfigItem.getItemMeta();
        if (growthConfigMeta != null) {
            growthConfigMeta.displayName(TextUtils.parse("<green>Growth Config"));
            growthConfigItem.setItemMeta(growthConfigMeta);
        }
        setItem(24, new Button(growthConfigItem, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new GrowthConfigEditorGui(plugin));
        }));

        // Packs Editor Button
        ItemStack packsEditorItem = new ItemStack(Material.CHEST);
        ItemMeta packsEditorMeta = packsEditorItem.getItemMeta();
        if (packsEditorMeta != null) {
            packsEditorMeta.displayName(TextUtils.parse("<gold>Packs Editor"));
            packsEditorItem.setItemMeta(packsEditorMeta);
        }
        setItem(40, new Button(packsEditorItem, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new PacksEditorGui(plugin));
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
        for (int i = 0; i < 45; i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                setItem(i, displayItem);
            }
        }
    }
}