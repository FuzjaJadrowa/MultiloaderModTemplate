// src/common/build.gradle.kts
// This subproject compiles the common shared java code and resource assets.
// It uses dev.architectury.loom-no-remap because it does not compile into a standalone loader jar.

plugins {
    id("dev.architectury.loom-no-remap") version "1.14-SNAPSHOT"
}

dependencies {
    // Mojang Minecraft dependency for compiling common code.
    minecraft("com.mojang:minecraft:${project.property("dep.minecraft.fabric")}")
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
