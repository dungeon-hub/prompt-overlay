package net.dungeonhub.promptoverlay.overlays

import net.dungeonhub.promptoverlay.api.render.AcceptableOverlay
import net.dungeonhub.promptoverlay.api.render.OneActionOverlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import java.awt.Color
import kotlin.time.Duration.Companion.seconds

class AbiphoneCallOverlay(caller: String?, val acceptCommand: String) : AcceptableOverlay, OneActionOverlay {
    override fun accept() {
        val command = if(acceptCommand.startsWith("/")) acceptCommand.substring(1) else acceptCommand

        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.sendCommand(command)
        }
    }

    override val borderColor get() = Color(OverlayCategory.abiphoneColor)
    override val message = Component.literal("Incoming call")
    override val description = caller?.let { Component.literal("From $it") } ?: Component.empty()
    override val firstText get() = "[${acceptKey()}] Accept"
    override val maxDisplayDuration = 7.seconds
}