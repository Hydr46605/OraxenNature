package it.hydr4.oraxennature.populators.treePopulator;

import it.hydr4.oraxennature.OraxenNature;
import it.hydr4.oraxennature.utils.Logger;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomTreePopulator extends BlockPopulator {

    private final OraxenNature plugin;
    private final List<CustomTree> customTrees;
    private final Random random;
    private final TreeGenerator treeGenerator;

    public CustomTreePopulator(OraxenNature plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.customTrees = loadCustomTrees(config);
        this.random = new Random();
        this.treeGenerator = new TreeGenerator(plugin);
    }

    private List<CustomTree> loadCustomTrees(FileConfiguration config) {
        List<CustomTree> trees = new ArrayList<>();
        if (!config.isConfigurationSection("trees")) {
            Logger.error("tree_populator.yml is missing the 'trees' section or is malformed. Tree population may not work as expected.");
            return trees;
        }

        ConfigurationSection treesSection = config.getConfigurationSection("trees");
        if (treesSection != null) {
            for (String key : treesSection.getKeys(false)) {
                ConfigurationSection treeConfig = treesSection.getConfigurationSection(key);
                if (treeConfig == null) {
                    Logger.warning("Invalid configuration section for tree: " + key + ". Skipping.");
                    continue;
                }

                if (!treeConfig.getBoolean("enabled", true)) {
                    Logger.debug("Tree entry '" + key + "' is disabled in tree_populator.yml. Skipping loading.");
                    continue;
                }

                trees.add(new CustomTree(key, treeConfig));
            }
        }
        return trees;
    }

    public List<CustomTree> getLoadedTrees() {
        return customTrees;
    }

    @Override
    public void populate(@NotNull World world, @NotNull Random random, @NotNull Chunk chunk) {
        if (plugin.getServer().getPluginManager().getPlugin("Oraxen") == null || !plugin.getServer().getPluginManager().getPlugin("Oraxen").isEnabled()) {
            return;
        }

        for (CustomTree tree : customTrees) {
            if (!tree.getWorlds().isEmpty() && !tree.getWorlds().contains(world.getName())) {
                continue;
            }

            Biome chunkBiome = chunk.getBlock(8, 128, 8).getBiome();
            if (!tree.getBiomes().isEmpty() && !tree.getBiomes().contains(chunkBiome.name())) {
                continue;
            }

            if (this.random.nextDouble() < tree.getChance()) {
                int x = this.random.nextInt(16) + chunk.getX() * 16;
                int z = this.random.nextInt(16) + chunk.getZ() * 16;
                int y = world.getHighestBlockYAt(x, z);

                Location surfaceLoc = new Location(world, x, y, z);
                Material surfaceBlock = surfaceLoc.getBlock().getType();

                if (tree.getSurfaceMaterials().contains(surfaceBlock.name())) {
                    Location trunkBaseLoc = surfaceLoc.clone().add(0, 1, 0);
                    treeGenerator.generateTree(trunkBaseLoc, tree);
                    Logger.info("Successfully spawned tree '" + tree.getId() + "' at " + trunkBaseLoc.toVector());
                }
            }
        }
    }
}
