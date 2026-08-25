package net.dungeonhub.promptoverlay.feature

import java.awt.Color
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import net.dungeonhub.promptoverlay.api.render.Overlay
import net.dungeonhub.promptoverlay.enums.RemoveType
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component

class PromptQueueManagerTest {
    @Test
    fun `entries remain FIFO and promote only after exit completes`() {
        val fixture = Fixture()
        val a = TestOverlay("A")
        val b = TestOverlay("B")
        val c = TestOverlay("C")

        fixture.manager.enqueue(a); fixture.manager.enqueue(b); fixture.manager.enqueue(c)
        assertSame(a, fixture.manager.currentPrompt()?.overlay)
        assertEquals(2, fixture.manager.waitingCount())

        val aId = fixture.manager.currentPrompt()!!.id
        fixture.manager.removePrompt(aId, RemoveType.Accept)
        assertNull(fixture.manager.currentPrompt())
        assertSame(a, fixture.manager.outgoingPrompt()?.overlay)

        fixture.manager.completeExit(aId)
        assertSame(b, fixture.manager.currentPrompt()?.overlay)
        assertEquals(1, fixture.manager.waitingCount())
    }

    @Test
    fun `enqueue during exit waits behind the outgoing entry`() {
        val fixture = Fixture()
        fixture.manager.enqueue(TestOverlay("A"))
        val id = fixture.manager.currentPrompt()!!.id
        fixture.manager.removePrompt(id, RemoveType.Dismiss)
        val b = TestOverlay("B")
        fixture.manager.enqueue(b)

        assertNull(fixture.manager.currentPrompt())
        fixture.manager.completeExit(id)
        assertSame(b, fixture.manager.currentPrompt()?.overlay)
    }

    @Test
    fun `stale and duplicate callbacks cannot resolve the promoted entry`() {
        val fixture = Fixture()
        fixture.manager.enqueue(TestOverlay("A")); fixture.manager.enqueue(TestOverlay("B"))
        val aId = fixture.manager.currentPrompt()!!.id
        fixture.manager.removePrompt(aId, RemoveType.Dismiss)
        fixture.manager.removePrompt(aId, RemoveType.Dismiss)
        assertEquals(1, fixture.exits.size)
        fixture.manager.completeExit(aId)
        val b = fixture.manager.currentPrompt()!!

        fixture.manager.removePrompt(aId, RemoveType.Dismiss)
        assertEquals(b, fixture.manager.currentPrompt())
    }

    @Test
    fun `action claim prevents recursive resolution while callback enqueues`() {
        val fixture = Fixture()
        fixture.manager.enqueue(TestOverlay("A"))
        val a = fixture.manager.currentPrompt()!!
        assertTrue(fixture.manager.removePrompt(a.id, RemoveType.Accept))
        fixture.manager.enqueue(TestOverlay("C"))
        assertFalse(fixture.manager.removePrompt(a.id, RemoveType.Accept))
        assertEquals(1, fixture.manager.waitingCount())
    }

    @Test
    fun `concurrently enqueueing and completing an exit does not throw`() {
        val enqueueStarted = CountDownLatch(1)
        val failure = AtomicReference<Throwable?>()
        lateinit var enqueueThread: Thread
        lateinit var manager: PromptQueueManager
        manager = PromptQueueManager({}, { _, _ -> }) {
            enqueueThread = Thread {
                enqueueStarted.countDown()
                try {
                    manager.enqueue(TestOverlay("C"))
                } catch (throwable: Throwable) {
                    failure.set(throwable)
                }
            }
            enqueueThread.start()
            enqueueStarted.await()
        }
        manager.enqueue(TestOverlay("A"))
        val b = TestOverlay("B")
        manager.enqueue(b)
        val aId = manager.currentPrompt()!!.id
        manager.removePrompt(aId, RemoveType.Dismiss)

        try {
            manager.completeExit(aId)
        } catch (throwable: Throwable) {
            failure.set(throwable)
        }
        enqueueThread.join()

        assertSame(b, manager.currentPrompt()?.overlay)
        assertEquals(1, manager.waitingCount())
        assertNull(failure.get())
    }

    private class Fixture {
        val shown = mutableListOf<PromptEntry>()
        val exits = mutableListOf<PromptEntry>()
        val manager = PromptQueueManager(shown::add, { entry, _ -> exits.add(entry) })
    }

    private class TestOverlay(name: String) : Overlay {
        override val borderColor: Color = Color.WHITE
        override val message: Component = Component.literal(name)
        override fun getActionsHeight(width: Int) = 0
        override fun getActionsWidth(font: Font) = 0
        override fun renderActions(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int) = Unit
    }
}
