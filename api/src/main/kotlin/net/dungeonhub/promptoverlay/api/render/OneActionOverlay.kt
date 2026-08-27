package net.dungeonhub.promptoverlay.api.render

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor

interface OneActionOverlay : Overlay {
    val firstText: String

    override fun getActionsHeight(width: Int) = ActionLayout.height(1)
    override fun getActionsWidth(font: Font) = ActionLayout.width(font, texts())
    override fun renderActions(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int) =
        ActionLayout.render(graphics, x, y, width, texts())

    private fun texts() = listOf(firstText, dismissText())
}
