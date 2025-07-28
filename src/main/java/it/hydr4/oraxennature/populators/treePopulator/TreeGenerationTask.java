package it.hydr4.oraxennature.populators.treePopulator;

import it.hydr4.oraxennature.OraxenNature;
import it.hydr4.oraxennature.utils.Logger;
import io.th0rgal.oraxen.api.OraxenBlocks;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TreeGenerationTask extends BukkitRunnable {

    private final OraxenNature plugin;
    private final Queue<Map.Entry<Location, String>> blocksToPlace;
    private final int batchSize;

    public TreeGenerationTask(OraxenNature plugin, List<Map.Entry<Location, String>> blocksToPlace) {
        this.plugin = plugin;
        this.blocksToPlace = new ConcurrentLinkedQueue<>(blocksToPlace);
        this.batchSize = plugin.getSettingsConfig().getInt("tree_generation_batch_size", 100);
        Logger.debug("Starting TreeGenerationTask with " + blocksToPlace.size() + " blocks to place, batch size: " + batchSize);
    }

    @Override
    public void run() {
        if (blocksToPlace.isEmpty()) {
            Logger.debug("TreeGenerationTask finished.");
            cancel();
            return;
        }

        int placedCount = 0;
        while (!blocksToPlace.isEmpty() && placedCount < batchSize) {
            Map.Entry<Location, String> entry = blocksToPlace.poll();
            if (entry != null) {
                Location loc = entry.getKey();
                String oraxenId = entry.getValue();

                if (loc.getBlock().isReplaceable()) {
                    OraxenBlocks.place(oraxenId, loc);
                    placedCount++;
                }
            }
        }

        if (blocksToPlace.isEmpty()) {
            Logger.debug("TreeGenerationTask completed all blocks.");
            cancel();
        }
    }
}

