package it.hydr4.oraxennature.populators.treePopulator;

import it.hydr4.oraxennature.OraxenNature;
import it.hydr4.oraxennature.utils.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TreeGenerator {

    private final OraxenNature plugin;
    private final Random random;
    public TreeGenerator(OraxenNature plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    public void generateTree(Location trunkBaseLoc, CustomTree tree) {
        if (tree.getSchematic() != null) {
            if (plugin.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
                SchematicPaster schematicPaster = new SchematicPaster(plugin);
                File schematicFile = new File(plugin.getDataFolder(), "schematics/" + tree.getSchematic());
                if (schematicFile.exists()) {
                    schematicPaster.pasteSchematic(trunkBaseLoc, schematicFile, tree.getBlockReplacements());
                } else {
                    Logger.error("Schematic file not found: " + schematicFile.getPath());
                }
            } else {
                Logger.error("WorldEdit is not enabled, but a schematic is defined for tree: " + tree.getId());
            }
            return;
        }

        List<Map.Entry<Location, String>> blocksToPlace = new ArrayList<>();
        int height = random.nextInt(tree.getMaxHeight() - tree.getMinHeight() + 1) + tree.getMinHeight();

        switch (tree.getShape().toUpperCase()) {
            case "PINE":
                generatePineTree(trunkBaseLoc, tree, height, blocksToPlace);
                break;
            case "OAK":
            default:
                generateOakTree(trunkBaseLoc, tree, height, blocksToPlace);
                break;
        }

        Logger.debug("Scheduling TreeGenerationTask with " + blocksToPlace.size() + " blocks.");
        new TreeGenerationTask(plugin, blocksToPlace).runTaskTimer(plugin, 0L, 1L);
    }

    private void generateOakTree(Location trunkBaseLoc, CustomTree tree, int height, List<Map.Entry<Location, String>> blocksToPlace) {
        // Generate trunk
        for (int i = 0; i < height; i++) {
            placeBlock(blocksToPlace, trunkBaseLoc.clone().add(0, i, 0), tree.getLogOraxenId());
        }

        // Generate canopy
        Location canopyBase = trunkBaseLoc.clone().add(0, height - 2, 0); // Canopy starts a bit below the top of the trunk
        int canopyRadius = random.nextInt(tree.getCanopyMaxRadius() - tree.getCanopyMinRadius() + 1) + tree.getCanopyMinRadius();
        generateIrregularCanopy(canopyBase, canopyRadius, tree.getCanopyDensity(), tree.getLeafOraxenId(), blocksToPlace);

        // Generate branches (more varied)
        int numBranches = random.nextInt(height / 3) + 2; // 2 to height/3 branches
        for (int i = 0; i < numBranches; i++) {
            Location branchStart = trunkBaseLoc.clone().add(0, random.nextInt(height - 2) + 2, 0); // Branches from mid-trunk to top
            generateBranch(branchStart, tree, blocksToPlace);
        }
    }

    private void generatePineTree(Location trunkBaseLoc, CustomTree tree, int height, List<Map.Entry<Location, String>> blocksToPlace) {
        // Generate trunk
        for (int i = 0; i < height; i++) {
            placeBlock(blocksToPlace, trunkBaseLoc.clone().add(0, i, 0), tree.getLogOraxenId());
        }

        // Generate conical canopy layers
        for (int y = height; y >= height / 2; y--) {
            int radius = (height - y) / 2 + 1; // Tapering radius
            generateCanopyLayer(trunkBaseLoc.clone().add(0, y, 0), radius, tree.getCanopyDensity(), tree.getLeafOraxenId(), blocksToPlace);
        }
    }

    private void generateBranch(Location start, CustomTree tree, List<Map.Entry<Location, String>> blocksToPlace) {
        Location current = start.clone();
        int length = random.nextInt(tree.getCanopyMaxRadius()) + 1; // Branch length related to canopy size
        double yaw = random.nextDouble() * 2 * Math.PI;
        double pitch = Math.toRadians(random.nextInt(40) + 20); // More varied upward angle

        for (int i = 0; i < length; i++) {
            current.add(Math.cos(yaw) * 0.8, Math.sin(pitch) * 0.8, Math.sin(yaw) * 0.8); // Slower growth
            placeBlock(blocksToPlace, current, tree.getLogOraxenId());
        }
        // Place a small canopy at the end of the branch
        generateIrregularCanopy(current, tree.getCanopyMinRadius(), tree.getCanopyDensity() * 0.7, tree.getLeafOraxenId(), blocksToPlace);
    }

    private void generateIrregularCanopy(Location center, int radius, double density, String leafId, List<Map.Entry<Location, String>> blocksToPlace) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    // Introduce more randomness in shape and density
                    // Use a Perlin-like noise or simpler random walk for clumping
                    double noise = random.nextDouble(); // Simple noise for now
                    if (distance <= radius + noise * 1.5 && random.nextDouble() < density * (1.0 - (distance / (radius + 1.0)) * (distance / (radius + 1.0)))) { // Quadratic falloff
                        // Add more vertical spread
                        Location leafLoc = center.clone().add(x, y + (random.nextDouble() - 0.5) * 2.0, z); // Increased vertical variation
                        // Prevent leaves from going too far below the center of the canopy
                        if (leafLoc.getY() >= center.getY() - radius * 0.75) { // Adjust this threshold as needed
                            placeBlock(blocksToPlace, leafLoc, leafId);
                        }
                    }
                }
            }
        }
    }

    private void generateCanopyLayer(Location center, int radius, double density, String leafId, List<Map.Entry<Location, String>> blocksToPlace) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                double noise = random.nextDouble();
                if (distance <= radius + noise * 1.0 && random.nextDouble() < density * (1 - distance / (radius + 1))) {
                    placeBlock(blocksToPlace, center.clone().add(x, 0, z), leafId);
                }
            }
        }
    }

    private void placeBlock(List<Map.Entry<Location, String>> blocksToPlace, Location loc, String oraxenId) {
        if (isReplaceable(loc.getBlock())) {
            blocksToPlace.add(new AbstractMap.SimpleEntry<>(loc, oraxenId));
        }
    }

    private boolean isReplaceable(Block block) {
        return block.getType().isAir() || block.getType() == Material.DIRT || block.getType() == Material.GRASS_BLOCK || block.getType().toString().contains("LEAVES");
    }
}
