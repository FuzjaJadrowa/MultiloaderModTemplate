// src/neoforge/build.gradle.kts
// NeoForge-specific build configuration using net.neoforged.moddev.

plugins {
    id("net.neoforged.moddev") version "2.0.141"
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

neoForge {
    // Declares which NeoForge version to use.
    version = mod.dep("neoforge_loader")

    mods {
        register(mod.id) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        register("client") {
            gameDirectory = rootProject.file("run/neoforge/client")
            client()
        }
        register("server") {
            gameDirectory = rootProject.file("run/neoforge/server")
            server()
        }
    }
}

val requiredJava = JavaVersion.toVersion(mod.prop("java_version"))
java {
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
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

// Activates the custom ModPublishPlugin release task configured in buildSrc.
configurePublishing()