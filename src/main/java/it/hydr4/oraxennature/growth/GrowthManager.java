package it.hydr4.oraxennature.growth;

import it.hydr4.oraxennature.OraxenNature;
import it.hydr4.oraxennature.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrowthManager {

    private final OraxenNature plugin;
    private final Map<String, GrowableBlock> growableBlocks;
    private BukkitTask growthTask;
    private final List<String> loadedGrowthConfigNames = new ArrayList<>();

    public GrowthManager(OraxenNature plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.growableBlocks = new HashMap<>();
        loadGrowableBlocks(config);
    }

    private void loadGrowableBlocks(FileConfiguration config) {
        // No need to load file here, config is passed in

        ConfigurationSection growableSection = config.getConfigurationSection("growable_blocks");
        if (growableSection != null) {
            for (String key : growableSection.getKeys(false)) {
                ConfigurationSection blockConfig = growableSection.getConfigurationSection(key);
                if (blockConfig == null) continue;

                String initialId = blockConfig.getString("initial_oraxen_id");
                List<String> growthStages = blockConfig.getStringList("growth_stages");
                int growthInterval = blockConfig.getInt("growth_interval_seconds", 60);

                ConfigurationSection growthConditionsSection = blockConfig.getConfigurationSection("growth_conditions");
                GrowableBlock.GrowthConditions growthConditions = parseConditions(growthConditionsSection);

                ConfigurationSection decayConditionsSection = blockConfig.getConfigurationSection("decay_conditions");
                GrowableBlock.GrowthConditions decayConditions = parseConditions(decayConditionsSection);

                if (initialId != null && !growthStages.isEmpty()) {
                    growableBlocks.put(initialId, new GrowableBlock(key, initialId, growthStages, growthInterval, growthConditions, decayConditions));
                    loadedGrowthConfigNames.add(key);
                } else {
                    Logger.warning("Invalid growable block configuration for '" + key + "'. Missing initial_oraxen_id or growth_stages.");
                }
            }
        }
    }

    public List<String> getLoadedGrowthConfigNames() {
        return loadedGrowthConfigNames;
    }

    private GrowableBlock.GrowthConditions parseConditions(ConfigurationSection section) {
        if (section == null) {
            return new GrowableBlock.GrowthConditions(0, 15, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
        int lightMin = section.getInt("light_level_min", 0);
        int lightMax = section.getInt("light_level_max", 15);
        List<String> blockBelow = section.getStringList("block_below");
        List<String> biomes = section.getStringList("biomes");
        List<String> worlds = section.getStringList("worlds");
        return new GrowableBlock.GrowthConditions(lightMin, lightMax, blockBelow, biomes, worlds);
    }

    public void startGrowthTask() {
        if (growthTask != null) {
            growthTask.cancel();
        }
        // Schedule the task to run every second (20 ticks)
        growthTask = new GrowthTask(plugin, growableBlocks).runTaskTimer(plugin, 0L, 20L);
        plugin.getLogger().info("Growth task started.");
    }

    public void stopGrowthTask() {
        if (growthTask != null) {
            growthTask.cancel();
            growthTask = null;
            plugin.getLogger().info("Growth task stopped.");
        }
    }

    public Map<String, GrowableBlock> getGrowableBlocks() {
        return growableBlocks;
    }
}