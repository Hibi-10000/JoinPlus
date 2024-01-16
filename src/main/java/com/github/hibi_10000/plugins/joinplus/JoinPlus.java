package com.github.hibi_10000.plugins.joinplus;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
            if (!config.getGeoIP2LicenseKey().isEmpty()) {
                dbUpdateUtil.schedule();
            } else {
                logger.warning("maxmindのライセンスキーが設定されていないため、GeoIPデータベースはアップデートされません");
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("joinplus")) return false;
        if (!(sender instanceof Player || sender instanceof ConsoleCommandSender)) return false;
        if (!checkPermission(sender, "joinplus.command")) return false;
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "help" -> {
                    sendMessage(sender, ChatColor.GRAY, "Available Commands:");
                    sendMessage(sender, ChatColor.AQUA, "/joinplus help" + ChatColor.GRAY + ": This general plugin information.");
                    sendMessage(sender, ChatColor.AQUA, "/joinplus reload" + ChatColor.GRAY + ": Reloads the configuration.");
                    sendMessage(sender, ChatColor.AQUA, "/joinplus geoupdate" + ChatColor.GRAY + ": Updates the database.");
                    return true;
                }
                case "reload" -> {
                    if (!checkPermission(sender, "joinplus.command.reload")) return false;
                    config.reloadConfig();
                    sendMessage(sender, ChatColor.GRAY, "Configuration reloaded.");
                    return true;
                }
                case "geoupdate" -> {
                    if (!checkPermission(sender, "joinplus.command.geoupdate")) return false;
                    if (config.getGeoIP2LicenseKey().isEmpty()) {
                        sendMessage(sender, ChatColor.RED, "maxmindのライセンスキーが設定されていないため、GeoIPデータベースのアップデートはできません");
                        return false;
                    }
                    if (sender instanceof Player) {
                        sendMessage(sender, ChatColor.GREEN, "GeoIPデータベースのアップデートを確認しています");
                        logger.info(sender.getName() + " がGeoIPデータベースのアップデートを開始しました");
                        if (dbUpdateUtil.checkUpdates()) {
                            sendMessage(sender, ChatColor.GREEN, "GeoIPデータベースをアップデートしています");
                            if (!dbUpdateUtil.updateDB()) {
                                sendMessage(sender, ChatColor.RED, "GeoIPデータベースのアップデートに失敗しました。コンソールに出力したログを確認してください");
                                return false;
                            }
                            sendMessage(sender, ChatColor.GREEN, "GeoIPデータベースのアップデートが完了しました");
                        } else sendMessage(sender, ChatColor.GREEN, "GeoIPデータベースは最新です");
                    } else {
                        if (dbUpdateUtil.checkUpdates()) dbUpdateUtil.updateDB();
                    }
                    return true;
                }
            }
        }
        sendMessage(sender, ChatColor.GRAY, "Unknown command. Type " + ChatColor.AQUA + "/joinplus help" + ChatColor.GRAY + " for help.");
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

    public static void sendMessage(CommandSender sender, ChatColor color, String message) {
        sender.sendMessage(ChatColor.YELLOW + "[JoinPlus] " + color + message);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean checkPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) return true;
        sendMessage(sender, ChatColor.RED, "You don't have permission to do that.");
        return false;
    }
}
