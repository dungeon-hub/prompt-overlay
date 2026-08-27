package net.dungeonhub.promptoverlay.overlays

import net.dungeonhub.promptoverlay.api.render.AcceptableOverlay
import net.dungeonhub.promptoverlay.api.render.DeniableOverlay
import net.dungeonhub.promptoverlay.api.render.TwoActionsOverlay
import net.dungeonhub.promptoverlay.api.render.TwoOptionsOverlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import java.awt.Color

class TwoOptionsSelectOverlay(
    val firstOption: String,
    val firstCommand: String,
    val secondOption: String,
    val secondCommand: String,
    messageOverride: String? = null
): AcceptableOverlay, DeniableOverlay, TwoActionsOverlay, TwoOptionsOverlay {
    override val borderColor: Color = Color(OverlayCategory.optionSelectColor)
    override val message = Component.literal(messageOverride ?: "Select an option")

    override fun firstOption() {
        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.sendCommand(firstCommand)
        }
    }

    override fun dismissText() = super<TwoActionsOverlay>.dismissText()

    override fun accept() {
        firstOption()
    }

    override fun secondOption() {
        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.sendCommand(secondCommand)
        }
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