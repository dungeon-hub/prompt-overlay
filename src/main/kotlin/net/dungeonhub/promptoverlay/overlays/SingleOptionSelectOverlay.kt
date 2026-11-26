package net.dungeonhub.promptoverlay.overlays

import net.dungeonhub.promptoverlay.api.render.AcceptableOverlay
import net.dungeonhub.promptoverlay.api.render.OneActionOverlay
import net.dungeonhub.promptoverlay.api.render.OneOptionOverlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.dungeonhub.promptoverlay.feature.ChatHandler.execute
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import java.awt.Color

class SingleOptionSelectOverlay(
    val firstOption: String,
    val firstAction: ClickEvent,
    messageOverride: String? = null
): AcceptableOverlay, OneActionOverlay, OneOptionOverlay {
    override val borderColor: Color = Color(OverlayCategory.optionSelectColor)
    override val message = Component.literal(messageOverride ?: "Select an option")

    override fun firstOption() {
        firstAction.execute()
    }

    override fun accept() {
        firstOption()
    }

    override val firstText: String
        get() {
            return "[${acceptKey()}/${firstOptionKey()}] $firstOption"
        }
}