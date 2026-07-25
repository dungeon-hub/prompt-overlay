package net.dungeonhub.promptoverlay.service

import com.mojang.blaze3d.platform.InputConstants
import net.dungeonhub.promptoverlay.PromptOverlay
import net.dungeonhub.promptoverlay.PromptOverlayApi
import net.dungeonhub.promptoverlay.api.KeyMappingProvider
import net.dungeonhub.promptoverlay.feature.KeyPressHandler
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW

object KeyMappingService : KeyMappingProvider {
    private val category = KeyMapping.Category(Identifier.fromNamespaceAndPath(PromptOverlay.MOD_ID, "keybinds"))

    val acceptKey = KeyMapping(
        "key.prompt-overlay.accept",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_Z,
        category
    )
    override val acceptKeyName: String get() = acceptKey.translatedKeyMessage.string

    val denyKey = KeyMapping(
        "key.prompt-overlay.deny",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_N,
        category
    )
    override val denyKeyName: String get() = denyKey.translatedKeyMessage.string

    val dismissKey = KeyMapping(
        "key.prompt-overlay.dismiss",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_X,
        category
    )
    override val dismissKeyName: String get() = dismissKey.translatedKeyMessage.string

    val firstOptionKey = KeyMapping(
        "key.prompt-overlay.first-option",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_1,
        category
    )
    override val firstOptionKeyName: String get() = firstOptionKey.translatedKeyMessage.string

    val secondOptionKey = KeyMapping(
        "key.prompt-overlay.second-option",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_2,
        category
    )
    override val secondOptionKeyName: String get() = secondOptionKey.translatedKeyMessage.string

    val thirdOptionKey = KeyMapping(
        "key.prompt-overlay.third-option",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_3,
        category
    )
    override val thirdOptionKeyName: String get() = thirdOptionKey.translatedKeyMessage.string

    val fourthOptionKey = KeyMapping(
        "key.prompt-overlay.fourth-option",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_4,
        category
    )
    override val fourthOptionKeyName: String get() = fourthOptionKey.translatedKeyMessage.string

    val fifthOptionKey = KeyMapping(
        "key.prompt-overlay.fifth-option",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_5,
        category
    )
    override val fifthOptionKeyName: String get() = fifthOptionKey.translatedKeyMessage.string

    fun init() {
        KeyMappingHelper.registerKeyMapping(acceptKey)
        KeyMappingHelper.registerKeyMapping(denyKey)
        KeyMappingHelper.registerKeyMapping(dismissKey)

        KeyMappingHelper.registerKeyMapping(firstOptionKey)
        KeyMappingHelper.registerKeyMapping(secondOptionKey)
        KeyMappingHelper.registerKeyMapping(thirdOptionKey)
        KeyMappingHelper.registerKeyMapping(fourthOptionKey)
        KeyMappingHelper.registerKeyMapping(fifthOptionKey)

        PromptOverlayApi.registerKeyMappingProvider(this)
    }

    fun callListeners() {
        handleKeyDown(acceptKey) {
            KeyPressHandler.handleAccept()
        }

        handleKeyDown(denyKey) {
            KeyPressHandler.handleDeny()
        }

        handleKeyDown(dismissKey) {
            KeyPressHandler.handleDismiss()
        }

        handleKeyDown(firstOptionKey, true) {
            KeyPressHandler.handleFirstOption()
        }

        handleKeyDown(secondOptionKey, true) {
            KeyPressHandler.handleSecondOption()
        }

        handleKeyDown(thirdOptionKey, true) {
            KeyPressHandler.handleThirdOption()
        }

        handleKeyDown(fourthOptionKey, true) {
            KeyPressHandler.handleFourthOption()
        }

        handleKeyDown(fifthOptionKey, true) {
            KeyPressHandler.handleFifthOption()
        }
    }

    private fun handleKeyDown(keyMapping: KeyMapping, ignoreHotbarPresses: Boolean = false, action: () -> Boolean) {
        if(keyMapping.consumeClick()) {
            if(action() && ignoreHotbarPresses) {
                for(i in 0..8) {
                    val hotbarSlot = Minecraft.getInstance().options.keyHotbarSlots[i]

                    if(hotbarSlot.same(keyMapping)) {
                        hotbarSlot.consumeClick()
                    }
                }
            }
        }
    }
}
