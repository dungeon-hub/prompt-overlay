package net.dungeonhub.promptoverlay.enums

import net.dungeonhub.promptoverlay.feature.OverlayFeature
import net.dungeonhub.promptoverlay.overlays.FriendRequestOverlay
import net.dungeonhub.promptoverlay.overlays.TrophyFishGgOverlay
import net.minecraft.network.chat.Component

enum class ChatRegex(val regex: Regex, val action: (Component, MatchResult) -> Unit) {
    FriendRequest(Regex("Friend request from ((?<rank>\\[.+] )?(?<player>\\S{1,16})).*"), action={ _, result ->
        val player = result.groups["player"]?.value

        if(player != null) OverlayFeature.setOverlay(FriendRequestOverlay(player))
    }),
    TrophyFishGg(Regex("§6§lCLICK HERE §eto say §6gg§e!"), action={ _, _ -> OverlayFeature.setOverlay(TrophyFishGgOverlay()) })
}