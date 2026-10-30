package net.dungeonhub.promptoverlay.api.render

import net.dungeonhub.promptoverlay.PromptOverlayApi

interface OneOptionOverlay {
    /**
     * @return The key currently set for selecting the first option.
     */
    fun firstOptionKey(): String = try {
        PromptOverlayApi.getKeyMappingProvider().firstOptionKeyName
    } catch (_: IllegalStateException) {
        ""
    }

    fun firstOption()
}