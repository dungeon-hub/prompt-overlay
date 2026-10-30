package net.dungeonhub.promptoverlay.overlays

import net.dungeonhub.promptoverlay.api.render.ZeroActionsOverlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.minecraft.network.chat.Component
import java.awt.Color

class AreaDiscoveryOverlay(override val message: Component, override val description: Component) : ZeroActionsOverlay {
    override val borderColor: Color get() = Color(OverlayCategory.areaDiscoveryColor)
}