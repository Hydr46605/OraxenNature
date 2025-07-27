package it.hydr4.oraxennature.growth;

import it.hydr4.oraxennature.OraxenNature;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.mechanics.Mechanic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Arrays;

public class GrowthTask extends BukkitRunnable {

    private final OraxenNature plugin;
    private final Map<String, GrowableBlock> growableBlocks;
    private final Map<Location, Long> lastGrowthAttempt;

    public GrowthTask(OraxenNature plugin, Map<String, GrowableBlock> growableBlocks) {
        this.plugin = plugin;
        this.growableBlocks = growableBlocks;
        this.lastGrowthAttempt = new HashMap<>();
    }

    @Override
    public void run() {
        // Check if Oraxen is enabled and loaded
        if (plugin.getServer().getPluginManager().getPlugin("Oraxen") == null || !plugin.getServer().getPluginManager().getPlugin("Oraxen").isEnabled()) {
            plugin.getLogger().warning("Oraxen is not enabled. Skipping growth task.");
            return;
        }

        // Iterate over all loaded chunks in all worlds
        Bukkit.getWorlds().forEach(world -> {
            Arrays.asList(world.getLoadedChunks()).forEach(chunk -> { // Convert Chunk[] to List
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
                            Block block = chunk.getBlock(x, y, z);
                            Mechanic oraxenMechanic = OraxenBlocks.getOraxenBlock(block.getLocation());
                            String oraxenId = (oraxenMechanic != null) ? oraxenMechanic.getItemID() : null;

                            if (oraxenId != null) {
                                GrowableBlock growable = growableBlocks.get(oraxenId);
                                if (growable != null) {
                                    long currentTime = System.currentTimeMillis();
                                    long lastAttempt = lastGrowthAttempt.getOrDefault(block.getLocation(), 0L);

                                    if (currentTime - lastAttempt >= growable.getGrowthIntervalSeconds() * 1000L) {
                                        lastGrowthAttempt.put(block.getLocation(), currentTime);
                                        checkAndApplyGrowth(block, growable);
                                    }
                                }
                            }
                        }
                    }
                }
            });
        });
    }

    private void checkAndApplyGrowth(Block block, GrowableBlock growable) {
        Mechanic currentOraxenMechanic = OraxenBlocks.getOraxenBlock(block.getLocation());
        String currentOraxenId = (currentOraxenMechanic != null) ? currentOraxenMechanic.getItemID() : null;

        if (currentOraxenId == null) return; // Should not happen if it was found in run()

        int currentStageIndex = growable.getGrowthStages().indexOf(currentOraxenId);

        // If it's the initial block or a known growth stage
        if (currentOraxenId.equals(growable.getInitialOraxenId()) || currentStageIndex != -1) {
            // Check for growth
            if (growable.getGrowthConditions().check(block)) {
                if (currentStageIndex < growable.getGrowthStages().size() - 1) { // Not yet at final stage
                    String nextStageId = growable.getGrowthStages().get(currentStageIndex + 1);
                    if (OraxenBlocks.getOraxenBlock(block.getLocation()) == null) { // Only place if not already an Oraxen block
                        if (io.th0rgal.oraxen.api.OraxenItems.getItemById(nextStageId) != null) {
                            OraxenBlocks.place(nextStageId, block.getLocation());
                            plugin.getLogger().info("Growable block '" + growable.getId() + "' grew to stage '" + nextStageId + "' at " + block.getLocation().toVector().toString());
                        } else {
                            plugin.getLogger().warning("Invalid Oraxen ID '" + nextStageId + "' for growth placement at " + block.getLocation().toVector().toString() + ". Skipping.");
                        }
                    } else {
                        plugin.getLogger().warning("Skipping growth placement at " + block.getLocation().toVector().toString() + " as it's already an Oraxen block.");
                    }
                } else if (currentStageIndex == -1 && !growable.getGrowthStages().isEmpty()) { // Initial block, grow to first stage
                    String firstStageId = growable.getGrowthStages().get(0);
                    if (OraxenBlocks.getOraxenBlock(block.getLocation()) == null) { // Only place if not already an Oraxen block
                        if (io.th0rgal.oraxen.api.OraxenItems.getItemById(firstStageId) != null) {
                            OraxenBlocks.place(firstStageId, block.getLocation());
                            plugin.getLogger().info("Growable block '" + growable.getId() + "' grew to initial stage '" + firstStageId + "' at " + block.getLocation().toVector().toString());
                        } else {
                            plugin.getLogger().warning("Invalid Oraxen ID '" + firstStageId + "' for initial growth placement at " + block.getLocation().toVector().toString() + ". Skipping.");
                        }
                    } else {
                        plugin.getLogger().warning("Skipping initial growth placement at " + block.getLocation().toVector().toString() + " as it's already an Oraxen block.");
                    }
                }
            }
            // Check for decay
            else if (growable.getDecayConditions().check(block)) {
                if (currentStageIndex > 0) { // Not yet at initial stage
                    String previousStageId = growable.getGrowthStages().get(currentStageIndex - 1);
                    if (OraxenBlocks.getOraxenBlock(block.getLocation()) == null) { // Only place if not already an Oraxen block
                        if (io.th0rgal.oraxen.api.OraxenItems.getItemById(previousStageId) != null) {
                            OraxenBlocks.place(previousStageId, block.getLocation());
                            plugin.getLogger().info("Growable block '" + growable.getId() + "' decayed to stage '" + previousStageId + "' at " + block.getLocation().toVector().toString());
                        } else {
                            plugin.getLogger().warning("Invalid Oraxen ID '" + previousStageId + "' for decay placement at " + block.getLocation().toVector().toString() + ". Skipping.");
                        }
                    } else {
                        plugin.getLogger().warning("Skipping decay placement at " + block.getLocation().toVector().toString() + " as it's already an Oraxen block.");
                    }
                } else if (currentStageIndex == 0 || currentOraxenId.equals(growable.getInitialOraxenId())) { // At first stage or initial, decay to air
                    block.setType(org.bukkit.Material.AIR);
                    plugin.getLogger().info("Growable block '" + growable.getId() + "' decayed to air at " + block.getLocation().toVector().toString());
                }
            }
        }
    }
}