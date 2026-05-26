// src/neoforge/build.gradle.kts
// NeoForge-specific build configuration using dev.architectury.loom.

plugins {
    id("dev.architectury.loom") version "1.14-SNAPSHOT"
    id("me.modmuss50.mod-publish-plugin")
}

// Ensure the common project has been evaluated so its source set references are populated.
evaluationDependsOn(":common")

version = "${mod.version}+${mod.prop("mc_title")}"
group = mod.group

base {
    // Generates output named e.g., ExampleMod-neoforge-1.0.0+1.21.1.jar
    archivesName.set("${mod.name}-neoforge")
}

sourceSets {
    main {
        // Link to the common java sources and resource files so they compile directly into this loader.
        val commonSourceSets = project(":common").extensions.getByType<SourceSetContainer>()
        val commonMain = commonSourceSets.named("main").get()
        java.srcDirs(commonMain.java.srcDirs)
        resources.srcDirs(commonMain.resources.srcDirs)
    }
}

repositories {
    maven("https://maven.neoforged.net/releases/")
}

dependencies {
    minecraft("com.mojang:minecraft:${mod.dep("minecraft.neoforge")}")
    mappings(loom.officialMojangMappings())
    neoForge("net.neoforged:neoforge:${mod.dep("neoforge_loader")}")
    compileOnly(project(":common")) {
        isTransitive = false
    }
}

loom {
    runs {
        named("client") {
            runDir = "../../run/neoforge/client"
        }
        named("server") {
            runDir = "../../run/neoforge/server"
        }
    }
}

val requiredJava = JavaVersion.toVersion(mod.prop("java_version"))
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(requiredJava.majorVersion))
    }
}

tasks.withType<JavaExec>().configureEach {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(requiredJava.majorVersion))
    })
}

tasks.processResources {
    // Replace tokens in neoforge.mods.toml with properties loaded from gradle.properties
    properties(
        listOf("META-INF/neoforge.mods.toml"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to mod.prop("mc_targets"),
        "loader" to mod.dep("neoforge_loader_range"),
        "neoforge" to mod.dep("neoforge_version_range")
    )
    // Replace tokens in mixin configurations
    properties(
        listOf("*.mixins.json"),
        "java" to mod.prop("java_version")
    )
}

tasks.remapJar {
    archiveClassifier.set("")
}

tasks.jar {
    archiveClassifier.set("dev")
}

// Activates the custom ModPublishPlugin release task configured in buildSrc.
configurePublishing()