package net.dungeonhub.promptoverlay.api.render

import net.dungeonhub.promptoverlay.PromptOverlayApi
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import kotlin.math.max

interface ThreeActionsOverlay : Overlay, TwoOptionsOverlay {
    val firstText: String
    val secondText: String
    val thirdText: String

    /**
     * @return The key currently set for selecting the third option.
     */
    fun thirdOptionKey(): String = PromptOverlayApi.getKeyMappingProvider().thirdOptionKeyName

    fun thirdOption()

    override fun getActionsHeight(width: Int): Int {
        val font = Minecraft.getInstance().font
        return font.lineHeight * 2 + 8 // Two lines with spacing
    }

    override fun getActionsWidth(font: Font): Int {
        val dismissKeyName = PromptOverlayApi.getKeyMappingProvider().dismissKeyName
        val dismissText = "[$dismissKeyName] Dismiss"

        val firstLineWidth = font.width(firstText) + font.width(secondText) + 20
        val secondLineWidth = font.width(thirdText) + font.width(dismissText) + 20

        return max(firstLineWidth, secondLineWidth)
    }

    override fun renderActions(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int) {
        val font = Minecraft.getInstance().font

        val dismissKeyName = PromptOverlayApi.getKeyMappingProvider().dismissKeyName
        val dismissText = "[$dismissKeyName] Dismiss"

        val textColor = 0xFFFFFFFF.toInt()

        // First line: two actions side-by-side
        val firstLineWidth = font.width(firstText) + font.width(secondText) + 20 // 20px spacing
        val firstLineStartX = x + (width - firstLineWidth) / 2

        graphics.text(font, firstText, firstLineStartX, y, textColor)
        graphics.text(font, secondText, firstLineStartX + font.width(firstText) + 20, y, textColor)

        val secondLineY = y + font.lineHeight + 4 // 4px spacing between lines
        val secondLineWidth = font.width(thirdText) + font.width(dismissText) + 20 // 20px spacing
        val secondLineX = x + (width - secondLineWidth) / 2

        graphics.text(font, thirdText, secondLineX, secondLineY, textColor)
        graphics.text(font, dismissText, secondLineX + font.width(thirdText) + 20, secondLineY, textColor)
    }
}