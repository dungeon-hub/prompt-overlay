package net.dungeonhub.promptoverlay.api.render

import java.lang.reflect.Field
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`

class ActionLayoutTest {
    private val minecraftInstanceField: Field = Minecraft::class.java.getDeclaredField("instance").apply {
        trySetAccessible()
    }
    private var previousMinecraft: Minecraft? = null
    private lateinit var font: Font
    private lateinit var graphics: GuiGraphicsExtractor

    @BeforeTest
    fun installMinecraftWithMockFont() {
        previousMinecraft = minecraftInstanceField.get(null) as Minecraft?
        font = mock(Font::class.java)
        graphics = mock(GuiGraphicsExtractor::class.java)
        setInstanceField(font, "lineHeight", 9)

        val minecraft = mock(Minecraft::class.java)
        setInstanceField(minecraft, "font", font)
        minecraftInstanceField.set(null, minecraft)
    }

    @AfterTest
    fun restoreMinecraft() {
        minecraftInstanceField.set(null, previousMinecraft)
    }

    @Test fun `zero lines have zero height`() = assertEquals(0, ActionLayout.height(0))
    @Test fun `one line includes spacing`() = assertEquals(13, ActionLayout.height(1))
    @Test fun `two lines include spacing`() = assertEquals(26, ActionLayout.height(2))
    @Test fun `three lines include spacing`() = assertEquals(39, ActionLayout.height(3))

    @Test
    fun `height reads the current minecraft font`() {
        setInstanceField(font, "lineHeight", 12)
        assertEquals(32, ActionLayout.height(2))
    }

    @Test fun `single text width has no spacing`() = assertWidth(10, listOf("dismiss"), 10)
    @Test fun `two text width includes spacing`() = assertWidth(40, listOf("one", "dismiss"), 10, 10)
    @Test fun `widest row determines width`() = assertWidth(110, listOf("wide", "small", "dismiss"), 40, 50, 70)
    @Test fun `later row can determine width`() = assertWidth(120, listOf("one", "two", "three", "dismiss"), 10, 10, 50, 50)
    @Test fun `unequal widths are added correctly`() = assertWidth(55, listOf("first", "second"), 15, 20)

    @Test
    fun `single text is centered`() {
        stubWidths("dismiss" to 10)
        ActionLayout.render(graphics, 10, 5, 100, listOf("dismiss"))
        verify(graphics).text(font, "dismiss", 55, 5, -1)
    }

    @Test
    fun `two texts are centered with spacing`() {
        stubWidths("one" to 10, "two" to 10)
        ActionLayout.render(graphics, 0, 0, 100, listOf("one", "two"))
        verify(graphics).text(font, "one", 30, 0, -1)
        verify(graphics).text(font, "two", 60, 0, -1)
    }

    @Test
    fun `second row uses line height and spacing`() {
        stubWidths("one" to 10, "two" to 10, "dismiss" to 10)
        ActionLayout.render(graphics, 0, 7, 100, listOf("one", "two", "dismiss"))
        verify(graphics).text(font, "dismiss", 45, 20, -1)
    }

    @Test
    fun `rows are centered independently`() {
        stubWidths("one" to 10, "two" to 10, "dismiss" to 30)
        ActionLayout.render(graphics, 0, 0, 100, listOf("one", "two", "dismiss"))
        verify(graphics).text(font, "one", 30, 0, -1)
        verify(graphics).text(font, "dismiss", 35, 13, -1)
    }

    @Test
    fun `x origin offsets rendered text`() {
        stubWidths("dismiss" to 10)
        ActionLayout.render(graphics, 25, 0, 100, listOf("dismiss"))
        verify(graphics).text(font, "dismiss", 70, 0, -1)
    }

    @Test
    fun `y origin offsets rendered text`() {
        stubWidths("dismiss" to 10)
        ActionLayout.render(graphics, 0, 42, 100, listOf("dismiss"))
        verify(graphics).text(font, "dismiss", 45, 42, -1)
    }

    @Test
    fun `unequal text widths preserve spacing`() {
        stubWidths("short" to 10, "long" to 30)
        ActionLayout.render(graphics, 0, 0, 100, listOf("short", "long"))
        verify(graphics).text(font, "short", 20, 0, -1)
        verify(graphics).text(font, "long", 50, 0, -1)
    }

    @Test
    fun `four texts fill two rows`() {
        stubWidths("one" to 10, "two" to 10, "three" to 10, "dismiss" to 10)
        ActionLayout.render(graphics, 0, 0, 100, listOf("one", "two", "three", "dismiss"))
        verify(graphics).text(font, "three", 30, 13, -1)
        verify(graphics).text(font, "dismiss", 60, 13, -1)
    }

    @Test
    fun `six texts fill three rows`() {
        val actions = List(6) { "action$it" }
        stubWidths(*actions.map { it to 10 }.toTypedArray())
        ActionLayout.render(graphics, 0, 0, 100, actions)
        verify(graphics).text(font, "action4", 30, 26, -1)
        verify(graphics).text(font, "action5", 60, 26, -1)
    }

    @Test
    fun `empty action list renders nothing`() {
        ActionLayout.render(graphics, 0, 0, 100, emptyList())
        verifyNoInteractions(graphics)
    }

    private fun assertWidth(expected: Int, actions: List<String>, vararg widths: Int) {
        stubWidths(*actions.zip(widths.toList()).toTypedArray())
        assertEquals(expected, ActionLayout.width(font, actions))
    }

    private fun stubWidths(vararg widths: Pair<String, Int>) {
        clearInvocations(font)
        widths.forEach { (text, width) -> `when`(font.width(text)).thenReturn(width) }
    }

    private fun setInstanceField(target: Any, name: String, value: Any) {
        val declaringClass = when (target) {
            is Minecraft -> Minecraft::class.java
            is Font -> Font::class.java
            else -> error("Unsupported field owner: ${target.javaClass.name}")
        }
        declaringClass.getDeclaredField(name).apply {
            check(trySetAccessible()) { "Cannot access ${declaringClass.name}.$name" }
            set(target, value)
            check(get(target) === value || get(target) == value)
        }
    }
}
