package net.dungeonhub.promptoverlay.overlays

import net.dungeonhub.promptoverlay.api.render.AcceptableOverlay
import net.dungeonhub.promptoverlay.api.render.OneActionOverlay
import net.dungeonhub.promptoverlay.config.categories.OverlayCategory
import net.dungeonhub.promptoverlay.util.MessageUtil.sendMessage
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import java.awt.Color

class PartyCommandOverlay(val player: String, val partyCommand: PartyCommand) : AcceptableOverlay, OneActionOverlay {
    val ingameCommand: String = when (partyCommand) {
        PartyCommand.Warp -> "party warp"
        PartyCommand.PartyTransfer -> "party transfer $player"
        PartyCommand.AllInvite -> "party settings allinvite"
        PartyCommand.Floor1 -> dungeons("one")
        PartyCommand.Floor2 -> dungeons("two")
        PartyCommand.Floor3 -> dungeons("three")
        PartyCommand.Floor4 -> dungeons("four")
        PartyCommand.Floor5 -> dungeons("five")
        PartyCommand.Floor6 -> dungeons("six")
        PartyCommand.Floor7 -> dungeons("seven")
        PartyCommand.MasterMode1 -> master("one")
        PartyCommand.MasterMode2 -> master("two")
        PartyCommand.MasterMode3 -> master("three")
        PartyCommand.MasterMode4 -> master("four")
        PartyCommand.MasterMode5 -> master("five")
        PartyCommand.MasterMode6 -> master("six")
        PartyCommand.MasterMode7 -> master("seven")
    }

    val buttonText: String = when (partyCommand) {
        PartyCommand.Warp -> "Warp"
        PartyCommand.PartyTransfer -> "Transfer"
        PartyCommand.AllInvite -> "Toggle"
        PartyCommand.Floor1, PartyCommand.Floor2, PartyCommand.Floor3, PartyCommand.Floor4, PartyCommand.Floor5,
        PartyCommand.Floor6, PartyCommand.Floor7,
        PartyCommand.MasterMode1, PartyCommand.MasterMode2, PartyCommand.MasterMode3, PartyCommand.MasterMode4,
        PartyCommand.MasterMode5, PartyCommand.MasterMode6, PartyCommand.MasterMode7 -> "Join"
    }

    val messageText: String = when (partyCommand) {
        PartyCommand.Warp -> "Warp your party?"
        PartyCommand.PartyTransfer -> "Transfer the party to $player?"
        PartyCommand.AllInvite -> "Toggle party allinvite?"
        PartyCommand.Floor1 -> enterDungeons(1)
        PartyCommand.Floor2 -> enterDungeons(2)
        PartyCommand.Floor3 -> enterDungeons(3)
        PartyCommand.Floor4 -> enterDungeons(4)
        PartyCommand.Floor5 -> enterDungeons(5)
        PartyCommand.Floor6 -> enterDungeons(6)
        PartyCommand.Floor7 -> enterDungeons(7)
        PartyCommand.MasterMode1 -> enterMaster(1)
        PartyCommand.MasterMode2 -> enterMaster(2)
        PartyCommand.MasterMode3 -> enterMaster(3)
        PartyCommand.MasterMode4 -> enterMaster(4)
        PartyCommand.MasterMode5 -> enterMaster(5)
        PartyCommand.MasterMode6 -> enterMaster(6)
        PartyCommand.MasterMode7 -> enterMaster(7)
    }

    override fun accept() {
        if(partyCommand == PartyCommand.PartyTransfer && player == Minecraft.getInstance().player?.name?.string) {
            Minecraft.getInstance().sendMessage(Component.literal("§cWhy would you think that's possible, mr. smarty pants?"))
            return
        }

        Minecraft.getInstance().execute {
            Minecraft.getInstance().player?.connection?.sendCommand(ingameCommand)
        }
    }

    override val borderColor: Color get() = Color(OverlayCategory.partyColor)
    override val message = Component.literal(messageText)

    override val firstText: String
        get() {
            return "[${acceptKey()}] $buttonText"
        }

    enum class PartyCommand(val commands: List<String>) {
        Warp("warp", "w"),
        PartyTransfer("ptme", "transfer"),
        AllInvite("allinvite", "allinv"),
        Floor1("floor1", "f1"),
        Floor2("floor2", "f2"),
        Floor3("floor3", "f3"),
        Floor4("floor4", "f4"),
        Floor5("floor5", "f5"),
        Floor6("floor6", "f6"),
        Floor7("floor7", "f7"),
        MasterMode1("master1", "mm1", "m1"),
        MasterMode2("master2", "mm2", "m2"),
        MasterMode3("master3", "mm3", "m3"),
        MasterMode4("master4", "mm4", "m4"),
        MasterMode5("master5", "mm5", "m5"),
        MasterMode6("master6", "mm6", "m6"),
        MasterMode7("master7", "mm7", "m7");

        constructor(vararg commands: String) : this(commands.toList())

        companion object {
            fun getCommand(command: String) = entries.firstOrNull { partyCommand ->
                partyCommand.commands.any { it == command.lowercase() }
            }
        }
    }

    companion object {
        fun join(instance: String) = "joininstance $instance"
        fun dungeons(floor: String) = join("catacombs_floor_$floor")
        fun master(floor: String) = join("master_catacombs_floor_$floor")
        fun enterDungeons(floor: Int) = "Enter Floor $floor?"
        fun enterMaster(floor: Int) = "Enter Master Mode $floor?"
    }
}