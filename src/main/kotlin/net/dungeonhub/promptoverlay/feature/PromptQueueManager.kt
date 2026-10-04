package net.dungeonhub.promptoverlay.feature

import java.util.ArrayDeque
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant
import net.dungeonhub.promptoverlay.api.PromptHandle
import net.dungeonhub.promptoverlay.api.PromptLifecycleListener
import net.dungeonhub.promptoverlay.api.PromptLifecycleState
import net.dungeonhub.promptoverlay.api.PromptOutcome
import net.dungeonhub.promptoverlay.api.PromptSource
import net.dungeonhub.promptoverlay.api.PromptUpdate
import net.dungeonhub.promptoverlay.api.render.Overlay
import net.dungeonhub.promptoverlay.enums.RemoveType
import org.slf4j.LoggerFactory

internal data class PromptEntry(
    val id: UUID,
    var overlay: Overlay,
    val enqueuedAt: Instant,
    val source: PromptSource,
    val ownerToken: Any,
    val listener: PromptLifecycleListener?,
    var expiresAtEpochMillis: Long?,
    var displayDurationMillis: Long?,
    var visibleAtEpochMillis: Long? = null,
    var outcome: PromptOutcome? = null,
    var handle: PromptHandle? = null,
)

internal class PromptQueueManager(
    private val onShow: (PromptEntry) -> Unit,
    private val onExit: (PromptEntry, RemoveType) -> Unit,
    private val onUpdate: (PromptEntry) -> Unit = {},
    private val onExitComplete: (PromptEntry) -> Unit = {},
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var currentPrompt: PromptEntry? = null
    private val pendingEntries = ArrayDeque<PromptEntry>()
    private var outgoingPrompt: PromptEntry? = null

    /** Compatibility path used by internal/legacy callers. */
    @Synchronized fun enqueue(overlay: Overlay) {
        val entry = PromptEntry(UUID.randomUUID(), overlay, Clock.System.now(), PromptSource("legacy-api"), Any(), null, null, null)
        enqueue(entry)
    }

    @Synchronized fun enqueue(entry: PromptEntry) {
        if (currentPrompt == null && outgoingPrompt == null) show(entry) else pendingEntries.addLast(entry)
    }

    @Synchronized fun removePrompt(id: UUID, type: RemoveType, outcome: PromptOutcome = outcome(type)): Boolean {
        val entry = currentPrompt ?: return false
        if (entry.id != id || entry.outcome != null) return false
        entry.outcome = outcome
        (entry.handle as? LifecyclePromptHandle)?.setState(PromptLifecycleState.ANIMATING_OUT)
        currentPrompt = null
        outgoingPrompt = entry
        onExit(entry, type)
        return true
    }

    @Synchronized fun cancel(id: UUID, token: Any): Boolean {
        val visible = currentPrompt
        if (visible?.id == id && visible.ownerToken === token) {
            return removePrompt(id, RemoveType.Dismiss, PromptOutcome.CANCELED_BY_SOURCE)
        }
        val queued = pendingEntries.firstOrNull { it.id == id && it.ownerToken === token } ?: return false
        pendingEntries.remove(queued)
        queued.outcome = PromptOutcome.CANCELED_BY_SOURCE
        notifyResolved(queued)
        return true
    }

    @Synchronized fun update(id: UUID, token: Any, update: PromptUpdate, now: Long = System.currentTimeMillis()): Boolean {
        val entry = sequenceOf(currentPrompt, *pendingEntries.toTypedArray()).filterNotNull()
            .firstOrNull { it.id == id && it.ownerToken === token && it.outcome == null } ?: return false
        update.overlay?.let { entry.overlay = it }
        if (update.expirationChanged) entry.expiresAtEpochMillis = update.expiresAtEpochMillis
        if (update.displayDurationChanged) entry.displayDurationMillis = update.displayDurationMillis
        if (update.restartDisplayTimer && entry === currentPrompt) entry.visibleAtEpochMillis = now
        val expiration = entry.expiresAtEpochMillis
        if (expiration != null && expiration <= now) {
            if (entry === currentPrompt) removePrompt(id, RemoveType.Dismiss, PromptOutcome.EXPIRED_WHILE_VISIBLE)
            else {
                pendingEntries.remove(entry)
                entry.outcome = PromptOutcome.EXPIRED_WHILE_QUEUED
                notifyResolved(entry)
            }
        } else if (entry === currentPrompt) onUpdate(entry)
        return true
    }

    @Synchronized fun expire(now: Long = System.currentTimeMillis()) {
        pendingEntries.toList().filter { it.expiresAtEpochMillis?.let { deadline -> deadline <= now } == true }.forEach {
            pendingEntries.remove(it); it.outcome = PromptOutcome.EXPIRED_WHILE_QUEUED; notifyResolved(it)
        }
        currentPrompt?.takeIf { it.expiresAtEpochMillis?.let { deadline -> deadline <= now } == true }
            ?.let { removePrompt(it.id, RemoveType.Dismiss, PromptOutcome.EXPIRED_WHILE_VISIBLE) }
    }

    @Synchronized fun markActionFailed(id: UUID) { outgoingPrompt?.takeIf { it.id == id }?.outcome = PromptOutcome.ACTION_FAILED }

    @Synchronized fun completeExit(id: UUID) {
        val completed = outgoingPrompt?.takeIf { it.id == id } ?: return
        outgoingPrompt = null
        onExitComplete(completed)
        notifyResolved(completed)
        while (pendingEntries.isNotEmpty() && currentPrompt == null) {
            val next = pendingEntries.removeFirst()
            if (next.expiresAtEpochMillis?.let { it <= System.currentTimeMillis() } == true) {
                next.outcome = PromptOutcome.EXPIRED_WHILE_QUEUED; notifyResolved(next)
            } else show(next)
        }
    }

    @Synchronized fun currentPrompt(): PromptEntry? = currentPrompt
    @Synchronized fun outgoingPrompt(): PromptEntry? = outgoingPrompt
    @Synchronized fun waitingCount(): Int = pendingEntries.size

    private fun show(entry: PromptEntry) {
        currentPrompt = entry
        entry.visibleAtEpochMillis = System.currentTimeMillis()
        (entry.handle as? LifecyclePromptHandle)?.setState(PromptLifecycleState.VISIBLE)
        safe(entry) { entry.listener?.onShown(entry.handle!!) }
        onShow(entry)
    }

    private fun notifyResolved(entry: PromptEntry) {
        (entry.handle as? LifecyclePromptHandle)?.setState(PromptLifecycleState.RESOLVED)
        safe(entry) { entry.listener?.onResolved(entry.handle!!, entry.outcome!!) }
    }

    private fun safe(entry: PromptEntry, callback: () -> Unit) = try { callback() } catch (error: Throwable) {
        logger.error("Prompt lifecycle callback failed: id={}, source={}, state={}", entry.id, entry.source.namespace, entry.handle?.state(), error)
    }

    companion object { private fun outcome(type: RemoveType) = when (type) {
        RemoveType.Accept -> PromptOutcome.ACCEPTED; RemoveType.Deny -> PromptOutcome.DENIED; RemoveType.Dismiss -> PromptOutcome.DISMISSED_BY_USER
    } }
}

internal class LifecyclePromptHandle(
    override val id: UUID,
    override val source: PromptSource,
    private val token: Any,
    private val dispatch: ((() -> Unit) -> Unit),
    private val queue: PromptQueueManager,
) : PromptHandle {
    @Volatile private var lifecycleState = PromptLifecycleState.QUEUED
    override fun state() = lifecycleState
    internal fun setState(value: PromptLifecycleState) { lifecycleState = value }
    override fun update(update: PromptUpdate): Boolean = accept { queue.update(id, token, update) }
    override fun cancel(): Boolean = accept { queue.cancel(id, token) }
    private fun accept(operation: () -> Unit): Boolean {
        if (lifecycleState == PromptLifecycleState.RESOLVED || lifecycleState == PromptLifecycleState.ANIMATING_OUT) return false
        dispatch(operation); return true
    }
}
