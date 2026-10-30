package net.dungeonhub.promptoverlay.overlays

import net.dungeonhub.promptoverlay.api.render.ZeroActionsOverlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.dungeonhub.promptoverlay.feature.OverlayRenderer
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import java.awt.Color

class DismissableNotificationOverlay(override val message: Component, override val description: Component, val dismissCommand: String) : ZeroActionsOverlay {
    constructor(message: Pair<Component, Component>, dismissCommand: String) : this(message.first, message.second, dismissCommand)

    constructor(dismissMessage: Component, dismissCommand: String) : this(parseMessage(dismissMessage), dismissCommand)

    override val borderColor: Color get() = Color(OverlayCategory.dismissableNotificationColor)

    override fun dismissText(): String {
        return super.dismissText() + " and don't show again"
    }

    override fun dismiss() {
        val command = if(dismissCommand.startsWith("/")) dismissCommand.substring(1) else dismissCommand

        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.sendCommand(command)
        }
    }

    companion object {
        private fun parseMessage(dismissMessage: Component): Pair<Component, Component> {
            val font = Minecraft.getInstance().font

            var title: FormattedText = dismissMessage
            var descriptionText: FormattedText = Component.empty()
            if(dismissMessage.string.contains("\n") || font.width(dismissMessage) > OverlayRenderer.MAX_WIDTH) {
                val lines = font.splitIgnoringLanguage(dismissMessage, OverlayRenderer.MAX_WIDTH)
                if(lines.size > 1) {
                    title = lines[0]
                    descriptionText = FormattedText.composite(lines.drop(1))
                }
            }

            return OverlayRenderer.toComponent(title) to OverlayRenderer.toComponent(descriptionText)
        }
    }
}