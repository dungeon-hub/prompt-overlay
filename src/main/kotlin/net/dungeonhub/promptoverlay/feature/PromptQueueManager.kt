package net.dungeonhub.promptoverlay.feature

import java.util.ArrayDeque
import net.dungeonhub.promptoverlay.api.PromptListener
import net.dungeonhub.promptoverlay.api.PromptOutcome
import net.dungeonhub.promptoverlay.api.render.Overlay
import net.dungeonhub.promptoverlay.enums.RemoveType
import kotlin.time.Clock
import kotlin.time.Instant

internal data class PromptEntry(
    val id: Long,
    val overlay: Overlay,
    val enqueuedAt: Instant,
    val listener: PromptListener? = null,
) {
    /** Set when the entry leaves the queue or starts animating out; reported once the exit completes. */
    var outcome: PromptOutcome? = null
}

/**
 * Manages prompt transitions and invokes lifecycle callbacks synchronously while holding its instance lock.
 * Callbacks invoked by [setCurrentPrompt], [removePrompt], or [completeExit] must not call back into this
 * manager on the calling thread. [onResolved] is the exception: it is only invoked once the queue is in a
 * consistent state again, so it may enqueue new prompts.
 */
internal class PromptQueueManager(
    private val onShow: (PromptEntry) -> Unit,
    private val onExit: (PromptEntry, RemoveType) -> Unit,
    private val onExitComplete: (PromptEntry) -> Unit = {},
    private val isExpired: (PromptEntry) -> Boolean = { false },
    private val onResolved: (PromptEntry, PromptOutcome) -> Unit = { _, _ -> },
) {
    private var nextId = 1L
    private var currentPrompt: PromptEntry? = null
    private val pendingEntries = ArrayDeque<PromptEntry>()
    private var outgoingPrompt: PromptEntry? = null

    @Synchronized
    fun enqueue(overlay: Overlay, listener: PromptListener? = null): PromptEntry {
        val entry = PromptEntry(nextId++, overlay, Clock.System.now(), listener)
        if (currentPrompt == null && outgoingPrompt == null) setCurrentPrompt(entry) else pendingEntries.addLast(entry)
        return entry
    }

    @Synchronized
    fun removePrompt(id: Long, type: RemoveType, outcome: PromptOutcome = type.toOutcome()): Boolean {
        val entry = currentPrompt ?: return false
        if (entry.id != id) return false
        entry.outcome = outcome
        currentPrompt = null
        outgoingPrompt = entry
        onExit(entry, type)
        return true
    }

    /**
     * Cancels a visible or queued prompt. A visible prompt animates out like a dismissal,
     * a queued prompt is removed immediately.
     */
    @Synchronized
    fun cancel(id: Long): Boolean {
        if (currentPrompt?.id == id) return removePrompt(id, RemoveType.Dismiss, PromptOutcome.CANCELED)

        val entry = pendingEntries.firstOrNull { it.id == id } ?: return false
        pendingEntries.remove(entry)
        resolve(entry, PromptOutcome.CANCELED)
        return true
    }

    /** Overrides the outcome of a prompt whose action callback threw after it started animating out. */
    @Synchronized
    fun markActionFailed(id: Long) {
        outgoingPrompt?.takeIf { it.id == id }?.outcome = PromptOutcome.ACTION_FAILED
    }

    @Synchronized
    fun isActive(id: Long): Boolean = currentPrompt?.id == id || pendingEntries.any { it.id == id }

    @Synchronized
    fun completeExit(id: Long) {
        val completed = outgoingPrompt?.takeIf { it.id == id } ?: return
        outgoingPrompt = null
        onExitComplete(completed)
        val expired = mutableListOf<PromptEntry>()
        while (pendingEntries.isNotEmpty()) {
            val next = pendingEntries.removeFirst()
            if (!isExpired(next)) {
                setCurrentPrompt(next)
                break
            }
            expired.add(next)
        }

        // Resolve only after the next prompt is shown, so listeners that enqueue a new prompt cannot jump the queue.
        resolve(completed, completed.outcome ?: PromptOutcome.DISMISSED)
        expired.forEach { resolve(it, PromptOutcome.TIMED_OUT) }
    }

    @Synchronized
    fun currentPrompt(): PromptEntry? = currentPrompt

    @Synchronized
    fun outgoingPrompt(): PromptEntry? = outgoingPrompt

    @Synchronized
    fun waitingCount(): Int = pendingEntries.size

    private fun setCurrentPrompt(entry: PromptEntry) {
        check(currentPrompt == null && outgoingPrompt == null)
        currentPrompt = entry
        onShow(entry)
    }

    private fun resolve(entry: PromptEntry, outcome: PromptOutcome) {
        entry.outcome = outcome
        onResolved(entry, outcome)
    }

    private fun RemoveType.toOutcome() = when (this) {
        RemoveType.Accept -> PromptOutcome.ACCEPTED
        RemoveType.Deny -> PromptOutcome.DENIED
        RemoveType.Dismiss -> PromptOutcome.DISMISSED
    }
}
