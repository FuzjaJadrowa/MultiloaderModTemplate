package com.example.examplemod.fabric;

import com.example.examplemod.ExampleMod;
import net.fabricmc.api.ModInitializer;

/**
 * Entrypoint class for Fabric mod loader.
 * Specified in fabric.mod.json under the "main" entrypoint list.
 */
public final class ExampleModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Delegate initialization to the common init method.
        ExampleMod.init();
        ExampleMod.LOGGER.info("Fabric loader initialized successfully!");
    }
}
