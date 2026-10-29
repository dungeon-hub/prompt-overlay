package net.dungeonhub.promptoverlay.overlays

import net.dungeonhub.promptoverlay.api.render.ZeroActionsOverlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.minecraft.network.chat.Component
import java.awt.Color

class TimberNotificationOverlay(override val message: Component) : ZeroActionsOverlay {
    override val borderColor: Color get() = Color(OverlayCategory.timberColor)
}