package net.dungeonhub.promptoverlay.feature

import java.awt.Color
import java.time.LocalDate
import java.time.Month
import kotlin.math.sin
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import net.dungeonhub.promptoverlay.PromptOverlay.MOD_ID
import net.dungeonhub.promptoverlay.api.render.Overlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.dungeonhub.promptoverlay.enums.GlowStyle
import net.dungeonhub.promptoverlay.enums.PromptStyle
import net.dungeonhub.promptoverlay.enums.RemoveType
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier
import net.minecraft.util.FormattedCharSequence

object OverlayRenderer {
    internal var animationStartTime: Instant = Instant.DISTANT_PAST
        private set
    internal var autoDismissStartTime: Instant = Instant.DISTANT_PAST
        private set
    internal var isAnimatingIn: Boolean = false
        private set
    internal var isAnimatingOut: Boolean = false
        private set
    internal var animationOutType: RemoveType? = null
        private set

    internal val ANIMATION_DURATION = 500.milliseconds

    private const val PADDING = 6
    private const val MIN_WIDTH = 150
    private const val MAX_WIDTH = 400
    private const val MAX_BADGE_COUNT = 99
    private const val BADGE_OUTLINE_COLOR = 0xB0000000.toInt()
    private const val BADGE_BACKGROUND_COLOR = 0xFFE53935.toInt()
    private const val BADGE_TEXT_SHADOW_COLOR = 0xC0000000.toInt()
    private const val BADGE_TEXT_COLOR = 0xFFFFFFFF.toInt()

    private val customBackground =
        Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/prompt-background.png")

    internal fun startAnimatingIn(enqueuedAt: Instant, currentTime: Instant = Clock.System.now()) {
        animationStartTime = currentTime
        autoDismissStartTime = enqueuedAt
        isAnimatingIn = true
        isAnimatingOut = false
        animationOutType = null
    }

    internal fun startAnimatingOut(type: RemoveType, currentTime: Instant = Clock.System.now()) {
        animationStartTime = currentTime
        isAnimatingIn = false
        isAnimatingOut = true
        animationOutType = type
    }

    internal fun completeAnimatingOut() {
        isAnimatingOut = false
        animationOutType = null
    }

    internal fun completeAnimatingIn() {
        isAnimatingIn = false
    }

    fun calculateBoxWidth(overlay: Overlay): Int {
        val font = Minecraft.getInstance().font

        /// Calculate required width based on the message and optional description
        val messageWidth = font.width(overlay.message)
        val descriptionWidth = font.width(overlay.description)
        val requiredMessageWidth = maxOf(messageWidth, descriptionWidth) + PADDING * 2 + 20 // Extra space for padding and margins

        // Calculate required width based on actions
        val tempActionsWidth = overlay.getActionsWidth(font)
        val requiredActionsWidth = tempActionsWidth + PADDING * 2 + 20

        // Choose the larger of the two, clamped between min and max
        return maxOf(requiredMessageWidth, requiredActionsWidth).coerceIn(MIN_WIDTH, MAX_WIDTH)
    }

    fun getDescriptionLines(overlay: Overlay, boxWidth: Int): List<FormattedCharSequence> {
        val font = Minecraft.getInstance().font

        return overlay.description.takeIf { it.string.isNotBlank() }?.let {
            font.split(it, boxWidth - PADDING * 2)
        } ?: emptyList()
    }

    fun calculateTitleHeight(overlay: Overlay, boxWidth: Int, descriptionLines: List<FormattedCharSequence> = getDescriptionLines(overlay, boxWidth)): Int {
        val font = Minecraft.getInstance().font

        val descriptionHeight = if (descriptionLines.isEmpty()) 0 else PADDING / 2 + descriptionLines.size * font.lineHeight
        return font.lineHeight + descriptionHeight + PADDING * 2
    }

    fun calculateTotalHeight(overlay: Overlay, boxWidth: Int = calculateBoxWidth(overlay), descriptionLines: List<FormattedCharSequence> = getDescriptionLines(overlay, boxWidth)): Int {
        val messageHeight = calculateTitleHeight(overlay, boxWidth, descriptionLines)
        val actionsHeight = overlay.getActionsHeight(boxWidth) // Get height without rendering
        return messageHeight + actionsHeight + PADDING
    }

    fun renderPreview(graphics: GuiGraphicsExtractor, overlay: Overlay, x: Int, y: Int, dismissProgress: Double = 0.5) {
        render(
            graphics,
            overlay,
            x,
            y,
            dismissProgress,
            progressComplete = false
        )
    }

    fun render(
        graphics: GuiGraphicsExtractor,
        overlay: Overlay,
        x: Int,
        y: Int,
        dismissProgress: Double,
        progressComplete: Boolean,
        waitingCount: Int = 0,
    ) {
        val font = Minecraft.getInstance().font
        val boxWidth = calculateBoxWidth(overlay)
        val descriptionLines = getDescriptionLines(overlay, boxWidth)
        val messageHeight = calculateTitleHeight(overlay, boxWidth, descriptionLines)
        val totalHeight = calculateTotalHeight(overlay, boxWidth, descriptionLines)

        // Convert AWT Color to RGB int
        val borderColorRGB = overlay.borderColor.rgb and 0x00FFFFFF
        val borderColor = 0xFF000000.toInt() or borderColorRGB

        val style = OverlayCategory.style
        val wrapProgress = style.supportsWrappedProgress && OverlayCategory.wrapProgress

        val borderThickness = 2
        val cornerRadius = if (style == PromptStyle.Rounded) 8 else 0

        drawGlow(graphics, x, y, boxWidth, totalHeight, cornerRadius, borderColor)

        // Draw background
        drawBackground(graphics, x, y, boxWidth, totalHeight, cornerRadius)

        if (!wrapProgress) {
            when (style) {
                PromptStyle.Default, PromptStyle.Rounded -> drawBorders(graphics, x, y, boxWidth, totalHeight, cornerRadius, borderThickness, borderColor)
                PromptStyle.CornerAccents -> drawCornerAccents(graphics, x, y, boxWidth, totalHeight, borderThickness, borderColor)
                PromptStyle.DoubleBorder -> drawDoubleBorder(graphics, x, y, boxWidth, totalHeight, borderColor)
                PromptStyle.SideRails -> drawSideRails(graphics, x, y, boxWidth, totalHeight, borderColor)
            }
        }

        // Draw loading bar separator between message and actions
        val separatorY = y + messageHeight
        val separatorWidth = boxWidth - PADDING * 2

        val clampedDismissProgress = dismissProgress.coerceIn(0.0, 1.0)
        val pride = LocalDate.now().month == Month.JUNE || OverlayCategory.alwaysPrideMonth

        if (wrapProgress) {
            drawWrappedProgress(graphics, style, x, y, boxWidth, totalHeight, cornerRadius, borderThickness, clampedDismissProgress, borderColor, pride)
            // Wrapped progress replaces only the animated timer. Keep a static,
            // fully colored separator between the message and its actions.
            graphics.fill(x + PADDING, separatorY, x + boxWidth - PADDING, separatorY + borderThickness, borderColor)
        } else if (!progressComplete) {
            val filledWidth = (separatorWidth * clampedDismissProgress).toInt()

            // Draw filled portion
            if (filledWidth > 0) {
                if (pride) {
                    // Draw rainbow gradient
                    drawRainbowBar(graphics, x + PADDING, separatorY, filledWidth, 2)
                } else {
                    // Draw with border color
                    graphics.fill(x + PADDING, separatorY, x + PADDING + filledWidth, separatorY + 2, borderColor)
                }
            }

            // Draw empty portion with dim color
            if (filledWidth < separatorWidth) {
                graphics.fill(x + PADDING + filledWidth, separatorY, x + boxWidth - PADDING, separatorY + 2, 0x40FFFFFF)
            }
        } else {
            // When animating out, show full bar
            if (pride) {
                drawRainbowBar(graphics, x + PADDING, separatorY, separatorWidth, 2)
            } else {
                graphics.fill(x + PADDING, separatorY, x + boxWidth - PADDING, separatorY + 2, borderColor)
            }
        }

        // Render message (centered)
        val messageText = overlay.message
        val textWidth = font.width(messageText)
        val messageX = x + (boxWidth - textWidth) / 2
        val messageY = y + PADDING
        graphics.text(font, messageText, messageX, messageY, 0xFFFFFFFF.toInt())

        // Render the optional description below the message, wrapping it to the overlay width.
        descriptionLines.forEachIndexed { index, line ->
            val lineWidth = font.width(line)
            val lineX = x + (boxWidth - lineWidth) / 2
            val lineY = messageY + font.lineHeight + PADDING / 2 + index * font.lineHeight
            graphics.text(font, line, lineX, lineY, 0xFFBFBFBF.toInt())
        }

        // Render actions
        overlay.renderActions(graphics, x + PADDING, separatorY + PADDING, boxWidth - PADDING * 2)

        if (waitingCount > 0) drawQueueBadge(graphics, x, y, boxWidth, totalHeight, waitingCount)
    }

    internal fun badgeText(waitingCount: Int) =
        if (waitingCount > MAX_BADGE_COUNT) "$MAX_BADGE_COUNT+" else waitingCount.toString()

    private fun drawQueueBadge(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, waitingCount: Int) {
        val font = Minecraft.getInstance().font
        val text = badgeText(waitingCount)
        val badgeHeight = maxOf(font.lineHeight + 2, 11)
        val badgeWidth = maxOf(badgeHeight, font.width(text) + 6)
        val left = x + width - 2 - badgeWidth / 2
        val top = y + height - 2 - badgeHeight / 2
        drawRoundedFill(graphics, left - 1, top - 1, badgeWidth + 2, badgeHeight + 2, badgeHeight / 2 + 1, BADGE_OUTLINE_COLOR)
        drawRoundedFill(graphics, left, top, badgeWidth, badgeHeight, badgeHeight / 2, BADGE_BACKGROUND_COLOR)
        val textX = left + (badgeWidth - font.width(text)) / 2
        val textY = top + (badgeHeight - font.lineHeight) / 2
        graphics.text(font, text, textX + 1, textY + 1, BADGE_TEXT_SHADOW_COLOR)
        graphics.text(font, text, textX, textY, BADGE_TEXT_COLOR)
    }

    private fun drawBackground(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, radius: Int) {
        if (OverlayCategory.customBackgroundImage) {
            drawRoundedBackgroundImage(graphics, x, y, width, height, radius)
            drawRoundedFill(graphics, x, y, width, height, radius, 0xA8000000.toInt())
        } else {
            val color = OverlayCategory.backgroundColor
            drawRoundedFill(graphics, x, y, width, height, radius, color)
        }
    }

    private fun drawRoundedBackgroundImage(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, radius: Int) {
        if (radius <= 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, customBackground, x, y, 0f, 0f, width, height, width, height)
            return
        }

        // Draw runs of rows with the same inset so the texture follows the same
        // rounded silhouette as the fill and border without changing its scale.
        var firstRow = 0
        while (firstRow < height) {
            val inset = roundedInset(firstRow, height, radius)
            var lastRow = firstRow + 1
            while (lastRow < height && roundedInset(lastRow, height, radius) == inset) {
                lastRow++
            }

            val clippedWidth = width - inset * 2
            if (clippedWidth > 0) {
                graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    customBackground,
                    x + inset,
                    y + firstRow,
                    inset.toFloat(),
                    firstRow.toFloat(),
                    clippedWidth,
                    lastRow - firstRow,
                    width,
                    height
                )
            }
            firstRow = lastRow
        }
    }

    private fun drawRoundedFill(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, radius: Int, color: Int) {
        if (radius <= 0) {
            graphics.fill(x, y, x + width, y + height, color)
            return
        }
        // Render every row exactly once. Overlapping translucent fills compound
        // their alpha and made the center darker than the rounded side sections.
        for (row in 0 until height) {
            val inset = roundedInset(row, height, radius)
            graphics.fill(x + inset, y + row, x + width - inset, y + row + 1, color)
        }
    }

    private fun drawBorders(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, radius: Int, thickness: Int, color: Int) {
        if (radius > 0) {
            // Draw only the ring. Painting a translucent background over a solid
            // border made the border color bleed through at the left and right.
            for (row in 0 until height) {
                val outerInset = roundedInset(row, height, radius)
                val innerRow = row - thickness
                if (innerRow !in 0 until height - thickness * 2) {
                    graphics.fill(x + outerInset, y + row, x + width - outerInset, y + row + 1, color)
                    continue
                }

                val innerInset = thickness + roundedInset(innerRow, height - thickness * 2, (radius - thickness).coerceAtLeast(0))
                graphics.fill(x + outerInset, y + row, x + innerInset, y + row + 1, color)
                graphics.fill(x + width - innerInset, y + row, x + width - outerInset, y + row + 1, color)
            }
            return
        }
        // Top border
        graphics.fill(x, y, x + width, y + thickness, color)
        // Bottom border
        graphics.fill(x, y + height - thickness, x + width, y + height, color)
        // Left border
        graphics.fill(x, y, x + thickness, y + height, color)
        // Right border
        graphics.fill(x + width - thickness, y, x + width, y + height, color)
    }

    private fun roundedInset(row: Int, height: Int, radius: Int): Int {
        if (radius <= 0 || row in radius until height - radius) return 0
        val distanceFromEdge = if (row < radius) row else height - row - 1
        return radius - kotlin.math.sqrt((radius * radius - (radius - distanceFromEdge) * (radius - distanceFromEdge)).toDouble()).toInt()
    }

    private fun drawCornerAccents(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, thickness: Int, color: Int) {
        val length = 14
        graphics.fill(x, y, x + length, y + thickness, color); graphics.fill(x, y, x + thickness, y + length, color)
        graphics.fill(x + width - length, y, x + width, y + thickness, color); graphics.fill(x + width - thickness, y, x + width, y + length, color)
        graphics.fill(x, y + height - thickness, x + length, y + height, color); graphics.fill(x, y + height - length, x + thickness, y + height, color)
        graphics.fill(x + width - length, y + height - thickness, x + width, y + height, color); graphics.fill(x + width - thickness, y + height - length, x + width, y + height, color)
    }

    private fun drawDoubleBorder(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, color: Int) {
        drawBorders(graphics, x, y, width, height, 0, 1, color)
        val inset = 3
        drawBorders(graphics, x + inset, y + inset, width - inset * 2, height - inset * 2, 0, 1, withAlpha(color, 0x80))
    }

    private fun drawSideRails(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, color: Int) {
        val railWidth = 3
        val capLength = 18
        graphics.fill(x, y, x + railWidth, y + height, color)
        graphics.fill(x + width - railWidth, y, x + width, y + height, color)
        graphics.fill(x, y, x + capLength, y + 1, color)
        graphics.fill(x + width - capLength, y, x + width, y + 1, color)
        graphics.fill(x, y + height - 1, x + capLength, y + height, color)
        graphics.fill(x + width - capLength, y + height - 1, x + width, y + height, color)
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        (alpha.coerceIn(0, 0xFF) shl 24) or (color and 0x00FFFFFF)

    private fun drawGlow(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, radius: Int, color: Int) {
        val strength = when (OverlayCategory.glow) {
            GlowStyle.None -> return
            GlowStyle.Soft -> 0.45
            GlowStyle.Strong -> 0.75
            GlowStyle.Pulse -> {
                val phase = (System.currentTimeMillis() % 2_000L).toDouble() / 2_000.0 * Math.PI * 2.0
                0.55 + (sin(phase) + 1.0) * 0.225
            }
        }

        // Several increasingly faint rings create a glow without requiring a
        // post-processing shader, so it remains compatible with HUD extraction.
        for (distance in 5 downTo 1) {
            val alpha = (strength * (6 - distance) * 32).toInt()
            drawBorders(
                graphics,
                x - distance,
                y - distance,
                width + distance * 2,
                height + distance * 2,
                if (radius > 0) radius + distance else 0,
                1,
                withAlpha(color, alpha)
            )
        }
    }

    private fun drawWrappedProgress(graphics: GuiGraphicsExtractor, style: PromptStyle, x: Int, y: Int, width: Int, height: Int, radius: Int, thickness: Int, progress: Double, color: Int, pride: Boolean) {
        if (style == PromptStyle.DoubleBorder) {
            drawRectangularWrappedProgress(graphics, x, y, width, height, 1, progress, color, pride)

            val inset = 3
            drawRectangularWrappedProgress(
                graphics,
                x + inset,
                y + inset,
                width - inset * 2,
                height - inset * 2,
                1,
                progress,
                withAlpha(color, 0x80),
                pride,
                0x20FFFFFF
            )
            return
        }

        if (radius > 0) {
            val path = RoundedPerimeterCache.get(width - thickness + 1, height - thickness + 1, radius)
            drawBorders(graphics, x, y, width, height, radius, thickness, 0x40FFFFFF)
            drawPath(graphics, path, x, y, thickness, (path.size * progress).toInt(), color, pride)
        } else {
            drawRectangularWrappedProgress(graphics, x, y, width, height, thickness, progress, color, pride)
        }
    }

    private fun drawRectangularWrappedProgress(
        graphics: GuiGraphicsExtractor,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        thickness: Int,
        progress: Double,
        color: Int,
        pride: Boolean,
        trackColor: Int = 0x40FFFFFF
    ) {
        val perimeter = 2 * width + 2 * (height - thickness * 2)
        drawPerimeter(graphics, x, y, width, height, thickness, perimeter, trackColor, false)
        drawPerimeter(graphics, x, y, width, height, thickness, (perimeter * progress).toInt(), color, pride)
    }

    private fun drawPath(graphics: GuiGraphicsExtractor, path: List<PerimeterPoint>, x: Int, y: Int, thickness: Int, pixels: Int, color: Int, rainbow: Boolean) {
        SameColorRun(graphics::fill) {
            for (index in 0 until pixels.coerceIn(0, path.size)) {
                val (px, py) = path[index]
                val pixelColor = if (rainbow) rainbowColor(index, path.size) else color
                add(x + px, y + py, x + px + thickness, y + py + thickness, pixelColor)
            }
        }
    }

    private fun drawPerimeter(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, thickness: Int, pixels: Int, color: Int, rainbow: Boolean) {
        var remaining = pixels
        var traveled = 0
        val perimeter = 2 * width + 2 * (height - thickness * 2)
        SameColorRun(graphics::fill) {
            fun segment(length: Int, draw: SameColorRun.(Int, Int) -> Unit) {
                val amount = remaining.coerceIn(0, length)
                for (i in 0 until amount) {
                    val pixelColor = if (rainbow) {
                        withAlpha(rainbowColor(traveled + i, perimeter), color ushr 24)
                    } else color
                    draw(i, pixelColor)
                }
                remaining -= amount; traveled += length
            }
            // Top and bottom own the corner squares. The side segments stop before
            // them, so translucent track pixels never stack into brighter squares.
            segment(width) { i, c -> add(x + i, y, x + i + 1, y + thickness, c) }
            segment(height - thickness * 2) { i, c -> add(x + width - thickness, y + thickness + i, x + width, y + thickness + i + 1, c) }
            segment(width) { i, c -> add(x + width - i - 1, y + height - thickness, x + width - i, y + height, c) }
            segment(height - thickness * 2) { i, c -> add(x, y + height - thickness - i - 1, x + thickness, y + height - thickness - i, c) }
        }
    }

    private fun rainbowColor(position: Int, length: Int): Int {
        val hue = position.toFloat() / length.coerceAtLeast(1)
        return 0xFF000000.toInt() or (Color.HSBtoRGB(hue, 0.85f, 1f) and 0x00FFFFFF)
    }

    private fun drawRainbowBar(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int) {
        // Rainbow colors: red, orange, yellow, green, cyan, blue, purple
        val rainbowColors = intArrayOf(
            0xFFE40603.toInt(), // Red
            0xFFFB9101.toInt(), // Orange
            0xFFE4F900.toInt(), // Yellow
            0xFF05BB1B.toInt(), // Green
            0xFF00A8FD.toInt(), // Cyan
            0xFF1E43D9.toInt(), // Blue
            0xFFCC009F.toInt()  // Purple
        )

        // Draw gradient by interpolating between colors for each pixel
        for (i in 0 until width) {
            val progress = i.toFloat() / width.toFloat()
            val colorIndex = progress * (rainbowColors.size - 1)
            val index1 = colorIndex.toInt().coerceIn(0, rainbowColors.size - 2)
            val index2 = (index1 + 1).coerceIn(0, rainbowColors.size - 1)
            val blend = colorIndex - index1

            val color1 = rainbowColors[index1]
            val color2 = rainbowColors[index2]

            val r = lerp((color1 shr 16) and 0xFF, (color2 shr 16) and 0xFF, blend)
            val g = lerp((color1 shr 8) and 0xFF, (color2 shr 8) and 0xFF, blend)
            val b = lerp(color1 and 0xFF, color2 and 0xFF, blend)

            val blendedColor = (0xFF000000.toInt()) or (r shl 16) or (g shl 8) or b
            graphics.fill(x + i, y, x + i + 1, y + height, blendedColor)
        }
    }

    private fun lerp(a: Int, b: Int, t: Float): Int {
        return (a + (b - a) * t).toInt()
    }
}
