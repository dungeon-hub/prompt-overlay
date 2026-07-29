package net.dungeonhub.promptoverlay.api

/**
 * Provider interface for key mapping names used in overlays.
 * Implement this interface and register it via [net.dungeonhub.promptoverlay.PromptOverlayApi.registerKeyMappingProvider]
 * during your mod's initialization.
 */
interface KeyMappingProvider {
    /**
     * Returns the translated/localized name of the accept key.
     * Used in overlay action text like "[Y] Accept".
     */
    val acceptKeyName: String

    /**
     * Returns the translated/localized name of the deny key.
     * Used in overlay action text like "[N] Deny".
     */
    val denyKeyName: String

    /**
     * Returns the translated/localized name of the dismiss key.
     * Used in overlay action text like "[X] Dismiss".
     */
    val dismissKeyName: String

    /**
     * Returns the translated/localized name of the first option key.
     */
    val firstOptionKeyName: String

    /**
     * Returns the translated/localized name of the second option key.
     */
    val secondOptionKeyName: String

    /**
     * Returns the translated/localized name of the third option key.
     */
    val thirdOptionKeyName: String

    /**
     * Returns the translated/localized name of the fourth option key.
     */
    val fourthOptionKeyName: String

    /**
     * Returns the translated/localized name of the fifth option key.
     */
    val fifthOptionKeyName: String
}
