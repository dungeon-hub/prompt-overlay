package net.dungeonhub.promptoverlay.overlays

import net.dungeonhub.promptoverlay.api.render.FourActionsOverlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket
import java.awt.Color

class FourOptionsSelectOverlay(
    firstOption: String,
    val firstAction: ClickEvent.Custom,
    secondOption: String,
    val secondAction: ClickEvent.Custom,
    thirdOption: String,
    val thirdAction: ClickEvent.Custom,
    fourthOption: String,
    val fourthAction: ClickEvent.Custom,
    messageOverride: String? = null
) : FourActionsOverlay {
    override val firstText = "[${firstOptionKey()}] $firstOption"
    override val secondText = "[${secondOptionKey()}] $secondOption"
    override val thirdText = "[${thirdOptionKey()}] $thirdOption"
    override val fourthText = "[${fourthOptionKey()}] $fourthOption"

    override val borderColor: Color = Color(OverlayCategory.optionSelectColor)
    override val message = Component.literal(messageOverride ?: "Select an option")

    override fun firstOption() {
        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.send(ServerboundCustomClickActionPacket(firstAction.id(), firstAction.payload()))
        }
    }

    override fun secondOption() {
        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.send(ServerboundCustomClickActionPacket(secondAction.id(), secondAction.payload()))
        }
    }

    override fun thirdOption() {
        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.send(ServerboundCustomClickActionPacket(thirdAction.id(), thirdAction.payload()))
        }
    }

    override fun fourthOption() {
        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.send(ServerboundCustomClickActionPacket(fourthAction.id(), fourthAction.payload()))
        }
    }
}