package net.dungeonhub.promptoverlay.api

/**
 * A reference to a prompt submitted through [net.dungeonhub.promptoverlay.PromptOverlayApi.submit].
 */
interface PromptHandle {
    /** `true` while the prompt is queued or visible, `false` once it is being removed or has been resolved. */
    val isActive: Boolean

    /**
     * Removes the prompt, for example because the underlying request was withdrawn.
     * The listener receives [PromptOutcome.CANCELED]; no overlay callback such as `dismiss` is invoked.
     *
     * @return `true` if the prompt was still active and is now being removed
     */
    fun cancel(): Boolean
}
