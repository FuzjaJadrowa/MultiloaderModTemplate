package com.example.examplemod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Main common mod entry point containing initialization logic shared between Fabric and NeoForge.
//
// Demonstrates the use of preprocessor directives (Stonecutter) to target different Minecraft versions
// within a single file.
public final class ExampleMod {
    public static final String MOD_ID = "examplemod";
    public static final Logger LOGGER = LoggerFactory.getLogger("Example Mod");

    private ExampleMod() {
    }

    public static void init() {
        LOGGER.info("Initializing Example Mod!");

        // Preprocessor Example:
        // When compile runs on target versions:
        // - >= 1.21.4: the first block is compiled.
        // - < 1.21.4: the second block (inside comments) is uncommented and compiled.
        
        //? if >=1.21.4 {
        LOGGER.info("Example Mod: Running on Minecraft 1.21.4 or newer!");
        //?} else {
        /*LOGGER.info("Example Mod: Running on Minecraft 1.21.1 or older!");
        *///?}
    }
}
