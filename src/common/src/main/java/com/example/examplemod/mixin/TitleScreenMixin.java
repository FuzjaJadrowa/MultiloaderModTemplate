package com.example.examplemod.mixin;

import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.example.examplemod.ExampleMod;

// Shared mixin targeting the TitleScreen class in Minecraft.
//
// Demonstrates utilizing Stonecutter preprocessing inside mixins to handle differences between game versions.
@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Inject(at = @At("HEAD"), method = "init()V")
    private void init(CallbackInfo info) {
        ExampleMod.LOGGER.info("Example Mod: Title screen mixin injected!");

        //? if >=1.21.4 {
        ExampleMod.LOGGER.info("Greeting from TitleScreenMixin on Minecraft 1.21.4 or newer!");
        //?} else {
        /*ExampleMod.LOGGER.info("Greeting from TitleScreenMixin on Minecraft 1.21.1 or older!");
        *///?}
    }
}
