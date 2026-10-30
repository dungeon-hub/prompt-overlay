package net.dungeonhub.promptoverlay.api

/**
 * Receives the [PromptOutcome] of a submitted prompt.
 * It is called exactly once, on the Minecraft client thread.
 */
fun interface PromptListener {
    fun onResolved(outcome: PromptOutcome)
}
