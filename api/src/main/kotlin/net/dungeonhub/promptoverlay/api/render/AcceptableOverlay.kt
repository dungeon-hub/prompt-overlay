package net.dungeonhub.promptoverlay.api.render

import net.dungeonhub.promptoverlay.PromptOverlayApi

interface AcceptableOverlay : Overlay {
    /**
     * @return The key currently set for accepting this prompt.
     */
    fun acceptKey(): String = PromptOverlayApi.getKeyMappingProvider().acceptKeyName

    fun accept()
}