package net.dungeonhub.promptoverlay.feature

import java.util.ArrayDeque
import net.dungeonhub.promptoverlay.api.render.Overlay
import net.dungeonhub.promptoverlay.enums.RemoveType
import kotlin.time.Clock
import kotlin.time.Instant

internal data class PromptEntry(
    val id: Long,
    val overlay: Overlay,
    val enqueuedAt: Instant,
)

internal class PromptQueueManager(
    private val onShow: (PromptEntry) -> Unit,
    private val onExit: (PromptEntry, RemoveType) -> Unit,
    private val onExitComplete: (PromptEntry) -> Unit = {},
    private val isExpired: (PromptEntry) -> Boolean = { false },
) {
    private var nextId = 1L
    private var currentPrompt: PromptEntry? = null
    private val pendingEntries = ArrayDeque<PromptEntry>()
    private var outgoingPrompt: PromptEntry? = null

    @Synchronized
    fun enqueue(overlay: Overlay) {
        val entry = PromptEntry(nextId++, overlay, Clock.System.now())
        if (currentPrompt == null && outgoingPrompt == null) setCurrentPrompt(entry) else pendingEntries.addLast(entry)
    }

    @Synchronized
    fun removePrompt(id: Long, type: RemoveType): Boolean {
        val entry = currentPrompt ?: return false
        if (entry.id != id) return false
        currentPrompt = null
        outgoingPrompt = entry
        onExit(entry, type)
        return true
    }

    @Synchronized
    fun completeExit(id: Long) {
        val completed = outgoingPrompt?.takeIf { it.id == id } ?: return
        outgoingPrompt = null
        onExitComplete(completed)
        while (pendingEntries.isNotEmpty()) {
            val next = pendingEntries.removeFirst()
            if (!isExpired(next)) {
                setCurrentPrompt(next)
                break
            }
        }
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
}
