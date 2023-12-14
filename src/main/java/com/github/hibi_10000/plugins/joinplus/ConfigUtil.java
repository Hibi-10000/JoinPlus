package com.github.hibi_10000.plugins.joinplus;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Objects;

public class ConfigUtil {
    private ConfigUtil() {}

    static JoinPlus plugin;
    static FileConfiguration config;

    static void setPluginInstance(JoinPlus instance) {
        plugin = instance;
        config = instance.getConfig();
    }

    public static boolean getFirstJoinMessageEnabled() {
        return config.getBoolean("messages.first-join-message.enabled");
    }

    public static String getFirstJoinMessage() {
        return getString("messages.first-join-message.message");
    }

    public static boolean getJoinMessageEnabled() {
        return config.getBoolean("messages.join-message.enabled");
    }

    public static String getJoinMessage() {
        return getString("messages.join-message.message");
    }

    public static boolean getQuitMessageEnabled() {
        return config.getBoolean("messages.quit-message.enabled");
    }

    public static String getQuitMessage() {
        return getString("messages.quit-message.message");
    }

    public static boolean getKickMessageEnabled() {
        return config.getBoolean("messages.kick-message.enabled");
    }

    public static String getKickMessage() {
        return getString("messages.kick-message.message");
    }

    public static String getGeoLite2DownloadURL() {
        return getString("GeoLite2.Download-URL");
    }

    public static String getGeoLite2LicenseKey() {
        return getString("GeoLite2.LicenseKey");
    }

    public static String getGeoLite2LastDBUpdate() {
        return getString("GeoLite2.LastDBUpdate");
    }

    private static String getString(String path) {
        String str = config.getString(path);
        if (str != null && str.isEmpty()) str = config.getDefaults().getString(path);
        return str;
    }

    public static void setGeoLite2LastDBUpdate(String date) {
        config.set("GeoLite2.LastDBUpdate", date);
        plugin.saveConfig();
    }

    public static String replaceVariables(String string, Player player) {
        string = string.replace("%player_name%", player.getName());
        string = string.replace("%player_display_name%", player.getDisplayName());
        string = string.replace("%player_uuid%", player.getUniqueId().toString());
        string = string.replace("%player_country%", plugin.geoutil.getCountry(Objects.requireNonNull(player.getAddress()).getAddress()));
        //string = string.replace("%player_city%", plugin.geoutil.getCity(player.getAddress().getAddress()));
        string = string.replace("%total_players%", String.valueOf(plugin.getServer().getOnlinePlayers().size()));
        string = string.replace("%max_players%", String.valueOf(plugin.getServer().getMaxPlayers()));
        string = string.replace("%player_ip%", player.getAddress().getAddress().getHostAddress());
        //string = string.replace("%new_line%", "\n");
        string = ChatColor.translateAlternateColorCodes('&', string);
        return string;
    }

    public static String replaceVariables(String string, Player player, String reason) {
        return replaceVariables(string.replace("%reason%", reason), player);
    }
}
