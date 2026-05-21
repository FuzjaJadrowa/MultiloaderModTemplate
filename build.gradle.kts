// Root build.gradle.kts
// Applies the mod-publish-plugin at the root level to coordinate and bundle assets from all subprojects.

plugins {
    id("me.modmuss50.mod-publish-plugin")
}

// Registers a clean task to clean build directories for the root project.
tasks.register("clean") {
    delete(layout.buildDirectory)
}