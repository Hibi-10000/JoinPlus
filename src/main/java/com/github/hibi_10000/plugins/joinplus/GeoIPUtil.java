package com.github.hibi_10000.plugins.joinplus;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;

import java.io.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class GeoIPUtil {
    JoinPlus plugin;
    Logger logger;
    File databasefile;

    public GeoIPUtil(JoinPlus plugin) {
        this.plugin = plugin;
        logger = plugin.getLogger();
        this.databasefile = plugin.databasefile;
    }

    DatabaseReader mmreader;

    public String getCountry(InetAddress ipAddress) {
        if (ipAddress.isLoopbackAddress() || ipAddress.isAnyLocalAddress()) {
            return "LocalHost";
        }
        try {
            File database = databasefile;
            mmreader = new DatabaseReader.Builder(database).build();
            CountryResponse response = mmreader.country(ipAddress);
            Country country = response.getCountry();
            return country.getName();
        } catch (IOException | GeoIp2Exception e) {
            plugin.getLogger().log(Level.SEVERE, "GeoLite2データベースの読み込みに失敗しました", e);
            return "N/A";
        }
    }

    public boolean updateDB() {
        try {
            logger.info("GeoLite2データベースのアップデートを開始します");
            String url = ConfigUtil.getGeoLite2DownloadURL();
            final String licenseKey = ConfigUtil.getGeoLite2LicenseKey();
            if (licenseKey == null || licenseKey.isEmpty()) {
                logger.severe("maxmindのライセンスキーを設定してください");
                //System.out.println("[JoinPlus]§c maxmindのライセンスキーを設定してください");
                return false;
            }
            url = url.replace("{LICENSE_KEY}", licenseKey);
            logger.info("GeoLite2データベースをダウンロードしています...");
            //System.out.println("§a[JoinPlus] GeoIPデータベースをダウンロードしています...");
            final URL downloadUrl = new URL(url);
            final URLConnection conn = downloadUrl.openConnection();
            conn.setConnectTimeout(10000);
            conn.connect();
            InputStream input = conn.getInputStream();
            final OutputStream output = new FileOutputStream(databasefile);
            final byte[] buffer = new byte[2048];
            if (url.contains("gz")) {
                input = new GZIPInputStream(input);
                if (url.contains("tar.gz")) {
                    // The new GeoIP2 uses tar.gz to pack the db file along with some other txt. So it makes things a bit complicated here.
                    String filename;
                    final TarInputStream tarInputStream = new TarInputStream(input);
                    TarEntry entry;
                    while ((entry = tarInputStream.getNextEntry()) != null) {
                        if (!entry.isDirectory()) {
                            filename = entry.getName();
                            if (filename.substring(filename.length() - 5).equalsIgnoreCase(".mmdb")) {
                                input = tarInputStream;
                                logger.info("GeoLite2データベースのアップデートが完了しました");
                                break;
                            }
                        } else {
                            ConfigUtil.setGeoLite2LastDBUpdate(entry.getName().replace("GeoLite2-Country_","").replace("/",""));
                        }
                    }
                }
            }
            int length = input.read(buffer);
            while (length >= 0) {
                output.write(buffer, 0, length);
                length = input.read(buffer);
            }
            output.close();
            input.close();
        } catch (final MalformedURLException ex) {
            logger.log(Level.SEVERE, "ConfigのGeoLite2のダウンロードURLが間違っています:", ex);
            return false;
        } catch (final IOException ex) {
            logger.log(Level.SEVERE, "GeoLite2のダウンロードサーバーに接続できませんでした:", ex);
            return false;
        }
        return true;
    }
}
