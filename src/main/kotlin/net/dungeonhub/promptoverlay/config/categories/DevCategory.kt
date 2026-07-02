package net.dungeonhub.promptoverlay.config.categories

import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import net.dungeonhub.promptoverlay.PromptOverlay.MOD_ID
import net.dungeonhub.promptoverlay.config.Config

object DevCategory : CategoryKt("$MOD_ID/auth") {
    override val name: TranslatableValue
        get() = Literal("Developer")

    override val hidden: Boolean
        get() = !Config.developer

    var extendedDebug by boolean("extended_debug", false) {
        name = Literal("Extended Debug output")
        description = Literal("This increases the amount of information sent to you ingame.")
    }
}