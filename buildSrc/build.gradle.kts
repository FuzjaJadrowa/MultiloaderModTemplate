// buildSrc/build.gradle.kts
// Shared build logic for the Multi-Version template.

plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.3.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Declares the mod-publish-plugin to allow publishing inside build scripts.
    implementation("me.modmuss50:mod-publish-plugin:1.1.0")
}
