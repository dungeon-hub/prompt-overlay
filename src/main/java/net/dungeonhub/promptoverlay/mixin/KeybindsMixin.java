package net.dungeonhub.promptoverlay.mixin;

import net.dungeonhub.promptoverlay.service.KeyMappingService;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class KeybindsMixin {
    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void onHandleKeybinds(CallbackInfo ci) {
        KeyMappingService.INSTANCE.callListeners();
    }
}