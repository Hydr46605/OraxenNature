package it.hydr4.oraxennature.populators;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import it.hydr4.oraxennature.OraxenNature;
import it.hydr4.oraxennature.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenItems;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class CustomBlockPopulator {

    private final OraxenNature plugin;
    private final FileConfiguration config;
    private final Random random;
    private final List<String> loadedBlockNames = new ArrayList<>();

    public CustomBlockPopulator(OraxenNature plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
        if (!this.config.isConfigurationSection("blocks")) {
            Logger.error("block_populator.yml is missing the 'blocks' section or is malformed. Block population may not work as expected.");
        }
        this.random = new Random();
        loadBlockNames();
    }

    private void loadBlockNames() {
        if (config.isConfigurationSection("blocks")) {
            for (String key : config.getConfigurationSection("blocks").getKeys(false)) {
                loadedBlockNames.add(key);
            }
        }
    }

    public List<String> getLoadedBlockNames() {
        return loadedBlockNames;
    }

    public void populate(Chunk chunk) {
        // Check if Oraxen is enabled and loaded
        if (plugin.getServer().getPluginManager().getPlugin("Oraxen") == null || !plugin.getServer().getPluginManager().getPlugin("Oraxen").isEnabled()) {
            Logger.warning("Oraxen is not enabled. Skipping block population in world " + chunk.getWorld().getName() + ".");
            return;
        }

        if (config.isConfigurationSection("blocks")) {
            for (String key : config.getConfigurationSection("blocks").getKeys(false)) {
                ConfigurationSection blockConfig = config.getConfigurationSection("blocks." + key);
                if (blockConfig == null) {
                    Logger.warning("Invalid configuration section for block: " + key + ". Skipping.");
                    continue;
                }

                boolean enabled = blockConfig.getBoolean("enabled", true); // Default to true if not specified
                if (!enabled) {
                    Logger.info("Block entry '" + key + "' is disabled in block_populator.yml. Skipping.");
                    continue;
                }

                // Underground generation parameters
                String oraxenId = blockConfig.getString("oraxen_id");
                Optional<Integer> iterations = parseIterationValue(blockConfig, "iterations", 50);
                int min_y = blockConfig.getInt("min_y");
                int max_y = blockConfig.getInt("max_y");
                Optional<Integer> vein_size = parseIterationValue(blockConfig, "vein_size", 5);
                double chance = blockConfig.getDouble("chance");
                double clusterChance = blockConfig.getDouble("cluster_chance", 0.0);
                List<String> replaceableMaterials = blockConfig.getStringList("replaceable_materials");
                List<String> placeOnMaterials = blockConfig.getStringList("place_on");
                List<String> placeBelowMaterials = blockConfig.getStringList("place_below");
                boolean airOnly = blockConfig.getBoolean("air_only", false);
                List<String> worlds = blockConfig.getStringList("worlds");
                List<String> biomes = blockConfig.getStringList("biomes");

                // Surface generation parameters (optional)
                Optional<Integer> surfaceIterations = parseIterationValue(blockConfig, "surface_iterations", -1);
                int surface_min_y = blockConfig.getInt("surface_min_y", -1);
                int surface_max_y = blockConfig.getInt("surface_max_y", -1);
                Optional<Integer> surface_vein_size = parseIterationValue(blockConfig, "surface_vein_size", -1);
                double surface_chance = blockConfig.getDouble("surface_chance", -1.0);
                double surface_clusterChance = blockConfig.getDouble("surface_cluster_chance", 0.0);
                List<String> surface_replaceableMaterials = blockConfig.getStringList("surface_replaceable_materials");
                List<String> surface_placeOnMaterials = blockConfig.getStringList("surface_place_on");
                List<String> surface_placeBelowMaterials = blockConfig.getStringList("surface_place_below");
                boolean surface_airOnly = blockConfig.getBoolean("surface_air_only", false);
                List<String> surface_worlds = blockConfig.getStringList("surface_worlds");
                List<String> surface_biomes = blockConfig.getStringList("surface_biomes");

                // Check world for underground generation
                if (!worlds.isEmpty() && !worlds.contains(chunk.getWorld().getName())) {
                    // Check world for surface generation if underground generation is skipped
                    if (surface_min_y == -1 || (!surface_worlds.isEmpty() && !surface_worlds.contains(chunk.getWorld().getName()))) {
                        continue;
                    }
                }

                // Check biome for underground generation
                Biome chunkBiome = chunk.getBlock(0, 0, 0).getBiome(); // Get biome from a block in the chunk
                if (!biomes.isEmpty() && !biomes.contains(chunkBiome.name())) {
                    // Check biome for surface generation if underground generation is skipped
                    if (surface_min_y == -1 || (!surface_biomes.isEmpty() && !surface_biomes.contains(chunkBiome.name()))) {
                        continue;
                    }
                }

                // Underground generation logic
                generateBlocks(chunk, oraxenId, iterations, min_y, max_y, vein_size, chance, clusterChance, replaceableMaterials, placeOnMaterials, placeBelowMaterials, airOnly, biomes, chunkBiome);

                // Surface generation logic (if configured)
                if (surface_min_y != -1) {
                    generateBlocks(chunk, oraxenId, surfaceIterations, surface_min_y, surface_max_y, surface_vein_size, surface_chance, surface_clusterChance, surface_replaceableMaterials, surface_placeOnMaterials, surface_placeBelowMaterials, surface_airOnly, surface_biomes, chunkBiome);
                }
            }
        }
    }

    private void generateBlocks(Chunk chunk, String oraxenId, Optional<Integer> iterationsObj, int min_y, int max_y, Optional<Integer> veinSizeObj, double chance, double clusterChance, List<String> replaceableMaterials, List<String> placeOnMaterials, List<String> placeBelowMaterials, boolean airOnly, List<String> biomes, Biome chunkBiome) {
        if (!iterationsObj.isPresent()) return; // Skip if iterations is not configured for this generation type
        int attempts = iterationsObj.get();

        for (int i = 0; i < attempts; i++) {
            if (random.nextDouble() < chance) {
                int x = random.nextInt(16);
                int z = random.nextInt(16);
                int y = random.nextInt(max_y - min_y + 1) + min_y;

                Location blockLoc = new Location(chunk.getWorld(), chunk.getX() * 16 + x, y, chunk.getZ() * 16 + z);
                Block block = blockLoc.getBlock();

                if (!biomes.isEmpty() && !biomes.contains(block.getBiome().name())) {
                    continue;
                }

                int currentVeinSize = veinSizeObj.orElse(1); // Default to 1 if not configured

                if (currentVeinSize > 1 && random.nextDouble() < clusterChance) {
                    generateVein(blockLoc, oraxenId, currentVeinSize, replaceableMaterials, placeOnMaterials, placeBelowMaterials, airOnly);
                } else {
                    if (canPlaceBlock(block, replaceableMaterials, placeOnMaterials, placeBelowMaterials, airOnly)) {
                        if (OraxenBlocks.getOraxenBlock(block.getLocation()) == null) { // Only place if not already an Oraxen block
                            if (io.th0rgal.oraxen.api.OraxenItems.getItemById(oraxenId) != null) {
                                OraxenBlocks.place(oraxenId, blockLoc);
                            } else {
                                Logger.warning("Invalid Oraxen ID '" + oraxenId + "' for block placement at " + blockLoc.toVector().toString() + ". Skipping.");
                            }
                        } else {
                            Logger.warning("Skipping block placement at " + blockLoc.toVector().toString() + " as it's already an Oraxen block.");
                        }
                    }
                }
            }
        }
    }

    private void generateVein(Location startLoc, String oraxenId, int veinSize, List<String> replaceableMaterials, List<String> placeOnMaterials, List<String> placeBelowMaterials, boolean airOnly) {
        Location currentLoc = startLoc;
        for (int i = 0; i < veinSize; i++) {
            Block currentBlock = currentLoc.getBlock();
            if (canPlaceBlock(currentBlock, replaceableMaterials, placeOnMaterials, placeBelowMaterials, airOnly)) {
                if (OraxenBlocks.getOraxenBlock(currentLoc) == null) { // Only place if not already an Oraxen block
                    if (io.th0rgal.oraxen.api.OraxenItems.getItemById(oraxenId) != null) {
                        OraxenBlocks.place(oraxenId, currentLoc);
                    } else {
                        Logger.warning("Invalid Oraxen ID '" + oraxenId + "' for vein block placement at " + currentLoc.toVector().toString() + ". Skipping.");
                    }
                } else {
                    Logger.warning("Skipping vein block placement at " + currentLoc.toVector().toString() + " as it's already an Oraxen block.");
                }
            }

            // Move to an adjacent block for the next iteration of the vein
            currentLoc = getAdjacentLocation(currentLoc);
        }
    }

    private boolean canPlaceBlock(Block block, List<String> replaceableMaterials, List<String> placeOnMaterials, List<String> placeBelowMaterials, boolean airOnly) {
        Material blockType = block.getType();
        Material blockAboveType = block.getLocation().add(0, 1, 0).getBlock().getType();
        Material blockBelowType = block.getLocation().add(0, -1, 0).getBlock().getType();

        // Check place_on
        if (!placeOnMaterials.isEmpty()) {
            if (placeOnMaterials.contains(blockBelowType.name())) {
                return !airOnly || blockType == Material.AIR;
            }
            return false;
        }

        // Check place_below
        if (!placeBelowMaterials.isEmpty()) {
            if (placeBelowMaterials.contains(blockAboveType.name())) {
                return !airOnly || blockType == Material.AIR;
            }
            return false;
        }

        // Check replaceable_materials
        if (!replaceableMaterials.isEmpty()) {
            return replaceableMaterials.contains(blockType.name());
        }

        return true; // If no specific rules, allow placement
    }

    private Location getAdjacentLocation(Location loc) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        int rand = random.nextInt(6); // 0-5 for 6 directions
        switch (rand) {
            case 0: return new Location(loc.getWorld(), x + 1, y, z);
            case 1: return new Location(loc.getWorld(), x - 1, y, z);
            case 2: return new Location(loc.getWorld(), x, y + 1, z);
            case 3: return new Location(loc.getWorld(), x, y - 1, z);
            case 4: return new Location(loc.getWorld(), x, y, z + 1);
            case 5: return new Location(loc.getWorld(), x, y, z - 1);
        }
        return loc; // Should not happen
    }

    private Optional<Integer> getIntValue(Object value) {
        if (value instanceof Integer) {
            return Optional.of((Integer) value);
        } else if (value instanceof String) {
            String str = (String) value;
            if (str.contains("-")) {
                String[] parts = str.split("-");
                try {
                    int min = Integer.parseInt(parts[0].trim());
                    int max = Integer.parseInt(parts[1].trim());
                    return Optional.of(random.nextInt(max - min + 1) + min);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid number format for range: " + str + ".");
                    return Optional.empty();
                }
            } else {
                try {
                    return Optional.of(Integer.parseInt(str.trim()));
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid number format: " + str + ".");
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    private Optional<Integer> parseIterationValue(ConfigurationSection section, String key, int defaultValue) {
        Object value = section.get(key, defaultValue);
        if (value instanceof String str) {
            if (str.contains("-")) {
                String[] parts = str.split("-");
                try {
                    int min = Integer.parseInt(parts[0].trim());
                    int max = Integer.parseInt(parts[1].trim());
                    if (min > max) {
                        int temp = min;
                        min = max;
                        max = temp;
                    }
                    return Optional.of(min + random.nextInt(max - min + 1));
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid number format for range in config ('" + key + "'): " + str + ". Using default value: " + defaultValue);
                    return Optional.of(defaultValue);
                }
            } else {
                try {
                    return Optional.of(Integer.parseInt(str.trim()));
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid number format in config ('" + key + "'): " + str + ". Using default value: " + defaultValue);
                    return Optional.of(defaultValue);
                }
            }
        } else if (value instanceof Number) {
            return Optional.of(((Number) value).intValue());
        }
        plugin.getLogger().warning("Invalid value type for config ('" + key + "'): " + value + ". Using default value: " + defaultValue);
        return Optional.of(defaultValue);
    }
}
