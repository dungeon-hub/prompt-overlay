package net.dungeonhub.promptoverlay.overlays

import net.dungeonhub.promptoverlay.api.render.ThreeActionsOverlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import java.awt.Color

class ThreeOptionsSelectOverlay(
    firstOption: String,
    val firstCommand: String,
    secondOption: String,
    val secondCommand: String,
    thirdOption: String,
    val thirdCommand: String,
    messageOverride: String? = null
) : ThreeActionsOverlay {
    override val firstText = "[${firstOptionKey()}] $firstOption"
    override val secondText = "[${secondOptionKey()}] $secondOption"
    override val thirdText = "[${thirdOptionKey()}] $thirdOption"

    override val borderColor: Color = Color(OverlayCategory.optionSelectColor)
    override val message = Component.literal(messageOverride ?: "Select an option")

    override fun firstOption() {
        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.sendCommand(firstCommand)
        }
    }

    override fun secondOption() {
        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.sendCommand(secondCommand)
        }
    }

    override fun thirdOption() {
        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.sendCommand(thirdCommand)
        }
    }
}