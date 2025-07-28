package it.hydr4.oraxennature.gui;

import it.hydr4.oraxennature.OraxenNature;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import io.th0rgal.oraxen.api.OraxenItems;

import java.io.File;
import java.io.IOException;
import it.hydr4.oraxennature.utils.Logger;

public class TreePopulatorDetailGui extends AbstractGui {

    private final OraxenNature plugin;
    private final String treePopulatorKey;

    public TreePopulatorDetailGui(OraxenNature plugin, String treePopulatorKey) {
        super(54, treePopulatorKey.equals("__NEW__") ? "§8Create New Tree Populator" : "§8Editing: " + treePopulatorKey); // Increased size to 6 rows
        this.plugin = plugin;
        this.treePopulatorKey = treePopulatorKey;
        setupItems();
    }

    @Override
    public void setupItems() {
        File treePopulatorFile = new File(plugin.getDataFolder(), "tree_populator.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(treePopulatorFile);

        if (treePopulatorKey.equals("__NEW__")) {
            // Display input for new name
            ItemStack nameInput = new ItemStack(Material.PAPER);
            ItemMeta nameInputMeta = nameInput.getItemMeta();
            if (nameInputMeta != null) {
                nameInputMeta.setDisplayName("§aClick to enter new Tree Populator Name");
                nameInput.setItemMeta(nameInputMeta);
            }
            setItem(22, new Button(nameInput, event -> {
                Player player = (Player) event.getWhoClicked();
                plugin.getGuiManager().closeGui(player);
                player.sendMessage("§eEnter the new Tree Populator name:");
                ChatInputListener.awaitInput(player, newName -> {
                    if (config.isConfigurationSection("trees." + newName)) {
                        player.sendMessage("§cError: A tree populator with that name already exists!");
                        plugin.getGuiManager().openGui(player, new TreePopulatorEditorGui(plugin));
                        return;
                    }
                    // Create new section with default values
                    config.createSection("trees." + newName);
                    config.set("trees." + newName + ".enabled", false);
                    config.set("trees." + newName + ".log_oraxen_id", "null");
                    config.set("trees." + newName + ".leaf_oraxen_id", "null");
                    config.set("trees." + newName + ".min-y", 0);
                    config.set("trees." + newName + ".max-y", 64);
                    config.set("trees." + newName + ".chance", 100);
                    try {
                        config.save(treePopulatorFile);
                        plugin.reloadTreePopulatorConfig();
                        plugin.getGuiManager().openGui(player, new TreePopulatorDetailGui(plugin, newName));
                    } catch (IOException e) {
                        plugin.getLogger().severe("Could not save tree_populator.yml: " + e.getMessage());
                        player.sendMessage("§cError saving configuration!");
                        plugin.getGuiManager().openGui(player, new TreePopulatorEditorGui(plugin));
                    }
                }, p -> plugin.getGuiManager().openGui(p, new TreePopulatorEditorGui(plugin)));
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
                plugin.getGuiManager().openGui(player, new TreePopulatorEditorGui(plugin));
            }));

        } else {
            // Existing tree populator logic
            // Display the name of the tree populator being edited
            ItemStack nameItem = new ItemStack(Material.NAME_TAG);
            ItemMeta nameMeta = nameItem.getItemMeta();
            if (nameMeta != null) {
                nameMeta.setDisplayName("§fTree Populator: §a" + treePopulatorKey);
                nameItem.setItemMeta(nameMeta);
            }
            setItem(4, new DisplayItem(nameItem));

            ConfigurationSection treeSection = config.getConfigurationSection("trees." + treePopulatorKey);
            if (treeSection != null) {
                // Display the Oraxen log icon if log_oraxen_id is set
                String logOraxenId = treeSection.getString("log_oraxen_id");
                if (logOraxenId != null && OraxenItems.getItemById(logOraxenId) != null) {
                    setItem(0, new DisplayItem(OraxenItems.getItemById(logOraxenId).build()));
                } else {
                    // Default to oak log if no Oraxen ID or Oraxen item not found
                    setItem(0, new DisplayItem(new ItemStack(Material.OAK_LOG)));
                }

                int slot = 9; // Start displaying properties from the second row
                for (String key : treeSection.getKeys(false)) {
                    Object value = treeSection.get(key);
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
                            config.set("trees." + treePopulatorKey + "." + key, newValue);
                            try {
                                config.save(treePopulatorFile);
                                plugin.reloadTreePopulatorConfig();
                                plugin.getGuiManager().openGui((Player) event.getWhoClicked(), new TreePopulatorDetailGui(plugin, treePopulatorKey));
                            } catch (IOException e) {
                                Logger.error("Could not save tree_populator.yml: " + e.getMessage());
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
                                    config.set("trees." + treePopulatorKey + "." + key, parsedValue);
                                    config.save(treePopulatorFile);
                                    plugin.reloadTreePopulatorConfig();
                                    player.sendMessage("§aConfiguration saved successfully!");
                                    plugin.getGuiManager().openGui(player, new TreePopulatorDetailGui(plugin, treePopulatorKey));
                                } catch (NumberFormatException e) {
                                    player.sendMessage("§cInvalid number format. Please enter a valid number.");
                                    plugin.getGuiManager().openGui(player, new TreePopulatorDetailGui(plugin, treePopulatorKey));
                                } catch (IOException e) {
                                    Logger.error("Could not save tree_populator.yml: " + e.getMessage());
                                    player.sendMessage("§cError saving configuration: " + e.getMessage());
                                    plugin.getGuiManager().openGui(player, new TreePopulatorDetailGui(plugin, treePopulatorKey));
                                }
                            }, p -> plugin.getGuiManager().openGui(p, new TreePopulatorDetailGui(plugin, treePopulatorKey)));
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
                                config.set("trees." + treePopulatorKey + "." + key, newValue);
                                try {
                                    config.save(treePopulatorFile);
                                    plugin.reloadTreePopulatorConfig();
                                    player.sendMessage("§aConfiguration saved successfully!");
                                    plugin.getGuiManager().openGui(player, new TreePopulatorDetailGui(plugin, treePopulatorKey));
                                } catch (IOException e) {
                                    Logger.error("Could not save tree_populator.yml: " + e.getMessage());
                                    player.sendMessage("§cError saving configuration: " + e.getMessage());
                                    plugin.getGuiManager().openGui(player, new TreePopulatorDetailGui(plugin, treePopulatorKey));
                                }
                            }, p -> plugin.getGuiManager().openGui(p, new TreePopulatorDetailGui(plugin, treePopulatorKey)));
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
                plugin.getGuiManager().openGui(player, new TreePopulatorEditorGui(plugin));
            }));

            // Delete button
            ItemStack deleteButton = new ItemStack(Material.BARRIER);
            ItemMeta deleteMeta = deleteButton.getItemMeta();
            if (deleteMeta != null) {
                deleteMeta.setDisplayName("§cDelete Tree Populator");
                deleteButton.setItemMeta(deleteMeta);
            }
            setItem(53, new Button(deleteButton, event -> {
                Player player = (Player) event.getWhoClicked();
                config.set("trees." + treePopulatorKey, null); // Remove the section
                try {
                    config.save(treePopulatorFile);
                    plugin.reloadTreePopulatorConfig();
                    player.sendMessage("§aTree Populator '" + treePopulatorKey + "' deleted successfully!");
                    plugin.getGuiManager().openGui(player, new TreePopulatorEditorGui(plugin));
                } catch (IOException e) {
                    Logger.error("Could not save tree_populator.yml: " + e.getMessage());
                    player.sendMessage("§cError deleting configuration: " + e.getMessage());
                }
            }));
        }
    }
}
