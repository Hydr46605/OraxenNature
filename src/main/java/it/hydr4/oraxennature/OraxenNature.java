package it.hydr4.oraxennature;

import it.hydr4.oraxennature.commands.OraxenNatureCommand;
import it.hydr4.oraxennature.growth.GrowthManager;
import it.hydr4.oraxennature.populators.CustomBlockPopulator;
import it.hydr4.oraxennature.populators.treePopulator.CustomTreePopulator;
import it.hydr4.oraxennature.gui.GuiManager;
import it.hydr4.oraxennature.gui.ChatInputListener;
import it.hydr4.oraxennature.utils.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
    private FileConfiguration settingsConfig;

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
        saveDefaultConfig("settings.yml");

        // Load configurations
        blockPopulatorConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "block_populator.yml"));
        treePopulatorConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "tree_populator.yml"));
        growthConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "growth_config.yml"));
        settingsConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "settings.yml"));

        // Load and merge pack configurations
        loadPacks();

        // Set debug mode
        debugMode = settingsConfig.getBoolean("debug", false);

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

        Logger.log("&r");
        Logger.success("Loaded &e" + blockPopulator.getLoadedBlockNames().size() + "&a custom block populators:");
        Logger.logList(blockPopulator.getLoadedBlockNames(), "  &7- &f");
        Logger.success("Loaded &e" + treePopulator.getLoadedTrees().size() + "&a custom tree populators:");
        treePopulator.getLoadedTrees().forEach(tree -> Logger.log("  &7- &f" + tree.getId()));
        Logger.success("Loaded &e" + growthManager.getLoadedGrowthConfigNames().size() + "&a growth configurations:");
        Logger.logList(growthManager.getLoadedGrowthConfigNames(), "  &7- &f");
        Logger.log("&r");
        Logger.log("&8&m------------------------------------------------");

        // Start version checker
        checkVersion();
    }

    private void loadPacks() {
        File packsFolder = new File(getDataFolder(), "packs");
        if (!packsFolder.exists()) {
            packsFolder.mkdirs();
            return;
        }

        for (File file : packsFolder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".yml")) {
                Logger.info("Loading pack: " + file.getName());
                FileConfiguration packConfig = YamlConfiguration.loadConfiguration(file);

                // Merge 'blocks' section
                if (packConfig.isConfigurationSection("blocks")) {
                    ConfigurationSection blocksSection = packConfig.getConfigurationSection("blocks");
                    for (String key : blocksSection.getKeys(false)) {
                        blockPopulatorConfig.set("blocks." + key, blocksSection.get(key));
                    }
                }

                // Merge 'trees' section
                if (packConfig.isConfigurationSection("trees")) {
                    ConfigurationSection treesSection = packConfig.getConfigurationSection("trees");
                    for (String key : treesSection.getKeys(false)) {
                        treePopulatorConfig.set("trees." + key, treesSection.get(key));
                    }
                }

                // Merge 'growth_configs' section (assuming it's named this way)
                if (packConfig.isConfigurationSection("growth_configs")) {
                    ConfigurationSection growthConfigsSection = packConfig.getConfigurationSection("growth_configs");
                    for (String key : growthConfigsSection.getKeys(false)) {
                        growthConfig.set("growth_configs." + key, growthConfigsSection.get(key));
                    }
                }

                // Merge 'settings' section (careful with this, might override core settings)
                if (packConfig.isConfigurationSection("settings")) {
                    ConfigurationSection settingsSection = packConfig.getConfigurationSection("settings");
                    for (String key : settingsSection.getKeys(false)) {
                        settingsConfig.set(key, settingsSection.get(key));
                    }
                }
            }
        }
    }

    private void checkVersion() {
        boolean versionCheckerEnabled = settingsConfig.getBoolean("version_checker_enabled", true);
        if (!versionCheckerEnabled) {
            return;
        }

        String repoOwner = "Hydr46605";
        String repoName = "OraxenNature";

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String urlString = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/releases/latest";
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "OraxenNature-VersionChecker");

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuilder content = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                        in.close();

                        JSONParser parser = new JSONParser();
                        JSONObject json = (JSONObject) parser.parse(content.toString());
                        String latestVersion = ((String) json.get("tag_name")).replaceFirst("^v", "");

                        String currentVersion = getDescription().getVersion();

                        if (latestVersion != null && !latestVersion.equals(currentVersion)) {
                            Logger.info("A new version of OraxenNature is available: " + latestVersion + " (Current: " + currentVersion + ")");
                            Logger.info("Download it from: https://github.com/" + repoOwner + "/" + repoName + "/releases");
                        } else {
                            Logger.info("You are running the latest version of OraxenNature.");
                        }
                    } else {
                        Logger.warning("Failed to check for updates. HTTP Response Code: " + responseCode);
                    }
                } catch (Exception e) {
                    Logger.warning("An error occurred during version check: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(this);
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
        settingsConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "settings.yml"));
        debugMode = settingsConfig.getBoolean("debug", false);
    }

    public void reloadBlockPopulatorConfig() {
        blockPopulatorConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "block_populator.yml"));
        loadPacks(); // Reload packs as well
        blockPopulator = new CustomBlockPopulator(this, blockPopulatorConfig);
        Logger.info("Block populator configuration reloaded.");
    }

    public void reloadTreePopulatorConfig() {
        treePopulatorConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "tree_populator.yml"));
        treePopulator = new CustomTreePopulator(this, treePopulatorConfig);
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

    public FileConfiguration getSettingsConfig() {
        return settingsConfig;
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

    public GrowthManager getGrowthManager() {
        return growthManager;
    }
}