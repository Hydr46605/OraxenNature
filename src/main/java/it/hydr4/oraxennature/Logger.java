package it.hydr4.oraxennature;

import org.bukkit.ChatColor;
import java.util.List;

public class Logger {

    public static void log(String message) {
        OraxenNature.getInstance().getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void info(String message) {
        log("&f[&bINFO&f] " + message);
    }

    public static void success(String message) {
        log("&f[&aSUCCESS&f] " + message);
    }

    public static void warning(String message) {
        log("&f[&eWARN&f] " + message);
    }

    public static void error(String message) {
        log("&f[&cERROR&f] " + message);
    }

    public static void logList(List<String> list, String prefix) {
        for (String item : list) {
            log(prefix + item);
        }
    }
}
