package net.dungeonhub.promptoverlay.feature

import java.util.LinkedHashMap
import kotlin.math.cos
import kotlin.math.sin

internal data class PerimeterPoint(val x: Int, val y: Int)

/** Caches position-independent paths so moving overlays can reuse the same geometry. */
internal object RoundedPerimeterCache {
    private const val MAX_ENTRIES = 64
    private data class Geometry(val width: Int, val height: Int, val radius: Int)

    private val paths = object : LinkedHashMap<Geometry, List<PerimeterPoint>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Geometry, List<PerimeterPoint>>): Boolean =
            size > MAX_ENTRIES
    }

    @Synchronized
    fun get(width: Int, height: Int, radius: Int): List<PerimeterPoint> =
        paths.getOrPut(Geometry(width, height, radius)) { create(width, height, radius) }

    @Synchronized
    internal fun clear() = paths.clear()

    @Synchronized
    internal fun size(): Int = paths.size

    private fun create(width: Int, height: Int, radius: Int): List<PerimeterPoint> = buildList {
        fun arc(centerX: Int, centerY: Int, startDegrees: Int) {
            for (degree in startDegrees until startDegrees + 90) {
                val angle = Math.toRadians(degree.toDouble())
                add(PerimeterPoint((centerX + cos(angle) * radius).toInt(), (centerY + sin(angle) * radius).toInt()))
            }
        }
        for (x in radius until width - radius) add(PerimeterPoint(x, 0))
        arc(width - radius - 1, radius, 270)
        for (y in radius until height - radius) add(PerimeterPoint(width - 1, y))
        arc(width - radius - 1, height - radius - 1, 0)
        for (x in width - radius - 1 downTo radius) add(PerimeterPoint(x, height - 1))
        arc(radius, height - radius - 1, 90)
        for (y in height - radius - 1 downTo radius) add(PerimeterPoint(0, y))
        arc(radius, radius, 180)
    }.distinct()
}

/** Combines only consecutive, same-color rectangles whose union is one rectangle. */
internal class SameColorRun(private val fill: (Int, Int, Int, Int, Int) -> Unit) {
    private var pending: Fill? = null

    internal data class Fill(val left: Int, val top: Int, val right: Int, val bottom: Int, val color: Int)

    companion object {
        /**
         * Scoped form which guarantees that the final pending run is emitted,
         * including when [block] exits by throwing.
         */
        operator fun invoke(
            fill: (Int, Int, Int, Int, Int) -> Unit,
            block: SameColorRun.() -> Unit
        ) {
            val runs = SameColorRun(fill)
            try {
                runs.block()
            } finally {
                runs.flush()
            }
        }
    }

    fun add(left: Int, top: Int, right: Int, bottom: Int, color: Int) {
        val next = Fill(left, top, right, bottom, color)
        val current = pending
        pending = when {
            current == null -> next
            canMergeHorizontally(current, next) -> current.copy(
                left = minOf(current.left, next.left), right = maxOf(current.right, next.right)
            )
            canMergeVertically(current, next) -> current.copy(
                top = minOf(current.top, next.top), bottom = maxOf(current.bottom, next.bottom)
            )
            else -> {
                emit(current)
                next
            }
        }
    }

    fun flush() {
        pending?.let(::emit)
        pending = null
    }

    private fun canMergeHorizontally(a: Fill, b: Fill): Boolean =
        a.color == b.color && a.top == b.top && a.bottom == b.bottom &&
            maxOf(a.left, b.left) <= minOf(a.right, b.right)

    private fun canMergeVertically(a: Fill, b: Fill): Boolean =
        a.color == b.color && a.left == b.left && a.right == b.right &&
            maxOf(a.top, b.top) <= minOf(a.bottom, b.bottom)

    private fun emit(value: Fill) = fill(value.left, value.top, value.right, value.bottom, value.color)
}
