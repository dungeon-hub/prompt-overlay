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
import net.dungeonhub.promptoverlay.enums.PromptAnimation
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.resources.Identifier
import java.util.concurrent.Executors
import kotlin.math.pow
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

object OverlayFeature {
    val currentOverlay: Overlay?
        get() = queue.currentPrompt?.overlay
    private var hideMessageJob: Job? = null
    private var transitionJob: Job? = null

    private val supervisor = SupervisorJob()
    private val dispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()

    private val scheduler = CoroutineScope(supervisor + dispatcher)

    private val queue = PromptQueueManager(
        onShow = ::setCurrentOverlay,
        onExit = ::removeCurrentOverlay,
        onExitComplete = { OverlayRenderer.completeAnimatingOut() },
    )

    fun init() {
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.PLAYER_LIST,
            Identifier.fromNamespaceAndPath(MOD_ID, "prompt")
        ) { graphics, _ -> render(graphics) }
    }

    fun setOverlay(overlay: Overlay) {
        queue.enqueue(overlay)
    }

    private fun setCurrentOverlay(entry: PromptEntry) {
        OverlayRenderer.startAnimatingIn(entry.enqueuedAt)
        hideMessageJob?.cancel()
        hideMessageJob = scheduler.launch {
            delay(remainingDisplayDuration(entry))
            removeOverlay(entry.id, RemoveType.Dismiss)
        }
    }

    fun removeOverlay(type: RemoveType) {
        val entry = queue.currentPrompt ?: return
        removeOverlay(entry.id, type)
    }

    private fun removeOverlay(id: Long, type: RemoveType) = queue.removePrompt(id, type)

    private fun removeCurrentOverlay(entry: PromptEntry, type: RemoveType) {
        hideMessageJob?.cancel()
        OverlayRenderer.startAnimatingOut(type)
        transitionJob?.cancel()
        transitionJob = scheduler.launch {
            delay(OverlayRenderer.ANIMATION_DURATION)
            queue.completeExit(entry.id)
        }
    }

    internal fun removeOverlay(entry: PromptEntry, type: RemoveType) = removeOverlay(entry.id, type)
    internal fun currentPrompt() = queue.currentPrompt

    fun render(graphics: GuiGraphicsExtractor) {
        val overlay = ((if (OverlayRenderer.isAnimatingOut) queue.outgoingPrompt else queue.currentPrompt)?.overlay) ?: return

        val minecraft = Minecraft.getInstance()

        if (minecraft.options.hideGui) return

        val window = minecraft.window
        val screenWidth = window.guiScaledWidth
        val screenHeight = window.guiScaledHeight

        val boxWidth = OverlayRenderer.calculateBoxWidth(overlay)

        val totalHeight = OverlayRenderer.calculateTotalHeight(overlay)

        // Base position (center)
        val baseX = (screenWidth - boxWidth) / 2
        val baseY = 20 // Top of screen with some margin

        // Calculate animation progress (0.0 to 1.0)
        val elapsed = Clock.System.now() - OverlayRenderer.animationStartTime
        val progress = (elapsed / OverlayRenderer.ANIMATION_DURATION).coerceIn(0.0, 1.0)
        val easedProgress = easeInOutCubic(progress)

        // Apply animation offset
        val (x, y) = when {
            OverlayRenderer.isAnimatingIn -> {
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
            OverlayRenderer.isAnimatingOut -> {
                when (OverlayRenderer.animationOutType) {
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
        if (!isOnScreen && OverlayRenderer.isAnimatingOut) {
            // Animation complete, fully off screen
            return
        }

        if (OverlayRenderer.isAnimatingIn && progress >= 1.0) {
            OverlayRenderer.completeAnimatingIn()
        }

        val dismissProgress = if (!OverlayRenderer.isAnimatingOut) {
            val elapsedDismissMs = Clock.System.now() - OverlayRenderer.autoDismissStartTime
            dismissProgress(elapsedDismissMs, overlay)
        } else 1.0

        OverlayRenderer.render(graphics, overlay, x, y, dismissProgress, OverlayRenderer.isAnimatingOut, queue.waitingCount())
    }

    internal fun effectiveDisplayDuration(overlay: Overlay) =
        overlay.maxDisplayDuration
            ?.takeIf { it.isPositive() && it.isFinite() }
            ?.coerceAtMost(OverlayCategory.overlayDisplayDuration.seconds)
            ?: OverlayCategory.overlayDisplayDuration.seconds

    internal fun autoDismissDelay(overlay: Overlay) = effectiveDisplayDuration(overlay)

    internal fun remainingDisplayDuration(entry: PromptEntry, currentTime: Instant = Clock.System.now()) =
        (autoDismissDelay(entry.overlay) - (currentTime - entry.enqueuedAt)).coerceAtLeast(0.milliseconds)

    internal fun dismissProgress(elapsedTime: Duration, overlay: Overlay) =
        (elapsedTime / effectiveDisplayDuration(overlay)).coerceIn(0.0, 1.0)

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

}
