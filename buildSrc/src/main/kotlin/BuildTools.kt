import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.maven
import org.gradle.language.jvm.tasks.ProcessResources

/**
 * Accessor to retrieve mod configuration properties defined in gradle.properties.
 * Example usage: project.mod.id, project.mod.version
 */
val Project.mod: ModData get() = ModData(this)

/**
 * Helper to retrieve a property from gradle.properties as a String.
 */
fun Project.prop(key: String): String? = findProperty(key)?.toString()

/**
 * Helper to capitalize the first letter of a String (e.g., "fabric" -> "Fabric").
 */
fun String.upperCaseFirst() = replaceFirstChar { if (it.isLowerCase()) it.uppercaseChar() else it }

/**
 * Utility to restrict a Maven repository to only resolve specific groups.
 * This improves build speeds and prevents dependency resolution confusion.
 */
fun RepositoryHandler.strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
    forRepository { maven(url) { name = alias } }
    filter { groups.forEach(::includeGroup) }
}

/**
 * Replaces tokens in resource files (e.g., fabric.mod.json, neoforge.mods.toml) during build time.
 * For example, "${id}" becomes "examplemod".
 */
fun ProcessResources.properties(files: Iterable<String>, vararg properties: Pair<String, Any>) {
    for ((name, value) in properties) inputs.property(name, value)
    filesMatching(files) {
        expand(properties.toMap())
    }
}

/**
 * Structured container for accessing mod metadata and dependency versions from gradle.properties.
 */
@JvmInline
value class ModData(private val project: Project) {
    val id: String get() = requireNotNull(project.prop("mod.id")) { "Missing 'mod.id' in gradle.properties" }
    val name: String get() = requireNotNull(project.prop("mod.name")) { "Missing 'mod.name' in gradle.properties" }
    val version: String get() = requireNotNull(project.prop("mod.version")) { "Missing 'mod.version' in gradle.properties" }
    val group: String get() = requireNotNull(project.prop("mod.group")) { "Missing 'mod.group' in gradle.properties" }

    /**
     * Retrieve any property prefixed with "mod."
     */
    fun prop(key: String) = requireNotNull(project.prop("mod.$key")) { "Missing 'mod.$key' in gradle.properties" }

    /**
     * Retrieve any dependency version prefixed with "dep."
     */
    fun dep(key: String) = requireNotNull(project.prop("dep.$key")) { "Missing 'dep.$key' in gradle.properties" }
}