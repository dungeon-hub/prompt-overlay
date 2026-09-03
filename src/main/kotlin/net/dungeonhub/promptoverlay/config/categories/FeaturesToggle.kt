package net.dungeonhub.promptoverlay.config.categories

import com.teamresourceful.resourcefulconfigkt.api.ObjectKt

object FeaturesToggle : ObjectKt() {
    val abiphoneCalls by boolean("show_abiphone_calls", true) {
        name = Literal("Abiphone Calls")
    }

    val catacombsRequeue by boolean("show_catacombs_requeue", true) {
        name = Literal("Catacombs Requeue")
    }

    val darkAuctionWarp by boolean("show_dark_auction_warp", true) {
        name = Literal("Dark Auction Warp")
        description = Literal("Show the Dark Auction Warp reminder.")
    }

    val dismissableNotification by boolean("show_dismissable_notification", true) {
        name = Literal("Dismissable Notification")
        description = Literal("Show a prompt for dismissable notifications, such as tutorial messages teaching you about the game mechanics.")
    }

    val duelInvite by boolean("show_duel_invite", true) {
        name = Literal("Duel Invite")
    }

    val eventRewards by boolean("show_event_rewards", true) {
        name = Literal("Event Rewards")
    }

    val friendRequest by boolean("show_friend_request", true) {
        name = Literal("Friend Request")
    }

    val guildInvite by boolean("show_guild_invite", true) {
        name = Literal("Guild Invite")
    }

    val npcOptionSelection by boolean("show_option_selects", true) {
        name = Literal("NPC Dialog Options")
        description = Literal("This currently only supports up to 2 options, so don't freak out if some dialogs aren't getting recognized")
    }

    // TODO make a toggle per command possible
    val partyCommands by boolean("show_party_commands", true) {
        name = Literal("Party Commands")
    }

    val partyInvites by boolean("show_party_invites", true) {
        name = Literal("Party Invites")
    }

    val skyblockTrade by boolean("show_skyblock_trade", true) {
        name = Literal("Skyblock Trade Request")
    }

    val starlynSisterRewards by boolean("show_starlyn_sister_rewards", true) {
        name = Literal("Starlyn Sister Rewards")
    }

    val trapperHunt by boolean("show_trapper_hunt", true) {
        name = Literal("Trapper Hunt")
        description = Literal("Gives you a prompt to call Trevor after another hunt task is available. Note: Requires you to have an active Cookie Buff.")
    }

    val travelingZoo by boolean("show_traveling_zoo_warp", true) {
        name = Literal("Traveling Zoo")
        description = Literal("Show the Traveling Zoo Warp reminder.")
    }

    val trophyFishGg by boolean("show_trophy_fish_gg", true) {
        name = Literal("Trophy Fish GG")
    }
}