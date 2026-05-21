import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.ReleaseType.STABLE
import me.modmuss50.mpp.platforms.curseforge.CurseforgeOptions
import me.modmuss50.mpp.platforms.modrinth.ModrinthOptions
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named

private const val MODRINTH_PROJECT_ID_PROPERTY = "publish.modrinth.project_id"
private const val CURSEFORGE_PROJECT_ID_PROPERTY = "publish.curseforge.project_id"

/**
 * Configures the mod-publish-plugin for loader subprojects (Fabric, NeoForge).
 * Pulls metadata from gradle.properties and environment variables.
 */
fun Project.configurePublishing() {
    configureRootGithubPublishing()

    val loader = name // "fabric" or "neoforge"
    val loaderTitle = loader.upperCaseFirst()
    val minecraftTitle = mod.prop("mc_title")
    val minecraftTargets = mod.prop("mc_targets")
    val releaseDisplayName = "Release ${mod.version} for $loaderTitle $minecraftTitle"
    
    val fallbackMinecraft = mod.dep("minecraft.$loader")
    val supportedMinecraftVersions = publishedMinecraftVersions(minecraftTargets, fallbackMinecraft)
    
    // Looks for changelog files in `.github/changelogs/<loader>-changelog.md`
    val changelogFile = rootProject.layout.projectDirectory.file(".github/changelogs/$loader-changelog.md")
    val changelogProvider = if (changelogFile.asFile.exists()) {
        providers.fileContents(changelogFile).asText
    } else {
        providers.provider { "Release ${mod.version} for $loaderTitle" }
    }

    extensions.configure<ModPublishExtension>("publishMods") {
        file.set(publishJarTaskName(loader).let { taskName ->
            tasks.named<AbstractArchiveTask>(taskName).flatMap { it.archiveFile }
        })
        this.changelog.set(changelogProvider)
        type.set(STABLE)
        version.set("${mod.version}+$minecraftTitle-$loader")
        displayName.set(releaseDisplayName)
        modLoaders.add(loader)

        // Configures publishing to Modrinth
        modrinth {
            accessToken.set(providers.environmentVariable("MODRINTH_API_KEY"))
            projectId.set(providerPropertyOrEnvironment(MODRINTH_PROJECT_ID_PROPERTY, "MODRINTH_PROJECT_ID"))
            configureMinecraftVersions(supportedMinecraftVersions)
            if (loader == "fabric") {
                requires("fabric-api")
                optional("modmenu")
            }
        }

        // Configures publishing to CurseForge
        curseforge {
            accessToken.set(providers.environmentVariable("CURSEFORGE_API_KEY"))
            projectId.set(providerPropertyOrEnvironment(CURSEFORGE_PROJECT_ID_PROPERTY, "CURSEFORGE_PROJECT_ID"))
            configureMinecraftVersions(supportedMinecraftVersions)
            clientRequired.set(true)
            serverRequired.set(true)
            this.changelog.set(changelogProvider)
            changelogType.set("markdown")
            if (loader == "fabric") {
                requires("fabric-api")
                optional("modmenu")
            }
        }
    }
}

/**
 * Configures global GitHub release publishing at the root project level.
 * Combines builds from all loader subprojects to upload them under a single GitHub release.
 */
private fun Project.configureRootGithubPublishing() {
    val configuredMarker = "multiloadertemplate.publish.root_configured"
    if (rootProject.extensions.extraProperties.has(configuredMarker)) {
        return
    }
    rootProject.extensions.extraProperties.set(configuredMarker, true)
    rootProject.plugins.apply("me.modmuss50.mod-publish-plugin")

    val modVersion = rootProject.providers.gradleProperty("mod.version")
    
    val rootChangelogFile = rootProject.layout.projectDirectory.file(".github/changelogs/changelog.md")
    val rootChangelog = if (rootChangelogFile.asFile.exists()) {
        rootProject.providers.fileContents(rootChangelogFile).asText
    } else {
        rootProject.providers.provider { "Release ${modVersion.getOrElse("1.0.0")}" }
    }

    rootProject.extensions.configure<ModPublishExtension>("publishMods") {
        changelog.set(rootChangelog)
        type.set(STABLE)
        version.set(modVersion)
        displayName.set(modVersion.map { "v$it" })

        // Configures GitHub Releases integration
        github {
            accessToken.set(rootProject.providers.environmentVariable("GITHUB_TOKEN"))
            repository.set(
                rootProject.providers.gradleProperty("publish.github.repository")
                    .orElse(rootProject.providers.environmentVariable("GITHUB_REPOSITORY"))
            )
            commitish.set(
                rootProject.providers.gradleProperty("publish.github.commitish")
                    .orElse(rootProject.providers.environmentVariable("GITHUB_SHA"))
                    .orElse(rootProject.providers.environmentVariable("GITHUB_REF_NAME"))
                    .orElse("master")
            )
            tagName.set(modVersion.map { "v$it" })
            allowEmptyFiles.set(true)
        }
    }

    // Task definitions to build/validate/publish all loaders
    val publishAllMods = rootProject.tasks.register("publishAllMods") {
        group = "publishing"
        description = "Publishes the GitHub release and all Modrinth/CurseForge files."
        dependsOn(rootProject.tasks.named("publishMods"))
    }
    val validatePublishTargets = rootProject.tasks.register("validatePublishTargets") {
        group = "verification"
        description = "Validates versioned Modrinth/CurseForge Minecraft target metadata before uploading."
    }
    val buildAllVersionedMods = rootProject.tasks.register("buildAllVersionedMods") {
        group = "build"
        description = "Builds all loader subprojects."
    }

    rootProject.gradle.projectsEvaluated {
        val loaderProjects = rootProject.subprojects
            .filter { it.name == "fabric" || it.name == "neoforge" }
            .sortedBy { it.name }

        if (loaderProjects.isNotEmpty()) {
            val releaseFiles = loaderProjects.map { project ->
                val loader = project.name
                project.tasks.named<AbstractArchiveTask>(project.publishJarTaskName(loader)).flatMap { it.archiveFile }
            }

            rootProject.extensions.configure<ModPublishExtension>("publishMods") {
                file.set(releaseFiles.first())
                additionalFiles.from(releaseFiles.drop(1))
            }
        }

        val subprojectBuilds = loaderProjects.map { it.tasks.named("build") }
        val subprojectPublishTasks = loaderProjects.map { it.tasks.named("publishMods") }

        validatePublishTargets.configure {
            doLast {
                loaderProjects.forEach { project ->
                    val targets = project.mod.prop("mc_targets")
                    val loader = project.name
                    val versions = publishedMinecraftVersions(targets, project.mod.dep("minecraft.$loader"))
                    require(versions.isNotEmpty()) {
                        "${project.path}: unsupported mod.mc_targets '$targets'."
                    }
                    logger.lifecycle("${project.path}: publishing for Minecraft ${versions.joinToString(", ")}")
                }
            }
        }

        buildAllVersionedMods.configure {
            dependsOn(subprojectBuilds)
            dependsOn(validatePublishTargets)
        }

        rootProject.tasks.named<Task>("publishMods") {
            mustRunAfter(buildAllVersionedMods)
        }
        subprojectPublishTasks.forEach { publishTask ->
            publishTask.configure {
                dependsOn(rootProject.tasks.named("publishMods"))
                mustRunAfter(buildAllVersionedMods)
            }
        }

        publishAllMods.configure {
            dependsOn(buildAllVersionedMods)
            dependsOn(subprojectPublishTasks)
        }
    }
}

private fun Project.publishJarTaskName(loader: String): String {
    return if (loader == "fabric" && tasks.names.contains("remapJar")) {
        "remapJar"
    } else {
        "jar"
    }
}

private fun Project.providerPropertyOrEnvironment(propertyName: String, environmentName: String) =
    providers.gradleProperty(propertyName).orElse(providers.environmentVariable(environmentName))

private fun ModrinthOptions.configureMinecraftVersions(versions: List<String>) {
    versions.forEach { minecraftVersions.add(it) }
}

private fun CurseforgeOptions.configureMinecraftVersions(versions: List<String>) {
    versions.forEach { minecraftVersions.add(it) }
}

private fun publishedMinecraftVersions(range: String, fallbackVersion: String): List<String> {
    val explicitVersions = explicitMinecraftVersions(range)
    if (explicitVersions != null) {
        return explicitVersions
    }
    return minecraftVersionRange(range)
        ?.let { versionRange ->
            listOf(versionRange.start, fallbackVersion, versionRange.end.takeIf { versionRange.includeEnd })
                .filterNotNull()
                .distinct()
                .filter { compareMinecraftVersions(it, versionRange.end) <= if (versionRange.includeEnd) 0 else -1 }
        }
        ?: emptyList()
}

private fun explicitMinecraftVersions(range: String): List<String>? {
    if (range.contains('<') || range.contains('>')) {
        return null
    }
    return range.split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
}

private fun minecraftVersionRange(range: String): MinecraftVersionRange? {
    val start = Regex(""">=\s*([^\s]+)""").find(range)?.groupValues?.get(1)
    val endMatch = Regex("""<(?<inclusive>=)?\s*(?<version>[^\s]+)""").find(range)
    val end = endMatch?.groups?.get("version")?.value
    val includeEnd = endMatch?.groups?.get("inclusive")?.value == "="
    return if (start != null && end != null) MinecraftVersionRange(start, end, includeEnd) else null
}

private data class MinecraftVersionRange(
    val start: String,
    val end: String,
    val includeEnd: Boolean,
)

private fun compareMinecraftVersions(left: String, right: String): Int {
    val leftParts = left.split('.').map { it.toIntOrNull() ?: 0 }
    val rightParts = right.split('.').map { it.toIntOrNull() ?: 0 }
    val maxSize = maxOf(leftParts.size, rightParts.size)
    for (index in 0 until maxSize) {
        val leftPart = leftParts.getOrElse(index) { 0 }
        val rightPart = rightParts.getOrElse(index) { 0 }
        if (leftPart != rightPart) {
            return leftPart.compareTo(rightPart)
        }
    }
    return 0
}