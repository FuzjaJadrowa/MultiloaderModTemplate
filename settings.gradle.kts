// settings.gradle.kts
// This file initializes the Gradle project, declares plugin repositories, and includes subprojects.

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        // Repository for Fabric Loom plugins and components
        maven("https://maven.fabricmc.net/")
        // Repository for Architectury Loom and helper utilities
        maven("https://maven.architectury.dev")
        // Repository for NeoForge Gradle/moddev plugins
        maven("https://maven.neoforged.net/releases/")
    }
}

// Rename this to match your mod project name.
rootProject.name = "MultiloaderModTemplate"

// Include the 'common' subproject containing code/resources shared between both loaders.
include("common")
project(":common").projectDir = file("src/common")

// Include the 'fabric' subproject containing Fabric-specific code and resources.
include("fabric")
project(":fabric").projectDir = file("src/fabric")

// Include the 'neoforge' subproject containing NeoForge-specific code and resources.
include("neoforge")
project(":neoforge").projectDir = file("src/neoforge")
