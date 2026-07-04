package net.dungeonhub.promptoverlay.api

import net.dungeonhub.promptoverlay.api.render.Overlay

interface OverlayHandler {
    fun setOverlay(overlay: Overlay)
}