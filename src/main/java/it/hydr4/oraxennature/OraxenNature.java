package it.hydr4.oraxennature;

import it.hydr4.oraxennature.commands.OraxenNatureCommand;
import it.hydr4.oraxennature.growth.GrowthManager;
import it.hydr4.oraxennature.populators.CustomBlockPopulator;
import it.hydr4.oraxennature.populators.treePopulator.CustomTreePopulator;
import it.hydr4.oraxennature.gui.GuiManager;
import it.hydr4.oraxennature.gui.ChatInputListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class OraxenNature extends JavaPlugin {

    private static OraxenNature instance;
    private GrowthManager growthManager;
    private CustomBlockPopulator blockPopulator;
    private CustomTreePopulator treePopulator;
    private GuiManager guiManager;
    private ChatInputListener chatInputListener;
    private boolean debugMode = false;

    private FileConfiguration blockPopulatorConfig;
    private FileConfiguration treePopulatorConfig;
    private FileConfiguration growthConfig;

    @Override
    public void onEnable() {
        instance = this;

        Logger.log("&8&m------------------------------------------------");
        Logger.log("&r");
        Logger.log("  &a&lOraxenNature &a- &fEnabled");
        Logger.log("  &r");
        Logger.log("  &fDeveloped by: &bHydr4");
        Logger.log("  &fVersion: &e" + getDescription().getVersion());
        Logger.log("&r");

        // Check if Oraxen is enabled
        if (getServer().getPluginManager().getPlugin("Oraxen") == null || !getServer().getPluginManager().getPlugin("Oraxen").isEnabled()) {
            Logger.error("Oraxen is not enabled! OraxenNature requires Oraxen to function. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Save default resources if they don't exist
        saveDefaultConfig("block_populator.yml");
        saveDefaultConfig("tree_populator.yml");
        saveDefaultConfig("growth_config.yml");

        // Load configurations
        blockPopulatorConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "block_populator.yml"));
        treePopulatorConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "tree_populator.yml"));
        growthConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "growth_config.yml"));

        // Initialize populators and growth manager
        blockPopulator = new CustomBlockPopulator(this, blockPopulatorConfig);
        treePopulator = new CustomTreePopulator(this, treePopulatorConfig);
        growthManager = new GrowthManager(this, growthConfig);
        growthManager.startGrowthTask();

        // Initialize GUI Manager
        guiManager = new GuiManager(this);
        chatInputListener = new ChatInputListener(this);

        // Register commands
        if (getCommand("oraxennature") != null) {
            getCommand("oraxennature").setExecutor(new OraxenNatureCommand(this));
            getCommand("oraxennature").setTabCompleter(new OraxenNatureCommand(this));
        } else {
            Logger.error("Failed to register command 'oraxennature'. It might be already registered or there's an issue with plugin.yml.");
        }

        getServer().getPluginManager().registerEvents(new it.hydr4.oraxennature.listeners.ChunkLoadListener(this), this);
        // Register the custom tree populator for all worlds
        for (World world : getServer().getWorlds()) {
            world.getPopulators().add(treePopulator);
        }

        Logger.log("&r");
        Logger.success("Loaded &e" + blockPopulator.getLoadedBlockNames().size() + "&a custom block populators:");
        Logger.logList(blockPopulator.getLoadedBlockNames(), "  &7- &f");
        Logger.success("Loaded &e" + treePopulator.getLoadedTreeNames().size() + "&a custom tree populators:");
        Logger.logList(treePopulator.getLoadedTreeNames(), "  &7- &f");
        Logger.success("Loaded &e" + growthManager.getLoadedGrowthConfigNames().size() + "&a growth configurations:");
        Logger.logList(growthManager.getLoadedGrowthConfigNames(), "  &7- &f");
        Logger.log("&r");
        Logger.log("&8&m------------------------------------------------");
    }

    @Override
    public void onDisable() {
        Logger.log("&8&m------------------------------------------------");
        Logger.log("&r");
        Logger.log("  &c&lOraxenNature &c- &fDisabled");
        Logger.log("  &r");
        Logger.log("  &fDeveloped by: &bHydr4");
        Logger.log("  &fVersion: &e" + getDescription().getVersion());
        Logger.log("&r");
        Logger.log("&8&m------------------------------------------------");
        if (growthManager != null) {
            growthManager.stopGrowthTask();
        }
    }

    public static OraxenNature getInstance() {
        return instance;
    }

    public void reloadAllConfigs() {
        reloadBlockPopulatorConfig();
        reloadTreePopulatorConfig();
        reloadGrowthConfig();
        Logger.info("All OraxenNature configurations reloaded.");
    }

    public void reloadBlockPopulatorConfig() {
        blockPopulatorConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "block_populator.yml"));
        blockPopulator = new CustomBlockPopulator(this, blockPopulatorConfig);
        Logger.info("Block populator configuration reloaded.");
    }

    public void reloadTreePopulatorConfig() {
        // Remove old populators and add new ones
        for (World world : getServer().getWorlds()) {
            world.getPopulators().remove(treePopulator);
        }
        treePopulatorConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "tree_populator.yml"));
        treePopulator = new CustomTreePopulator(this, treePopulatorConfig);
        for (World world : getServer().getWorlds()) {
            world.getPopulators().add(treePopulator);
        }
        Logger.info("Tree populator configuration reloaded.");
    }

    public void reloadGrowthConfig() {
        if (growthManager != null) {
            growthManager.stopGrowthTask();
        }
        growthConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "growth_config.yml"));
        growthManager = new GrowthManager(this, growthConfig);
        growthManager.startGrowthTask();
        Logger.info("Growth configuration reloaded.");
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    private void saveDefaultConfig(String resourcePath) {
        File resourceFile = new File(getDataFolder(), resourcePath);
        if (!resourceFile.exists()) {
            saveResource(resourcePath, false);
        }
    }

    public FileConfiguration getBlockPopulatorConfig() {
        return blockPopulatorConfig;
    }

    public FileConfiguration getTreePopulatorConfig() {
        return treePopulatorConfig;
    }

    public FileConfiguration getGrowthConfig() {
        return growthConfig;
    }

    public CustomBlockPopulator getBlockPopulator() {
        return blockPopulator;
    }

    public CustomTreePopulator getTreePopulator() {
        return treePopulator;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }
}