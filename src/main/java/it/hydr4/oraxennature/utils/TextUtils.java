package it.hydr4.oraxennature.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class TextUtils {

    // MiniMessage instance
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    public static Component parse(String text) {
        if (text == null) return Component.empty();
        // If it contains MiniMessage tags, parse it as MiniMessage
        if (text.contains("<") && text.contains(">")) {
            return MINI_MESSAGE.deserialize(text);
        }
        // Otherwise parse it as legacy with & or §
        return LEGACY_SERIALIZER.deserialize(text.replace("&", "§"));
    }
}
