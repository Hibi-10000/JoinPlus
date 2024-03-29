/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.hibi_10000.plugins.joinplus;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.logging.Level;

public class DBUpdateUtil {
    private final JoinPlus plugin;

    public DBUpdateUtil(JoinPlus instance) {
        this.plugin = instance;
    }

    public static String fileHash(InputStream input) throws IOException {
        byte[] bytes = ByteStreams.toByteArray(input);
        return Hashing.sha256().hashBytes(bytes).toString();
    }

    public void schedule() {
        final Date dbDate = plugin.geoUtil.getDBBuildDate();
        if (dbDate == null) return;
        final long diff = new Date().getTime() - dbDate.getTime();
        long later = (1000 * 60 * 60 * 24) - diff;
        if ((diff / (1000 * 60 * 60)) > 24) later = 0;
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (checkUpdates()) updateDB();
        }, later / 50, 20L * 60 * 60 * 24);
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

    public boolean checkUpdates() {
        plugin.logger.info("GeoIPデータベースのアップデートを確認しています...");
        String hash = getHash();
        updateJsonCountry(null);
        UpdaterJson json = getJson();
        if (json == null || !hash.equals(json.getCountry().getSha256())) {
            plugin.logger.info("GeoIPデータベースのアップデートが見つかりました");
            return true;
        }
        plugin.logger.info("GeoIPデータベースは最新です");
        return false;
    }

    public boolean updateDB() {
        plugin.logger.info("GeoIPデータベースのアップデートを開始します");
        final String licenseKey = plugin.config.getGeoIP2LicenseKey();
        final String url = plugin.config.getGeoIP2DBDownloadURL().replace("{LICENSE_KEY}", licenseKey);
        final URLConnection conn = getConnection(url);
        if (conn == null) return false;
        plugin.logger.info("GeoIPデータベースをダウンロードしています...");
        try (final InputStream input = conn.getInputStream();
             final GzipCompressorInputStream gzipInput = new GzipCompressorInputStream(input);
             final TarArchiveInputStream tarInput = new TarArchiveInputStream(gzipInput)) {
            String fileHash = fileHash(input);
            String expectHash = getHash();
            if (expectHash == null) return false;
            if (!fileHash.equals(expectHash)) {
                plugin.logger.severe("ダウンロードしたファイルのハッシュ検証に失敗しました");
                return false;
            }
            TarArchiveEntry entry;
            while ((entry = tarInput.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().equals(plugin.config.getGeoIP2DBFileName())) break;
            }
            try (final OutputStream output = new FileOutputStream(plugin.databasefile)) {
                tarInput.transferTo(output);
            }
            updateJsonCountry(fileHash);
        } catch (final IOException e) {
            plugin.logger.log(Level.SEVERE, "何らかの理由でGeoIPデータベースのダウンロードに失敗しました:", e);
            return false;
        }
        plugin.logger.info("GeoIPデータベースのアップデートが完了しました");
        return true;
    }

    void updateJsonCountry(String sha256) {
        UpdaterJson json = getJson();
        if (json == null) json = new UpdaterJson();
        UpdaterJson.UpdaterJsonData country = json.getCountry();
        if (sha256 != null) country.setSha256(sha256);
        country.setLastCheckUpdate(new Date().getTime());
        json.setCountry(country);
        updateJson(json);
    }

    UpdaterJson getJson() {
        File updaterJson = new File(plugin.getDataFolder(), "dbupdater.json");
        if (!updaterJson.exists() || updaterJson.isDirectory()) return null;
        try (FileReader reader = new FileReader(updaterJson, StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, UpdaterJson.class);
        } catch (IOException e) {
            plugin.logger.log(Level.SEVERE, "dbupdater.jsonの読み込みに失敗しました", e);
            return null;
        }
    }

    void updateJson(@NotNull UpdaterJson json) {
        File updaterJson = new File(plugin.getDataFolder(), "dbupdater.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(updaterJson, StandardCharsets.UTF_8)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            plugin.logger.log(Level.SEVERE, "dbupdater.jsonへの書き込みに失敗しました", e);
        }
    }

    static class UpdaterJson {
        @NotNull
        private UpdaterJsonData country = new UpdaterJsonData();
        @NotNull
        private UpdaterJsonData city = new UpdaterJsonData();

        @NotNull
        public UpdaterJsonData getCountry() {
            return country;
        }

        @NotNull
        public UpdaterJsonData getCity() {
            return city;
        }

        public void setCountry(@NotNull UpdaterJsonData country) {
            this.country = country;
        }

        public void setCity(@NotNull UpdaterJsonData city) {
            this.city = city;
        }

        static class UpdaterJsonData {
            @NotNull
            private String sha256 = "";
            @NotNull
            private Long lastCheckUpdate = 0L;

            @NotNull
            public String getSha256() {
                return sha256;
            }

            @NotNull
            public Long getLastCheckUpdate() {
                return lastCheckUpdate;
            }

            public void setSha256(@NotNull String sha256) {
                this.sha256 = sha256;
            }

            public void setLastCheckUpdate(@NotNull Long lastCheckUpdate) {
                this.lastCheckUpdate = lastCheckUpdate;
            }
        }
    }
}
