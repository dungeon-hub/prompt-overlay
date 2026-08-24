package net.dungeonhub.promptoverlay.feature

import java.awt.Color
import java.lang.reflect.Field
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import net.dungeonhub.promptoverlay.api.render.Overlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import kotlin.time.Instant

class OverlayFeatureTest {
    private val minecraftInstanceField: Field = Minecraft::class.java.getDeclaredField("instance").apply {
        trySetAccessible()
    }
    private var previousMinecraft: Minecraft? = null
    private lateinit var font: Font

    @BeforeTest
    fun installMinecraftWithMockFont() {
        previousMinecraft = minecraftInstanceField.get(null) as Minecraft?
        font = mock(Font::class.java)
        setInstanceField(font, "lineHeight", 9)

        val minecraft = mock(Minecraft::class.java)
        setInstanceField(minecraft, "font", font)
        minecraftInstanceField.set(null, minecraft)
    }

    @AfterTest
    fun restoreMinecraft() {
        minecraftInstanceField.set(null, previousMinecraft)
    }

    @Test
    fun `box width uses message width plus padding and margins`() {
        val overlay = TestOverlay(actionsWidth = 40)
        `when`(font.width(overlay.message)).thenReturn(200)

        assertEquals(232, OverlayFeature.calculateBoxWidth(overlay))
        assertSame(font, overlay.widthFont)
    }

    @Test
    fun `box width uses action width when actions are wider than message`() {
        val overlay = TestOverlay(actionsWidth = 250)
        `when`(font.width(overlay.message)).thenReturn(100)

        assertEquals(282, OverlayFeature.calculateBoxWidth(overlay))
    }

    @Test
    fun `box width uses description width when description is wider than message`() {
        val overlay = TestOverlay(actionsWidth = 40, descriptionText = "A wider description")
        `when`(font.width(overlay.message)).thenReturn(100)
        `when`(font.width(overlay.description)).thenReturn(240)

        assertEquals(272, OverlayFeature.calculateBoxWidth(overlay))
    }

    @Test
    fun `box width uses message width when description is narrower`() {
        val overlay = TestOverlay(actionsWidth = 40, descriptionText = "Short description")
        `when`(font.width(overlay.message)).thenReturn(210)
        `when`(font.width(overlay.description)).thenReturn(80)

        assertEquals(242, OverlayFeature.calculateBoxWidth(overlay))
    }

    @Test
    fun `box width is clamped to its minimum and maximum`() {
        val narrowOverlay = TestOverlay(actionsWidth = 0, messageText = "narrowtext")
        `when`(font.width(narrowOverlay.message)).thenReturn(0)
        val wideOverlay = TestOverlay(actionsWidth = 500)
        `when`(font.width(wideOverlay.message)).thenReturn(500)

        assertEquals(150, OverlayFeature.calculateBoxWidth(narrowOverlay))
        assertEquals(400, OverlayFeature.calculateBoxWidth(wideOverlay))
    }

    @Test
    fun `box width preserves values exactly at clamp boundaries`() {
        val minimumOverlay = TestOverlay(actionsWidth = 118, messageText = "minimum")
        val maximumOverlay = TestOverlay(actionsWidth = 368, messageText = "maximum")

        assertEquals(150, OverlayFeature.calculateBoxWidth(minimumOverlay))
        assertEquals(400, OverlayFeature.calculateBoxWidth(maximumOverlay))
    }

    @Test
    fun `title height without description includes only line height and vertical padding`() {
        val overlay = TestOverlay(actionsWidth = 0)

        assertEquals(21, OverlayFeature.calculateTitleHeight(overlay, 150))
        verify(font, never()).split(overlay.description, 138)
    }

    @Test
    fun `blank description is treated as absent`() {
        val overlay = TestOverlay(actionsWidth = 0, descriptionText = "   ")

        assertEquals(emptyList(), OverlayFeature.getDescriptionLines(overlay, 200))
        assertEquals(21, OverlayFeature.calculateTitleHeight(overlay, 200))
        verify(font, never()).split(overlay.description, 188)
    }

    @Test
    fun `description is split using box width minus horizontal padding`() {
        val overlay = TestOverlay(actionsWidth = 0, descriptionText = "Description")
        val line = mock(FormattedCharSequence::class.java)
        `when`(font.split(overlay.description, 188)).thenReturn(listOf(line))

        assertEquals(listOf(line), OverlayFeature.getDescriptionLines(overlay, 200))
        verify(font).split(overlay.description, 188)
    }

    @Test
    fun `single line description adds spacing and one line height`() {
        val overlay = TestOverlay(actionsWidth = 0, descriptionText = "Description")
        val line = mock(FormattedCharSequence::class.java)
        `when`(font.split(overlay.description, 188)).thenReturn(listOf(line))

        assertEquals(33, OverlayFeature.calculateTitleHeight(overlay, 200))
    }

    @Test
    fun `wrapped description adds one line height per line`() {
        val overlay = TestOverlay(actionsWidth = 0, descriptionText = "A wrapped description")
        val lines = List(3) { mock(FormattedCharSequence::class.java) }
        `when`(font.split(overlay.description, 188)).thenReturn(lines)

        assertEquals(51, OverlayFeature.calculateTitleHeight(overlay, 200))
    }

    @Test
    fun `total height combines message actions and bottom padding`() {
        val overlay = TestOverlay(actionsWidth = 180, actionsHeight = 37)
        `when`(font.width(overlay.message)).thenReturn(100)

        assertEquals(64, OverlayFeature.calculateTotalHeight(overlay))
        assertEquals(212, overlay.heightWidth)
    }

    @Test
    fun `total height includes wrapped description lines`() {
        val overlay = TestOverlay(actionsWidth = 168, actionsHeight = 37, descriptionText = "Wrapped description")
        val lines = List(2) { mock(FormattedCharSequence::class.java) }
        `when`(font.split(overlay.description, 188)).thenReturn(lines)

        assertEquals(85, OverlayFeature.calculateTotalHeight(overlay))
        assertEquals(200, overlay.heightWidth)
    }

    @Test
    fun `null overlay duration uses configured duration`() {
        assertEquals(configuredDuration, OverlayFeature.effectiveDisplayDuration(TestOverlay(0)))
    }

    @Test
    fun `non-positive overlay durations use configured duration`() {
        listOf(Duration.ZERO, (-1).seconds).forEach { duration ->
            assertEquals(
                configuredDuration,
                OverlayFeature.effectiveDisplayDuration(TestOverlay(0, maxDisplayDuration = duration)),
            )
        }
    }

    @Test
    fun `infinite overlay duration uses configured duration`() {
        assertEquals(
            configuredDuration,
            OverlayFeature.effectiveDisplayDuration(TestOverlay(0, maxDisplayDuration = Duration.INFINITE)),
        )
    }

    @Test
    fun `overlay duration shorter than configured duration is preserved`() {
        val shorterDuration = configuredDuration - 1.seconds

        assertEquals(
            shorterDuration,
            OverlayFeature.effectiveDisplayDuration(TestOverlay(0, maxDisplayDuration = shorterDuration)),
        )
    }

    @Test
    fun `overlay duration longer than configured duration is capped`() {
        val overlay = TestOverlay(0, maxDisplayDuration = configuredDuration + 1.seconds)

        assertEquals(configuredDuration, OverlayFeature.effectiveDisplayDuration(overlay))
    }

    @Test
    fun `auto-dismiss scheduling uses effective display duration`() {
        val overlay = TestOverlay(0, maxDisplayDuration = configuredDuration - 1.seconds)

        assertEquals(OverlayFeature.effectiveDisplayDuration(overlay), OverlayFeature.autoDismissDelay(overlay))
    }

    @Test
    fun `queued time counts toward display duration`() {
        val overlay = TestOverlay(0, maxDisplayDuration = 5.seconds)
        val entry = PromptEntry(1, overlay, enqueuedAt = Instant.fromEpochMilliseconds(1000))

        assertEquals(3.seconds, OverlayFeature.remainingDisplayDuration(entry, currentTime = Instant.fromEpochMilliseconds(3_000)))
        assertEquals(Duration.ZERO, OverlayFeature.remainingDisplayDuration(entry, currentTime = Instant.fromEpochMilliseconds(7_000)))
    }

    @Test
    fun `progress calculation uses effective display duration`() {
        val overlay = TestOverlay(0, maxDisplayDuration = configuredDuration - 2.seconds)
        val effectiveDuration = OverlayFeature.effectiveDisplayDuration(overlay)

        assertEquals(
            0.5,
            OverlayFeature.dismissProgress(effectiveDuration / 2, overlay),
        )
        assertEquals(1.0, OverlayFeature.dismissProgress(effectiveDuration, overlay))
    }

    @Test
    fun `badge count is capped at its maximum display value`() {
        assertEquals("99", OverlayFeature.badgeText(99))
        assertEquals("99+", OverlayFeature.badgeText(100))
    }

    /**
     * Minecraft exposes [Minecraft.font] and [Font.lineHeight] as final fields.
     * They cannot be stubbed through Mockito because production code reads them
     * directly with GETFIELD, so initialize the fields on the mock instances and
     * verify the write rather than silently relying on a Mockito property stub.
     */
    private fun setInstanceField(target: Any, name: String, value: Any) {
        val declaringClass = when (target) {
            is Minecraft -> Minecraft::class.java
            is Font -> Font::class.java
            else -> error("Unsupported field owner: ${target.javaClass.name}")
        }
        declaringClass.getDeclaredField(name).apply {
            check(trySetAccessible()) { "Cannot access ${declaringClass.name}.$name" }
            set(target, value)
            check(get(target) === value || get(target) == value) {
                "Failed to initialize final field ${declaringClass.name}.$name"
            }
        }
    }

    private class TestOverlay(
        private val actionsWidth: Int,
        private val actionsHeight: Int = 0,
        messageText: String = "A title that can be measured",
        descriptionText: String = "",
        override val maxDisplayDuration: Duration? = null,
    ) : Overlay {
        override val borderColor: Color = Color.WHITE
        override val message: Component = Component.literal(messageText)
        override val description: Component = Component.literal(descriptionText)
        var widthFont: Font? = null
            private set
        var heightWidth: Int? = null
            private set

        override fun getActionsHeight(width: Int): Int {
            heightWidth = width
            return actionsHeight
        }

        override fun getActionsWidth(font: Font): Int {
            widthFont = font
            return actionsWidth
        }

        override fun renderActions(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int) = Unit
    }

    private val configuredDuration
        get() = OverlayCategory.overlayDisplayDuration.seconds
}
