package net.dungeonhub.promptoverlay.feature

import net.dungeonhub.promptoverlay.enums.ChatRegex
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.network.chat.Component
import java.util.LinkedList

object ChatHandler {
    private const val MAX_HISTORY_SIZE = 100
    private val messageHistory = LinkedList<Component>()
    private val pendingWaits = mutableListOf<PendingWait>()

    private class PendingWait(val count: Int, val action: (List<Component>) -> Unit) {
        val messages = mutableListOf<Component>()
    }

    fun init() {
        ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { message, overlay ->
            if (!overlay) {
                handle(message)
            }
        })
    }

    fun handle(message: Component) {
        addToHistory(message)
        processPendingWaits(message)

        for(regex in ChatRegex.entries.filter { it.enabled() }) {
            val result = regex.regex.find(message.string) ?: continue

            regex.action(message, result)
        }
    }

    /**
     * Adds a message to the history buffer, maintaining the maximum size.
     */
    private fun addToHistory(message: Component) {
        messageHistory.addFirst(message)
        if (messageHistory.size > MAX_HISTORY_SIZE) {
            messageHistory.removeLast()
        }
    }

    /**
     * Waits for the next N chat messages and then runs the given action.
     * The message currently being handled (if called from within a chat action) is not counted.
     *
     * @param count The number of messages to wait for
     * @param action The code to run, receiving the collected messages (oldest first)
     */
    fun waitForMessages(count: Int, action: (messages: List<Component>) -> Unit) {
        if (count <= 0) {
            action(emptyList())
            return
        }

        pendingWaits.add(PendingWait(count, action))
    }

    /**
     * Feeds a message to all pending waits and runs the actions of those that are complete.
     */
    private fun processPendingWaits(message: Component) {
        if (pendingWaits.isEmpty()) return

        val completed = pendingWaits.filter { wait ->
            wait.messages.add(message)
            wait.messages.size >= wait.count
        }
        pendingWaits.removeAll(completed)

        completed.forEach { it.action(it.messages) }
    }

    /**
     * Retrieves the last N messages from chat history, excluding the current message.
     *
     * @param count The number of messages to retrieve
     * @return A list of messages, with the most recent first (if "skipCurrent" is true, not including the current message)
     */
    fun getLastMessages(count: Int, skipCurrent: Boolean = true): List<Component> {
        return if(skipCurrent) {
            // Skip the first message (current) and return the next N
            messageHistory.drop(1).take(count.coerceAtLeast(0))
        } else {
            messageHistory.take(count.coerceAtLeast(0))
        }
    }

    /**
     * Searches through the last N messages for one that matches the given predicate.
     *
     * @param count The number of messages to search through
     * @param predicate A function to test each message's string content
     * @return The first matching message component, or null if not found
     */
    fun findInHistory(count: Int, skipCurrent: Boolean = true, predicate: (String) -> Boolean): Component? {
        return getLastMessages(count, skipCurrent).firstOrNull { predicate(it.string) }
    }
}