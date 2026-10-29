package net.dungeonhub.promptoverlay.feature

import net.minecraft.SharedConstants
import net.minecraft.network.chat.Component
import net.minecraft.server.Bootstrap
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChatHandlerTest {
    @BeforeTest
    fun bootstrap() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
    }

    private fun send(vararg texts: String) {
        texts.forEach { ChatHandler.handle(Component.literal(it)) }
    }

    @Test
    fun `action runs only after the given number of messages`() {
        var received: List<String>? = null
        ChatHandler.waitForMessages(3) { messages -> received = messages.map { it.string } }

        send("wait test 1", "wait test 2")
        assertNull(received)

        send("wait test 3")
        assertEquals(listOf("wait test 1", "wait test 2", "wait test 3"), received)
    }

    @Test
    fun `action runs only once`() {
        var calls = 0
        ChatHandler.waitForMessages(1) { calls++ }

        send("wait test 1", "wait test 2", "wait test 3")
        assertEquals(1, calls)
    }

    @Test
    fun `non-positive count runs the action immediately with no messages`() {
        var received: List<Component>? = null
        ChatHandler.waitForMessages(0) { received = it }

        assertEquals(emptyList(), received)
    }

    @Test
    fun `multiple waits are tracked independently`() {
        val completed = mutableListOf<Int>()
        ChatHandler.waitForMessages(2) { completed.add(2) }
        ChatHandler.waitForMessages(1) { completed.add(1) }

        send("wait test 1")
        assertEquals(listOf(1), completed)

        send("wait test 2")
        assertEquals(listOf(1, 2), completed)
    }

    @Test
    fun `wait registered during a message does not count that message`() {
        var received: List<String>? = null
        ChatHandler.waitForMessages(1) {
            ChatHandler.waitForMessages(1) { messages -> received = messages.map { it.string } }
        }

        send("wait test 1")
        assertNull(received)

        send("wait test 2")
        assertEquals(listOf("wait test 2"), received)
    }
}
