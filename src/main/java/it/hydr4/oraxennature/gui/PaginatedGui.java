package it.hydr4.oraxennature.gui;

import it.hydr4.oraxennature.OraxenNature;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class PaginatedGui extends AbstractGui {

    protected final OraxenNature plugin;
    protected int currentPage;
    protected final int itemsPerPage;
    protected List<GuiItem> allItems;

    public PaginatedGui(OraxenNature plugin, int size, String title, int itemsPerPage) {
        super(size, title);
        this.plugin = plugin;
        this.currentPage = 0;
        this.itemsPerPage = itemsPerPage;
        this.allItems = new ArrayList<>();
    }

    protected abstract void loadAllItems();

    protected void setupPaginationButtons() {
        // Previous Page Button
        ItemStack prevButton = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prevButton.getItemMeta();
        if (prevMeta != null) {
            prevMeta.setDisplayName("§aPrevious Page");
            prevButton.setItemMeta(prevMeta);
        }
        setItem(inventory.getSize() - 9, new Button(prevButton, event -> {
            if (currentPage > 0) {
                currentPage--;
                updatePage();
            }
        }));

        // Back button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§cBack");
            backButton.setItemMeta(backMeta);
        }
        setItem(inventory.getSize() - 5, new Button(backButton, event -> { // Placed at the bottom center
            Player player = (Player) event.getWhoClicked();
            plugin.getGuiManager().openGui(player, new EditorGui(plugin));
        }));

        // Next Page Button
        ItemStack nextButton = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextButton.getItemMeta();
        if (nextMeta != null) {
            nextMeta.setDisplayName("§aNext Page");
            nextButton.setItemMeta(nextMeta);
        }
        setItem(inventory.getSize() - 1, new Button(nextButton, event -> {
            if ((currentPage + 1) * itemsPerPage < allItems.size()) {
                currentPage++;
                updatePage();
            }
        }));
    }

    protected void updatePage() {
        clearPageItems();
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, allItems.size());

        int currentSlot = 0;
        for (int i = start; i < end; i++) {
            setItem(currentSlot, allItems.get(i));
            currentSlot++;
        }

        // Update page number display (optional, can be added later)
    }

    protected void clearPageItems() {
        for (int i = 0; i < itemsPerPage; i++) {
            inventory.setItem(i, null);
            guiItems.remove(i);
        }
    }

    @Override
    protected void setupItems() {
        loadAllItems();
        updatePage();
        setupPaginationButtons();
    }
}
