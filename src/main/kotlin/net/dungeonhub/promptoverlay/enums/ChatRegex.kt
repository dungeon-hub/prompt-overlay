package net.dungeonhub.promptoverlay.enums

import net.dungeonhub.promptoverlay.feature.OverlayFeature
import net.dungeonhub.promptoverlay.overlays.DuelInviteOverlay
import net.dungeonhub.promptoverlay.overlays.FriendRequestOverlay
import net.dungeonhub.promptoverlay.overlays.GuildRequestOverlay
import net.dungeonhub.promptoverlay.overlays.PartyInviteOverlay
import net.dungeonhub.promptoverlay.overlays.SkyblockTradeOverlay
import net.dungeonhub.promptoverlay.overlays.TrapperHuntOverlay
import net.dungeonhub.promptoverlay.overlays.TrophyFishGgOverlay
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component

enum class ChatRegex(val regex: Regex, val action: (message: Component, result: MatchResult) -> Unit) {
    DuelInvite(Regex("(\\[.*] )?(?<player>\\S{1,16}) has invited you to (?<duel>\\S+)!"), action={ _, result ->
        val player = result.groups["player"]?.value
        val duel = result.groups["duel"]?.value

        if(player != null && duel != null) OverlayFeature.setOverlay(DuelInviteOverlay(player, duel))
    }),
    FriendRequest(Regex("Friend request from ((?<rank>\\[.+] )?(?<player>\\S{1,16})).*"), action={ _, result ->
        val player = result.groups["player"]?.value

        if(player != null) OverlayFeature.setOverlay(FriendRequestOverlay(player))
    }),
    GuildInvite(Regex("Click here to accept or type (?<command>/guild accept (?<name>\\w+))!"), action={ message, result ->
        val inviter = result.groups["name"]?.value

        // Optionally extract guild name from the full message
        val guildName = extractGuildName(message)

        if(inviter != null) {
            OverlayFeature.setOverlay(GuildRequestOverlay(inviter, guildName))
        }
    }),
    PartyInvite(Regex("(?:\\[.*] )?(?<player>\\S{1,16}) has invited you to join (?:their|(?:\\[.*] ?)?\\w{1,16}'s)? party!"), action={ _, result ->
        val player = result.groups["player"]?.value

        if(player != null) OverlayFeature.setOverlay(PartyInviteOverlay(player))
    }),
    SkyblockTrade(Regex("(?<player>\\S{1,16}) (?:§.)?has sent you a trade request"), action={ message, result ->
        val player = result.groups["player"]?.value
        val acceptCommand = findClickCommand(message) { it.startsWith("/tradeaccept") }

        if(player != null && acceptCommand != null) {
            OverlayFeature.setOverlay(SkyblockTradeOverlay(player, acceptCommand))
        }
    }),
    TrapperHunt(Regex("Accept the trapper's task to hunt the animal?"), action={ message, _ ->
        val acceptCommand = findClickCommand(message) { it.contains("[YES]") }
        val denyCommand = findClickCommand(message) { it.contains("[NO]") }

        if (acceptCommand != null && denyCommand != null) {
            OverlayFeature.setOverlay(TrapperHuntOverlay(acceptCommand, denyCommand))
        }
    }),
    TrophyFishGg(Regex("§6§lCLICK HERE §eto say §6gg§e!"), action={ _, _ -> OverlayFeature.setOverlay(TrophyFishGgOverlay()) });

    companion object {
        /**
         * Traverses a Component tree to find a click command that matches the given predicate.
         *
         * @param component The root component to traverse
         * @param predicate A function that tests either the command string or the component's text
         * @return The matching command string, or null if not found
         */
        private fun findClickCommand(component: Component, predicate: (String) -> Boolean): String? {
            val style = component.style
            val clickEvent = style.clickEvent as? ClickEvent.RunCommand

            if (clickEvent != null) {
                val command = clickEvent.command
                val text = component.string

                // Check if either the command or text matches the predicate
                if (predicate(command) || predicate(text)) {
                    return command
                }
            }

            // Traverse siblings
            for (sibling in component.siblings) {
                val result = findClickCommand(sibling, predicate)
                if (result != null) return result
            }

            return null
        }

        /**
         * Extracts the guild name from a guild invite message.
         * Looks for the pattern "has invited you to join their guild, <guildName>!"
         *
         * @param message The full message component
         * @return The guild name, or null if not found
         */
        private fun extractGuildName(message: Component): String? {
            val fullText = message.string
            val guildPattern = Regex("has invited you to join their guild, (.+)!")
            val match = guildPattern.find(fullText)
            return match?.groups?.get(1)?.value
        }
    }
}