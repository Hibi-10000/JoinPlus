package com.github.hibi_10000.plugins.joinplus;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class JoinPlus extends JavaPlugin {
  
  public List<String> noPVP = new ArrayList<>();
  
  public List<String> godMode = new ArrayList<>();

  public GeoIPUtil geoutil;

  public File databasefile;

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(new PlayerListeners(this), this);

    ConfigUtil.setPluginInstance(this);

    saveDefaultConfig();

    databasefile = new File(this.getDataFolder(),"GeoLite2-Country.mmdb");
    geoutil = new GeoIPUtil(this);
    if (!databasefile.exists()) {
      String conflicenseley = ConfigUtil.getGeoLite2LicenseKey();
      if (conflicenseley != null && conflicenseley.equalsIgnoreCase("LICENSEKEY_HERE")) {
        this.saveResource("GeoLite2-Country.mmdb", false);
        getLogger().warning("maxmindのライセンスキーが設定されていなかったため、デフォルトのデータベースを使用します");
      } else {
        geoutil.updateDB();
      }
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    long lastUpdated;
    try {
      lastUpdated = sdf.parse(ConfigUtil.getGeoLite2LastDBUpdate()).getTime();
    } catch (ParseException e) {
      lastUpdated = new Date(1650326400).getTime();
    }

    final long diff = new Date().getTime() - lastUpdated;
    if ((diff / 1000 / 3600 / 24) > 7) {
      geoutil.updateDB();
    }
  }

  @Override public void onDisable() {saveConfig();}

  @Override
  public boolean onCommand(CommandSender cs, Command cmnd, String alias, String[] args) {
    if (args.length == 0) {
      cs.sendMessage(ChatColor.YELLOW + "[JoinPlus] " + ChatColor.GRAY + "Version " + ChatColor.AQUA + getDescription().getVersion() + ChatColor.GRAY + " by " + getDescription().getAuthors().get(0) + ".");
      return true;
    } 
    if (args.length != 1) {
      cs.sendMessage(formatCommandResponse("Usage: /joinplus reload"));
      return true;
    } 
    if (args[0].equalsIgnoreCase("help")) {
      cs.sendMessage(formatCommandResponse("Available Commands:"));
      cs.sendMessage(formatCommandResponse(ChatColor.AQUA + "/joinplus help" + ChatColor.GRAY + ": This general plugin information."));
      cs.sendMessage(formatCommandResponse(ChatColor.AQUA + "/joinplus reload" + ChatColor.GRAY + ": Reloads the configuration."));
      cs.sendMessage(formatCommandResponse(ChatColor.AQUA + "/joinplus geoupdate" + ChatColor.GRAY + ": Updates the database."));
      return true;
    } 
    if (args[0].equalsIgnoreCase("reload")) {
      if (cs.hasPermission("joinplus.reload")) {
        saveConfig();
        reloadConfig();
        ConfigUtil.setPluginInstance(this);
        cs.sendMessage(formatCommandResponse("Configuration reloaded."));
      } else {
        cs.sendMessage(getNoPermissionMessage());
      } 
      return true;
    }
    if (args[0].equalsIgnoreCase("geoupdate")) {
      if (cs.hasPermission("joinplus.geoupdate")) {
        if (cs instanceof Player) {
          cs.sendMessage("§a[JoinPlus] GeoLite2データベースのアップデートを開始しました");
        }
        getLogger().info(cs.getName() + " がGeoLite2データベースのアップデートを開始しました");
        if (!geoutil.updateDB()) {
          cs.sendMessage("§c[JoinPlus] GeoLite2データベースのアップデートが失敗しました。コンソールに出力したログを確認してください。");
          return false;
        }
      }
      return true;
    }
    if (!(cs instanceof Player)) {
      cs.sendMessage(formatCommandResponse("You must be a player to do that."));
      return true;
    }
    cs.sendMessage(formatCommandResponse("Unknown command. Type " + ChatColor.AQUA + "/joinplus help" + ChatColor.GRAY + " for help."));
    return true;
  }
  
  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    if (command.getName().equalsIgnoreCase("joinplus")) {
      if (args.length == 1) {
        List<String> list = new ArrayList<>();
        list.add("help");
        list.add("reload");
        list.add("geoupdate");
        return list;
      }
    }
    return null;
  }
  
  public static String formatCommandResponse(String string) {
    return ChatColor.YELLOW + "[JoinPlus] " + ChatColor.GRAY + string;
  }

  public static String getNoPermissionMessage() {
    return formatCommandResponse("§cYou don't have permission to do that.");
  }
}