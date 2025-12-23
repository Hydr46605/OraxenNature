package it.hydr4.oraxennature.gui.manager;

import it.hydr4.oraxennature.OraxenNature;
import it.hydr4.oraxennature.utils.TextUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatInputListener implements Listener {

    private static final Map<UUID, InputContext> awaitingInput = new HashMap<>();

    private static class InputContext {
        final Consumer<String> callback;
        final Consumer<Player> guiReopenAction;

        InputContext(Consumer<String> callback, Consumer<Player> guiReopenAction) {
            this.callback = callback;
            this.guiReopenAction = guiReopenAction;
        }
    }
    private final OraxenNature plugin;

    public ChatInputListener(OraxenNature plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static void awaitInput(Player player, Consumer<String> callback, Consumer<Player> guiReopenAction) {
        awaitingInput.put(player.getUniqueId(), new InputContext(callback, guiReopenAction));
        player.sendMessage(TextUtils.parse("<yellow>Please enter the new value in chat. Type <red>'cancel'</red> to abort."));
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (awaitingInput.containsKey(playerUUID)) {
            event.setCancelled(true);
            String message = event.getMessage();
            InputContext context = awaitingInput.get(playerUUID);

            if (message.equalsIgnoreCase("cancel")) {
                player.sendMessage(TextUtils.parse("<red>Input cancelled."));
                awaitingInput.remove(playerUUID);
                // Re-open the GUI if needed
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    context.guiReopenAction.accept(player);
                });
                return;
            }

            Consumer<String> callback = awaitingInput.remove(playerUUID).callback;
            if (callback != null) {
                plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(message));
            }
        }
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
    }
}
