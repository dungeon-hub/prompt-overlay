package net.dungeonhub.promptoverlay.feature

import java.util.ArrayDeque
import net.dungeonhub.promptoverlay.api.render.Overlay
import net.dungeonhub.promptoverlay.enums.RemoveType

internal data class PromptEntry(val id: Long, val overlay: Overlay)

internal class PromptQueueManager(
    private val onShow: (PromptEntry) -> Unit,
    private val onExit: (PromptEntry, RemoveType) -> Unit,
    private val onExitComplete: (PromptEntry) -> Unit = {},
) {
    private var nextId = 1L
    var currentPrompt: PromptEntry? = null
        private set
    private val pendingEntries = ArrayDeque<PromptEntry>()
    var outgoingPrompt: PromptEntry? = null
        private set

    fun enqueue(overlay: Overlay) {
        val entry = PromptEntry(nextId++, overlay)
        if (currentPrompt == null && outgoingPrompt == null) setCurrentPrompt(entry) else pendingEntries.addLast(entry)
    }

    fun removePrompt(id: Long, type: RemoveType): Boolean {
        val entry = currentPrompt ?: return false
        if (entry.id != id) return false
        currentPrompt = null
        outgoingPrompt = entry
        onExit(entry, type)
        return true
    }

    fun completeExit(id: Long) {
        if (outgoingPrompt?.id != id) return
        val completed = outgoingPrompt ?: return
        outgoingPrompt = null
        onExitComplete(completed)
        if (pendingEntries.isNotEmpty()) setCurrentPrompt(pendingEntries.removeFirst())
    }

    fun waitingCount(): Int = pendingEntries.size

    private fun setCurrentPrompt(entry: PromptEntry) {
        check(currentPrompt == null && outgoingPrompt == null)
        currentPrompt = entry
        onShow(entry)
    }
}
