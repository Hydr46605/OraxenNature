package it.hydr4.oraxennature.growth;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Ageable;
import org.bukkit.Material;

import java.util.List;

public class GrowableBlock {
    private final String id;
    private final String initialOraxenId;
    private final List<String> growthStages;
    private final int growthIntervalSeconds;
    private final GrowthConditions growthConditions;
    private final GrowthConditions decayConditions;

    public GrowableBlock(String id, String initialOraxenId, List<String> growthStages, int growthIntervalSeconds, GrowthConditions growthConditions, GrowthConditions decayConditions) {
        this.id = id;
        this.initialOraxenId = initialOraxenId;
        this.growthStages = growthStages;
        this.growthIntervalSeconds = growthIntervalSeconds;
        this.growthConditions = growthConditions;
        this.decayConditions = decayConditions;
    }

    public String getId() {
        return id;
    }

    public String getInitialOraxenId() {
        return initialOraxenId;
    }

    public List<String> getGrowthStages() {
        return growthStages;
    }

    public int getGrowthIntervalSeconds() {
        return growthIntervalSeconds;
    }

    public GrowthConditions getGrowthConditions() {
        return growthConditions;
    }

    public GrowthConditions getDecayConditions() {
        return decayConditions;
    }

    public static class GrowthConditions {
        private final int lightLevelMin;
        private final int lightLevelMax;
        private final List<String> allowedBlockBelow;
        private final List<String> allowedBiomes;
        private final List<String> allowedWorlds;

        public GrowthConditions(int lightLevelMin, int lightLevelMax, List<String> allowedBlockBelow, List<String> allowedBiomes, List<String> allowedWorlds) {
            this.lightLevelMin = lightLevelMin;
            this.lightLevelMax = lightLevelMax;
            this.allowedBlockBelow = allowedBlockBelow;
            this.allowedBiomes = allowedBiomes;
            this.allowedWorlds = allowedWorlds;
        }

        public boolean check(Block block) {
            // Light level check
            int lightLevel = block.getLightFromBlocks(); // Or getLightFromSky()
            if (lightLevel < lightLevelMin || lightLevel > lightLevelMax) {
                return false;
            }

            // Block below check
            if (!allowedBlockBelow.isEmpty()) {
                if (!allowedBlockBelow.contains(block.getRelative(BlockFace.DOWN).getType().name())) {
                    return false;
                }
            }

            // Biome check
            if (!allowedBiomes.isEmpty()) {
                if (!allowedBiomes.contains(block.getBiome().name())) {
                    return false;
                }
            }

            // World check
            if (!allowedWorlds.isEmpty()) {
                if (!allowedWorlds.contains(block.getWorld().getName())) {
                    return false;
                }
            }

            return true;
        }
    }
}