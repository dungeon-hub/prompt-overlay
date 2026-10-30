package net.dungeonhub.promptoverlay

import net.dungeonhub.promptoverlay.api.KeyMappingProvider
import net.dungeonhub.promptoverlay.api.OverlayHandler
import net.dungeonhub.promptoverlay.api.PromptHandle
import net.dungeonhub.promptoverlay.api.PromptListener
import net.dungeonhub.promptoverlay.api.SetOverlayResult
import net.dungeonhub.promptoverlay.api.render.Overlay
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import org.slf4j.LoggerFactory

/**
 * Central API registry for the Prompt Overlay system.
 * Mods using this API should register their [net.dungeonhub.promptoverlay.api.KeyMappingProvider] during initialization.
 */
object PromptOverlayApi {
    private val logger = LoggerFactory.getLogger(PromptOverlayApi::class.java)

    var promptOverlay: OverlayHandler? = null
        private set
    var keyMappingProvider: KeyMappingProvider? = null
        private set

    /**
     * Registers a [OverlayHandler] for the overlay system.
     * This should be called once during mod initialization.
     *
     * @param handler The handler to register
     * @throws IllegalStateException if a handler is already registered
     */
    fun registerOverlayHandler(handler: OverlayHandler) {
        if (promptOverlay != null) {
            throw IllegalStateException(
                "OverlayHandler is already registered. Only one handler can be registered per application instance."
            )
        }
        this.promptOverlay = handler
    }

    @JvmStatic
    fun setOverlay(overlay: Overlay): SetOverlayResult {
        if (!FabricLoader.getInstance().isModLoaded("prompt-overlay")) {
            return SetOverlayResult.ModNotInstalled
        }

        val overlayHandler = promptOverlay
            ?: return overlayError(IllegalStateException("Prompt Overlay is installed, but no overlay handler is registered."))

        return try {
            overlayHandler.setOverlay(overlay)
            SetOverlayResult.Queued
        } catch (exception: Exception) {
            overlayError(exception)
        } catch (linkageError: LinkageError) {
            // Linkage errors (for example NoSuchFieldError) are particularly important here: they can occur when a mod bundles an older API whose Overlay implementation lacks newer members.
            overlayError(linkageError)
        }
    }

    /**
     * Queues an overlay like [setOverlay], but returns a [PromptHandle] that can cancel the prompt
     * and notifies [listener] once with the [net.dungeonhub.promptoverlay.api.PromptOutcome].
     *
     * @return the handle, or `null` if Prompt Overlay is not installed or the prompt could not be queued.
     * In that case the listener is never called, so the calling mod should use its normal fallback.
     */
    @JvmStatic
    @JvmOverloads
    fun submit(overlay: Overlay, listener: PromptListener? = null): PromptHandle? {
        if (!FabricLoader.getInstance().isModLoaded("prompt-overlay")) {
            return null
        }

        val overlayHandler = promptOverlay
        if (overlayHandler == null) {
            overlayError(IllegalStateException("Prompt Overlay is installed, but no overlay handler is registered."))
            return null
        }

        return try {
            overlayHandler.submit(overlay, listener)
        } catch (exception: Exception) {
            overlayError(exception)
            null
        } catch (linkageError: LinkageError) {
            // An installed Prompt Overlay that predates this API version does not implement submit.
            overlayError(linkageError)
            null
        }
    }

    private fun overlayError(throwable: Throwable): SetOverlayResult.Error {
        logger.error("An error occurred while a mod tried to queue an overlay.", throwable)

        Minecraft.getInstance().execute {
            Minecraft.getInstance().gui.hud.chat.addClientSystemMessage(
                Component.literal(
                    "An error occurred while a mod tried to set an overlay. Please make sure that your mods are on their latest version."
                ).withStyle(ChatFormatting.RED)
            )
        }

        return SetOverlayResult.Error(throwable)
    }

    /**
     * Registers a [KeyMappingProvider] for the overlay system.
     * This should be called once during mod initialization.
     *
     * @param provider The provider to register
     * @throws IllegalStateException if a provider is already registered
     */
    fun registerKeyMappingProvider(provider: KeyMappingProvider) {
        if (keyMappingProvider != null) {
            throw IllegalStateException(
                "KeyMappingProvider is already registered. Only one provider can be registered per application instance."
            )
        }
        this.keyMappingProvider = provider
    }

    /**
     * Internal method to retrieve the registered [KeyMappingProvider].
     * Used by overlay implementations to access key mapping names.
     *
     * @return The registered provider
     * @throws IllegalStateException if no provider has been registered
     */
    internal fun getKeyMappingProvider(): KeyMappingProvider {
        return keyMappingProvider
            ?: throw IllegalStateException(
                "No KeyMappingProvider registered. Call PromptOverlayApi.registerKeyMappingProvider() during mod initialization."
            )
    }
}
