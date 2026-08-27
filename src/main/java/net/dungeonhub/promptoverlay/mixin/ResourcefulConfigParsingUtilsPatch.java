package net.dungeonhub.promptoverlay.mixin;

import com.teamresourceful.resourcefulconfig.common.config.ParsingUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParsingUtils.class)
public class ResourcefulConfigParsingUtilsPatch { // TODO remove once this bug is fixed in resourceful config
    @Inject(method = "parseEnum", at = @At(value = "HEAD"), cancellable = true)
    private static void parseEnum(Class<?> clazz, String name, CallbackInfoReturnable<Enum<?>> cir) {
        try {
            if (clazz.isEnum()) {
                cir.setReturnValue(Enum.valueOf((Class<Enum>) clazz, name));
                return;
            }
            if (clazz.getSuperclass().isEnum()) {
                cir.setReturnValue(Enum.valueOf((Class<Enum>) clazz.getSuperclass(), name));
                return;
            }
            cir.setReturnValue(null);
        } catch (Exception e) {
            cir.setReturnValue(null);
        }
    }
}
