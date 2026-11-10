package net.dungeonhub.promptoverlay.enums

import net.dungeonhub.promptoverlay.feature.OverlayFeature
import net.dungeonhub.promptoverlay.feature.PromptQueueManager
import net.dungeonhub.promptoverlay.overlays.ThreeOptionsSelectOverlay
import net.minecraft.SharedConstants
import net.minecraft.client.Minecraft
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.TagParser
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.server.Bootstrap
import org.mockito.Mockito.mock
import java.lang.reflect.Field
import kotlin.test.*

class ChatRegexTest {
    private val minecraftInstanceField: Field = Minecraft::class.java.getDeclaredField("instance").apply {
        trySetAccessible()
    }
    private var previousMinecraft: Minecraft? = null
    private val queue by lazy {
        OverlayFeature::class.java.getDeclaredField("queue").apply { trySetAccessible() }
            .get(null) as PromptQueueManager
    }

    @BeforeTest
    fun installMockMinecraft() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
        previousMinecraft = minecraftInstanceField.get(null) as? Minecraft
        // OverlayFeature schedules the notification sound through Minecraft.execute
        minecraftInstanceField.set(null, mock(Minecraft::class.java))
    }

    @AfterTest
    fun restoreMinecraft() {
        OverlayFeature.removeOverlay(RemoveType.Dismiss)
        // Finish the exit immediately instead of waiting for the animation delay
        queue.outgoingPrompt()?.let { queue.completeExit(it.id) }
        minecraftInstanceField.set(null, previousMinecraft)
    }

    @Test
    fun `option select with options grouped under one click event shows three options`() {
        val message = parseComponent(
            """
            {extra: [{click_event: {command: "10/30/2026", action: "suggest_command"}, color: "#FF55FF", hover_event: {action: "show_text", value: {color: "#FFFFFF", text: "10/30/2026"}}, text: "", insertion: "1793355415008"}, {color: "yellow", extra: [{click_event: {payload: {npcId: "tyashoi", responseKey: "x_6"}, action: "custom", id: "skyblock:dialogue_response"}, extra: [{color: "green", text: "[Yes] "}, {color: "yellow", text: "[Well] "}, {color: "red", text: "[No] "}], hover_event: {action: "show_text", value: {color: "yellow", text: "Click to select!"}}, text: ""}], text: "Select an option: "}, ""], text: ""}
            """.trimIndent()
        )

        val result = ChatRegex.OptionSelect.regex.find(message.string)!!
        ChatRegex.OptionSelect.action(message, result)

        val overlay = assertIs<ThreeOptionsSelectOverlay>(OverlayFeature.currentOverlay)
        assertEquals(
            listOf("Yes", "Well", "No"),
            listOf(overlay.firstText, overlay.secondText, overlay.thirdText).map { it.substringAfter("] ") },
        )
    }

    @Test
    fun `option select with carnival fisherman shows three options with arrows`() {
        val message = parseComponent(
            """
            {extra: [{click_event: {command: "11/10/2026", action: "suggest_command"}, color: "#FF55FF", hover_event: {action: "show_text", value: {color: "#FFFFFF", text: "11/10/2026"}}, text: "", insertion: "1794346017769"}, {color: "yellow", extra: [{click_event: {payload: {npcId: "carnival_fisherman", responseKey: "r_2_1"}, action: "custom", id: "skyblock:dialogue_response"}, extra: [{color: "yellow", text: " ➜ "}, {color: "green", text: "[You guessed it!] "}], hover_event: {action: "show_text", value: {color: "yellow", text: "Click to select!"}}, text: "\n "}, {click_event: {payload: {npcId: "carnival_fisherman", responseKey: "r_2_2"}, action: "custom", id: "skyblock:dialogue_response"}, extra: [{color: "yellow", text: " ➜ "}, {color: "aqua", text: "[Could you tell me the rules again?] "}], hover_event: {action: "show_text", value: {color: "yellow", text: "Click to select!"}}, text: "\n "}, {click_event: {payload: {npcId: "carnival_fisherman", responseKey: "r_2_3"}, action: "custom", id: "skyblock:dialogue_response"}, extra: [{color: "yellow", text: " ➜ "}, {color: "red", text: "[Nope.] "}], hover_event: {action: "show_text", value: {color: "yellow", text: "Click to select!"}}, text: "\n "}], text: "Select an option: "}, ""], text: ""}
            """.trimIndent()
        )

        val result = ChatRegex.OptionSelect.regex.find(message.string)!!
        ChatRegex.OptionSelect.action(message, result)

        val overlay = assertIs<ThreeOptionsSelectOverlay>(OverlayFeature.currentOverlay)
        assertEquals(
            listOf("You guessed it!", "Could you tell me the rules again?", "Nope."),
            listOf(overlay.firstText, overlay.secondText, overlay.thirdText).map { it.substringAfter("] ") },
        )
    }

    private fun parseComponent(snbt: String): Component =
        ComponentSerialization.CODEC.parse(NbtOps.INSTANCE, TagParser.parseCompoundFully(snbt)).getOrThrow()
}
