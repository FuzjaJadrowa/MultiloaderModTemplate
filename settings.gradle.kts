// settings.gradle.kts
// Root settings configuration for the Multi-Version Stonecutter template.
// This file initializes all target subprojects dynamically based on the requested Minecraft versions.

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    // Stonecutter plugin manages multi-version layouts
    id("dev.kikugie.stonecutter") version "0.9"
}

stonecutter {
    kotlinController = true
    shared {
        /**
         * Registers a list of target versions for a specific mod loader.
         * Automatically ensures the directory structure exists and synchronizes property files.
         */
        fun mc(loader: String, vararg versions: String) {
            for (version in versions) {
                val targetDir = file("versions/$version-$loader")
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }

                // Automatically sync properties from gradle/targets/ to the versioned folder.
                // This guarantees that local development is always in sync with central target definitions.
                val sourceProps = file("gradle/targets/$version.properties")
                val targetProps = file("versions/$version-$loader/gradle.properties")
                if (sourceProps.exists()) {
                    sourceProps.copyTo(targetProps, overwrite = true)
                }

                // Choose the appropriate buildscript template depending on the Minecraft version.
                // Modern versions (e.g. >= 26.1) use unobfuscated scripts, while others use standard.
                val buildscript = when {
                    sc.eval(version, ">= 26.1") && loader == "fabric" -> "build-unobfuscated-fabric.gradle.kts"
                    sc.eval(version, ">= 26.1") && loader == "neoforge" -> "build-unobfuscated-neoforge.gradle.kts"
                    loader == "fabric" -> "build-obfuscated-fabric.gradle.kts"
                    loader == "neoforge" -> "build-obfuscated-neoforge.gradle.kts"
                    else -> error("Unsupported loader: $loader")
                }
                
                version("$version-$loader", version).buildscript(buildscript)
            }
        }

        // --- CONFIGURE YOUR TARGET VERSIONS HERE ---
        // To add a new Minecraft version:
        // 1. Create a corresponding properties file in gradle/targets/ (e.g., gradle/targets/1.20.4.properties)
        // 2. Add the version string here.
        mc("fabric", "1.21.1", "26.1")
        mc("neoforge", "1.21.1", "26.1")
    }
    create(rootProject)
}

rootProject.name = "MultiloaderModTemplate"