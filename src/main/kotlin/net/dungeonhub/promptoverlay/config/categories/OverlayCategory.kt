package net.dungeonhub.promptoverlay.config.categories

import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import net.dungeonhub.promptoverlay.PromptOverlay.MOD_ID

object OverlayCategory : CategoryKt("$MOD_ID/overlay") {
    override val name: TranslatableValue
        get() = Literal("Overlays")
}