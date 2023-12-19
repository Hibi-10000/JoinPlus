package com.github.hibi_10000.plugins.joinplus;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class JoinPlus extends JavaPlugin {
    public GeoIPUtil geoutil;
    public File databasefile;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PlayerListeners(this), this);
        ConfigUtil.setPluginInstance(this);

        databasefile = new File(this.getDataFolder(), "GeoLite2-Country.mmdb");
        geoutil = new GeoIPUtil(this);
        if (!databasefile.exists()) {
            if (ConfigUtil.getGeoLite2LicenseKey().isEmpty()) {
                if (this.getResource("GeoLite2-Country.mmdb") != null) {
                    this.saveResource("GeoLite2-Country.mmdb", false);
                    getLogger().warning("maxmindのライセンスキーが設定されていなかったため、デフォルトのデータベースを使用します");
                } else {
                    getLogger().severe("maxmindのライセンスキーが設定されていなかったため、GeoIPを使用できません！");
                    getLogger().severe("ライセンスキーを設定するか、GeoIPデータベースを手動で配置してください！");
                    getServer().getPluginManager().disablePlugin(this);
                    return;
                }
            } else {
                geoutil.updateDB();
            }
        }
        final Date dbDate = geoutil.getDBBuildDate();
        if (dbDate == null) return;
        final long diff = new Date().getTime() - dbDate.getTime();
        if ((diff / 1000 / 3600) > 24) geoutil.updateDB();
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
        if (!checkPermission(cs, "joinplus.command")) return false;
        if (args.length == 0) {
            cs.sendMessage(ChatColor.YELLOW + "[JoinPlus] " + ChatColor.GRAY + "Version " + ChatColor.AQUA + getDescription().getVersion() + ChatColor.GRAY + " by " + getDescription().getAuthors().get(0) + ".");
            return true;
        }
        if (args.length != 1) {
            cs.sendMessage(formatCommandResponse("Usage: /joinplus reload"));
            return false;
        }
        if (args[0].equalsIgnoreCase("help")) {
            cs.sendMessage(formatCommandResponse("Available Commands:"));
            cs.sendMessage(formatCommandResponse(ChatColor.AQUA + "/joinplus help" + ChatColor.GRAY + ": This general plugin information."));
            cs.sendMessage(formatCommandResponse(ChatColor.AQUA + "/joinplus reload" + ChatColor.GRAY + ": Reloads the configuration."));
            cs.sendMessage(formatCommandResponse(ChatColor.AQUA + "/joinplus geoupdate" + ChatColor.GRAY + ": Updates the database."));
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!checkPermission(cs, "joinplus.command.reload")) return false;
            ConfigUtil.reloadConfig();
            cs.sendMessage(formatCommandResponse("Configuration reloaded."));
            return true;
        }
        if (args[0].equalsIgnoreCase("geoupdate")) {
            if (!checkPermission(cs, "joinplus.command.geoupdate")) return false;
            cs.sendMessage("§a[JoinPlus] GeoLite2データベースのアップデートを開始しました");
            if (cs instanceof Player) getLogger().info(cs.getName() + " がGeoLite2データベースのアップデートを開始しました");
            if (!geoutil.updateDB()) {
                cs.sendMessage("§c[JoinPlus] GeoLite2データベースのアップデートが失敗しました。コンソールに出力したログを確認してください。");
                return false;
            }
            return true;
        }
        if (!(cs instanceof Player)) {
            cs.sendMessage(formatCommandResponse("You must be a player to do that."));
            return false;
        }
        cs.sendMessage(formatCommandResponse("Unknown command. Type " + ChatColor.AQUA + "/joinplus help" + ChatColor.GRAY + " for help."));
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("joinplus.command")) return null;
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

    public static boolean checkPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) return true;
        sender.sendMessage(getNoPermissionMessage());
        return false;
    }
}
