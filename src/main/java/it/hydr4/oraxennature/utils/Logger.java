package it.hydr4.oraxennature.utils;

import it.hydr4.oraxennature.OraxenNature;
import org.bukkit.Bukkit;
import java.util.List;

public class Logger {

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(TextUtils.parse(message));
    }

    public static void info(String message) {
        if (!OraxenNature.getInstance().getSettingsConfig().getBoolean("suppress_info_logs", false)) {
            log("<white>[<blue>INFO<white>] " + message);
        }
    }

    public static void success(String message) {
        if (!OraxenNature.getInstance().getSettingsConfig().getBoolean("suppress_info_logs", false)) {
            log("<white>[<green>SUCCESS<white>] " + message);
        }
    }

    public static void warning(String message) {
        log("<white>[<yellow>WARN<white>] " + message);
    }

    public static void error(String message) {
        log("<white>[<red>ERROR<white>] " + message);
    }

    public static void debug(String message) {
        if (OraxenNature.getInstance().isDebugMode()) {
            log("<white>[<light_purple>DEBUG<white>] " + message);
        }
    }

    public static void logList(List<String> list, String prefix) {
        for (String item : list) {
            log(prefix + item);
        }
    }
}
