package com.example.examplemod.mixin;

import com.example.examplemod.ExampleMod;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Example Mixin targeting net.minecraft.client.gui.screens.TitleScreen.
 * Mixins allow you to inject custom code into Minecraft's internal classes.
 */
@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(at = @At("HEAD"), method = "init")
    private void init(CallbackInfo info) {
        ExampleMod.LOGGER.info("Example Mod successfully injected code into TitleScreen.init() via Mixins!");
    }
}
