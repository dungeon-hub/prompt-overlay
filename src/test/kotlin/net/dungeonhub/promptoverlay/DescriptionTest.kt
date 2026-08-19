package net.dungeonhub.promptoverlay

import net.dungeonhub.promptoverlay.api.render.Overlay
import net.dungeonhub.promptoverlay.overlays.AbiphoneCallOverlay
import net.dungeonhub.promptoverlay.overlays.FriendRequestOverlay
import net.dungeonhub.promptoverlay.overlays.SkyblockTradeOverlay
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import java.awt.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class DescriptionTest {
    @Test
    fun `description defaults to an empty component for existing overlay implementations`() {
        val overlay = object : Overlay {
            override val borderColor = Color.WHITE
            override val message: Component = Component.literal("Title")

            override fun getActionsHeight(width: Int) = 0
            override fun getActionsWidth(font: Font) = 0
            override fun renderActions(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int) = Unit
        }

        assertEquals("", overlay.description.string)
    }

    @Test
    fun `request overlays expose useful descriptions`() {
        assertEquals("From SomePlayer", FriendRequestOverlay("SomePlayer").description.string)
        assertEquals("From SomePlayer", SkyblockTradeOverlay("§aSomePlayer", "/trade SomePlayer").description.string)
        assertEquals("From SomePlayer", AbiphoneCallOverlay("SomePlayer", "/call accept").description.string)
        assertEquals("", AbiphoneCallOverlay(null, "/call accept").description.string)
    }
}
