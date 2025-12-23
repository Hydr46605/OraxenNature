package it.hydr4.oraxennature.gui.impl.details;

import it.hydr4.oraxennature.OraxenNature;
import it.hydr4.oraxennature.gui.base.AbstractGui;
import it.hydr4.oraxennature.gui.base.Button;
import it.hydr4.oraxennature.gui.base.DisplayItem;
import it.hydr4.oraxennature.gui.impl.editors.BlockPopulatorEditorGui;
import it.hydr4.oraxennature.gui.manager.ChatInputListener;
import it.hydr4.oraxennature.utils.TextUtils;
import it.hydr4.oraxennature.utils.Logger;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import io.th0rgal.oraxen.api.OraxenItems;

import java.io.File;
import java.io.IOException;

public class BlockPopulatorDetailGui extends AbstractGui {

    private final OraxenNature plugin;
    private final String blockPopulatorKey;

    public BlockPopulatorDetailGui(OraxenNature plugin, String blockPopulatorKey) {
        super(54, blockPopulatorKey.equals("__NEW__") ? TextUtils.parse("<gradient:#2ecc71:#27ae60><bold>Create Block Populator</bold></gradient>") : TextUtils.parse("<gradient:#3498db:#2980b9><bold>Block Property Editor</bold></gradient> <gray>(" + blockPopulatorKey + ")"));
        this.plugin = plugin;
        this.blockPopulatorKey = blockPopulatorKey;
        setupItems();
    }

    @Override
    public void setupItems() {
        fillBorder();
        File blockPopulatorFile = new File(plugin.getDataFolder(), "block_populator.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(blockPopulatorFile);

        if (blockPopulatorKey.equals("__NEW__")) {
            ItemStack nameInput = new ItemStack(Material.PAPER);
            ItemMeta nameInputMeta = nameInput.getItemMeta();
            if (nameInputMeta != null) {
                nameInputMeta.displayName(TextUtils.parse("<green>Click to enter name"));
                nameInput.setItemMeta(nameInputMeta);
            }
            setItem(22, new Button(nameInput, event -> {
                Player player = (Player) event.getWhoClicked();
                plugin.getGuiManager().closeGui(player);
                player.sendMessage(TextUtils.parse("<yellow>Enter the new Block Populator name:"));
                ChatInputListener.awaitInput(player, newName -> {
                    if (config.isConfigurationSection("blocks." + newName)) {
                        player.sendMessage(TextUtils.parse("<red>Error: A block populator with that name already exists!"));
                        plugin.getGuiManager().openGui(player, new BlockPopulatorEditorGui(plugin));
                        return;
                    }
                    config.createSection("blocks." + newName);
                    config.set("blocks." + newName + ".enabled", false);
                    config.set("blocks." + newName + ".min-y", 0);
                    config.set("blocks." + newName + ".max-y", 64);
                    config.set("blocks." + newName + ".chance", 100);
                    try {
                        config.save(blockPopulatorFile);
                        plugin.reloadBlockPopulatorConfig();
                        plugin.getGuiManager().openGui(player, new BlockPopulatorDetailGui(plugin, newName));
                    } catch (IOException e) {
                        Logger.error("Could not save block_populator.yml: " + e.getMessage());
                        player.sendMessage(TextUtils.parse("<red>Error saving configuration!"));
                        plugin.getGuiManager().openGui(player, new BlockPopulatorEditorGui(plugin));
                    }
                }, p -> plugin.getGuiManager().openGui(p, new BlockPopulatorEditorGui(plugin)));
            }));
        } else {
            ConfigurationSection blockSection = config.getConfigurationSection("blocks." + blockPopulatorKey);
            if (blockSection != null) {
                String oraxenId = blockSection.getString("oraxen_id");
                ItemStack icon = (oraxenId != null && OraxenItems.getItemById(oraxenId) != null) ? OraxenItems.getItemById(oraxenId).build() : new ItemStack(Material.STONE);
                setItem(4, new DisplayItem(icon));

                int slot = 19;
                for (String key : blockSection.getKeys(false)) {
                    if (slot > 43) break;
                    if (slot % 9 == 0 || slot % 9 == 8) slot++; 

                    Object value = blockSection.get(key);
                    ItemStack item;
                    if (value instanceof Boolean) {
                        boolean currentValue = (Boolean) value;
                        item = new ItemStack(currentValue ? Material.LIME_DYE : Material.GRAY_DYE);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.displayName(TextUtils.parse("<aqua>" + key + ": " + (currentValue ? "<green>Enabled" : "<red>Disabled")));
                            item.setItemMeta(meta);
                        }
                        setItem(slot, new Button(item, event -> {
                            config.set("blocks." + blockPopulatorKey + "." + key, !currentValue);
                            saveAndReload(config, blockPopulatorFile, (Player) event.getWhoClicked());
                        }));
                    } else {
                        item = new ItemStack(Material.BOOK);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.displayName(TextUtils.parse("<aqua>" + key + ": <white>" + value));
                            item.setItemMeta(meta);
                        }
                        setItem(slot, new Button(item, event -> handleInput(key, value, config, blockPopulatorFile, (Player) event.getWhoClicked())));
                    }
                    slot++;
                    if (slot % 9 == 8) slot += 2;
                }
            }

            // Delete button
            ItemStack deleteButton = new ItemStack(Material.BARRIER);
            ItemMeta deleteMeta = deleteButton.getItemMeta();
            if (deleteMeta != null) {
                deleteMeta.displayName(TextUtils.parse("<red>Delete Populator"));
                deleteButton.setItemMeta(deleteMeta);
            }
            setItem(53, new Button(deleteButton, event -> {
                config.set("blocks." + blockPopulatorKey, null);
                saveAndReload(config, blockPopulatorFile, (Player) event.getWhoClicked());
            }));
        }

        // Back button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(TextUtils.parse("<red>Back"));
            backButton.setItemMeta(backMeta);
        }
        setItem(45, new Button(backButton, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new BlockPopulatorEditorGui(plugin));
        }));
    }

    private void handleInput(String key, Object value, YamlConfiguration config, File file, Player player) {
        plugin.getGuiManager().closeGui(player);
        player.sendMessage(TextUtils.parse("<yellow>Enter new value for <aqua>" + key + "<yellow>:"));
        ChatInputListener.awaitInput(player, newValue -> {
            try {
                if (value instanceof Number) {
                    if (value instanceof Integer) config.set("blocks." + blockPopulatorKey + "." + key, Integer.parseInt(newValue));
                    else if (value instanceof Double) config.set("blocks." + blockPopulatorKey + "." + key, Double.parseDouble(newValue));
                } else {
                    config.set("blocks." + blockPopulatorKey + "." + key, newValue);
                }
                saveAndReload(config, file, player);
            } catch (Exception e) {
                player.sendMessage(TextUtils.parse("<red>Invalid format!"));
                plugin.getGuiManager().openGui(player, new BlockPopulatorDetailGui(plugin, blockPopulatorKey));
            }
        }, p -> plugin.getGuiManager().openGui(p, new BlockPopulatorDetailGui(plugin, blockPopulatorKey)));
    }

    private void saveAndReload(YamlConfiguration config, File file, Player player) {
        try {
            config.save(file);
            plugin.reloadBlockPopulatorConfig();
            plugin.getGuiManager().openGui(player, new BlockPopulatorDetailGui(plugin, blockPopulatorKey));
        } catch (IOException e) {
            Logger.error("Error saving config: " + e.getMessage());
        }
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
