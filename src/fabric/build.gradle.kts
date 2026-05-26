// src/fabric/build.gradle.kts
// Fabric-specific build configuration using dev.architectury.loom.

plugins {
    id("dev.architectury.loom") version "1.14-SNAPSHOT"
    id("me.modmuss50.mod-publish-plugin")
}

// Ensure the common project has been evaluated so its source set references are populated.
evaluationDependsOn(":common")

version = "${mod.version}+${mod.prop("mc_title")}"
group = mod.group

base {
    // Generates output named e.g., ExampleMod-fabric-1.0.0+1.21.1.jar
    archivesName.set("${mod.name}-fabric")
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

dependencies {
    minecraft("com.mojang:minecraft:${mod.dep("minecraft.fabric")}")
    mappings(loom.officialMojangMappings())
    compileOnly(project(":common"))

    modImplementation("net.fabricmc:fabric-loader:${mod.dep("fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${mod.dep("fabric_api_version")}")
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

loom {
    runs {
        named("client") {
            runDir = "../../run/fabric/client"
        }
        named("server") {
            runDir = "../../run/fabric/server"
        }
    }
}

tasks.processResources {
    // Replace tokens in fabric.mod.json with properties loaded from gradle.properties
    properties(
        listOf("fabric.mod.json"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to mod.prop("mc_targets"),
        "java" to mod.prop("java_version"),
        "fabric_loader" to mod.dep("fabric_loader")
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