package it.hydr4.oraxennature.populators.treePopulator;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import it.hydr4.oraxennature.OraxenNature;
import it.hydr4.oraxennature.utils.Logger;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SchematicPaster {

    private final OraxenNature plugin;

    public SchematicPaster(OraxenNature plugin) {
        this.plugin = plugin;
    }

    public void pasteSchematic(Location location, File schematicFile) {
        try {
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                Logger.error("Unknown schematic format for file: " + schematicFile.getName());
                return;
            }

            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                Clipboard clipboard = reader.read();
                if (clipboard == null) {
                    Logger.error("Failed to read schematic: " + schematicFile.getName());
                    return;
                }

                World world = BukkitAdapter.adapt(location.getWorld());
                EditSession editSession = WorldEdit.getInstance().newEditSession(world);
                ClipboardHolder holder = new ClipboardHolder(clipboard);

                com.sk89q.worldedit.math.BlockVector3 to = BlockVector3.at(location.getX(), location.getY(), location.getZ());
                holder.createPaste(editSession).to(to).build();
                editSession.close();

            } catch (IOException e) {
                Logger.error("Failed to paste schematic: " + e.getMessage());
            }
        } catch (Exception e) {
            Logger.error("An unexpected error occurred while pasting schematic: " + e.getMessage());
        }
    }
}
