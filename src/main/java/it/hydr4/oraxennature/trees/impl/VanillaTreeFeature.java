package it.hydr4.oraxennature.trees.impl;

import it.hydr4.oraxennature.trees.CustomTree;
import it.hydr4.oraxennature.trees.TreeFeature;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;

public class VanillaTreeFeature implements TreeFeature {

    private final Random random = new Random();

    @Override
    public List<Map.Entry<Location, String>> generate(Location origin, CustomTree tree) {
        List<Map.Entry<Location, String>> blocks = new ArrayList<>();
        int height = random.nextInt(tree.getMaxHeight() - tree.getMinHeight() + 1) + tree.getMinHeight();

        switch (tree.getShape().toUpperCase()) {
            case "PINE":
                generatePine(origin, tree, height, blocks);
                break;
            case "OAK":
            default:
                generateOak(origin, tree, height, blocks);
                break;
        }

        return blocks;
    }

    private void generateOak(Location origin, CustomTree tree, int height, List<Map.Entry<Location, String>> blocks) {
        // Trunk
        for (int i = 0; i < height; i++) {
            addBlock(blocks, origin.clone().add(0, i, 0), tree.getLogOraxenId());
        }

        // Simple Vanilla-like canopy
        int radius = tree.getCanopyMinRadius();
        Location top = origin.clone().add(0, height, 0);
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) + Math.abs(y) + Math.abs(z) <= radius + 1) {
                        addBlock(blocks, top.clone().add(x, y, z), tree.getLeafOraxenId());
                    }
                }
            }
        }
    }

    private void generatePine(Location origin, CustomTree tree, int height, List<Map.Entry<Location, String>> blocks) {
        // Trunk
        for (int i = 0; i < height; i++) {
            addBlock(blocks, origin.clone().add(0, i, 0), tree.getLogOraxenId());
        }

        // Layered canopy
        for (int i = 0; i <= 2; i++) {
            int radius = 2 - i;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) + Math.abs(z) <= radius) {
                        addBlock(blocks, origin.clone().add(x, height - i, z), tree.getLeafOraxenId());
                    }
                }
            }
        }
    }

    private void addBlock(List<Map.Entry<Location, String>> blocks, Location loc, String id) {
        if (loc.getBlock().isReplaceable()) {
            blocks.add(new AbstractMap.SimpleEntry<>(loc, id));
        }
    }
}
