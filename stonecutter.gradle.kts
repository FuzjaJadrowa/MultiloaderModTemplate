// stonecutter.gradle.kts
// Stonecutter configuration file defining the active version and preprocessor settings.

plugins {
    id("dev.kikugie.stonecutter")
}

// The active target version currently opened in the IDE (change this to swap context)
stonecutter active "1.21.1-fabric"

stonecutter parameters {
    // Defines string replacements that the preprocessor will insert in place of code keys.
    // E.g. replacing 'mod_version' in Java code with the actual project version.
    swaps["mod_version"] = "\"" + property("mod.version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"
    
    // Defines boolean constants that can be used in conditional preprocessing directives.
    constants["release"] = property("mod.id") != "template"
}