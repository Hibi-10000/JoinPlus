package com.github.hibi_10000.plugins.joinplus;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListeners implements Listener {
    JoinPlus plugin;

    public PlayerListeners(JoinPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore() && ConfigUtil.getFirstJoinMessageEnabled()) {
            String message = ConfigUtil.getFirstJoinMessage();
            if (message != null && !message.equalsIgnoreCase("%none%")) {
                Bukkit.broadcastMessage(ConfigUtil.replaceVariables(message, event.getPlayer()));
            }
        }
        if (ConfigUtil.getJoinMessageEnabled()) {
            String message = ConfigUtil.getJoinMessage();
            if (message != null && !message.equalsIgnoreCase("%none%")) {
                event.setJoinMessage(ConfigUtil.replaceVariables(message, event.getPlayer()));
            } else {
                event.setJoinMessage(null);
            }
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        if (ConfigUtil.getKickMessageEnabled()) {
            String message = ConfigUtil.getKickMessage();
            if (message != null && !message.equalsIgnoreCase("%none%")) {
                event.setLeaveMessage(ConfigUtil.replaceVariables(message, event.getPlayer(), event.getReason()));
            } else {
                event.setLeaveMessage("");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (ConfigUtil.getQuitMessageEnabled()) {
            String message = ConfigUtil.getQuitMessage();
            if (message != null && !message.equalsIgnoreCase("%none%")) {
                event.setQuitMessage(ConfigUtil.replaceVariables(message, event.getPlayer()));
            } else {
                event.setQuitMessage(null);
            }
        }
    }
}
