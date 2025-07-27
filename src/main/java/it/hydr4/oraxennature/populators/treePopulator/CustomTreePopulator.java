package it.hydr4.oraxennature.populators.treePopulator;

import it.hydr4.oraxennature.OraxenNature;
import it.hydr4.oraxennature.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class CustomTreePopulator extends BlockPopulator {

    private final OraxenNature plugin;
    private final List<CustomTree> customTrees;
    private final Random random;
    private final List<String> loadedTreeNames = new ArrayList<>();

    public CustomTreePopulator(OraxenNature plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.customTrees = loadCustomTrees(config);
        this.random = new Random();
    }

    private List<CustomTree> loadCustomTrees(FileConfiguration config) {
        List<CustomTree> trees = new ArrayList<>();
        // No need to load file here, config is passed in
        if (!config.isConfigurationSection("trees")) {
            Logger.error("tree_populator.yml is missing the 'trees' section or is malformed. Tree population may not work as expected.");
        }

        ConfigurationSection treesSection = config.getConfigurationSection("trees");
        if (treesSection != null) {
            for (String key : treesSection.getKeys(false)) {
                ConfigurationSection treeConfig = treesSection.getConfigurationSection(key);
                if (treeConfig == null) {
                    Logger.warning("Invalid configuration section for tree: " + key + ". Skipping.");
                    continue;
                }

                String logId = treeConfig.getString("log_oraxen_id");
                String leafId = treeConfig.getString("leaf_oraxen_id");
                int minY = treeConfig.getInt("min_y");
                int maxY = treeConfig.getInt("max_y");
                double chance = treeConfig.getDouble("chance");
                List<String> worlds = treeConfig.getStringList("worlds");
                List<String> biomes = treeConfig.getStringList("biomes");

                // New advanced parameters
                int trunkHeight = treeConfig.getInt("trunk_height", 5);
                int branchLengthMin = treeConfig.getInt("branch_length_min", 2);
                int branchLengthMax = treeConfig.getInt("branch_length_max", 4);
                int branchAngleVariation = treeConfig.getInt("branch_angle_variation", 30);
                int maxBranches = treeConfig.getInt("max_branches", 5);
                int leafRadius = treeConfig.getInt("leaf_radius", 2);
                double leafDensity = treeConfig.getDouble("leaf_density", 0.8);
                String treeType = treeConfig.getString("tree_type", "BRANCHING");
                boolean enabled = treeConfig.getBoolean("enabled", true); // Default to true if not specified

                if (logId != null && leafId != null) {
                    trees.add(new CustomTree(key, logId, leafId, minY, maxY, chance, worlds, biomes,
                            trunkHeight, branchLengthMin, branchLengthMax, branchAngleVariation, maxBranches, leafRadius, leafDensity, treeType, enabled));
                    loadedTreeNames.add(key);
                } else {
                    Logger.warning("Invalid tree configuration for '" + key + "'. Missing log_oraxen_id or leaf_oraxen_id.");
                }
            }
        }
        return trees;
    }

    public List<String> getLoadedTreeNames() {
        return loadedTreeNames;
    }

    @Override
    public void populate(@NotNull World world, @NotNull Random random, @NotNull Chunk chunk) {
        // Check if Oraxen is enabled and loaded
        if (plugin.getServer().getPluginManager().getPlugin("Oraxen") == null || !plugin.getServer().getPluginManager().getPlugin("Oraxen").isEnabled()) {
            plugin.getLogger().warning("Oraxen is not enabled. Skipping tree population in world " + world.getName() + ".");
            return;
        }

        for (CustomTree tree : customTrees) {
            if (!tree.isEnabled()) {
                plugin.getLogger().info("Tree entry '" + tree.getId() + "' is disabled in tree_populator.yml. Skipping.");
                continue;
            }

            if (!tree.getWorlds().isEmpty() && !tree.getWorlds().contains(world.getName())) {
                continue;
            }

            Biome chunkBiome = chunk.getBlock(0, 0, 0).getBiome();
            if (!tree.getBiomes().isEmpty() && !tree.getBiomes().contains(chunkBiome.name())) {
                continue;
            }

            if (random.nextDouble() < tree.getChance()) {
                int x = random.nextInt(16);
                int z = random.nextInt(16);
                int y = random.nextInt(tree.getMaxY() - tree.getMinY() + 1) + tree.getMinY();

                Location trunkBaseLoc = new Location(world, chunk.getX() * 16 + x, y, chunk.getZ() * 16 + z);
                generateAdvancedTree(trunkBaseLoc, tree);
                plugin.getLogger().info("Generated custom tree '" + tree.getId() + "' at " + trunkBaseLoc.toVector().toString() + " in world " + world.getName());
            }
        }
    }

    private void generateAdvancedTree(Location trunkBaseLoc, CustomTree tree) {
        // Generate trunk
        for (int i = 0; i < tree.getTrunkHeight(); i++) {
            Location loc = trunkBaseLoc.clone().add(0, i, 0);
            if (loc.getBlock().getType() == Material.AIR || loc.getBlock().getType() == Material.DIRT || loc.getBlock().getType() == Material.GRASS_BLOCK) {
                if (OraxenBlocks.getOraxenBlock(loc) == null) { // Only place if not already an Oraxen block
                    if (io.th0rgal.oraxen.api.OraxenItems.getItemById(tree.getLogOraxenId()) != null) {
                        OraxenBlocks.place(tree.getLogOraxenId(), loc);
                    } else {
                        plugin.getLogger().warning("Invalid Oraxen ID '" + tree.getLogOraxenId() + "' for log placement at " + loc.toVector().toString() + ". Skipping.");
                    }
                } else {
                    plugin.getLogger().warning("Skipping log placement at " + loc.toVector().toString() + " as it's already an Oraxen block.");
                }
            }
        }

        // Start branching from the top of the trunk
        Location branchStartLoc = trunkBaseLoc.clone().add(0, tree.getTrunkHeight(), 0);
        generateBranch(branchStartLoc, tree, 0, 0, 1.0); // Initial direction: upwards (pitch 0, yaw 0), scale 1.0
    }

    private void generateBranch(Location currentLoc, CustomTree tree, double pitch, double yaw, double scale) {
        if (scale < 0.1) return; // Stop branching if scale is too small

        int branchLength = random.nextInt(tree.getBranchLengthMax() - tree.getBranchLengthMin() + 1) + tree.getBranchLengthMin();

        for (int i = 0; i < branchLength; i++) {
            // Calculate new position based on pitch and yaw
            double dx = Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * scale;
            double dy = Math.sin(Math.toRadians(pitch)) * scale;
            double dz = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * scale;

            currentLoc.add(dx, dy, dz);
            Block block = currentLoc.getBlock();

            if (block.getType() == Material.AIR || block.getType().isOccluding()) { // Place log if air or replaceable
                if (OraxenBlocks.getOraxenBlock(currentLoc) == null) { // Only place if not already an Oraxen block
                    if (io.th0rgal.oraxen.api.OraxenItems.getItemById(tree.getLogOraxenId()) != null) {
                        OraxenBlocks.place(tree.getLogOraxenId(), currentLoc);
                    } else {
                        plugin.getLogger().warning("Invalid Oraxen ID '" + tree.getLogOraxenId() + "' for branch log placement at " + currentLoc.toVector().toString() + ". Skipping.");
                    }
                } else {
                    plugin.getLogger().warning("Skipping branch log placement at " + currentLoc.toVector().toString() + " as it's already an Oraxen block.");
                }
            }

            // Place leaves around the branch segment
            placeLeaves(currentLoc, tree.getLeafOraxenId(), tree.getLeafRadius(), tree.getLeafDensity());
        }

        // Branching out
        if (tree.getMaxBranches() > 0) {
            for (int i = 0; i < random.nextInt(tree.getMaxBranches()) + 1; i++) {
                double newPitch = pitch + (random.nextDouble() * tree.getBranchAngleVariation() * 2) - tree.getBranchAngleVariation();
                double newYaw = yaw + (random.nextDouble() * tree.getBranchAngleVariation() * 2) - tree.getBranchAngleVariation();
                generateBranch(currentLoc.clone(), tree, newPitch, newYaw, scale * 0.8); // Reduce scale for sub-branches
            }
        }
    }

    private void placeLeaves(Location centerLoc, String leafId, int radius, double density) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z <= radius * radius) { // Sphere shape
                        if (random.nextDouble() < density) {
                            Location loc = centerLoc.clone().add(x, y, z);
                            if (loc.getBlock().getType() == Material.AIR) {
                                if (OraxenBlocks.getOraxenBlock(loc) == null) { // Only place if not already an Oraxen block
                                    if (io.th0rgal.oraxen.api.OraxenItems.getItemById(leafId) != null) {
                                        OraxenBlocks.place(leafId, loc);
                                    } else {
                                        plugin.getLogger().warning("Invalid Oraxen ID '" + leafId + "' for leaf placement at " + loc.toVector().toString() + ". Skipping.");
                                    }
                                } else {
                                    plugin.getLogger().warning("Skipping leaf placement at " + loc.toVector().toString() + " as it's already an Oraxen block.");
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}