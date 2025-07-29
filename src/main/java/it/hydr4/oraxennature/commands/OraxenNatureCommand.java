package it.hydr4.oraxennature.commands;

import it.hydr4.oraxennature.OraxenNature;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import it.hydr4.oraxennature.gui.EditorGui;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OraxenNatureCommand implements CommandExecutor, TabCompleter {

    private final OraxenNature plugin;

    public OraxenNatureCommand(OraxenNature plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            handleEditorCommand(sender);
            return true;
        }

        if (!sender.hasPermission("oraxennature.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                handleReloadCommand(sender, args);
                break;
            case "set":
                handleSetCommand(sender, args);
                break;
            case "get":
                handleGetCommand(sender, args);
                break;
            case "debug":
                handleDebugCommand(sender, args);
                break;
            case "help":
                sendHelpMessage(sender);
                break;
            default:
                sender.sendMessage("§cUnknown subcommand: " + subCommand + ". Use /" + label + " help for a list of commands.");
                break;
        }

        return true;
    }

    private void handleReloadCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /" + plugin.getName().toLowerCase() + " reload <all|block_populator|tree_populator|growth_config>");
            return;
        }

        String reloadType = args[1].toLowerCase();

        switch (reloadType) {
            case "all":
                plugin.reloadAllConfigs();
                sender.sendMessage("§aAll OraxenNature configurations reloaded.");
                break;
            case "block_populator":
                plugin.reloadBlockPopulatorConfig();
                sender.sendMessage("§aBlock populator configuration reloaded.");
                break;
            case "tree_populator":
                plugin.reloadTreePopulatorConfig();
                sender.sendMessage("§aTree populator configuration reloaded.");
                break;
            case "growth_config":
                plugin.reloadGrowthConfig();
                sender.sendMessage("§aGrowth configuration reloaded.");
                break;
            default:
                sender.sendMessage("§cUnknown reload type: " + reloadType + ". Valid types: all, block_populator, tree_populator, growth_config.");
                break;
        }
    }

    private void handleSetCommand(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /" + plugin.getName().toLowerCase() + " set <config_file> <path> <value>");
            return;
        }

        String configFile = args[1].toLowerCase();
        String path = args[2];
        String valueStr = args[3];

        FileConfiguration config = getConfigFile(sender, configFile);
        if (config == null) return;

        Object value = parseValue(valueStr);
        if (value == null) {
            sender.sendMessage("§cInvalid value format: " + valueStr + ". Supported types: boolean, int, double, string, list (comma-separated).");
            return;
        }

        config.set(path, value);
        try {
            config.save(new File(plugin.getDataFolder(), configFile + ".yml"));
            sender.sendMessage("§aSuccessfully set " + path + " in " + configFile + ".yml to " + valueStr + ".");
            plugin.reloadAllConfigs(); // Reload all configs to apply changes
        } catch (Exception e) {
            sender.sendMessage("§cFailed to save configuration: " + e.getMessage());
            it.hydr4.oraxennature.utils.Logger.error("Failed to save configuration " + configFile + ".yml: " + e.getMessage());
        }
    }

    private void handleGetCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /" + plugin.getName().toLowerCase() + " get <config_file> <path>");
            return;
        }

        String configFile = args[1].toLowerCase();
        String path = args[2];

        FileConfiguration config = getConfigFile(sender, configFile);
        if (config == null) return;

        Object value = config.get(path);
        if (value != null) {
            sender.sendMessage("§aValue of " + path + " in " + configFile + ".yml: §f" + value.toString());
        } else {
            sender.sendMessage("§cPath " + path + " not found in " + configFile + ".yml.");
        }
    }

    private FileConfiguration getConfigFile(CommandSender sender, String configName) {
        switch (configName) {
            case "block_populator":
                return plugin.getBlockPopulatorConfig();
            case "tree_populator":
                return plugin.getTreePopulatorConfig();
            case "growth_config":
                return plugin.getGrowthConfig();
            default:
                sender.sendMessage("§cUnknown config file: " + configName + ". Valid files: block_populator, tree_populator, growth_config.");
                return null;
        }
    }

    private Object parseValue(String valueStr) {
        if (valueStr.equalsIgnoreCase("true")) return true;
        if (valueStr.equalsIgnoreCase("false")) return false;
        try {
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            // Not an int
        }
        try {
            return Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            // Not a double
        }
        if (valueStr.contains(",")) {
            return Arrays.asList(valueStr.split(",")).stream().map(String::trim).collect(Collectors.toList());
        }
        return valueStr; // Default to string
    }

    private void handleDebugCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /" + plugin.getName().toLowerCase() + " debug <true|false>");
            return;
        }

        boolean enableDebug = Boolean.parseBoolean(args[1]);
        plugin.setDebugMode(enableDebug);
        sender.sendMessage("§aDebug mode set to: " + enableDebug);
        it.hydr4.oraxennature.utils.Logger.info("Debug mode set to: " + enableDebug + " by " + sender.getName());
    }

    private void handleEditorCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by a player.");
            return;
        }
        Player player = (Player) sender;
        plugin.getGuiManager().openGui(player, new EditorGui(plugin));
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§e--- OraxenNature Help ---");
        sender.sendMessage("§a/on reload <all|block_populator|tree_populator|growth_config> §7- Reloads plugin configurations.");
        sender.sendMessage("§a/on set <config_file> <path> <value> §7- Sets a configuration value (e.g., /on set block_populator blocks.my_block.enabled true).");
        sender.sendMessage("§a/on get <config_file> <path> §7- Gets a configuration value (e.g., /on get block_populator blocks.my_block.enabled).");
        sender.sendMessage("§a/on debug <true|false> §7- Toggles debug mode for more verbose logging.");
        sender.sendMessage("§a/on help §7- Displays this help message.");
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if ("reload".startsWith(args[0].toLowerCase())) {
                completions.add("reload");
            }
            if ("set".startsWith(args[0].toLowerCase())) {
                completions.add("set");
            }
            if ("get".startsWith(args[0].toLowerCase())) {
                completions.add("get");
            }
            if ("debug".startsWith(args[0].toLowerCase())) {
                completions.add("debug");
            }
            if ("help".startsWith(args[0].toLowerCase())) {
                completions.add("help");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reload")) {
                if ("all".startsWith(args[1].toLowerCase())) {
                    completions.add("all");
                }
                if ("block_populator".startsWith(args[1].toLowerCase())) {
                    completions.add("block_populator");
                }
                if ("tree_populator".startsWith(args[1].toLowerCase())) {
                    completions.add("tree_populator");
                }
                if ("growth_config".startsWith(args[1].toLowerCase())) {
                    completions.add("growth_config");
                }
            } else if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("get")) {
                if ("block_populator".startsWith(args[1].toLowerCase())) {
                    completions.add("block_populator");
                }
                if ("tree_populator".startsWith(args[1].toLowerCase())) {
                    completions.add("tree_populator");
                }
                if ("growth_config".startsWith(args[1].toLowerCase())) {
                    completions.add("growth_config");
                }
            } else if (args[0].equalsIgnoreCase("debug")) {
                if ("true".startsWith(args[1].toLowerCase())) {
                    completions.add("true");
                }
                if ("false".startsWith(args[1].toLowerCase())) {
                    completions.add("false");
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("get")) {
                // Provide basic path suggestions based on config file
                String configFile = args[1].toLowerCase();
                if (configFile.equals("block_populator")) {
                    completions.add("blocks.");
                    completions.add("blocks.example_custom_block.enabled");
                } else if (configFile.equals("tree_populator")) {
                    completions.add("trees.");
                    completions.add("trees.example_advanced_tree.enabled");
                } else if (configFile.equals("growth_config")) {
                    completions.add("growable_blocks.");
                }
            }
        }

        return completions.stream()
                .filter(s -> sender.hasPermission("oraxennature.admin")) // Only show completions if user has permission
                .collect(Collectors.toList());
    }
}