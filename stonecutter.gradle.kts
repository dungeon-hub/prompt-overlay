plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.1.2" /* [SC] DO NOT EDIT */

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["minecraft_version"] = "\"${node.metadata.version}\""

    replacements {
        // 1.21.11 (<26.1) still uses the pre-split rendering/chat/keybinding/command APIs.
        string(current.parsed < "26.1") {
            replace("GuiGraphicsExtractor", "GuiGraphics")
            replace(".text(", ".drawString(")
            replace(".addClientSystemMessage(", ".addMessage(")
            replace("ClientCommands", "ClientCommandManager")
            replace("net.fabricmc.fabric.api.client.keymapping.v1", "net.fabricmc.fabric.api.client.keybinding.v1")
            replace("KeyMappingHelper", "KeyBindingHelper")
            replace("registerKeyMapping", "registerKeyBinding")
        }

        // 26.2 moved the chat HUD behind Gui#hud and reworked a few client accessors.
        string(current.parsed >= "26.2") {
            replace("gui.chat", "gui.hud.chat")
            replace("Minecraft.getInstance().setScreen(", "Minecraft.getInstance().setScreenAndShow(")
            replace("minecraft.options.hideGui", "minecraft.gui.hud.isHidden")
        }
    }
}
