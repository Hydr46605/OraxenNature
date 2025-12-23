package it.hydr4.oraxennature.trees;

import org.bukkit.Location;
import java.util.List;
import java.util.Map;

public interface TreeFeature {
    /**
     * Generates the tree structure and returns a list of blocks to be placed.
     * @param origin The location of the trunk base.
     * @param tree The custom tree configuration.
     * @return A list of entries mapping locations to Oraxen IDs.
     */
    List<Map.Entry<Location, String>> generate(Location origin, CustomTree tree);
}
