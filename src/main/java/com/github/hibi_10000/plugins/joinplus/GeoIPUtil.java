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
import java.util.Date;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

public class GeoIPUtil {
    private final JoinPlus plugin;

    public GeoIPUtil(JoinPlus instance) {
        this.plugin = instance;
    }

    public String getCountry(final InetAddress ipAddress) {
        if (ipAddress.isLoopbackAddress() || ipAddress.isAnyLocalAddress()) {
            return "LocalHost";
        }
        try {
            final CountryResponse response;
            try (DatabaseReader mmReader = new DatabaseReader.Builder(plugin.databasefile).build()) {
                response = mmReader.country(ipAddress);
            }
            return response.getCountry().getName();
        } catch (final IOException | GeoIp2Exception e) {
            plugin.logger.log(Level.SEVERE, "GeoIPデータベースの読み込みに失敗しました", e);
            return "N/A";
        }
    }

    public Date getDBBuildDate() {
        try (DatabaseReader mmReader = new DatabaseReader.Builder(plugin.databasefile).build()) {
            return mmReader.getMetadata().getBuildDate();
        } catch (final IOException e) {
            plugin.logger.log(Level.SEVERE, "GeoIPデータベースの読み込みに失敗しました", e);
            return null;
        }
    }

    public boolean updateDB() {
        plugin.logger.info("GeoIPデータベースのアップデートを開始します");
        final String licenseKey = plugin.config.getGeoIP2LicenseKey();
        if (licenseKey == null || licenseKey.isEmpty()) {
            plugin.logger.severe("maxmindのライセンスキーが設定されていません");
            return false;
        }
        final String url = plugin.config.getGeoIP2DBDownloadURL().replace("{LICENSE_KEY}", licenseKey);
        if (!url.contains("tar.gz")) {
            plugin.logger.severe("GeoIPデータベースのダウンロードURLが間違っています");
            return false;
        }
        final URLConnection conn;
        try {
            final URL downloadUrl = new URL(url);
            conn = downloadUrl.openConnection();
            conn.setConnectTimeout(10000);
            conn.connect();
        } catch (final MalformedURLException e) {
            plugin.logger.log(Level.SEVERE, "GeoIPデータベースのダウンロードURLが間違っています:", e);
            return false;
        } catch (final IOException e) {
            plugin.logger.log(Level.SEVERE, "GeoIPデータベースのダウンロードサーバーに接続できませんでした:", e);
            return false;
        }
        plugin.logger.info("GeoIPデータベースをダウンロードしています...");
        try (final InputStream input = conn.getInputStream();
             final GZIPInputStream gzipInput = new GZIPInputStream(input);
             final TarInputStream tarInput = new TarInputStream(gzipInput)) {
            TarEntry entry;
            while ((entry = tarInput.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().equals(plugin.config.getGeoIP2DBFileName())) break;
            }
            try (final OutputStream output = new FileOutputStream(plugin.databasefile)) {
                final byte[] buffer = new byte[2048];
                int length = tarInput.read(buffer);
                while (length >= 0) {
                    output.write(buffer, 0, length);
                    length = tarInput.read(buffer);
                }
            }
        } catch (final IOException e) {
            plugin.logger.log(Level.SEVERE, "何らかの理由でGeoIPデータベースのダウンロードに失敗しました:", e);
            return false;
        }
        plugin.logger.info("GeoIPデータベースのアップデートが完了しました");
        return true;
    }
}
