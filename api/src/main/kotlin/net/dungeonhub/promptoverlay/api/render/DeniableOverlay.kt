package net.dungeonhub.promptoverlay.api.render

import net.dungeonhub.promptoverlay.PromptOverlayApi

interface DeniableOverlay : Overlay {
    /**
     * @return The key currently set for denying this prompt.
     */
    fun denyKey(): String = PromptOverlayApi.getKeyMappingProvider().denyKeyName

    fun deny()
}