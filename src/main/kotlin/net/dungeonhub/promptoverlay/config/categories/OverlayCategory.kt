package net.dungeonhub.promptoverlay.config.categories

import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigElementRenderer
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigUI
import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfigElement
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import net.dungeonhub.promptoverlay.PromptOverlay.MOD_ID
import net.dungeonhub.promptoverlay.api.render.Overlay
import net.dungeonhub.promptoverlay.enums.GlowStyle
import net.dungeonhub.promptoverlay.enums.PromptAnimation
import net.dungeonhub.promptoverlay.enums.PromptStyle
import net.dungeonhub.promptoverlay.feature.OverlayRenderer
import net.dungeonhub.promptoverlay.overlays.FriendRequestOverlay
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import java.awt.Color
import java.util.function.Predicate

object OverlayCategory : CategoryKt("overlay") {
    private val previewRenderer = Identifier.fromNamespaceAndPath(MOD_ID, "overlay_preview_renderer")

    override val name: TranslatableValue
        get() = Literal("Overlays")

    init {
        ResourcefulConfigUI.registerElementRenderer(previewRenderer) { OverlayPreviewRenderer }
        element(OverlayPreviewElement)
    }

    private object OverlayPreviewElement : ResourcefulConfigElement {
        override fun renderer(): Identifier = previewRenderer
        override fun search(predicate: Predicate<String>): Boolean = predicate.test("Overlay Preview")
    }

    private object OverlayPreviewRenderer : ResourcefulConfigElementRenderer {
        override fun title(): Component = Component.literal("Preview")
        override fun description(): Component = Component.empty()
        override fun widgets(): MutableList<AbstractWidget> = mutableListOf(OverlayPreviewWidget())
    }

    val overlayDisplayDuration by int("overlay_display_duration", 10) {
        name = Literal("Overlay Display Duration")
        description = Literal("How long (in seconds) the prompt is displayed before being automatically dismissed.")
        range = 3..90
        slider = true
    }

    val soundsEnabled by boolean("sounds_enabled", true) {
        name = Literal("Enable Sounds")
        description = Literal("Play sounds when prompt overlays appear.")
    }

    val backgroundColor by color("background_color", 0xA0000000.toInt()) {
        name = Literal("Background Color")
        allowAlpha = true

        presets = arrayOf(
            0xA0000000.toInt(), // Default (gray)
            0xE0141E33.toInt(), // Dark Blue
            0xE0331118.toInt(), // Crimson Red
            0xE01C3528.toInt(), // Light Green
            0xE03B3213.toInt(), // Golden Yellow
            0xE03A2334.toInt(), // Light Pink
            0xF5101018.toInt(), // Opaque Obsidian: Near-opaque charcoal with a subtle violet cast for maximum readability
            0  // Transparent / 0x00000000
        ).toIntArray()
    }

    val customBackgroundImage by boolean("custom_background_image", false) {
        name = Literal("Custom Background Image")
        description = Literal("Please note that this requires a texture pack to use.")
    }

    val style by enum("prompt_style", PromptStyle.Default) {
        name = Literal("Prompt Style")
    }

    val wrapProgress by boolean("wrap_progress", false) {
        name = Literal("Wrap Progress")
        description = Literal("Wraps the progress bar around the prompt. Note that only certain styles support this!")
        condition = { style.supportsWrappedProgress }
    }

    val animation by enum("entry_animation", PromptAnimation.FlyIn) {
        name = Literal("Entry Animation")
        description = Literal("The animation that's played when a new prompt is received.")
    }

    val glow by enum("glow_style", GlowStyle.None) {
        name = Literal("Glow Style")
    }

    val abiphoneColor by color("abiphone_call_color", Color.LIGHT_GRAY.rgb) {
        name = Literal("Abiphone Call Color")
        description = Literal("The color of the abiphone call overlay.")
    }

    val catacombsRequeueColor by color("catacombs_requeue_color", Color.DARK_GRAY.rgb) {
        name = Literal("Catacombs Requeue Color")
        description = Literal("The color of the catacombs requeue overlay.")
    }

    val darkAuctionColor by color("dark_auction_warp_color", Color.BLACK.rgb) {
        name = Literal("Dark Auction Color")
        description = Literal("The color of the dark auction warp overlay.")
    }

    val duelColor by color("duel_color", Color.RED.rgb) {
        name = Literal("Duel Color")
        description = Literal("The color of the duel invite overlay.")
    }

    val friendColor by color("friend_color", Color.PINK.rgb) {
        name = Literal("Friend Color")
        description = Literal("The color of the friend request overlay.")
    }

    val guildColor by color("guild_color", Color.GREEN.rgb) {
        name = Literal("Guild Color")
        description = Literal("The color of the guild request overlay.")
    }

    val optionSelectColor by color("option_select_color", Color.GRAY.rgb) {
        name = Literal("Option Select")
        description = Literal("The color of the generic option select - used in NPC dialogs such as conversations.")
    }

    val partyColor by color("party_color", Color.BLUE.rgb) {
        name = Literal("Party Color")
        description = Literal("The color of the party invite overlay.")
    }

    val starlynSisterColor by color("starlyn_sister_color", Color.YELLOW.rgb) {
        name = Literal("Starlyn Sister Color")
        description = Literal("The color of the starlyn sister rewards overlay.")
    }

    val tradeColor by color("trade_color", Color(0x2BA801).rgb) {
        name = Literal("Trade Color")
        description = Literal("The color of the Skyblock trade overlay.")
    }

    val trapperColor by color("trapper_color", Color(0xA52A2A).rgb) {
        name = Literal("Trapper Color")
        description = Literal("The color of the Trapper Hunt overlay.")
    }

    val travelingZooColor by color("traveling_zoo_warp_color", Color.BLACK.rgb) {
        name = Literal("Traveling Zoo Color")
        description = Literal("The color of the traveling zoo warp overlay.")
    }

    val trophyFishColor by color("trophy_fish_color", Color.YELLOW.rgb) {
        name = Literal("Trophy Fish Color")
        description = Literal("The color of the Trophy Fish GG overlay.")
    }

    val alwaysPrideMonth by boolean("always_pride_month", false) {
        name = Literal("Always Pride Month")
        description = Literal("Always assume that it's the Pride Month, giving you a special theme.")
    }

    private class OverlayPreviewWidget(val overlay: Overlay = FriendRequestOverlay("Taubsie"), width: Int = OverlayRenderer.calculateBoxWidth(overlay), height: Int = OverlayRenderer.calculateTotalHeight(overlay)) :
        AbstractWidget(0, 0, width, height, CommonComponents.EMPTY) {

        override fun extractWidgetRenderState(
            graphics: GuiGraphicsExtractor,
            mouseX: Int,
            mouseY: Int,
            partialTick: Float
        ) {
            OverlayRenderer.renderPreview(graphics, overlay, x, y)
        }

        override fun updateWidgetNarration(output: NarrationElementOutput) = Unit
    }
}
