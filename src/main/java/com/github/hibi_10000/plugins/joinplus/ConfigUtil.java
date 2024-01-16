/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.hibi_10000.plugins.joinplus;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ConfigUtil {
    private final JoinPlus plugin;
    private FileConfiguration config;

    public ConfigUtil(JoinPlus instance) {
        plugin = instance;
        config = instance.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public boolean getFirstJoinMessageEnabled() {
        return config.getBoolean("messages.firstJoin.enabled");
    }

    public String getFirstJoinMessage() {
        return getString("messages.firstJoin.message");
    }

    public boolean getJoinMessageEnabled() {
        return config.getBoolean("messages.join.enabled");
    }

    public String getJoinMessage() {
        return getString("messages.join.message");
    }

    public boolean getQuitMessageEnabled() {
        return config.getBoolean("messages.quit.enabled");
    }

    public String getQuitMessage() {
        return getString("messages.quit.message");
    }

    public boolean getKickMessageEnabled() {
        return config.getBoolean("messages.kick.enabled");
    }

    public String getKickMessage() {
        return getString("messages.kick.message");
    }

    public String getGeoIP2DBFileName() {
        return getString("GeoIP2.database.country.fileName");
    }

    public String getGeoIP2DBDownloadURL() {
        return getString("GeoIP2.database.country.url.download");
    }

    public String getGeoIP2DBSha256URL() {
        return getString("GeoIP2.database.country.url.sha256");
    }

    public String getGeoIP2LicenseKey() {
        return getString("GeoIP2.licenseKey");
    }

    private String getString(final String path) {
        String str = config.getString(path);
        Configuration defaults = config.getDefaults();
        if (str != null && str.isEmpty() && defaults != null) str = defaults.getString(path);
        return str;
    }

    public String replaceVariables(String string, Player player) {
        string = ChatColor.translateAlternateColorCodes('&', string);
        string = string.replace("%player_name%", player.getName());
        string = string.replace("%player_display_name%", player.getDisplayName());
        string = string.replace("%player_uuid%", player.getUniqueId().toString());
        string = string.replace("%total_players%", String.valueOf(plugin.getServer().getOnlinePlayers().size()));
        string = string.replace("%max_players%", String.valueOf(plugin.getServer().getMaxPlayers()));
        InetSocketAddress socketAddress = player.getAddress();
        if (socketAddress != null) {
            InetAddress address = player.getAddress().getAddress();
            string = string.replace("%player_country%", plugin.geoUtil.getCountry(address));
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

    public String replaceVariables(String string, Player player, String reason) {
        return replaceVariables(string.replace("%reason%", reason), player);
    }
}
