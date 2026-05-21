package com.example.examplemod.fabric;

import net.fabricmc.api.ModInitializer;
import com.example.examplemod.ExampleMod;

// Entry point for the Fabric version of the mod.
public final class ExampleModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ExampleMod.LOGGER.info("Hello Fabric!");
        ExampleMod.init();
    }
}