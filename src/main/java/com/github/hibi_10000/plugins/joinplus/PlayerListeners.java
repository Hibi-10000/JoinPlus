package com.github.hibi_10000.plugins.joinplus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListeners implements Listener {
  JoinPlus plugin;
  public PlayerListeners(JoinPlus plugin) {
    this.plugin = plugin;
  }
  
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onEntityDamage(EntityDamageEvent event) {
    if (event.getEntity() instanceof Player && 
      plugin.godMode.contains(event.getEntity().getName()))
      event.setCancelled(true); 
  }
  
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Player && event.getDamager() instanceof Player && (
            plugin.noPVP.contains(event.getEntity().getName()) || plugin.noPVP.contains(event.getDamager().getName())))
      event.setCancelled(true); 
  }
  
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (!event.getPlayer().hasPlayedBefore()) {
      if (ConfigUtil.getFirstJoinMessageEnabled()) {
        String message = ConfigUtil.getFirstJoinMessage();
        if (message != null && !message.equalsIgnoreCase("%none%")) {
          Bukkit.broadcastMessage(ConfigUtil.replaceVariables(plugin.getConfig().getString("messages.first-join-message.message"), event.getPlayer()));
        }
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
      if (message == null || message.equalsIgnoreCase("%none%")) {
        event.setLeaveMessage("");
      /*} else if (event.getReason().equalsIgnoreCase("Kicked by an operator")) {
        event.setLeaveMessage(ConfigUtil.replaceVariables(message, event.getPlayer(), event.getReason()));*/
      } else {
        event.setLeaveMessage(ConfigUtil.replaceVariables(message, event.getPlayer(), event.getReason()));
      }
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    if (ConfigUtil.getQuitMessageEnabled()) {
      String message = ConfigUtil.getQuitMessage();
      if (message != null && !message.equalsIgnoreCase("%none%")) {
        if (event.getQuitMessage() != null && event.getQuitMessage().equalsIgnoreCase(ChatColor.YELLOW + event.getPlayer().getName() + " left the game")) {
          event.setQuitMessage(ConfigUtil.replaceVariables(message, event.getPlayer()));
        }
      } else {
        event.setQuitMessage(null);
      } 
    } 
  }
}