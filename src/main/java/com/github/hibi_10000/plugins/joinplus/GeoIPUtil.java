/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.hibi_10000.plugins.joinplus;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;

import java.io.*;
import java.net.InetAddress;
import java.util.Date;
import java.util.logging.Level;

public class GeoIPUtil {
    private final JoinPlus plugin;

    public GeoIPUtil(JoinPlus instance) {
        this.plugin = instance;
    }

    public String getCountry(final InetAddress ipAddress) {
        if (plugin.databasefile.exists()) return "N/A";
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
}
