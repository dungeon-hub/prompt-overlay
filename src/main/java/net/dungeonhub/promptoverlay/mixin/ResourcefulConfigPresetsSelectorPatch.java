package net.dungeonhub.promptoverlay.mixin;

import com.teamresourceful.resourcefulconfig.client.components.options.types.ColorOptionWidget;
import com.teamresourceful.resourcefulconfig.client.components.options.types.color.HsbState;
import com.teamresourceful.resourcefulconfig.client.components.options.types.color.PresetsSelector;
import com.teamresourceful.resourcefulconfig.client.utils.State;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PresetsSelector.class)
public class ResourcefulConfigPresetsSelectorPatch {
    @Mutable
    @Final
    @Shadow
    private boolean withAlpha;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/teamresourceful/resourcefulconfig/client/components/options/types/color/PresetsSelector;getColors()Ljava/util/Collection;"))
    public void constructorHead(
            int width,
            int[] presets,
            State<ColorOptionWidget.PresetType> type,
            HsbState state,
            boolean withAlpha,
            CallbackInfo ci
    ) {
        this.withAlpha = withAlpha; // TODO remove once this oversight is fixed in resourceful config
    }
}
