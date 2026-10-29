package net.dungeonhub.promptoverlay.overlays

import net.dungeonhub.promptoverlay.api.render.AcceptableOverlay
import net.dungeonhub.promptoverlay.api.render.OneActionOverlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import java.awt.Color

class TreeGiftMilestoneOverlay(type: Component, val claimCommand: String) : AcceptableOverlay, OneActionOverlay {
    override val borderColor: Color get() = Color(OverlayCategory.treeGiftMilestoneColor)
    override val message = Component.literal("Tree Gift Milestone")
    override val description = type

    override val firstText: String
        get() {
            return "[${acceptKey()}] Claim"
        }

    override fun accept() {
        val command = if(claimCommand.startsWith("/")) claimCommand.substring(1) else claimCommand

        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.sendCommand(command)
        }
    }
}