// src/common/build.gradle.kts
// This subproject compiles the common shared java code and resource assets.
// It does not compile into a standalone loader jar.

plugins {
    id("dev.architectury.loom") version "1.14-SNAPSHOT"
}

dependencies {
    // Mojang Minecraft dependency for compiling common code.
    minecraft("com.mojang:minecraft:${project.property("dep.minecraft.fabric")}")
    // Other dependencies to avoid visual errors in code editors.
    mappings(loom.officialMojangMappings())
    modCompileOnly("net.fabricmc:fabric-loader:${project.property("dep.fabric_loader")}")
}

// We disable java compilation tasks in the common module itself because the common sources
// are pulled and compiled directly within the loader-specific subprojects (fabric, neoforge)
// to ensure correct dependencies and mappings are applied.
tasks.compileJava {
    enabled = false
}

tasks.jar {
    enabled = false
}

tasks.named("remapJar") {
    enabled = false
}