package net.dungeonhub.promptoverlay.util

import net.dungeonhub.promptoverlay.server.KnownServer
import net.dungeonhub.promptoverlay.server.Server
import net.minecraft.client.Minecraft

object GameUtil {
    fun getCurrentServer(): Server {
        val ip = Minecraft.getInstance().currentServer?.ip?.lowercase() ?: return Server.UnknownServer

        return KnownServer.entries.firstOrNull { it.regex.matches(ip) } ?: Server.UnknownServer
    }

    fun isOnHypixel(): Boolean {
        return getCurrentServer() == KnownServer.Hypixel
    }

    fun isInSkyblock(): Boolean {
        return isOnHypixel() // TODO check if the user is in skyblock is still missing, but also atm not needed
    }
}