package net.dungeonhub.promptoverlay.api

enum class PromptLifecycleState { QUEUED, VISIBLE, ANIMATING_OUT, RESOLVED }

enum class PromptOutcome {
    ACCEPTED, DENIED, DISMISSED_BY_USER, AUTO_DISMISSED,
    EXPIRED_WHILE_QUEUED, EXPIRED_WHILE_VISIBLE, CANCELED_BY_SOURCE,
    CLEARED_ON_DISCONNECT, ACTION_FAILED,
}

fun interface PromptLifecycleListener {
    fun onResolved(handle: PromptHandle, outcome: PromptOutcome)

    fun onShown(handle: PromptHandle) {}
}
