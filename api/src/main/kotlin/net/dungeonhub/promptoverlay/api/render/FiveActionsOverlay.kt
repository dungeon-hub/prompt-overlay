package net.dungeonhub.promptoverlay.api.render

import net.dungeonhub.promptoverlay.PromptOverlayApi
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import kotlin.math.max

interface FiveActionsOverlay : FourActionsOverlay {
    val fifthText: String

    /**
     * @return The key currently set for selecting the fifth option.
     */
    fun fifthOptionKey(): String = PromptOverlayApi.getKeyMappingProvider().fifthOptionKeyName

    fun fifthOption()

    override fun getActionsHeight(width: Int): Int {
        val font = Minecraft.getInstance().font
        return font.lineHeight * 3 + 12 // Three lines with spacing
    }

    override fun getActionsWidth(font: Font): Int {
        val dismissKeyName = PromptOverlayApi.getKeyMappingProvider().dismissKeyName
        val dismissText = "[$dismissKeyName] Dismiss"

        val firstLineWidth = font.width(firstText) + font.width(secondText) + 20
        val secondLineWidth = font.width(thirdText) + font.width(fourthText) + 20
        val thirdLineWidth = font.width(fifthText) + font.width(dismissText) + 20

        return max(max(firstLineWidth, secondLineWidth), thirdLineWidth)
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
        val secondLineWidth = font.width(thirdText) + font.width(fourthText) + 20 // 20px spacing
        val secondLineX = x + (width - secondLineWidth) / 2

        graphics.text(font, thirdText, secondLineX, secondLineY, textColor)
        graphics.text(font, fourthText, secondLineX + font.width(thirdText) + 20, secondLineY, textColor)

        // Third line: dismiss centered below
        val thirdLineY = y + (font.lineHeight + 4) * 2
        val thirdLineWidth = font.width(fifthText) + font.width(dismissText) + 20
        val thirdLineX = x + (width - thirdLineWidth) / 2

        graphics.text(font, fifthText, thirdLineX, thirdLineY, textColor)
        graphics.text(font, dismissText, thirdLineX + font.width(fifthText) + 20, thirdLineY, textColor)
    }
}