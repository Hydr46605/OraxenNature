package it.hydr4.oraxennature;

import it.hydr4.oraxennature.commands.OraxenNatureCommand;
import it.hydr4.oraxennature.growth.GrowthManager;
import it.hydr4.oraxennature.populators.CustomBlockPopulator;
import it.hydr4.oraxennature.populators.treePopulator.CustomTreePopulator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public final class OraxenNature extends JavaPlugin {

    private GrowthManager growthManager;
    private CustomBlockPopulator blockPopulator;
    private CustomTreePopulator treePopulator;
    private boolean debugMode = false;

    private FileConfiguration blockPopulatorConfig;
    private FileConfiguration treePopulatorConfig;
    private FileConfiguration growthConfig;

    @Override
    public void onEnable() {
        // Plugin startup logic
        logInfo("OraxenNature is enabling...");

        // Check if Oraxen is enabled
        if (getServer().getPluginManager().getPlugin("Oraxen") == null || !getServer().getPluginManager().getPlugin("Oraxen").isEnabled()) {
            getLogger().severe("Oraxen is not enabled! OraxenNature requires Oraxen to function. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Save default resources if they don't exist
        saveResource("block_populator.yml", false);
        saveResource("tree_populator.yml", false);
        saveResource("growth_config.yml", false);

        // Load configurations
        blockPopulatorConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "block_populator.yml"));
        treePopulatorConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "tree_populator.yml"));
        growthConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "growth_config.yml"));

        // Initialize populators and growth manager
        blockPopulator = new CustomBlockPopulator(this, blockPopulatorConfig);
        treePopulator = new CustomTreePopulator(this, treePopulatorConfig);
        growthManager = new GrowthManager(this, growthConfig);
        growthManager.startGrowthTask();

        // Register commands
        if (getCommand("oraxennature") != null) {
            getCommand("oraxennature").setExecutor(new OraxenNatureCommand(this));
            getCommand("oraxennature").setTabCompleter(new OraxenNatureCommand(this));
        } else {
            getLogger().severe("Failed to register command 'oraxennature'. It might be already registered or there's an issue with plugin.yml.");
        }

        getServer().getPluginManager().registerEvents(new it.hydr4.oraxennature.listeners.ChunkLoadListener(this), this);
        // Register the custom tree populator for all worlds
        for (World world : getServer().getWorlds()) {
            world.getPopulators().add(treePopulator);
        }
        logInfo("OraxenNature enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        logInfo("OraxenNature is disabling...");
        if (growthManager != null) {
            growthManager.stopGrowthTask();
        }
        logInfo("OraxenNature disabled successfully!");
    }

    public void reloadAllConfigs() {
        reloadBlockPopulatorConfig();
        reloadTreePopulatorConfig();
        reloadGrowthConfig();
        logInfo("All OraxenNature configurations reloaded.");
    }

    public void reloadBlockPopulatorConfig() {
        blockPopulatorConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "block_populator.yml"));
        blockPopulator = new CustomBlockPopulator(this, blockPopulatorConfig);
        logInfo("Block populator configuration reloaded.");
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
        logInfo("Tree populator configuration reloaded.");
    }

    public void reloadGrowthConfig() {
        if (growthManager != null) {
            growthManager.stopGrowthTask();
        }
        growthConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "growth_config.yml"));
        growthManager = new GrowthManager(this, growthConfig);
        growthManager.startGrowthTask();
        logInfo("Growth configuration reloaded.");
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    // Custom logging method that respects debugMode
    public void logInfo(String message) {
        if (debugMode) {
            getLogger().info(message);
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
}