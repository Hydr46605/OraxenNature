package it.hydr4.oraxennature.listeners;

import it.hydr4.oraxennature.OraxenNature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkLoadListener implements Listener {

    private final PopulationTask populationTask;

    public ChunkLoadListener(OraxenNature plugin) {
        this.populationTask = new PopulationTask(plugin);
        this.populationTask.runTaskTimer(plugin, 0L, 1L);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        populationTask.queueChunk(event.getChunk());
    }
}
