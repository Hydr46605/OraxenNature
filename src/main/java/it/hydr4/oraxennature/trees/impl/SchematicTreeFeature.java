package it.hydr4.oraxennature.trees.impl;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import it.hydr4.oraxennature.OraxenNature;
import it.hydr4.oraxennature.trees.CustomTree;
import it.hydr4.oraxennature.trees.TreeFeature;
import it.hydr4.oraxennature.utils.Logger;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SchematicTreeFeature implements TreeFeature {

    private final OraxenNature plugin;

    public SchematicTreeFeature(OraxenNature plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<Map.Entry<Location, String>> generate(Location origin, CustomTree tree) {
        List<Map.Entry<Location, String>> blocksToPlace = new ArrayList<>();
        String schematicName = tree.getSchematic();
        if (schematicName == null) return blocksToPlace;

        File schematicFile = new File(plugin.getDataFolder(), "schematics/" + schematicName);
        if (!schematicFile.exists()) {
            Logger.error("Schematic file not found: " + schematicFile.getPath());
            return blocksToPlace;
        }

        try {
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                Logger.error("Unknown schematic format for file: " + schematicFile.getName());
                return blocksToPlace;
            }

            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                Clipboard clipboard = reader.read();
                if (clipboard == null) {
                    Logger.error("Failed to read schematic: " + schematicFile.getName());
                    return blocksToPlace;
                }

                World world = BukkitAdapter.adapt(origin.getWorld());
                EditSession editSession = WorldEdit.getInstance().newEditSession(world);
                ClipboardHolder holder = new ClipboardHolder(clipboard);

                BlockVector3 to = BlockVector3.at(origin.getX(), origin.getY(), origin.getZ());
                holder.createPaste(editSession).to(to).build();

                // Handle custom block replacements for Oraxen
                Map<String, String> replacements = tree.getBlockReplacements();
                for (BlockVector3 blockPosition : clipboard.getRegion()) {
                    BlockState blockState = clipboard.getBlock(blockPosition);
                    String blockName = blockState.getBlockType().getId().replace("minecraft:", "").toUpperCase();

                    if (replacements.containsKey(blockName)) {
                        String oraxenId = replacements.get(blockName);
                        Location blockLoc = new Location(origin.getWorld(), blockPosition.getX() + origin.getX(), blockPosition.getY() + origin.getY(), blockPosition.getZ() + origin.getZ());
                        blocksToPlace.add(new AbstractMap.SimpleEntry<>(blockLoc, oraxenId));
                    }
                }
                editSession.close();
            }
        } catch (IOException e) {
            Logger.error("Failed to process schematic: " + e.getMessage());
        }

        return blocksToPlace;
    }
}
