package it.hydr4.oraxennature.gui;

import it.hydr4.oraxennature.OraxenNature;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import it.hydr4.oraxennature.Logger;

public class GrowthConfigDetailGui extends AbstractGui {

    private final OraxenNature plugin;
    private final String growthConfigKey;

    public GrowthConfigDetailGui(OraxenNature plugin, String growthConfigKey) {
        super(54, growthConfigKey.equals("__NEW__") ? "§8Create New Growth Config" : "§8Editing: " + growthConfigKey); // Increased size to 6 rows
        this.plugin = plugin;
        this.growthConfigKey = growthConfigKey;
        setupItems();
    }

    @Override
    protected void setupItems() {
        File growthConfigFile = new File(plugin.getDataFolder(), "growth_config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(growthConfigFile);

        if (growthConfigKey.equals("__NEW__")) {
            // Display input for new name
            ItemStack nameInput = new ItemStack(Material.PAPER);
            ItemMeta nameInputMeta = nameInput.getItemMeta();
            if (nameInputMeta != null) {
                nameInputMeta.setDisplayName("§aClick to enter new Growth Config Name");
                nameInput.setItemMeta(nameInputMeta);
            }
            setItem(22, new Button(nameInput, event -> {
                Player player = (Player) event.getWhoClicked();
                plugin.getGuiManager().closeGui(player);
                player.sendMessage("§eEnter the new Growth Config name:");
                ChatInputListener.awaitInput(player, newName -> {
                    if (config.isConfigurationSection("growth_configs." + newName)) {
                        player.sendMessage("§cError: A growth config with that name already exists!");
                        plugin.getGuiManager().openGui(player, new GrowthConfigEditorGui(plugin));
                        return;
                    }
                    // Create new section with default values
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
                        plugin.getLogger().severe("Could not save growth_config.yml: " + e.getMessage());
                        player.sendMessage("§cError saving configuration!");
                        plugin.getGuiManager().openGui(player, new GrowthConfigEditorGui(plugin));
                    }
                }, p -> plugin.getGuiManager().openGui(p, new GrowthConfigEditorGui(plugin)));
            }));

            // Back button for new creation
            ItemStack backButton = new ItemStack(Material.ARROW);
            ItemMeta backMeta = backButton.getItemMeta();
            if (backMeta != null) {
                backMeta.setDisplayName("§cBack");
                backButton.setItemMeta(backMeta);
            }
            setItem(49, new Button(backButton, event -> {
                Player player = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGui(player, new GrowthConfigEditorGui(plugin));
            }));

        } else {
            // Existing growth config logic
            // Display the name of the growth config being edited
            ItemStack nameItem = new ItemStack(Material.NAME_TAG);
            ItemMeta nameMeta = nameItem.getItemMeta();
            if (nameMeta != null) {
                nameMeta.setDisplayName("§fGrowth Config: §a" + growthConfigKey);
                nameItem.setItemMeta(nameMeta);
            }
            setItem(4, new DisplayItem(nameItem));

            ConfigurationSection configSection = config.getConfigurationSection("growth_configs." + growthConfigKey);
            if (configSection != null) {
                int slot = 9; // Start displaying properties from the second row
                for (String key : configSection.getKeys(false)) {
                    Object value = configSection.get(key);
                    ItemStack item = new ItemStack(Material.BOOK);
                    if (value instanceof Boolean) {
                        boolean currentValue = (Boolean) value;
                        item = new ItemStack(currentValue ? Material.LIME_WOOL : Material.RED_WOOL);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName("§b" + key + ": " + (currentValue ? "§aEnabled" : "§cDisabled"));
                            item.setItemMeta(meta);
                        }
                        setItem(slot, new Button(item, event -> {
                            // Toggle boolean value
                            boolean newValue = !currentValue;
                            config.set("growth_configs." + growthConfigKey + "." + key, newValue);
                            try {
                                config.save(growthConfigFile);
                                plugin.reloadGrowthConfig();
                                plugin.getGuiManager().openGui((Player) event.getWhoClicked(), new GrowthConfigDetailGui(plugin, growthConfigKey));
                            } catch (IOException e) {
                                Logger.error("Could not save growth_config.yml: " + e.getMessage());
                                ((Player) event.getWhoClicked()).sendMessage("§cError saving configuration: " + e.getMessage());
                            }
                        }));
                    } else if (value instanceof Number) {
                        item = new ItemStack(Material.KNOWLEDGE_BOOK);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName("§b" + key + ": §f" + value);
                            item.setItemMeta(meta);
                        }
                        setItem(slot, new Button(item, event -> {
                            Player player = (Player) event.getWhoClicked();
                            plugin.getGuiManager().closeGui(player);
                            player.sendMessage("§eEnter new value for " + key + ":");
                            ChatInputListener.awaitInput(player, newValue -> {
                                try {
                                    Number parsedValue;
                                    if (value instanceof Integer) {
                                        parsedValue = Integer.parseInt(newValue);
                                    } else if (value instanceof Double) {
                                        parsedValue = Double.parseDouble(newValue);
                                    } else if (value instanceof Float) {
                                        parsedValue = Float.parseFloat(newValue);
                                    } else if (value instanceof Long) {
                                        parsedValue = Long.parseLong(newValue);
                                    } else {
                                        player.sendMessage("§cUnsupported number type.");
                                        return;
                                    }
                                    config.set("growth_configs." + growthConfigKey + "." + key, parsedValue);
                                    config.save(growthConfigFile);
                                    plugin.reloadGrowthConfig();
                                    player.sendMessage("§aConfiguration saved successfully!");
                                    plugin.getGuiManager().openGui(player, new GrowthConfigDetailGui(plugin, growthConfigKey));
                                } catch (NumberFormatException e) {
                                    player.sendMessage("§cInvalid number format. Please enter a valid number.");
                                    plugin.getGuiManager().openGui(player, new GrowthConfigDetailGui(plugin, growthConfigKey));
                                } catch (IOException e) {
                                    Logger.error("Could not save growth_config.yml: " + e.getMessage());
                                    player.sendMessage("§cError saving configuration: " + e.getMessage());
                                    plugin.getGuiManager().openGui(player, new GrowthConfigDetailGui(plugin, growthConfigKey));
                                }
                            }, p -> plugin.getGuiManager().openGui(p, new GrowthConfigDetailGui(plugin, growthConfigKey)));
                        }));
                    } else if (value instanceof String) {
                        item = new ItemStack(Material.WRITABLE_BOOK);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName("§b" + key + ": §f" + value);
                            item.setItemMeta(meta);
                        }
                        setItem(slot, new Button(item, event -> {
                            Player player = (Player) event.getWhoClicked();
                            plugin.getGuiManager().closeGui(player);
                            player.sendMessage("§eEnter new value for " + key + ":");
                            ChatInputListener.awaitInput(player, newValue -> {
                                config.set("growth_configs." + growthConfigKey + "." + key, newValue);
                                try {
                                    config.save(growthConfigFile);
                                    plugin.reloadGrowthConfig();
                                    player.sendMessage("§aConfiguration saved successfully!");
                                    plugin.getGuiManager().openGui(player, new GrowthConfigDetailGui(plugin, growthConfigKey));
                                } catch (IOException e) {
                                    Logger.error("Could not save growth_config.yml: " + e.getMessage());
                                    player.sendMessage("§cError saving configuration: " + e.getMessage());
                                    plugin.getGuiManager().openGui(player, new GrowthConfigDetailGui(plugin, growthConfigKey));
                                }
                            }, p -> plugin.getGuiManager().openGui(p, new GrowthConfigDetailGui(plugin, growthConfigKey)));
                        }));
                    } else {
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName("§b" + key + ": §f" + value);
                            item.setItemMeta(meta);
                        }
                        setItem(slot, new DisplayItem(item));
                    }
                    slot++;
                }
            }

            // Back button
            ItemStack backButton = new ItemStack(Material.ARROW);
            ItemMeta backMeta = backButton.getItemMeta();
            if (backMeta != null) {
                backMeta.setDisplayName("§cBack");
                backButton.setItemMeta(backMeta);
            }
            setItem(49, new Button(backButton, event -> { // Placed at the bottom center
                Player player = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGui(player, new GrowthConfigEditorGui(plugin));
            }));

            // Delete button
            ItemStack deleteButton = new ItemStack(Material.BARRIER);
            ItemMeta deleteMeta = deleteButton.getItemMeta();
            if (deleteMeta != null) {
                deleteMeta.setDisplayName("§cDelete Growth Config");
                deleteButton.setItemMeta(deleteMeta);
            }
            setItem(53, new Button(deleteButton, event -> {
                Player player = (Player) event.getWhoClicked();
                config.set("growth_configs." + growthConfigKey, null); // Remove the section
                try {
                    config.save(growthConfigFile);
                    plugin.reloadGrowthConfig();
                    player.sendMessage("§aGrowth Config '" + growthConfigKey + "' deleted successfully!");
                    plugin.getGuiManager().openGui(player, new GrowthConfigEditorGui(plugin));
                } catch (IOException e) {
                    Logger.error("Could not save growth_config.yml: " + e.getMessage());
                    player.sendMessage("§cError deleting configuration: " + e.getMessage());
                }
            }));
        }
    }
}
