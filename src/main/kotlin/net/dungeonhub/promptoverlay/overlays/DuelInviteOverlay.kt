package net.dungeonhub.promptoverlay.overlays

import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.dungeonhub.promptoverlay.render.AcceptableOverlay
import net.dungeonhub.promptoverlay.render.OneActionOverlay
import net.dungeonhub.promptoverlay.service.KeyMappingService
import net.minecraft.client.Minecraft
import java.awt.Color

class DuelInviteOverlay(val player: String, duel: String): AcceptableOverlay, OneActionOverlay {
    override fun accept() {
        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.sendCommand("duel accept $player")
        }
    }

    override val borderColor: Color get() = Color(OverlayCategory.duelColor)
    override val message: String = "$player invited you to $duel"

    override val firstText: String
        get() {
            val acceptKeyName = KeyMappingService.acceptKey.translatedKeyMessage.string

            return "[$acceptKeyName] Accept"
        }
}