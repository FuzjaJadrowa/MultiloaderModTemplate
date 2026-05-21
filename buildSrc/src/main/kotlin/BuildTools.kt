import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.maven
import org.gradle.language.jvm.tasks.ProcessResources
import java.io.File

/**
 * Accessor to retrieve mod configuration properties defined in gradle.properties.
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
 */
fun RepositoryHandler.strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
    forRepository { maven(url) { name = alias } }
    filter { groups.forEach(::includeGroup) }
}

/**
 * Replaces tokens in resource files (e.g., fabric.mod.json, neoforge.mods.toml) during build time.
 */
fun ProcessResources.properties(files: Iterable<String>, vararg properties: Pair<String, Any>) {
    for ((name, value) in properties) inputs.property(name, value)
    filesMatching(files) {
        expand(properties.toMap())
    }
}

/**
 * Custom task that walks through Java sources, applies the Preprocessor to strip/transform code
 * based on the active target version, and registers the output folder as the main java source dir.
 * 
 * Roots are typical folders like `src/common/src/main/java`.
 */
fun Project.versionedJavaSources(vararg roots: File) {
    val generatedSources = layout.buildDirectory.dir("generated/preprocessed/main")
    
    val prepareSources = tasks.register("prepareVersionedJavaSources") {
        inputs.files(roots)
        outputs.dir(generatedSources)
        dependsOn(tasks.matching { it.name == "stonecutterGenerate" })

        doLast {
            val outputRoot = generatedSources.get().asFile
            outputRoot.deleteRecursively()

            for (root in roots) {
                if (!root.exists()) {
                    continue
                }

                root.walkTopDown()
                    .filter { it.isFile && it.extension == "java" }
                    .forEach { file ->
                        val relative = root.toPath().relativize(file.toPath())
                        val output = outputRoot.toPath().resolve(relative).toFile()
                        output.parentFile.mkdirs()
                        // Evaluates lines using the Preprocessor based on target Minecraft version
                        // target Minecraft version is extracted from subproject name (e.g. "1.21.1-fabric" -> "1.21.1")
                        val version = project.name.substringBeforeLast('-')
                        output.writeText(Preprocessor.transform(file.readLines(), version))
                    }
            }
        }
    }

    extensions.getByType<SourceSetContainer>().named("main") {
        java.setSrcDirs(listOf(generatedSources))
    }
    
    tasks.named("compileJava") {
        dependsOn(prepareSources)
        dependsOn(tasks.matching { it.name == "stonecutterGenerate" })
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

    fun prop(key: String) = requireNotNull(project.prop("mod.$key")) { "Missing 'mod.$key' in gradle.properties" }
    fun dep(key: String) = requireNotNull(project.prop("dep.$key")) { "Missing 'dep.$key' in gradle.properties" }
}
