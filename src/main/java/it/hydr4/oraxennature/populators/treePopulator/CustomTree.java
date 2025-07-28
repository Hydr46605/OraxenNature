package it.hydr4.oraxennature.populators.treePopulator;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Map;

public class CustomTree {

    private final String id;
    private final String logOraxenId;
    private final String leafOraxenId;
    private final double chance;
    private final List<String> worlds;
    private final List<String> biomes;
    private final List<String> surfaceMaterials;

    // New, simplified configuration
    private final String shape;
    private final int minHeight;
    private final int maxHeight;
    private final int canopyMinRadius;
    private final int canopyMaxRadius;
    private final double canopyDensity;

    private final String schematic;
    private final Map<String, String> blockReplacements;

    public CustomTree(String id, ConfigurationSection config) {
        this.id = id;
        this.logOraxenId = config.getString("log_oraxen_id");
        this.leafOraxenId = config.getString("leaf_oraxen_id");
        this.chance = config.getDouble("chance", 0.1);
        this.worlds = config.getStringList("worlds");
        this.biomes = config.getStringList("biomes");
        this.surfaceMaterials = config.getStringList("surface_materials");

        // New simplified config
        this.shape = config.getString("shape", "OAK");
        this.minHeight = config.getInt("height.min", 5);
        this.maxHeight = config.getInt("height.max", 8);
        this.canopyMinRadius = config.getInt("canopy.radius.min", 2);
        this.canopyMaxRadius = config.getInt("canopy.radius.max", 4);
        this.canopyDensity = config.getDouble("canopy.density", 0.6);
        this.schematic = config.getString("schematic");
        Map<String, String> replacements = new java.util.HashMap<>();
        ConfigurationSection replacementsSection = config.getConfigurationSection("block_replacements");
        if (replacementsSection != null) {
            for (String key : replacementsSection.getKeys(false)) {
                replacements.put(key, replacementsSection.getString(key));
            }
        }
        this.blockReplacements = replacements;
    }

    // Getters for all fields

    public String getId() {
        return id;
    }

    public String getLogOraxenId() {
        return logOraxenId;
    }

    public String getLeafOraxenId() {
        return leafOraxenId;
    }

    public double getChance() {
        return chance;
    }

    public List<String> getWorlds() {
        return worlds;
    }

    public List<String> getBiomes() {
        return biomes;
    }

    public List<String> getSurfaceMaterials() {
        return surfaceMaterials;
    }

    public String getShape() {
        return shape;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public int getCanopyMinRadius() {
        return canopyMinRadius;
    }

    public int getCanopyMaxRadius() {
        return canopyMaxRadius;
    }

    public double getCanopyDensity() {
        return canopyDensity;
    }

    public String getSchematic() {
        return schematic;
    }

    public Map<String, String> getBlockReplacements() {
        return blockReplacements;
    }
}
