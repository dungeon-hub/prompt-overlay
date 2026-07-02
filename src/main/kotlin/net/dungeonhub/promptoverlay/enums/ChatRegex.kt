package net.dungeonhub.promptoverlay.enums

import net.dungeonhub.promptoverlay.feature.OverlayFeature
import net.dungeonhub.promptoverlay.overlays.FriendRequestOverlay
import net.dungeonhub.promptoverlay.overlays.TrapperHuntOverlay
import net.dungeonhub.promptoverlay.overlays.TrophyFishGgOverlay
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component

enum class ChatRegex(val regex: Regex, val action: (message: Component, result: MatchResult) -> Unit) {
    FriendRequest(Regex("Friend request from ((?<rank>\\[.+] )?(?<player>\\S{1,16})).*"), action={ _, result ->
        val player = result.groups["player"]?.value

        if(player != null) OverlayFeature.setOverlay(FriendRequestOverlay(player))
    }),
    TrapperHunt(Regex("Accept the trapper's task to hunt the animal?"), action={ message, _ ->
        var acceptCommand: String? = null
        var denyCommand: String? = null

        // Traverse the component tree to find the click event commands
        fun traverseComponent(component: Component) {
            val style = component.style
            val clickEvent = style.clickEvent as? ClickEvent.RunCommand

            if (clickEvent != null) {
                val command = clickEvent.command
                val text = component.string

                when {
                    text.contains("[YES]") -> acceptCommand = command
                    text.contains("[NO]") -> denyCommand = command
                }
            }

            // Traverse siblings
            component.siblings.forEach { traverseComponent(it) }
        }

        traverseComponent(message)

        if (acceptCommand != null && denyCommand != null) {
            OverlayFeature.setOverlay(TrapperHuntOverlay(acceptCommand, denyCommand))
        }
    }),
    TrophyFishGg(Regex("§6§lCLICK HERE §eto say §6gg§e!"), action={ _, _ -> OverlayFeature.setOverlay(TrophyFishGgOverlay()) })
}