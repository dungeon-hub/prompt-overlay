package net.dungeonhub.promptoverlay.overlays

import net.dungeonhub.promptoverlay.api.render.AcceptableOverlay
import net.dungeonhub.promptoverlay.api.render.OneActionOverlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import java.awt.Color
import kotlin.time.Duration

class StarfallMeetingReminderOverlay(val duration: Duration) : AcceptableOverlay, OneActionOverlay {
    override val borderColor: Color = Color(OverlayCategory.starfallMeetingReminderColor)
    override val message get() = Component.literal("The Starfall Meeting starts in $duration")
    override val firstText: String get() = "[${acceptKey()}] Warp"

    override fun accept() {
        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.sendCommand("warp mines")
        }
    }
}
