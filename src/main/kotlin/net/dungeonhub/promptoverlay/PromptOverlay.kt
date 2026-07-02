package net.dungeonhub.promptoverlay

import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import net.dungeonhub.promptoverlay.config.Config
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft

object PromptOverlay : ClientModInitializer {
    const val MOD_ID = "prompt-overlay"

    lateinit var version: String

    val configurator = Configurator(MOD_ID)

    val config = Config.register(configurator)

    val isDev = FabricLoader.getInstance().isDevelopmentEnvironment

    override fun onInitializeClient() {
        version = FabricLoader.getInstance().getModContainer(MOD_ID)
            .map { it.metadata.version.friendlyString }
            .orElse("unknown")!!

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommands.literal("prompt-overlay")
                    .executes {
                        Minecraft.getInstance().schedule {
                            Minecraft.getInstance().setScreen(ResourcefulConfigScreen.getFactory(MOD_ID).apply(null))
                        }
                        return@executes 1
                    }
            )
        }
    }
}
