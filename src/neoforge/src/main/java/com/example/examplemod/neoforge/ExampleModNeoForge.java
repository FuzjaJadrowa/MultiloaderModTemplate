package com.example.examplemod.neoforge;

import com.example.examplemod.ExampleMod;
import net.neoforged.fml.common.Mod;

/**
 * Entrypoint class for NeoForge mod loader.
 * Specified using the @Mod annotation, matching the mod ID from gradle.properties.
 */
@Mod(ExampleMod.MOD_ID)
public final class ExampleModNeoForge {
    public ExampleModNeoForge() {
        // Delegate initialization to the common init method.
        ExampleMod.init();
        ExampleMod.LOGGER.info("NeoForge loader initialized successfully!");
    }
}
