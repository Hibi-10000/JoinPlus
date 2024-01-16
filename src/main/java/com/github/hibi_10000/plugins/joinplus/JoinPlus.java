package com.github.hibi_10000.plugins.joinplus;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class JoinPlus extends JavaPlugin {
    final Logger logger = getLogger();
    File databasefile;
    ConfigUtil config;
    GeoIPUtil geoUtil;
    DBUpdateUtil dbUpdateUtil;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PlayerListeners(this), this);
        saveDefaultConfig();
        config = new ConfigUtil(this);
        geoUtil = new GeoIPUtil(this);
        dbUpdateUtil = new DBUpdateUtil(this);
        databasefile = new File(this.getDataFolder(), config.getGeoIP2DBFileName());
        if (!databasefile.exists()) {
            if (config.getGeoIP2LicenseKey().isEmpty()) {
                if (this.getResource("GeoLite2-Country.mmdb") != null) {
                    this.saveResource("GeoLite2-Country.mmdb", false);
                    logger.warning("maxmindのライセンスキーが設定されていなかったため、デフォルトのデータベースを使用します");
                } else {
                    logger.severe("maxmindのライセンスキーが設定されていなかったため、GeoIPを使用できません！");
                    logger.severe("ライセンスキーを設定するか、GeoIPデータベースを手動で配置してください！");
                }
            } else {
                dbUpdateUtil.updateDB();
            }
        } else {
            final Date dbDate = geoUtil.getDBBuildDate();
            if (dbDate == null) return;
            final long diff = new Date().getTime() - dbDate.getTime();
            if ((diff / 1000 / 3600) > 24) dbUpdateUtil.updateDB();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("joinplus")) return false;
        if (!(sender instanceof Player || sender instanceof ConsoleCommandSender)) return false;
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "help" -> {
                    sender.sendMessage(formatCommandResponse("Available Commands:"));
                    sender.sendMessage(formatCommandResponse(ChatColor.AQUA + "/joinplus help" + ChatColor.GRAY + ": This general plugin information."));
                    sender.sendMessage(formatCommandResponse(ChatColor.AQUA + "/joinplus reload" + ChatColor.GRAY + ": Reloads the configuration."));
                    sender.sendMessage(formatCommandResponse(ChatColor.AQUA + "/joinplus geoupdate" + ChatColor.GRAY + ": Updates the database."));
                    return true;
                }
                case "reload" -> {
                    if (!checkPermission(sender, "joinplus.command.reload")) return false;
                    config.reloadConfig();
                    sender.sendMessage(formatCommandResponse("Configuration reloaded."));
                    return true;
                }
                case "geoupdate" -> {
                    if (!checkPermission(sender, "joinplus.command.geoupdate")) return false;
                    if (sender instanceof Player) {
                        sender.sendMessage("§a[JoinPlus] GeoIPデータベースのアップデートを開始しました");
                        logger.info(sender.getName() + " がGeoIPデータベースのアップデートを開始しました");
                        if (!dbUpdateUtil.updateDB()) {
                            sender.sendMessage("§c[JoinPlus] GeoIPデータベースのアップデートに失敗しました。コンソールに出力したログを確認してください。");
                            return false;
                        }
                    } else {
                        dbUpdateUtil.updateDB();
                    }
                    return true;
                }
            }
        }
        sender.sendMessage(formatCommandResponse("Unknown command. Type " + ChatColor.AQUA + "/joinplus help" + ChatColor.GRAY + " for help."));
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("joinplus")) return Collections.emptyList();
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("help");
            list.add("reload");
            list.add("geoupdate");
            return list;
        }
        return Collections.emptyList();
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
