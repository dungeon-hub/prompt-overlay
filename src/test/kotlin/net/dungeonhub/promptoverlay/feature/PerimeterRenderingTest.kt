package net.dungeonhub.promptoverlay.feature

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class PerimeterRenderingTest {
    @AfterTest
    fun clearCache() = RoundedPerimeterCache.clear()

    @Test
    fun `rounded paths are reused by geometry`() {
        val first = RoundedPerimeterCache.get(120, 40, 6)
        val second = RoundedPerimeterCache.get(120, 40, 6)

        assertSame(first, second)
        assertNotSame(first, RoundedPerimeterCache.get(121, 40, 6))
    }

    @Test
    fun `rounded path cache is bounded`() {
        repeat(100) { RoundedPerimeterCache.get(100 + it, 40, 6) }

        assertEquals(64, RoundedPerimeterCache.size())
    }

    @Test
    fun `cache retains recently used geometry when evicting`() {
        val recentlyUsed = RoundedPerimeterCache.get(100, 40, 6)
        val leastRecentlyUsed = RoundedPerimeterCache.get(101, 40, 6)
        repeat(62) { RoundedPerimeterCache.get(102 + it, 40, 6) }

        assertSame(recentlyUsed, RoundedPerimeterCache.get(100, 40, 6))
        RoundedPerimeterCache.get(200, 40, 6)

        assertSame(recentlyUsed, RoundedPerimeterCache.get(100, 40, 6))
        assertNotSame(leastRecentlyUsed, RoundedPerimeterCache.get(101, 40, 6))
    }

    @Test
    fun `different radius produces different cached geometry`() {
        assertNotSame(
            RoundedPerimeterCache.get(120, 40, 5),
            RoundedPerimeterCache.get(120, 40, 6)
        )
    }

    @Test
    fun `adjacent same-color pixels become one run in either direction`() {
        val fills = mutableListOf<SameColorRun.Fill>()
        val runs = SameColorRun { l, t, r, b, c -> fills += SameColorRun.Fill(l, t, r, b, c) }
        // Thickness-two path pixels overlap by one pixel as they advance.
        runs.add(2, 1, 4, 3, 7)
        runs.add(3, 1, 5, 3, 7)
        runs.add(4, 1, 6, 3, 7)
        runs.add(8, 5, 10, 6, 9)
        runs.add(8, 6, 10, 7, 9)
        runs.flush()

        assertEquals(
            listOf(SameColorRun.Fill(2, 1, 6, 3, 7), SameColorRun.Fill(8, 5, 10, 7, 9)),
            fills
        )
    }

    @Test
    fun `different colors and noncontiguous pixels preserve order`() {
        val fills = mutableListOf<SameColorRun.Fill>()
        val runs = SameColorRun { l, t, r, b, c -> fills += SameColorRun.Fill(l, t, r, b, c) }
        val expected = listOf(
            SameColorRun.Fill(0, 0, 1, 1, 1),
            SameColorRun.Fill(1, 0, 2, 1, 2),
            SameColorRun.Fill(4, 0, 5, 1, 2)
        )
        expected.forEach { runs.add(it.left, it.top, it.right, it.bottom, it.color) }
        runs.flush()

        assertEquals(expected, fills)
    }

    @Test
    fun `reverse-direction runs merge without changing their union`() {
        val fills = mutableListOf<SameColorRun.Fill>()
        SameColorRun({ l, t, r, b, c -> fills += SameColorRun.Fill(l, t, r, b, c) }) {
            add(4, 2, 6, 4, 3)
            add(3, 2, 5, 4, 3)
            add(2, 2, 4, 4, 3)
            add(8, 6, 10, 8, 4)
            add(8, 5, 10, 7, 4)
        }

        assertEquals(
            listOf(SameColorRun.Fill(2, 2, 6, 4, 3), SameColorRun.Fill(8, 5, 10, 8, 4)),
            fills
        )
    }

    @Test
    fun `different cross-axis spans are not merged`() {
        val fills = mutableListOf<SameColorRun.Fill>()
        SameColorRun({ l, t, r, b, c -> fills += SameColorRun.Fill(l, t, r, b, c) }) {
            add(0, 0, 2, 2, 5)
            add(2, 0, 4, 3, 5)
            add(4, 1, 6, 3, 5)
        }

        assertEquals(3, fills.size)
    }

    @Test
    fun `scoped DSL automatically flushes its final run`() {
        val fills = mutableListOf<SameColorRun.Fill>()
        SameColorRun({ l, t, r, b, c -> fills += SameColorRun.Fill(l, t, r, b, c) }) {
            add(1, 1, 2, 2, 9)
        }

        assertEquals(listOf(SameColorRun.Fill(1, 1, 2, 2, 9)), fills)
    }

    @Test
    fun `scoped DSL flushes before propagating an exception`() {
        val fills = mutableListOf<SameColorRun.Fill>()

        assertFailsWith<IllegalStateException> {
            SameColorRun({ l, t, r, b, c -> fills += SameColorRun.Fill(l, t, r, b, c) }) {
                add(1, 1, 2, 2, 9)
                error("render interrupted")
            }
        }

        assertEquals(listOf(SameColorRun.Fill(1, 1, 2, 2, 9)), fills)
    }

    @Test
    fun `flushing an empty run is harmless`() {
        val fills = mutableListOf<SameColorRun.Fill>()
        val runs = SameColorRun { l, t, r, b, c -> fills += SameColorRun.Fill(l, t, r, b, c) }

        runs.flush()
        runs.flush()

        assertEquals(emptyList(), fills)
    }
}
