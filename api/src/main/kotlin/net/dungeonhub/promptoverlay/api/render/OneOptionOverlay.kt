package net.dungeonhub.promptoverlay.api.render

import net.dungeonhub.promptoverlay.PromptOverlayApi

interface OneOptionOverlay {
    /**
     * @return The key currently set for selecting the first option.
     */
    fun firstOptionKey(): String = PromptOverlayApi.getKeyMappingProvider().firstOptionKeyName

    fun firstOption()

    fun dismissText(): String {
        return "[${PromptOverlayApi.getKeyMappingProvider().dismissKeyName}] Dismiss"
    }
}