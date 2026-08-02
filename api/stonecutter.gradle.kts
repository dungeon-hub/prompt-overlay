plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.1.2" /* [SC] DO NOT EDIT */

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["minecraft_version"] = "\"${node.metadata.version}\""

    replacements {
        // 1.21.11 (<26.1) still uses the pre-split rendering/chat API.
        string(current.parsed < "26.1") {
            replace("GuiGraphicsExtractor", "GuiGraphics")
            replace(".text(", ".drawString(")
            replace(".addClientSystemMessage(", ".addMessage(")
        }

        // 26.2 moved the chat HUD behind Gui#hud and reworked a few client accessors.
        string(current.parsed >= "26.2") {
            replace("gui.chat", "gui.hud.chat")
        }
    }
}
