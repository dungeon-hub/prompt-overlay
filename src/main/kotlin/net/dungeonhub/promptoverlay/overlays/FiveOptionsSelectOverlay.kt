package net.dungeonhub.promptoverlay.overlays

import net.dungeonhub.promptoverlay.api.render.FiveActionsOverlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.dungeonhub.promptoverlay.feature.ChatHandler.execute
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import java.awt.Color

class FiveOptionsSelectOverlay(
    firstOption: String,
    val firstAction: ClickEvent,
    secondOption: String,
    val secondAction: ClickEvent,
    thirdOption: String,
    val thirdAction: ClickEvent,
    fourthOption: String,
    val fourthAction: ClickEvent,
    fifthOption: String,
    val fifthAction: ClickEvent,
    messageOverride: String? = null
) : FiveActionsOverlay {
    override val firstText = "[${firstOptionKey()}] $firstOption"
    override val secondText = "[${secondOptionKey()}] $secondOption"
    override val thirdText = "[${thirdOptionKey()}] $thirdOption"
    override val fourthText = "[${fourthOptionKey()}] $fourthOption"
    override val fifthText = "[${fifthOptionKey()}] $fifthOption"

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

    override fun fourthOption() {
        fourthAction.execute()
    }

    override fun fifthOption() {
        fifthAction.execute()
    }
}