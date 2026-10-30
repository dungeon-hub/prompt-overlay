package net.dungeonhub.promptoverlay.overlays

import net.dungeonhub.promptoverlay.api.render.AcceptableOverlay
import net.dungeonhub.promptoverlay.api.render.OneActionOverlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import java.awt.Color

class SafariMilestoneReminderOverlay : AcceptableOverlay, OneActionOverlay {
    override val borderColor: Color get() = Color(OverlayCategory.safariMilestoneReminderColor)
    override val message = Component.literal("Safari Milestones")
    override val description = Component.literal("You have unclaimed Safari Milestones.")

    override val firstText: String
        get() {
            return "[${acceptKey()}] Claim"
        }

    override fun accept() {
        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.sendCommand("viewsafarimilestones")
        }
    }
}