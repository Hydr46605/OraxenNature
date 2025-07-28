package it.hydr4.oraxennature.growth;

import it.hydr4.oraxennature.OraxenNature;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.mechanics.Mechanic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GrowthTask extends BukkitRunnable {

    private final OraxenNature plugin;
    private final Map<String, GrowableBlock> growableBlocks;
    private final List<TrackedBlock> trackedBlocks = new ArrayList<>();

    public GrowthTask(OraxenNature plugin, Map<String, GrowableBlock> growableBlocks) {
        this.plugin = plugin;
        this.growableBlocks = growableBlocks;
    }

    public void addTrackedBlock(Block block, GrowableBlock growableBlock) {
        trackedBlocks.add(new TrackedBlock(block.getLocation(), growableBlock));
    }

    @Override
    public void run() {
        it.hydr4.oraxennature.utils.Logger.debug("Running GrowthTask. Tracked blocks: " + trackedBlocks.size());
        if (plugin.getServer().getPluginManager().getPlugin("Oraxen") == null || !plugin.getServer().getPluginManager().getPlugin("Oraxen").isEnabled()) {
            plugin.getLogger().warning("Oraxen is not enabled. Skipping growth task.");
            return;
        }

        trackedBlocks.removeIf(trackedBlock -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - trackedBlock.getLastGrowthTime() >= trackedBlock.getGrowableBlock().getGrowthIntervalSeconds() * 1000L) {
                trackedBlock.setLastGrowthTime(currentTime);
                checkAndApplyGrowth(trackedBlock.getLocation().getBlock(), trackedBlock.getGrowableBlock());
            }
            return false; // Keep the block in the list
        });
    }

    private boolean checkAndApplyGrowth(Block block, GrowableBlock growable) {
        Mechanic currentOraxenMechanic = OraxenBlocks.getOraxenBlock(block.getLocation());
        String currentOraxenId = (currentOraxenMechanic != null) ? currentOraxenMechanic.getItemID() : null;

        if (currentOraxenId == null) return false;

        int currentStageIndex = growable.getGrowthStages().indexOf(currentOraxenId);

        if (currentOraxenId.equals(growable.getInitialOraxenId()) || currentStageIndex != -1) {
            if (growable.getGrowthConditions().check(block)) {
                if (currentStageIndex < growable.getGrowthStages().size() - 1) {
                    String nextStageId = growable.getGrowthStages().get(currentStageIndex + 1);
                    if (OraxenItems.getItemById(nextStageId) != null) {
                        OraxenBlocks.place(nextStageId, block.getLocation());
                        plugin.getLogger().info("Growable block '" + growable.getId() + "' grew to stage '" + nextStageId + "' at " + block.getLocation().toVector().toString());
                        return false;
                    } else {
                        it.hydr4.oraxennature.utils.Logger.warning("Invalid Oraxen ID '" + nextStageId + "' for growth placement at " + block.getLocation().toVector().toString() + ". Skipping.");
                    }
                } else if (currentStageIndex == -1 && !growable.getGrowthStages().isEmpty()) {
                    String firstStageId = growable.getGrowthStages().get(0);
                    if (OraxenItems.getItemById(firstStageId) != null) {
                        OraxenBlocks.place(firstStageId, block.getLocation());
                        plugin.getLogger().info("Growable block '" + growable.getId() + "' grew to initial stage '" + firstStageId + "' at " + block.getLocation().toVector().toString());
                        return false;
                    } else {
                        it.hydr4.oraxennature.utils.Logger.warning("Invalid Oraxen ID '" + firstStageId + "' for initial growth placement at " + block.getLocation().toVector().toString() + ". Skipping.");
                    }
                }
            } else if (growable.getDecayConditions().check(block)) {
                if (currentStageIndex > 0) {
                    String previousStageId = growable.getGrowthStages().get(currentStageIndex - 1);
                    if (OraxenItems.getItemById(previousStageId) != null) {
                        OraxenBlocks.place(previousStageId, block.getLocation());
                        plugin.getLogger().info("Growable block '" + growable.getId() + "' decayed to stage '" + previousStageId + "' at " + block.getLocation().toVector().toString());
                        return false;
                    } else {
                        it.hydr4.oraxennature.utils.Logger.warning("Invalid Oraxen ID '" + previousStageId + "' for decay placement at " + block.getLocation().toVector().toString() + ". Skipping.");
                    }
                } else if (currentStageIndex == 0 || currentOraxenId.equals(growable.getInitialOraxenId())) {
                    block.setType(org.bukkit.Material.AIR);
                    plugin.getLogger().info("Growable block '" + growable.getId() + "' decayed to air at " + block.getLocation().toVector().toString());
                    return true;
                }
            }
        }
        return false;
    }
}