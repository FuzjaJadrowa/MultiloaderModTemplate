import groovy.json.JsonOutput
import groovy.json.JsonSlurper

// build-unobfuscated-neoforge.gradle.kts
// NeoForge unobfuscated build configuration file template for the Multi-Version setup.
// This script is dynamically loaded by Stonecutter for NeoForge target versions >= 26.1.

plugins {
    // NeoForge Mod Development plugin handles environment, mappings, and runs
    id("net.neoforged.moddev") version "2.0.141"
    // Mod Publish Plugin facilitates uploading to Modrinth, CurseForge, and GitHub
    id("me.modmuss50.mod-publish-plugin")
}

val minecraftTitle = mod.prop("mc_title")
val loader = stonecutter.current.project.substringAfterLast('-') // e.g. "neoforge"
val javaVersion = mod.prop("java_version")

version = "${mod.version}+$minecraftTitle"
group = mod.group
base {
    archivesName.set("${mod.name}-$loader")
}

sourceSets {
    main {
        // Point resources to the shared common and loader-specific folders
        resources.srcDir(rootProject.file("src/common/src/main/resources"))
        resources.srcDir(rootProject.file("src/neoforge/src/main/resources"))
    }
}

// Applies conditional preprocessing (Stonecutter-style directives) on all Java sources
versionedJavaSources(
    rootProject.file("src/common/src/main/java"),
    rootProject.file("src/neoforge/src/main/java")
)

repositories {
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.terraformersmc.com/")
}

val projectName = project.name

neoForge {
    version = mod.dep("neoforge_loader")

    mods {
        register(mod.id) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        register("client") {
            gameDirectory = rootProject.file("run/$projectName/client")
            client()
        }
        register("server") {
            gameDirectory = rootProject.file("run/$projectName/server")
            server()
        }
    }
}

val requiredJava = JavaVersion.toVersion(javaVersion)

java {
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

tasks.jar {
    inputs.property("archivesName", base.archivesName)
}

// Collects the output jar into build/libs/[version]/neoforge/ for convenience
val buildAndCollect = tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.jar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}/$loader"))
    dependsOn("build")
}

if (stonecutter.current.isActive) {
    rootProject.tasks.register("buildActive") {
        group = "project"
        description = "Builds and collects active subproject artifacts."
        dependsOn(buildAndCollect)
    }

    rootProject.tasks.register("testClient") {
        group = "project"
        description = "Launches the client for testing the active NeoForge version."
        dependsOn(tasks.named("clientRun"))
    }

    rootProject.tasks.register("testServer") {
        group = "project"
        description = "Launches the server for testing the active NeoForge version."
        dependsOn(tasks.named("serverRun"))
    }
}

// Dynamic properties replacement in neoforge.mods.toml and mixin configurations
tasks.processResources {
    properties(
        listOf("META-INF/neoforge.mods.toml"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to mod.prop("mc_targets"),
        "loader" to mod.dep("neoforge_loader_range"),
        "neoforge" to mod.dep("neoforge_version_range")
    )
    properties(
        listOf("*.mixins.json"),
        "java" to javaVersion
    )

    doLast {
        fileTree(outputs.files.singleFile).matching {
            include("**/*.json")
        }.forEach { file ->
            file.writeText(JsonOutput.toJson(JsonSlurper().parse(file)))
        }
    }
}

tasks.build {
    group = "versioned"
    description = "Stonecutter target build task."
}

stonecutter {
    // Inject constants to resolve conditional compile blocks (e.g. #if neoforge)
    constants {
        put("fabric", false)
        put("neoforge", true)
    }
}

// Register mod publishing settings defined in PublishTools.kt
configureModPublishing()