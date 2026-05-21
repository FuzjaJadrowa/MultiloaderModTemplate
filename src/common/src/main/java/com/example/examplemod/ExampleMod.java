package com.example.examplemod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main initialization class for the Example Mod.
 * This class is loaded by both Fabric and NeoForge loader subprojects.
 */
public final class ExampleMod {
    // The MOD_ID should match the 'mod.id' property defined in your gradle.properties.
    public static final String MOD_ID = "examplemod";
    
    // Recommended logger to print console messages.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private ExampleMod() {
        // Prevent instantiation of utility class.
    }

    /**
     * Initializes the mod.
     * This method is called from loader-specific entrypoints.
     */
    public static void init() {
        LOGGER.info("Hello from Example Mod common initialization!");
    }
}