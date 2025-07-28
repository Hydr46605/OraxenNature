package it.hydr4.oraxennature.gui;

import it.hydr4.oraxennature.OraxenNature;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;
import java.io.IOException;
import it.hydr4.oraxennature.utils.Logger;

public class BlockPopulatorDetailGui extends AbstractGui {

    private final OraxenNature plugin;
    private final String blockPopulatorKey;

    public BlockPopulatorDetailGui(OraxenNature plugin, String blockPopulatorKey) {
        super(54, blockPopulatorKey.equals("__NEW__") ? "§8Create New Block Populator" : "§8Editing: " + blockPopulatorKey); // Increased size to 6 rows
        this.plugin = plugin;
        this.blockPopulatorKey = blockPopulatorKey;
        setupItems();
    }

    @Override
    public void setupItems() {
        File blockPopulatorFile = new File(plugin.getDataFolder(), "block_populator.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(blockPopulatorFile);

        if (blockPopulatorKey.equals("__NEW__")) {
            // Display input for new name
            ItemStack nameInput = new ItemStack(Material.PAPER);
            ItemMeta nameInputMeta = nameInput.getItemMeta();
            if (nameInputMeta != null) {
                nameInputMeta.setDisplayName("§aClick to enter new Block Populator Name");
                nameInput.setItemMeta(nameInputMeta);
            }
            setItem(22, new Button(nameInput, event -> {
                Player player = (Player) event.getWhoClicked();
                plugin.getGuiManager().closeGui(player);
                player.sendMessage("§eEnter the new Block Populator name:");
                ChatInputListener.awaitInput(player, newName -> {
                    if (config.isConfigurationSection("blocks." + newName)) {
                        player.sendMessage("§cError: A block populator with that name already exists!");
                        plugin.getGuiManager().openGui(player, new BlockPopulatorEditorGui(plugin));
                        return;
                    }
                    // Create new section with default values
                    config.createSection("blocks." + newName);
                    config.set("blocks." + newName + ".enabled", false);
                    config.set("blocks." + newName + ".material", "STONE");
                    config.set("blocks." + newName + ".min-y", 0);
                    config.set("blocks." + newName + ".max-y", 64);
                    config.set("blocks." + newName + ".chance", 100);
                    try {
                        config.save(blockPopulatorFile);
                        plugin.reloadBlockPopulatorConfig();
                        plugin.getGuiManager().openGui(player, new BlockPopulatorDetailGui(plugin, newName));
                    } catch (IOException e) {
                        plugin.getLogger().severe("Could not save block_populator.yml: " + e.getMessage());
                        player.sendMessage("§cError saving configuration!");
                        plugin.getGuiManager().openGui(player, new BlockPopulatorEditorGui(plugin));
                    }
                }, p -> plugin.getGuiManager().openGui(p, new BlockPopulatorEditorGui(plugin)));
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
                plugin.getGuiManager().openGui(player, new BlockPopulatorEditorGui(plugin));
            }));

        } else {
            // Existing block populator logic
            // Display the name of the block populator being edited
            ItemStack nameItem = new ItemStack(Material.NAME_TAG);
            ItemMeta nameMeta = nameItem.getItemMeta();
            if (nameMeta != null) {
                nameMeta.setDisplayName("§fBlock Populator: §a" + blockPopulatorKey);
                nameItem.setItemMeta(nameMeta);
            }
            setItem(4, new DisplayItem(nameItem));

            ConfigurationSection blockSection = config.getConfigurationSection("blocks." + blockPopulatorKey);
            if (blockSection != null) {
                int slot = 9; // Start displaying properties from the second row
                for (String key : blockSection.getKeys(false)) {
                    Object value = blockSection.get(key);
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
                            config.set("blocks." + blockPopulatorKey + "." + key, newValue);
                            try {
                                config.save(blockPopulatorFile);
                                plugin.reloadBlockPopulatorConfig();
                                plugin.getGuiManager().openGui((Player) event.getWhoClicked(), new BlockPopulatorDetailGui(plugin, blockPopulatorKey));
                            } catch (IOException e) {
                                Logger.error("Could not save block_populator.yml: " + e.getMessage());
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
                                    config.set("blocks." + blockPopulatorKey + "." + key, parsedValue);
                                    config.save(blockPopulatorFile);
                                    plugin.reloadBlockPopulatorConfig();
                                    player.sendMessage("§aConfiguration saved successfully!");
                                    plugin.getGuiManager().openGui(player, new BlockPopulatorDetailGui(plugin, blockPopulatorKey));
                                } catch (NumberFormatException e) {
                                    player.sendMessage("§cInvalid number format. Please enter a valid number.");
                                    plugin.getGuiManager().openGui(player, new BlockPopulatorDetailGui(plugin, blockPopulatorKey));
                                } catch (IOException e) {
                                    Logger.error("Could not save block_populator.yml: " + e.getMessage());
                                    player.sendMessage("§cError saving configuration: " + e.getMessage());
                                    plugin.getGuiManager().openGui(player, new BlockPopulatorDetailGui(plugin, blockPopulatorKey));
                                }
                            }, p -> plugin.getGuiManager().openGui(p, new BlockPopulatorDetailGui(plugin, blockPopulatorKey)));
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
                                config.set("blocks." + blockPopulatorKey + "." + key, newValue);
                                try {
                                    config.save(blockPopulatorFile);
                                    plugin.reloadBlockPopulatorConfig();
                                    player.sendMessage("§aConfiguration saved successfully!");
                                    plugin.getGuiManager().openGui(player, new BlockPopulatorDetailGui(plugin, blockPopulatorKey));
                                } catch (IOException e) {
                                    Logger.error("Could not save block_populator.yml: " + e.getMessage());
                                    player.sendMessage("§cError saving configuration: " + e.getMessage());
                                    plugin.getGuiManager().openGui(player, new BlockPopulatorDetailGui(plugin, blockPopulatorKey));
                                }
                            }, p -> plugin.getGuiManager().openGui(p, new BlockPopulatorDetailGui(plugin, blockPopulatorKey)));
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
                plugin.getGuiManager().openGui(player, new BlockPopulatorEditorGui(plugin));
            }));

            // Delete button
            ItemStack deleteButton = new ItemStack(Material.BARRIER);
            ItemMeta deleteMeta = deleteButton.getItemMeta();
            if (deleteMeta != null) {
                deleteMeta.setDisplayName("§cDelete Block Populator");
                deleteButton.setItemMeta(deleteMeta);
            }
            setItem(53, new Button(deleteButton, event -> {
                Player player = (Player) event.getWhoClicked();
                config.set("blocks." + blockPopulatorKey, null); // Remove the section
                try {
                    config.save(blockPopulatorFile);
                    plugin.reloadBlockPopulatorConfig();
                    player.sendMessage("§aBlock Populator '" + blockPopulatorKey + "' deleted successfully!");
                    plugin.getGuiManager().openGui(player, new BlockPopulatorEditorGui(plugin));
                } catch (IOException e) {
                    Logger.error("Could not save block_populator.yml: " + e.getMessage());
                    player.sendMessage("§cError deleting configuration: " + e.getMessage());
                }
            }));
        }
    }
}
