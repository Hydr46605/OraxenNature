package it.hydr4.oraxennature.gui.impl.details;

import it.hydr4.oraxennature.OraxenNature;
import it.hydr4.oraxennature.gui.base.AbstractGui;
import it.hydr4.oraxennature.gui.base.Button;
import it.hydr4.oraxennature.gui.base.DisplayItem;
import it.hydr4.oraxennature.gui.impl.editors.BlockPopulatorEditorGui;
import it.hydr4.oraxennature.gui.impl.editors.GrowthConfigEditorGui;
import it.hydr4.oraxennature.gui.impl.editors.PacksEditorGui;
import it.hydr4.oraxennature.gui.impl.editors.TreePopulatorEditorGui;
import it.hydr4.oraxennature.utils.TextUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.kyori.adventure.text.Component;

public class PackDetailGui extends AbstractGui {

    private final String packName;
    private final OraxenNature plugin;

    public PackDetailGui(OraxenNature plugin, String packName) {
        super(54, TextUtils.parse("<gradient:#3498db:#2980b9><bold>Pack Settings</bold></gradient> <gray>(" + packName + ")"));
        this.plugin = plugin;
        this.packName = packName;
        setupItems();
    }

    @Override
    public void setupItems() {
        fillBorder();
        File packFile = new File(plugin.getDataFolder(), "packs/" + packName);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(packFile);

        // Pack Info Item
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(TextUtils.parse("<aqua><bold>" + config.getString("pack_info.name", packName)));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtils.parse("<gray>Version: <white>" + config.getString("pack_info.version", "N/A")));
            lore.add(TextUtils.parse("<gray>Author: <white>" + config.getString("pack_info.author", "N/A")));
            lore.add(TextUtils.parse("<gray>Description: <white>" + config.getString("pack_info.description", "N/A")));
            infoMeta.lore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        setItem(4, new DisplayItem(infoItem));

        // Enable/Disable Button
        boolean isEnabled = config.getBoolean("pack_info.enabled", false);
        ItemStack toggleButton = new ItemStack(isEnabled ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta toggleMeta = toggleButton.getItemMeta();
        if (toggleMeta != null) {
            toggleMeta.displayName(isEnabled ? TextUtils.parse("<green><bold>Enabled") : TextUtils.parse("<red><bold>Disabled"));
            toggleButton.setItemMeta(toggleMeta);
        }
        setItem(22, new Button(toggleButton, event -> {
            config.set("pack_info.enabled", !isEnabled);
            try {
                config.save(packFile);
                plugin.getGuiManager().openGui((Player) event.getWhoClicked(), new PackDetailGui(plugin, packName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        // Block Populator Button
        ItemStack blockPopulatorItem = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta blockPopulatorMeta = blockPopulatorItem.getItemMeta();
        if (blockPopulatorMeta != null) {
            blockPopulatorMeta.displayName(TextUtils.parse("<green>Edit Block Populators"));
            blockPopulatorItem.setItemMeta(blockPopulatorMeta);
        }
        setItem(20, new Button(blockPopulatorItem, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new BlockPopulatorEditorGui(plugin, packName));
        }));

        // Tree Populator Button
        ItemStack treePopulatorItem = new ItemStack(Material.OAK_SAPLING);
        ItemMeta treePopulatorMeta = treePopulatorItem.getItemMeta();
        if (treePopulatorMeta != null) {
            treePopulatorMeta.displayName(TextUtils.parse("<green>Edit Tree Populators"));
            treePopulatorItem.setItemMeta(treePopulatorMeta);
        }
        setItem(24, new Button(treePopulatorItem, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new TreePopulatorEditorGui(plugin, packName));
        }));

        // Growth Config Button
        ItemStack growthConfigItem = new ItemStack(Material.BONE_MEAL);
        ItemMeta growthConfigMeta = growthConfigItem.getItemMeta();
        if (growthConfigMeta != null) {
            growthConfigMeta.displayName(TextUtils.parse("<green>Edit Growth Configs"));
            growthConfigItem.setItemMeta(growthConfigMeta);
        }
        setItem(31, new Button(growthConfigItem, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new GrowthConfigEditorGui(plugin, packName));
        }));

        // Reload Plugin Button
        ItemStack reloadItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta reloadMeta = reloadItem.getItemMeta();
        if (reloadMeta != null) {
            reloadMeta.displayName(TextUtils.parse("<yellow><bold>Reload Plugin"));
            reloadItem.setItemMeta(reloadMeta);
        }
        setItem(33, new Button(reloadItem, event -> {
            plugin.reloadConfig();
            ((Player) event.getWhoClicked()).sendMessage(TextUtils.parse("<green>Plugin reloaded successfully!"));
        }));

        // Back Button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(TextUtils.parse("<red>Back"));
            backButton.setItemMeta(backMeta);
        }
        setItem(45, new Button(backButton, event -> {
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
        DisplayItem displayItem = new DisplayItem(border);
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                setItem(i, displayItem);
            }
        }
    }
}