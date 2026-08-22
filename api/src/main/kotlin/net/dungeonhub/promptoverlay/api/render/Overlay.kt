package net.dungeonhub.promptoverlay.api.render

import net.dungeonhub.promptoverlay.PromptOverlayApi
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import java.awt.Color
import kotlin.time.Duration

interface Overlay {
    val borderColor: Color
    val message: Component
    val description: Component
        get() = Component.empty()

    /**
     * Maximum time this overlay may remain visible before it is dismissed
     * automatically. A value of `null` applies no prompt-specific limit and uses
     * the Prompt Overlay user's configured duration.
     */
    val maxDisplayDuration: Duration?
        get() = null

    fun getActionsHeight(width: Int): Int // TODO remove the width parameter
    fun getActionsWidth(font: Font): Int
    fun renderActions(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int)

    fun dismiss() {  }

    fun dismissText(): String {
        return "[${PromptOverlayApi.getKeyMappingProvider().dismissKeyName}] Dismiss"
    }
}