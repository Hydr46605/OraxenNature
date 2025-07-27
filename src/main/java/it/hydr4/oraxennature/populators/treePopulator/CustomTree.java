package it.hydr4.oraxennature.populators.treePopulator;

import org.bukkit.block.Biome;

import java.util.List;

public class CustomTree {
    private final String id;
    private final String logOraxenId;
    private final String leafOraxenId;
    private final int minY;
    private final int maxY;
    private final double chance;
    private final List<String> worlds;
    private final List<String> biomes;
    private final boolean enabled;

    // New advanced parameters
    private final int trunkHeight;
    private final int branchLengthMin;
    private final int branchLengthMax;
    private final int branchAngleVariation;
    private final int maxBranches;
    private final int leafRadius;
    private final double leafDensity;
    private final String treeType;

    public CustomTree(String id, String logOraxenId, String leafOraxenId, int minY, int maxY, double chance, List<String> worlds, List<String> biomes,
                      int trunkHeight, int branchLengthMin, int branchLengthMax, int branchAngleVariation, int maxBranches, int leafRadius, double leafDensity, String treeType, boolean enabled) {
        this.id = id;
        this.logOraxenId = logOraxenId;
        this.leafOraxenId = leafOraxenId;
        this.minY = minY;
        this.maxY = maxY;
        this.chance = chance;
        this.worlds = worlds;
        this.biomes = biomes;
        this.trunkHeight = trunkHeight;
        this.branchLengthMin = branchLengthMin;
        this.branchLengthMax = branchLengthMax;
        this.branchAngleVariation = branchAngleVariation;
        this.maxBranches = maxBranches;
        this.leafRadius = leafRadius;
        this.leafDensity = leafDensity;
        this.treeType = treeType;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getId() {
        return id;
    }

    public String getLogOraxenId() {
        return logOraxenId;
    }

    public String getLeafOraxenId() {
        return leafOraxenId;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
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

    public int getTrunkHeight() {
        return trunkHeight;
    }

    public int getBranchLengthMin() {
        return branchLengthMin;
    }

    public int getBranchLengthMax() {
        return branchLengthMax;
    }

    public int getBranchAngleVariation() {
        return branchAngleVariation;
    }

    public int getMaxBranches() {
        return maxBranches;
    }

    public int getLeafRadius() {
        return leafRadius;
    }

    public double getLeafDensity() {
        return leafDensity;
    }

    public String getTreeType() {
        return treeType;
    }
}