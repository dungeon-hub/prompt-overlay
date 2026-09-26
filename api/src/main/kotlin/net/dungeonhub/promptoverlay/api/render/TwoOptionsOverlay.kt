package net.dungeonhub.promptoverlay.api.render

import net.dungeonhub.promptoverlay.PromptOverlayApi

interface TwoOptionsOverlay : OneOptionOverlay {
    /**
     * @return The key currently set for selecting the second option.
     */
    fun secondOptionKey(): String = try {
        PromptOverlayApi.getKeyMappingProvider().secondOptionKeyName
    } catch (_: IllegalStateException) {
        ""
    }

    fun secondOption()
}