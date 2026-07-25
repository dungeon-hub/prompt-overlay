package net.dungeonhub.promptoverlay.feature

import net.dungeonhub.promptoverlay.api.render.AcceptableOverlay
import net.dungeonhub.promptoverlay.api.render.DeniableOverlay
import net.dungeonhub.promptoverlay.api.render.FiveActionsOverlay
import net.dungeonhub.promptoverlay.api.render.FourActionsOverlay
import net.dungeonhub.promptoverlay.api.render.OneOptionOverlay
import net.dungeonhub.promptoverlay.api.render.ThreeActionsOverlay
import net.dungeonhub.promptoverlay.api.render.TwoOptionsOverlay
import net.dungeonhub.promptoverlay.enums.RemoveType

object KeyPressHandler {
    fun handleAccept(): Boolean {
        val currentOverlay = OverlayFeature.currentOverlay as? AcceptableOverlay ?: return false

        currentOverlay.accept()

        OverlayFeature.removeOverlay(RemoveType.Accept)

        return true
    }

    fun handleDeny(): Boolean {
        val currentOverlay = OverlayFeature.currentOverlay as? DeniableOverlay ?: return false

        currentOverlay.deny()

        OverlayFeature.removeOverlay(RemoveType.Deny)

        return true
    }

    fun handleDismiss(): Boolean {
        val currentOverlay = OverlayFeature.currentOverlay ?: return false

        currentOverlay.dismiss()

        OverlayFeature.removeOverlay(RemoveType.Dismiss)

        return true
    }

    fun handleFirstOption(): Boolean {
        val currentOverlay = OverlayFeature.currentOverlay as? OneOptionOverlay ?: return false

        currentOverlay.firstOption()

        OverlayFeature.removeOverlay(RemoveType.Accept)

        return true
    }

    fun handleSecondOption(): Boolean {
        val currentOverlay = OverlayFeature.currentOverlay as? TwoOptionsOverlay ?: return false

        currentOverlay.secondOption()

        OverlayFeature.removeOverlay(RemoveType.Accept)

        return true
    }

    fun handleThirdOption(): Boolean {
        val currentOverlay = OverlayFeature.currentOverlay as? ThreeActionsOverlay ?: return false

        currentOverlay.thirdOption()

        OverlayFeature.removeOverlay(RemoveType.Accept)

        return true
    }

    fun handleFourthOption(): Boolean {
        val currentOverlay = OverlayFeature.currentOverlay as? FourActionsOverlay ?: return false

        currentOverlay.fourthOption()

        OverlayFeature.removeOverlay(RemoveType.Accept)

        return true
    }

    fun handleFifthOption(): Boolean {
        val currentOverlay = OverlayFeature.currentOverlay as? FiveActionsOverlay ?: return false

        currentOverlay.fifthOption()

        OverlayFeature.removeOverlay(RemoveType.Accept)

        return true
    }
}