package net.dungeonhub.promptoverlay.api

/**
 * Describes the outcome of passing a prompt to Prompt Overlay.
 */
sealed interface SetOverlayResult {
    /** The prompt was accepted by the overlay handler and queued for presentation. */
    data object Queued : SetOverlayResult

    /** Prompt Overlay is not installed, so the calling mod should use its fallback. */
    data object ModNotInstalled : SetOverlayResult

    /** Prompt Overlay was found, but the prompt could not be queued. */
    data class Error(val throwable: Throwable) : SetOverlayResult
}