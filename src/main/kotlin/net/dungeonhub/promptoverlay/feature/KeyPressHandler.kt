package net.dungeonhub.promptoverlay.feature

import net.dungeonhub.promptoverlay.api.render.AcceptableOverlay
import net.dungeonhub.promptoverlay.api.render.DeniableOverlay
import net.dungeonhub.promptoverlay.api.render.FiveActionsOverlay
import net.dungeonhub.promptoverlay.api.render.FourActionsOverlay
import net.dungeonhub.promptoverlay.api.render.OneOptionOverlay
import net.dungeonhub.promptoverlay.api.render.Overlay
import net.dungeonhub.promptoverlay.api.render.ThreeActionsOverlay
import net.dungeonhub.promptoverlay.api.render.TwoOptionsOverlay
import net.dungeonhub.promptoverlay.enums.RemoveType
import org.slf4j.LoggerFactory

object KeyPressHandler {
    private val logger = LoggerFactory.getLogger(KeyPressHandler::class.java)

    fun handleAccept() = act<AcceptableOverlay>(RemoveType.Accept) { accept() }

    fun handleDeny() = act<DeniableOverlay>(RemoveType.Deny) { deny() }

    fun handleDismiss() = act<Overlay>(RemoveType.Dismiss) { dismiss() }

    fun handleFirstOption() = act<OneOptionOverlay>(RemoveType.Accept) { firstOption() }

    fun handleSecondOption() = act<TwoOptionsOverlay>(RemoveType.Accept) { secondOption() }

    fun handleThirdOption() = act<ThreeActionsOverlay>(RemoveType.Accept) { thirdOption() }

    fun handleFourthOption() = act<FourActionsOverlay>(RemoveType.Accept) { fourthOption() }

    fun handleFifthOption() = act<FiveActionsOverlay>(RemoveType.Accept) { fifthOption() }

    private inline fun <reified T> act(
        type: RemoveType,
        action: T.() -> Unit,
    ): Boolean {
        val entry = OverlayFeature.currentPrompt() ?: return false
        val overlay = entry.overlay as? T ?: return false
        if (!OverlayFeature.removeOverlay(entry, type)) return false
        // Overlay actions come from third-party implementations. Any failure must not break key handling and possibly crash the game in that action, so catch broadly and log.
        @Suppress("TooGenericExceptionCaught")
        try {
            overlay.action()
        } catch (exception: Exception) {
            logger.error("Prompt action failed for prompt {}", entry.id, exception)
        }
        return true
    }
}
