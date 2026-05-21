package com.example.examplemod.neoforge;

import net.neoforged.fml.common.Mod;
import com.example.examplemod.ExampleMod;

// Entry point for the NeoForge version of the mod.
@Mod(ExampleMod.MOD_ID)
public final class ExampleModNeoForge {
    public ExampleModNeoForge() {
        ExampleMod.LOGGER.info("Hello NeoForge!");
        ExampleMod.init();
    }
}
