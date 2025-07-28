package it.hydr4.oraxennature.utils;

import it.hydr4.oraxennature.OraxenNature;
import org.bukkit.ChatColor;
import java.util.List;

public class Logger {

    public static void log(String message) {
        OraxenNature.getInstance().getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void info(String message) {
        if (!OraxenNature.getInstance().getSettingsConfig().getBoolean("suppress_info_logs", false)) {
            log("&f[&bINFO&f] " + message);
        }
    }

    public static void success(String message) {
        if (!OraxenNature.getInstance().getSettingsConfig().getBoolean("suppress_info_logs", false)) {
            log("&f[&aSUCCESS&f] " + message);
        }
    }

    public static void warning(String message) {
        log("&f[&eWARN&f] " + message);
    }

    public static void error(String message) {
        log("&f[&cERROR&f] " + message);
    }

    public static void debug(String message) {
        if (OraxenNature.getInstance().isDebugMode()) {
            log("&f[&dDEBUG&f] " + message);
        }
    }

    public static void logList(List<String> list, String prefix) {
        for (String item : list) {
            log(prefix + item);
        }
    }
}
