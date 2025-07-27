package it.hydr4.oraxennature.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class Button implements GuiItem {

    private final ItemStack itemStack;
    private final Consumer<InventoryClickEvent> clickAction;

    public Button(ItemStack itemStack, Consumer<InventoryClickEvent> clickAction) {
        this.itemStack = itemStack;
        this.clickAction = clickAction;
    }

    @Override
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (clickAction != null) {
            clickAction.accept(event);
        }
    }
}