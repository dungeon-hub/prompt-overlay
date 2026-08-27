package net.dungeonhub.promptoverlay.api.render

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor

/**
 * An informational overlay with no custom actions. The only available action is dismissing it.
 */
interface ZeroActionsOverlay : Overlay {
    override fun getActionsHeight(width: Int) = ActionLayout.height(1)
    override fun getActionsWidth(font: Font) = ActionLayout.width(font, listOf(dismissText()))
    override fun renderActions(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int) =
        ActionLayout.render(graphics, x, y, width, listOf(dismissText()))
}
