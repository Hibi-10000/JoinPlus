package com.github.hibi_10000.plugins.joinplus;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

public class DBUpdateUtil {
    private final JoinPlus plugin;

    public DBUpdateUtil(JoinPlus instance) {
        this.plugin = instance;
    }

    public boolean updateDB() {
        plugin.logger.info("GeoIPデータベースのアップデートを開始します");
        final String licenseKey = plugin.config.getGeoIP2LicenseKey();
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
                tarInput.transferTo(output);
            }
        } catch (final IOException e) {
            plugin.logger.log(Level.SEVERE, "何らかの理由でGeoIPデータベースのダウンロードに失敗しました:", e);
            return false;
        }
        plugin.logger.info("GeoIPデータベースのアップデートが完了しました");
        return true;
    }
}
