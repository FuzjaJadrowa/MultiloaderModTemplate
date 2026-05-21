// buildSrc/build.gradle.kts
// This project defines custom build logic and build-time dependencies shared across the project.
// In this case, we configure the Kotlin DSL and define the mod-publish-plugin dependency.

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Used for automating publishing to GitHub releases, Modrinth, and CurseForge.
    implementation("me.modmuss50:mod-publish-plugin:1.1.0")
}