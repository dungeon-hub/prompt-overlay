package net.dungeonhub.promptoverlay.overlays

import net.dungeonhub.promptoverlay.api.render.AcceptableOverlay
import net.dungeonhub.promptoverlay.api.render.OneActionOverlay
import net.dungeonhub.promptoverlay.api.render.OneOptionOverlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket
import java.awt.Color

class SingleOptionSelectOverlay(
    val firstOption: String,
    val firstAction: ClickEvent.Custom,
    messageOverride: String? = null
): AcceptableOverlay, OneActionOverlay, OneOptionOverlay {
    override val borderColor: Color = Color(OverlayCategory.optionSelectColor)
    override val message = Component.literal(messageOverride ?: "Select an option")

    override fun firstOption() {
        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.send(ServerboundCustomClickActionPacket(firstAction.id(), firstAction.payload()))
        }
    }

    override fun accept() {
        firstOption()
    }

    override val firstText: String
        get() {
            return "[${acceptKey()}/${firstOptionKey()}] $firstOption"
        }
}