package net.dungeonhub.promptoverlay.api

enum class PromptSubmissionStatus { SUBMITTED, REJECTED, UNAVAILABLE }
enum class PromptRejectionReason { INVALID_SOURCE, INVALID_EXPIRATION, INVALID_DISPLAY_DURATION, QUEUE_FULL, HANDLER_NOT_READY }

/** Stable Java-friendly result; consumers must tolerate future enum constants. */
class PromptSubmissionResult private constructor(
    val status: PromptSubmissionStatus,
    val handle: PromptHandle?,
    val reason: PromptRejectionReason?,
) {
    companion object {
        @JvmStatic fun submitted(handle: PromptHandle) = PromptSubmissionResult(PromptSubmissionStatus.SUBMITTED, handle, null)
        @JvmStatic fun rejected(reason: PromptRejectionReason) = PromptSubmissionResult(PromptSubmissionStatus.REJECTED, null, reason)
        @JvmStatic fun unavailable() = PromptSubmissionResult(PromptSubmissionStatus.UNAVAILABLE, null, null)
    }
}
