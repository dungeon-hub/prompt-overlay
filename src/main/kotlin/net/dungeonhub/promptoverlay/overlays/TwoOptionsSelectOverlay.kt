package net.dungeonhub.promptoverlay.overlays

import net.dungeonhub.promptoverlay.api.render.AcceptableOverlay
import net.dungeonhub.promptoverlay.api.render.DeniableOverlay
import net.dungeonhub.promptoverlay.api.render.TwoActionsOverlay
import net.dungeonhub.promptoverlay.api.render.TwoOptionsOverlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.dungeonhub.promptoverlay.feature.ChatHandler.execute
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import java.awt.Color

class TwoOptionsSelectOverlay(
    val firstOption: String,
    val firstAction: ClickEvent,
    val secondOption: String,
    val secondAction: ClickEvent,
    messageOverride: String? = null
): AcceptableOverlay, DeniableOverlay, TwoActionsOverlay, TwoOptionsOverlay {
    override val borderColor: Color = Color(OverlayCategory.optionSelectColor)
    override val message = Component.literal(messageOverride ?: "Select an option")

    override fun firstOption() {
        firstAction.execute()
    }

    override fun dismissText() = super<TwoActionsOverlay>.dismissText()

    override fun accept() {
        firstOption()
    }

    override fun secondOption() {
        secondAction.execute()
    }

    override fun deny() {
        secondOption()
    }

    override val firstText: String
        get() {
            return "[${acceptKey()}/${firstOptionKey()}] $firstOption"
        }

    override val secondText: String
        get() {
            return "[${denyKey()}/${secondOptionKey()}] $secondOption"
        }
}