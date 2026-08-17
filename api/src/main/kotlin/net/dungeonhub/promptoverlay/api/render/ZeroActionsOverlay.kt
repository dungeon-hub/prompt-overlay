package net.dungeonhub.promptoverlay.api.render

import net.dungeonhub.promptoverlay.PromptOverlayApi
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor

/**
 * An informational overlay with no custom actions. The only available action is dismissing it.
 */
interface ZeroActionsOverlay : Overlay {
    override fun getActionsHeight(width: Int): Int {
        return Minecraft.getInstance().font.lineHeight + 4
    }

    override fun getActionsWidth(font: Font): Int {
        return font.width(dismissText())
    }

    override fun renderActions(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int) {
        val font = Minecraft.getInstance().font
        val text = dismissText()
        graphics.text(font, text, x + (width - font.width(text)) / 2, y, 0xFFFFFFFF.toInt())
    }

    private fun dismissText(): String {
        return "[${PromptOverlayApi.getKeyMappingProvider().dismissKeyName}] Dismiss"
    }
}