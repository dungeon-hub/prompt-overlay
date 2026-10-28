package net.dungeonhub.promptoverlay.overlays

import net.dungeonhub.promptoverlay.api.render.AcceptableOverlay
import net.dungeonhub.promptoverlay.api.render.OneActionOverlay
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import java.awt.Color
import kotlin.time.Duration

class WarpReminderOverlay(
    private val eventName: String,
    private val warp: String,
    private val color: () -> Int,
    val duration: Duration
) : AcceptableOverlay, OneActionOverlay {
    override fun accept() {
        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.sendCommand("warp $warp")
        }
    }

    override val borderColor get() = Color(color())
    override val message get() = Component.literal("The $eventName starts in $duration")
    override val firstText get() = "[${acceptKey()}] Warp"
}
