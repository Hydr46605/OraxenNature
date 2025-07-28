package it.hydr4.oraxennature.listeners;

import it.hydr4.oraxennature.OraxenNature;
import org.bukkit.Chunk;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PopulationTask extends BukkitRunnable {

    private final OraxenNature plugin;
    private final Queue<Chunk> chunkQueue = new ConcurrentLinkedQueue<>();

    public PopulationTask(OraxenNature plugin) {
        this.plugin = plugin;
    }

    public void queueChunk(Chunk chunk) {
        it.hydr4.oraxennature.utils.Logger.debug("Queuing chunk for population: " + chunk.getX() + ", " + chunk.getZ());
        chunkQueue.add(chunk);
    }

    @Override
    public void run() {
        if (!chunkQueue.isEmpty()) {
            Chunk chunk = chunkQueue.poll();
            plugin.getBlockPopulator().populate(chunk);
            plugin.getTreePopulator().populate(chunk.getWorld(), new java.util.Random(chunk.getWorld().getSeed()), chunk);
        }
    }
}
