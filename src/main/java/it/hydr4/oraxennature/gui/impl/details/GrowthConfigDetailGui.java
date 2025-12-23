package it.hydr4.oraxennature.gui.impl.details;

import it.hydr4.oraxennature.OraxenNature;
import it.hydr4.oraxennature.gui.base.AbstractGui;
import it.hydr4.oraxennature.gui.base.Button;
import it.hydr4.oraxennature.gui.base.DisplayItem;
import it.hydr4.oraxennature.gui.impl.editors.GrowthConfigEditorGui;
import it.hydr4.oraxennature.gui.manager.ChatInputListener;
import it.hydr4.oraxennature.utils.TextUtils;
import it.hydr4.oraxennature.utils.Logger;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class GrowthConfigDetailGui extends AbstractGui {

    private final OraxenNature plugin;
    private final String growthConfigKey;

    public GrowthConfigDetailGui(OraxenNature plugin, String growthConfigKey) {
        super(54, growthConfigKey.equals("__NEW__") ? TextUtils.parse("<gradient:#2ecc71:#27ae60><bold>Create Growth Config</bold></gradient>") : TextUtils.parse("<gradient:#3498db:#2980b9><bold>Growth Property Editor</bold></gradient> <gray>(" + growthConfigKey + ")"));
        this.plugin = plugin;
        this.growthConfigKey = growthConfigKey;
        setupItems();
    }

    @Override
    public void setupItems() {
        fillBorder();
        File growthConfigFile = new File(plugin.getDataFolder(), "growth_config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(growthConfigFile);

        if (growthConfigKey.equals("__NEW__")) {
            ItemStack nameInput = new ItemStack(Material.PAPER);
            ItemMeta nameInputMeta = nameInput.getItemMeta();
            if (nameInputMeta != null) {
                nameInputMeta.displayName(TextUtils.parse("<green>Click to enter name"));
                nameInput.setItemMeta(nameInputMeta);
            }
            setItem(22, new Button(nameInput, event -> {
                Player player = (Player) event.getWhoClicked();
                plugin.getGuiManager().closeGui(player);
                player.sendMessage(TextUtils.parse("<yellow>Enter the new Growth Config name:"));
                ChatInputListener.awaitInput(player, newName -> {
                    if (config.isConfigurationSection("growth_configs." + newName)) {
                        player.sendMessage(TextUtils.parse("<red>Error: A growth config with that name already exists!"));
                        plugin.getGuiManager().openGui(player, new GrowthConfigEditorGui(plugin));
                        return;
                    }
                    config.createSection("growth_configs." + newName);
                    config.set("growth_configs." + newName + ".enabled", false);
                    config.set("growth_configs." + newName + ".grow-interval", 20);
                    config.set("growth_configs." + newName + ".max-stage", 3);
                    config.set("growth_configs." + newName + ".block-type", "DIRT");
                    try {
                        config.save(growthConfigFile);
                        plugin.reloadGrowthConfig();
                        plugin.getGuiManager().openGui(player, new GrowthConfigDetailGui(plugin, newName));
                    } catch (IOException e) {
                        Logger.error("Could not save growth_config.yml: " + e.getMessage());
                        player.sendMessage(TextUtils.parse("<red>Error saving configuration!"));
                        plugin.getGuiManager().openGui(player, new GrowthConfigEditorGui(plugin));
                    }
                }, p -> plugin.getGuiManager().openGui(p, new GrowthConfigEditorGui(plugin)));
            }));
        } else {
            ConfigurationSection configSection = config.getConfigurationSection("growth_configs." + growthConfigKey);
            if (configSection != null) {
                ItemStack icon = new ItemStack(Material.BONE_MEAL);
                setItem(4, new DisplayItem(icon));

                int slot = 19;
                for (String key : configSection.getKeys(false)) {
                    if (slot > 43) break;
                    if (slot % 9 == 0 || slot % 9 == 8) slot++;

                    Object value = configSection.get(key);
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
                            config.set("growth_configs." + growthConfigKey + "." + key, !currentValue);
                            saveAndReload(config, growthConfigFile, (Player) event.getWhoClicked());
                        }));
                    } else {
                        item = new ItemStack(Material.BOOK);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.displayName(TextUtils.parse("<aqua>" + key + ": <white>" + value));
                            item.setItemMeta(meta);
                        }
                        setItem(slot, new Button(item, event -> handleInput(key, value, config, growthConfigFile, (Player) event.getWhoClicked())));
                    }
                    slot++;
                    if (slot % 9 == 8) slot += 2;
                }
            }

            ItemStack deleteButton = new ItemStack(Material.BARRIER);
            ItemMeta deleteMeta = deleteButton.getItemMeta();
            if (deleteMeta != null) {
                deleteMeta.displayName(TextUtils.parse("<red>Delete Config"));
                deleteButton.setItemMeta(deleteMeta);
            }
            setItem(53, new Button(deleteButton, event -> {
                config.set("growth_configs." + growthConfigKey, null);
                saveAndReload(config, growthConfigFile, (Player) event.getWhoClicked());
            }));
        }

        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(TextUtils.parse("<red>Back"));
            backButton.setItemMeta(backMeta);
        }
        setItem(45, new Button(backButton, event -> {
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new GrowthConfigEditorGui(plugin));
        }));
    }

    private void handleInput(String key, Object value, YamlConfiguration config, File file, Player player) {
        plugin.getGuiManager().closeGui(player);
        player.sendMessage(TextUtils.parse("<yellow>Enter new value for <aqua>" + key + "<yellow>:"));
        ChatInputListener.awaitInput(player, newValue -> {
            try {
                if (value instanceof Number) {
                    if (value instanceof Integer) config.set("growth_configs." + growthConfigKey + "." + key, Integer.parseInt(newValue));
                    else if (value instanceof Double) config.set("growth_configs." + growthConfigKey + "." + key, Double.parseDouble(newValue));
                } else {
                    config.set("growth_configs." + growthConfigKey + "." + key, newValue);
                }
                saveAndReload(config, file, player);
            } catch (Exception e) {
                player.sendMessage(TextUtils.parse("<red>Invalid format!"));
                plugin.getGuiManager().openGui(player, new GrowthConfigDetailGui(plugin, growthConfigKey));
            }
        }, p -> plugin.getGuiManager().openGui(p, new GrowthConfigDetailGui(plugin, growthConfigKey)));
    }

    private void saveAndReload(YamlConfiguration config, File file, Player player) {
        try {
            config.save(file);
            plugin.reloadGrowthConfig();
            plugin.getGuiManager().openGui(player, new GrowthConfigDetailGui(plugin, growthConfigKey));
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
