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
            logger.info("GeoIPデータベースのアップデートを開始します");
            String url = ConfigUtil.getGeoLite2DownloadURL();
            if (!url.contains("tar.gz")) {
                logger.severe("GeoIPデータベースのダウンロードURLが間違っています");
                return false;
            }
            final String licenseKey = ConfigUtil.getGeoLite2LicenseKey();
            if (licenseKey == null || licenseKey.isEmpty()) {
                logger.severe("maxmindのライセンスキーを設定してください");
                return false;
            }
            url = url.replace("{LICENSE_KEY}", licenseKey);
            final URL downloadUrl = new URL(url);
            final URLConnection conn = downloadUrl.openConnection();
            conn.setConnectTimeout(10000);
            conn.connect();
            logger.info("GeoIPデータベースをダウンロードしています...");
            InputStream input = conn.getInputStream();
            final GZIPInputStream gzipInput = new GZIPInputStream(input);
            final TarInputStream tarInput = new TarInputStream(gzipInput);
            TarEntry entry;
            while ((entry = tarInput.getNextEntry()) != null) {
                String filename = entry.getName();
                if (!entry.isDirectory()) {
                    if (filename.substring(filename.length() - 5).equalsIgnoreCase(".mmdb")) break;
                } else {
                    ConfigUtil.setGeoLite2LastDBUpdate(filename.replace("GeoLite2-Country_","").replace("/",""));
                }
            }
            final OutputStream output = new FileOutputStream(databasefile);
            final byte[] buffer = new byte[2048];
            int length = tarInput.read(buffer);
            while (length >= 0) {
                output.write(buffer, 0, length);
                length = tarInput.read(buffer);
            }
            output.close();
            tarInput.close();
            gzipInput.close();
            input.close();
            logger.info("GeoIPデータベースのアップデートが完了しました");
        } catch (final MalformedURLException e) {
            logger.log(Level.SEVERE, "GeoIPデータベースのダウンロードURLが間違っています:", e);
            return false;
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "GeoIPデータベースのダウンロードサーバーに接続できませんでした:", e);
            return false;
        }
        return true;
    }
}
