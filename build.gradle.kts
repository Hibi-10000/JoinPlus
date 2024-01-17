/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `java-library`
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

group = "com.github.hibi_10000.plugins"
version = "1.3.0-SNAPSHOT"
description = "JoinPlus"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven (url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.1.0")
    api("com.maxmind.geoip2:geoip2:4.2.0")
    api("org.apache.commons:commons-compress:1.25.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Jar> {
    from("LICENSE.txt", "README.md")
}

bukkit {
    author = "Hibi_10000"
    website = "https://github.com/Hibi-10000/JoinPlus"
    main = "com.github.hibi_10000.plugins.joinplus.JoinPlus"
    apiVersion = "1.17"
    foliaSupported = true
    defaultPermission = BukkitPluginDescription.Permission.Default.OP

    commands {
        register("joinplus") {
            description = "JoinPlus command."
            permission = "joinplus.command"
            // permissionMessage = "You don't have permission to use this command!"
        }
    }

    permissions {
        register("joinplus.command") {
            description = "Permission to use the /joinplus command."
        }
        register("joinplus.command.reload") {
            description = "Permission to use the /joinplus reload command."
            children = listOf("joinplus.command")
        }
        register("joinplus.command.geoupdate") {
            description = "Permission to use the /joinplus geoupdate command."
            children = listOf("joinplus.command")
        }
    }
}
