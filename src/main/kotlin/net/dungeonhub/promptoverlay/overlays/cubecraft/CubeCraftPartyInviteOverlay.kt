package net.dungeonhub.promptoverlay.overlays.cubecraft

import net.dungeonhub.promptoverlay.api.render.DeniableOverlay
import net.dungeonhub.promptoverlay.api.render.TwoActionsOverlay
import net.dungeonhub.promptoverlay.overlays.PartyInviteOverlay
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor

class CubeCraftPartyInviteOverlay(val userText: String) : PartyInviteOverlay(userText.removeSuffix(" $-$")), TwoActionsOverlay, DeniableOverlay {
    override fun accept() {
        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.sendCommand("party accept $userText")
        }
    }

    override fun deny() {
        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.sendCommand("party deny $userText")
        }
    }

    override val secondText = "[${acceptKey()}] Deny"

    override fun getActionsHeight(width: Int) = super<TwoActionsOverlay>.getActionsHeight(width)
    override fun getActionsWidth(font: Font) = super<TwoActionsOverlay>.getActionsWidth(font)
    override fun renderActions(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int) = super<TwoActionsOverlay>.renderActions(graphics, x, y, width)
}