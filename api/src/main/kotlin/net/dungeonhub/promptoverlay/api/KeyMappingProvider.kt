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
}
