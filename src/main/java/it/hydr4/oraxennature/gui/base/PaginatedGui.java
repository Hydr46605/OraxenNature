package it.hydr4.oraxennature.gui.base;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class PaginatedGui extends AbstractGui {

    protected int page = 0;
    protected int maxItemsPerPage;
    protected List<GuiItem> allItems;

    public PaginatedGui(int size, Component title, int maxItemsPerPage) {
        super(size, title);
        this.maxItemsPerPage = maxItemsPerPage;
        this.allItems = new ArrayList<>();
    }

    public void addGuiItem(GuiItem item) {
        allItems.add(item);
    }

    @Override
    public void setupItems() {
        inventory.clear();
        items.clear();

        int start = page * maxItemsPerPage;
        int end = Math.min(start + maxItemsPerPage, allItems.size());

        for (int i = start; i < end; i++) {
            setItem(i - start, allItems.get(i));
        }

        addNavigationButtons();
    }

    protected void addNavigationButtons() {
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta meta = prev.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text("§aPrevious Page"));
                prev.setItemMeta(meta);
            }
            setItem(inventory.getSize() - 9, new Button(prev, event -> {
                page--;
                setupItems();
            }));
        }

        if ((page + 1) * maxItemsPerPage < allItems.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta meta = next.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text("§aNext Page"));
                next.setItemMeta(meta);
            }
            setItem(inventory.getSize() - 1, new Button(next, event -> {
                page++;
                setupItems();
            }));
        }
    }
}
