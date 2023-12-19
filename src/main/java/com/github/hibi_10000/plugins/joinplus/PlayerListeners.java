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
        if (!event.getPlayer().hasPlayedBefore() && ConfigUtil.getFirstJoinMessageEnabled()) {
            final String message = ConfigUtil.getFirstJoinMessage();
            if (message != null) {
                Bukkit.broadcastMessage(ConfigUtil.replaceVariables(message, event.getPlayer()));
            }
        }
        if (ConfigUtil.getJoinMessageEnabled()) {
            final String message = ConfigUtil.getJoinMessage();
            if (message != null) {
                event.setJoinMessage(ConfigUtil.replaceVariables(message, event.getPlayer()));
            } else {
                event.setJoinMessage(null);
            }
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        if (ConfigUtil.getKickMessageEnabled()) {
            final String message = ConfigUtil.getKickMessage();
            if (message != null) {
                event.setLeaveMessage(ConfigUtil.replaceVariables(message, event.getPlayer(), event.getReason()));
            } else {
                event.setLeaveMessage("");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (ConfigUtil.getQuitMessageEnabled()) {
            final String message = ConfigUtil.getQuitMessage();
            if (message != null) {
                event.setQuitMessage(ConfigUtil.replaceVariables(message, event.getPlayer()));
            } else {
                event.setQuitMessage(null);
            }
        }
    }
}
