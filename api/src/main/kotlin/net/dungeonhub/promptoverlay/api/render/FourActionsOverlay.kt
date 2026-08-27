package net.dungeonhub.promptoverlay.api.render

import net.dungeonhub.promptoverlay.PromptOverlayApi
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor

interface FourActionsOverlay : ThreeActionsOverlay {
    val fourthText: String

    /**
     * @return The key currently set for selecting the fourth option.
     */
    fun fourthOptionKey(): String = PromptOverlayApi.getKeyMappingProvider().fourthOptionKeyName

    fun fourthOption()

    override fun getActionsHeight(width: Int) = ActionLayout.height(3)
    override fun getActionsWidth(font: Font) = ActionLayout.width(font, texts())
    override fun renderActions(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int) =
        ActionLayout.render(graphics, x, y, width, texts())

    private fun texts() = listOf(firstText, secondText, thirdText, fourthText, dismissText())
}
