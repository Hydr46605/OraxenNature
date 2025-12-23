package it.hydr4.oraxennature.trees;

import it.hydr4.oraxennature.OraxenNature;
import it.hydr4.oraxennature.trees.impl.VanillaTreeFeature;
import it.hydr4.oraxennature.utils.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;
import it.hydr4.oraxennature.trees.impl.SchematicTreeFeature;

public class TreeGenerator {

    private final OraxenNature plugin;
    private final Map<String, TreeFeature> registeredFeatures;

    public TreeGenerator(OraxenNature plugin) {
        this.plugin = plugin;
        this.registeredFeatures = new HashMap<>();
        // Register default features
        registerFeature("vanilla", new VanillaTreeFeature());
        registerFeature("schematic", new SchematicTreeFeature(plugin));
    }

    public void registerFeature(String id, TreeFeature feature) {
        registeredFeatures.put(id.toLowerCase(), feature);
    }

    public void generateTree(Location trunkBaseLoc, CustomTree tree) {
        String shape = tree.getSchematic() != null ? "schematic" : tree.getShape();
        TreeFeature feature = registeredFeatures.getOrDefault(shape.toLowerCase(), registeredFeatures.get("vanilla"));
        List<Map.Entry<Location, String>> blocksToPlace = feature.generate(trunkBaseLoc, tree);

        if (!blocksToPlace.isEmpty()) {
            Logger.debug("Scheduling TreeGenerationTask with " + blocksToPlace.size() + " blocks.");
            new TreeGenerationTask(plugin, blocksToPlace).runTaskTimer(plugin, 0L, 1L);
        }
    }
}
