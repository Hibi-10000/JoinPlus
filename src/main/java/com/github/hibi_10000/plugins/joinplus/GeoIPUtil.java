package com.github.hibi_10000.plugins.joinplus;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;

import java.io.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class GeoIPUtil {
    private final JoinPlus plugin;
    private final Logger logger;

    public GeoIPUtil(JoinPlus plugin) {
        this.plugin = plugin;
        logger = plugin.getLogger();
    }

    public String getCountry(InetAddress ipAddress) {
        if (ipAddress.isLoopbackAddress() || ipAddress.isAnyLocalAddress()) {
            return "LocalHost";
        }
        try {
            CountryResponse response;
            try (DatabaseReader mmReader = new DatabaseReader.Builder(plugin.databasefile).build()) {
                response = mmReader.country(ipAddress);
            }
            return response.getCountry().getName();
        } catch (IOException | GeoIp2Exception e) {
            logger.log(Level.SEVERE, "GeoIPデータベースの読み込みに失敗しました", e);
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
            try (final InputStream input = conn.getInputStream();
                 final GZIPInputStream gzipInput = new GZIPInputStream(input);
                 final TarInputStream tarInput = new TarInputStream(gzipInput)) {
                TarEntry entry;
                while ((entry = tarInput.getNextEntry()) != null) {
                    String filename = entry.getName();
                    if (!entry.isDirectory()) {
                        if (filename.endsWith(".mmdb")) break;
                    } else {
                        ConfigUtil.setGeoLite2LastDBUpdate(filename.substring(filename.length() - 8).replace("/", ""));
                    }
                }
                try (final OutputStream output = new FileOutputStream(plugin.databasefile)) {
                    final byte[] buffer = new byte[2048];
                    int length = tarInput.read(buffer);
                    while (length >= 0) {
                        output.write(buffer, 0, length);
                        length = tarInput.read(buffer);
                    }
                }
            }
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
