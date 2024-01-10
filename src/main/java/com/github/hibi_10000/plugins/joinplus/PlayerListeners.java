package com.github.hibi_10000.plugins.joinplus;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListeners implements Listener {
    final JoinPlus plugin;

    public PlayerListeners(JoinPlus instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore() && plugin.config.getFirstJoinMessageEnabled()) {
            final String message = plugin.config.getFirstJoinMessage();
            if (message != null) {
                Bukkit.broadcastMessage(plugin.config.replaceVariables(message, event.getPlayer()));
            }
        }
        if (plugin.config.getJoinMessageEnabled()) {
            final String message = plugin.config.getJoinMessage();
            if (message != null) {
                event.setJoinMessage(plugin.config.replaceVariables(message, event.getPlayer()));
            } else {
                event.setJoinMessage(null);
            }
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        if (plugin.config.getKickMessageEnabled()) {
            final String message = plugin.config.getKickMessage();
            if (message != null) {
                event.setLeaveMessage(plugin.config.replaceVariables(message, event.getPlayer(), event.getReason()));
            } else {
                event.setLeaveMessage("");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.config.getQuitMessageEnabled()) {
            final String message = plugin.config.getQuitMessage();
            if (message != null) {
                event.setQuitMessage(plugin.config.replaceVariables(message, event.getPlayer()));
            } else {
                event.setQuitMessage(null);
            }
        }
    }
}
