package net.dungeonhub.promptoverlay.api

/**
 * Describes how a submitted prompt ended.
 */
enum class PromptOutcome {
    /** The user accepted the prompt or selected one of its options. */
    ACCEPTED,

    /** The user denied the prompt. */
    DENIED,

    /** The user dismissed the prompt. */
    DISMISSED,

    /** The prompt's display duration ran out, either while it was visible or while it was queued. */
    TIMED_OUT,

    /** The prompt was canceled through its [PromptHandle]. */
    CANCELED,

    /** The overlay callback for the chosen action threw an exception. */
    ACTION_FAILED,
}
