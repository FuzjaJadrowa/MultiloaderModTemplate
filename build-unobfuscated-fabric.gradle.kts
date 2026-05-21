import groovy.json.JsonOutput
import groovy.json.JsonSlurper

// build-unobfuscated-fabric.gradle.kts
// Fabric unobfuscated build configuration file template for the Multi-Version setup.
// This script is dynamically loaded by Stonecutter for Fabric target versions >= 26.1.

plugins {
    // Architectury Loom (unobfuscated/no-remap variant) manages Minecraft environment, mappings, and runs.
    id("dev.architectury.loom-no-remap") version "1.14-SNAPSHOT"
    // Mod Publish Plugin facilitates uploading to Modrinth, CurseForge, and GitHub.
    id("me.modmuss50.mod-publish-plugin")
}

val minecraftTitle = mod.prop("mc_title")
val loader = stonecutter.current.project.substringAfterLast('-') // e.g. "fabric"
val minecraftDependency = mod.dep("minecraft.fabric")
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
        resources.srcDir(rootProject.file("src/fabric/src/main/resources"))
    }
}

// Applies conditional preprocessing (Stonecutter-style directives) on all Java sources
versionedJavaSources(
    rootProject.file("src/common/src/main/java"),
    rootProject.file("src/fabric/src/main/java")
)

repositories {
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.terraformersmc.com/")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftDependency")

    // Standard Fabric dependencies (unobfuscated build sets compileOnly/implementation directly)
    implementation("net.fabricmc:fabric-loader:${mod.dep("fabric_loader")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${mod.dep("fabric_api_version")}")
}

val requiredJava = JavaVersion.toVersion(javaVersion)

java {
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

tasks.jar {
    inputs.property("archivesName", base.archivesName)
}

// Collects the output jar into build/libs/[version]/fabric/ for convenience
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
        description = "Launches the client for testing the active Fabric version."
        dependsOn(tasks.named("runClient"))
    }

    rootProject.tasks.register("testServer") {
        group = "project"
        description = "Launches the server for testing the active Fabric version."
        dependsOn(tasks.named("runServer"))
    }
}

loom {
    runs {
        named("client") {
            runDir = project.projectDir.toPath()
                .relativize(rootProject.file("run/${project.name}/client").toPath())
                .toString()
        }
        named("server") {
            runDir = project.projectDir.toPath()
                .relativize(rootProject.file("run/${project.name}/server").toPath())
                .toString()
        }
    }
}

// Dynamic properties replacement in fabric.mod.json and mixin configurations
tasks.processResources {
    properties(
        listOf("fabric.mod.json"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to mod.prop("mc_targets"),
        "java" to javaVersion,
        "fabric_loader" to mod.dep("fabric_loader")
    )
    properties(
        listOf("*.mixins.json"),
        "java" to javaVersion
    )
}

tasks.build {
    group = "versioned"
    description = "Stonecutter target build task."
}

// Compacts all generated resource JSON files to minimize final jar footprint
tasks.processResources {
    doLast {
        fileTree(outputs.files.singleFile).matching {
            include("**/*.json")
        }.forEach { file ->
            file.writeText(JsonOutput.toJson(JsonSlurper().parse(file)))
        }
    }
}

stonecutter {
    // Inject constants to resolve conditional compile blocks (e.g. #if fabric)
    constants {
        put("fabric", true)
        put("neoforge", false)
    }
}

// Register mod publishing settings defined in PublishTools.kt
configureModPublishing()
