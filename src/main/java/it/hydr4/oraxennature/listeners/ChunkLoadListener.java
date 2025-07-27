package it.hydr4.oraxennature.listeners;

import it.hydr4.oraxennature.OraxenNature;
import it.hydr4.oraxennature.populators.CustomBlockPopulator;
import it.hydr4.oraxennature.populators.treePopulator.CustomTreePopulator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkLoadListener implements Listener {

    private final CustomBlockPopulator blockPopulator;
    private final CustomTreePopulator treePopulator;

    public ChunkLoadListener(OraxenNature plugin) {
        this.blockPopulator = plugin.getBlockPopulator();
        this.treePopulator = plugin.getTreePopulator();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        blockPopulator.populate(event.getChunk());
        treePopulator.populate(event.getChunk().getWorld(), new java.util.Random(event.getChunk().getWorld().getSeed()), event.getChunk());
    }
}