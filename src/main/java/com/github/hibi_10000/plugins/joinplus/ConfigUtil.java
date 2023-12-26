package com.github.hibi_10000.plugins.joinplus;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ConfigUtil {
    private ConfigUtil() {}

    static JoinPlus plugin;
    static FileConfiguration config;

    static void setPluginInstance(JoinPlus instance) {
        plugin = instance;
        config = instance.getConfig();
    }

    static void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
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

    private static String getString(final String path) {
        String str = config.getString(path);
        Configuration defaults = config.getDefaults();
        if (str != null && str.isEmpty() && defaults != null) str = defaults.getString(path);
        return str;
    }

    public static String replaceVariables(String string, Player player) {
        string = ChatColor.translateAlternateColorCodes('&', string);
        string = string.replace("%player_name%", player.getName());
        string = string.replace("%player_display_name%", player.getDisplayName());
        string = string.replace("%player_uuid%", player.getUniqueId().toString());
        string = string.replace("%total_players%", String.valueOf(plugin.getServer().getOnlinePlayers().size()));
        string = string.replace("%max_players%", String.valueOf(plugin.getServer().getMaxPlayers()));
        InetSocketAddress socketAddress = player.getAddress();
        if (socketAddress != null) {
            InetAddress address = player.getAddress().getAddress();
            string = string.replace("%player_country%", plugin.geoutil.getCountry(address));
            //string = string.replace("%player_city%", plugin.geoutil.getCity(address));
            String hostAddress = address.getHostAddress();
            string = string.replace("%player_ip%", hostAddress);
            string = string.replace("%player_ip_masked%", hostAddress.replaceAll("\\d+$", "xxx"));
        } else {
            plugin.getLogger().severe("プレイヤーのIPアドレスを取得できませんでした");
            string = string.replace("%player_country%", "N/A");
            //string = string.replace("%player_city%", "N/A");
            string = string.replace("%player_ip%", "N/A");
            string = string.replace("%player_ip_masked%", "N/A");
        }
        return string;
    }

    public static String replaceVariables(String string, Player player, String reason) {
        return replaceVariables(string.replace("%reason%", reason), player);
    }
}
