package net.dungeonhub.promptoverlay.feature

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import net.dungeonhub.promptoverlay.PromptOverlay.MOD_ID
import net.dungeonhub.promptoverlay.api.render.Overlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.dungeonhub.promptoverlay.enums.RemoveType
import net.dungeonhub.promptoverlay.enums.GlowStyle
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier
import java.time.LocalDate
import java.time.Month
import java.util.concurrent.Executors
import kotlin.math.pow
import kotlin.math.sin
import kotlin.time.Duration.Companion.seconds

object OverlayFeature {
    var currentOverlay: Overlay? = null
    private set
    private var hidingOverlay: Overlay? = null
    private var hideMessageJob: Job? = null

    private var animationStartTime: Long = 0
    private var autoDismissStartTime: Long = 0
    private var isAnimatingIn: Boolean = false
    private var isAnimatingOut: Boolean = false
    private var animationOutType: RemoveType? = null

    private val ANIMATION_DURATION = 500.milliseconds

    private val supervisor = SupervisorJob()
    private val dispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()

    private val scheduler = CoroutineScope(supervisor + dispatcher)

    val customBackground = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/prompt-background.png")

    fun init() {
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.PLAYER_LIST,
            Identifier.fromNamespaceAndPath(MOD_ID, "prompt")
        ) { graphics, _ -> render(graphics) }
    }

    fun setOverlay(overlay: Overlay) {
        if(currentOverlay != null) {
            hidingOverlay = currentOverlay
        }

        currentOverlay = overlay
        animationStartTime = System.currentTimeMillis()
        autoDismissStartTime = System.currentTimeMillis()
        isAnimatingIn = true
        isAnimatingOut = false
        animationOutType = null

        if(hideMessageJob != null) {
            hideMessageJob?.cancel()
        }

        hideMessageJob = scheduler.launch {
            delay(OverlayCategory.overlayDisplayDuration.seconds)

            if(currentOverlay == overlay) {
                removeOverlay(RemoveType.Dismiss)
            }
        }
    }

    fun removeOverlay(type: RemoveType) {
        if (currentOverlay == null) return

        hidingOverlay = currentOverlay
        currentOverlay = null
        animationStartTime = System.currentTimeMillis()
        isAnimatingIn = false
        isAnimatingOut = true
        animationOutType = type

        // Schedule cleanup after animation completes
        scheduler.launch {
            delay(ANIMATION_DURATION)
            if (isAnimatingOut && animationOutType == type) {
                currentOverlay = null
                hidingOverlay = null
                isAnimatingOut = false
                animationOutType = null
            }
        }
    }

    fun render(graphics: GuiGraphicsExtractor) {
        val overlay = if (isAnimatingOut) hidingOverlay else currentOverlay
        overlay ?: return

        val minecraft = net.minecraft.client.Minecraft.getInstance()

        if (minecraft.options.hideGui) return

        val font = minecraft.font
        val window = minecraft.window
        val screenWidth = window.guiScaledWidth
        val screenHeight = window.guiScaledHeight

        // Calculate dimensions dynamically based on content
        val padding = 6
        val minWidth = 150
        val maxWidth = 400

        // Calculate required width based on message
        val messageWidth = font.width(overlay.message)
        val requiredMessageWidth = messageWidth + padding * 2 + 20 // Extra space for padding and margins

        // Calculate required width based on actions
        val tempActionsWidth = overlay.getActionsWidth(font)
        val requiredActionsWidth = tempActionsWidth + padding * 2 + 20

        // Choose the larger of the two, clamped between min and max
        val boxWidth = maxOf(requiredMessageWidth, requiredActionsWidth).coerceIn(minWidth, maxWidth)

        val messageHeight = font.lineHeight + padding * 2
        val actionsHeight = overlay.getActionsHeight(boxWidth) // Get height without rendering
        val totalHeight = messageHeight + actionsHeight + padding

        // Base position (center)
        val baseX = (screenWidth - boxWidth) / 2
        val baseY = 20 // Top of screen with some margin

        // Calculate animation progress (0.0 to 1.0)
        val elapsedMs = System.currentTimeMillis() - animationStartTime
        val progress = (elapsedMs.toDouble() / ANIMATION_DURATION.inWholeMilliseconds).coerceIn(0.0, 1.0)
        val easedProgress = easeInOutCubic(progress)

        // Apply animation offset
        val (x, y) = when {
            isAnimatingIn -> {
                when (OverlayCategory.animation) {
                    PromptAnimation.FlyIn -> {
                        val offsetY = ((1.0 - easedProgress) * (baseY + totalHeight)).toInt()
                        baseX to (baseY - offsetY)
                    }
                    PromptAnimation.Bounce -> {
                        val bounceProgress = easeOutBack(progress)
                        val offsetY = ((1.0 - bounceProgress) * (baseY + totalHeight)).toInt()
                        baseX to (baseY - offsetY)
                    }
                    PromptAnimation.Appear -> baseX to baseY
                }
            }
            isAnimatingOut -> {
                when (animationOutType) {
                    RemoveType.Accept -> {
                        // Slide out to the left
                        val offsetX = (easedProgress * (baseX + boxWidth)).toInt()
                        (baseX - offsetX) to baseY
                    }
                    RemoveType.Deny -> {
                        // Slide out to the right
                        val offsetX = (easedProgress * (screenWidth - baseX)).toInt()
                        (baseX + offsetX) to baseY
                    }
                    RemoveType.Dismiss -> {
                        // Slide out to the top
                        val offsetY = (easedProgress * (baseY + totalHeight)).toInt()
                        baseX to (baseY - offsetY)
                    }
                    null -> baseX to baseY
                }
            }
            else -> baseX to baseY
        }

        // Only render if still on screen
        val isOnScreen = x + boxWidth > 0 && x < screenWidth && y + totalHeight > 0 && y < screenHeight
        if (!isOnScreen && isAnimatingOut) {
            // Animation complete, fully off screen
            return
        }

        if (isAnimatingIn && progress >= 1.0) {
            isAnimatingIn = false
        }

        // Convert AWT Color to RGB int
        val borderColorRGB = overlay.borderColor.rgb and 0x00FFFFFF
        val borderColor = 0xFF000000.toInt() or borderColorRGB

        val borderThickness = 2
        val cornerRadius = 8

        drawGlow(graphics, x, y, boxWidth, totalHeight, cornerRadius, borderColor)

        // Draw background
        drawBackground(graphics, x, y, boxWidth, totalHeight, cornerRadius)

        // Draw rounded border
        drawBorders(graphics, x, y, boxWidth, totalHeight, cornerRadius, borderThickness, borderColor)

        // Draw loading bar separator between message and actions
        val separatorY = y + messageHeight
        val separatorWidth = boxWidth - padding * 2

        // Calculate loading bar progress (0.0 to 1.0 as time passes)
        if (!isAnimatingOut) {
            val elapsedDismissMs = System.currentTimeMillis() - autoDismissStartTime
            val dismissProgress = (elapsedDismissMs.toDouble() / OverlayCategory.overlayDisplayDuration.seconds.inWholeMilliseconds).coerceIn(0.0, 1.0)
            val filledWidth = (separatorWidth * dismissProgress).toInt()

            val isJune = LocalDate.now().month == Month.JUNE

            // Draw filled portion
            if (filledWidth > 0) {
                if (isJune || OverlayCategory.alwaysPrideMonth) {
                    // Draw rainbow gradient
                    drawRainbowBar(graphics, x + padding, separatorY, filledWidth, 2)
                } else {
                    // Draw with border color
                    graphics.fill(x + padding, separatorY, x + padding + filledWidth, separatorY + 2, borderColor)
                }
            }

            // Draw empty portion with dim color
            if (filledWidth < separatorWidth) {
                graphics.fill(x + padding + filledWidth, separatorY, x + boxWidth - padding, separatorY + 2, 0x40FFFFFF)
            }
        } else {
            // When animating out, show full bar
            val isJune = LocalDate.now().month == Month.JUNE

            if (isJune || OverlayCategory.alwaysPrideMonth) {
                drawRainbowBar(graphics, x + padding, separatorY, separatorWidth, 2)
            } else {
                graphics.fill(x + padding, separatorY, x + boxWidth - padding, separatorY + 2, borderColor)
            }
        }

        // Render message (centered)
        val messageText = overlay.message
        val textWidth = font.width(messageText)
        val messageX = x + (boxWidth - textWidth) / 2
        val messageY = y + padding
        graphics.text(font, messageText, messageX, messageY, 0xFFFFFFFF.toInt())

        // Render actions
        overlay.renderActions(graphics, x + padding, separatorY + padding, boxWidth - padding * 2)
    }

    private fun easeInOutCubic(t: Double): Double {
        return if (t < 0.5) {
            4 * t * t * t
        } else {
            1 - (-2 * t + 2).pow(3.0) / 2
        }
    }

    private fun easeOutBack(t: Double): Double {
        val overshoot = 1.70158
        val shifted = t - 1.0
        return 1.0 + (overshoot + 1.0) * shifted.pow(3.0) + overshoot * shifted.pow(2.0)
    }

    private fun drawBackground(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, radius: Int) {
        if (OverlayCategory.customBackgroundImage) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, customBackground, x, y, 0f, 0f, width, height, width, height)
            drawRoundedFill(graphics, x, y, width, height, radius, 0xA8000000.toInt())
        } else {
            val color = OverlayCategory.backgroundColor
            drawRoundedFill(graphics, x, y, width, height, radius, color)
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