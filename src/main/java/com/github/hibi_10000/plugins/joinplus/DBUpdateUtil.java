package com.github.hibi_10000.plugins.joinplus;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

public class DBUpdateUtil {
    private final JoinPlus plugin;

    public DBUpdateUtil(JoinPlus instance) {
        this.plugin = instance;
    }

    public String fileHash(InputStream input) throws IOException {
        byte[] bytes = ByteStreams.toByteArray(input);
        return Hashing.sha256().hashBytes(bytes).toString();
    }

    public URLConnection getConnection(String url) {
        final URL downloadUrl;
        try {
            downloadUrl = new URL(url);
        } catch (final MalformedURLException e) {
            plugin.logger.log(Level.SEVERE, "GeoIPデータベースのダウンロードURLが間違っています:", e);
            return null;
        }
        final URLConnection conn;
        try {
            conn = downloadUrl.openConnection();
            conn.setConnectTimeout(10000);
            conn.connect();
        } catch (final IOException e) {
            plugin.logger.log(Level.SEVERE, "GeoIPデータベースのダウンロードサーバーに接続できませんでした:", e);
            return null;
        }
        return conn;
    }

    public String getHash() {
        final String licenseKey = plugin.config.getGeoIP2LicenseKey();
        final String url = plugin.config.getGeoIP2DBSha256URL().replace("{LICENSE_KEY}", licenseKey);
        if (!url.contains("tar.gz.sha256")) {
            plugin.logger.severe("GeoIPデータベースのSHA256のダウンロードURLが間違っています");
            return null;
        }
        final URLConnection conn = getConnection(url);
        if (conn == null) return null;
        String hash;
        try (final InputStream input = conn.getInputStream()) {
            hash = new String(input.readAllBytes(), StandardCharsets.UTF_8).substring(0, 64);
        } catch (IOException e) {
            plugin.logger.log(Level.SEVERE, "GeoIPデータベースのSHA256のダウンロードに失敗しました:", e);
            return null;
        }
        return hash;
    }

    public boolean updateDB() {
        plugin.logger.info("GeoIPデータベースのアップデートを開始します");
        final String licenseKey = plugin.config.getGeoIP2LicenseKey();
        final String url = plugin.config.getGeoIP2DBDownloadURL().replace("{LICENSE_KEY}", licenseKey);
        if (!url.contains("tar.gz")) {
            plugin.logger.severe("GeoIPデータベースのダウンロードURLが間違っています");
            return false;
        }
        final URLConnection conn = getConnection(url);
        if (conn == null) return false;
        plugin.logger.info("GeoIPデータベースをダウンロードしています...");
        try (final InputStream input = conn.getInputStream();
             final GZIPInputStream gzipInput = new GZIPInputStream(input);
             final TarInputStream tarInput = new TarInputStream(gzipInput)) {
            String fileHash = fileHash(input);
            String expectHash = getHash();
            if (expectHash == null) return false;
            if (!fileHash.equals(expectHash)) {
                plugin.logger.severe("ダウンロードしたファイルのハッシュ検証に失敗しました");
                return false;
            }
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
