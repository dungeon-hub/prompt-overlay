package net.dungeonhub.promptoverlay.api.render

import net.dungeonhub.promptoverlay.PromptOverlayApi

interface TwoOptionsOverlay : OneOptionOverlay {
    /**
     * @return The key currently set for selecting the second option.
     */
    fun secondOptionKey(): String = PromptOverlayApi.getKeyMappingProvider().secondOptionKeyName

    fun secondOption()
}