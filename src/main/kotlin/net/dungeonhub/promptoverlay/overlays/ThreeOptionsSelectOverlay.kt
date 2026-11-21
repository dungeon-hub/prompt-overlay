package net.dungeonhub.promptoverlay.overlays

import net.dungeonhub.promptoverlay.api.render.ThreeActionsOverlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.dungeonhub.promptoverlay.feature.ChatHandler.execute
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import java.awt.Color

class ThreeOptionsSelectOverlay(
    firstOption: String,
    val firstAction: ClickEvent,
    secondOption: String,
    val secondAction: ClickEvent,
    thirdOption: String,
    val thirdAction: ClickEvent,
    messageOverride: String? = null
) : ThreeActionsOverlay {
    override val firstText = "[${firstOptionKey()}] $firstOption"
    override val secondText = "[${secondOptionKey()}] $secondOption"
    override val thirdText = "[${thirdOptionKey()}] $thirdOption"

    override val borderColor: Color = Color(OverlayCategory.optionSelectColor)
    override val message = Component.literal(messageOverride ?: "Select an option")

    override fun firstOption() {
        firstAction.execute()
    }

    override fun secondOption() {
        secondAction.execute()
    }

    override fun thirdOption() {
        thirdAction.execute()
    }
}