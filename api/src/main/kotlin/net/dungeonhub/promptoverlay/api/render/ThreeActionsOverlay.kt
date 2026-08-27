package net.dungeonhub.promptoverlay.api.render

import net.dungeonhub.promptoverlay.PromptOverlayApi
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor

interface ThreeActionsOverlay : Overlay, TwoOptionsOverlay {
    val firstText: String
    val secondText: String
    val thirdText: String

    /**
     * @return The key currently set for selecting the third option.
     */
    fun thirdOptionKey(): String = PromptOverlayApi.getKeyMappingProvider().thirdOptionKeyName

    fun thirdOption()

    override fun getActionsHeight(width: Int) = ActionLayout.height(2)
    override fun getActionsWidth(font: Font) = ActionLayout.width(font, texts())
    override fun renderActions(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int) =
        ActionLayout.render(graphics, x, y, width, texts())

    private fun texts() = listOf(firstText, secondText, thirdText, dismissText())
}
