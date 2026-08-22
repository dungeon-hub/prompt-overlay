package net.dungeonhub.promptoverlay.overlays

import net.dungeonhub.promptoverlay.api.render.AcceptableOverlay
import net.dungeonhub.promptoverlay.api.render.OneActionOverlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.dungeonhub.promptoverlay.service.KeyMappingService
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import java.awt.Color
import kotlin.time.Duration.Companion.seconds

class PartyInviteOverlay(val inviter: String) : AcceptableOverlay, OneActionOverlay {
    override fun accept() {
        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.sendCommand("party accept $inviter")
        }
    }

    override val borderColor: Color get() = Color(OverlayCategory.partyColor)
    override val message = Component.literal("Party invite")
    override val description = Component.literal("From $inviter")
    override val maxDisplayDuration = 60.seconds

    override val firstText: String
        get() {
            val acceptKeyName = KeyMappingService.acceptKey.translatedKeyMessage.string

            return "[$acceptKeyName] Accept"
        }
}