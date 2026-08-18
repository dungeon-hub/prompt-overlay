package net.dungeonhub.promptoverlay.api.render

import net.dungeonhub.promptoverlay.PromptOverlayApi
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import java.awt.Color

interface Overlay {
    val borderColor: Color
    val message: Component
    val description: Component
        get() = Component.empty()

    fun getActionsHeight(width: Int): Int // TODO remove the width parameter
    fun getActionsWidth(font: Font): Int
    fun renderActions(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int)

    fun dismiss() {  }

    fun dismissText(): String {
        return "[${PromptOverlayApi.getKeyMappingProvider().dismissKeyName}] Dismiss"
    }
}