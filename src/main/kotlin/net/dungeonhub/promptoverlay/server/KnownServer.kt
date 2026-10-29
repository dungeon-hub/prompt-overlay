package net.dungeonhub.promptoverlay.server

enum class KnownServer(val regex: Regex) : Server {
    Hypixel(Regex("(\\w*\\.)?hypixel\\.net(:25565)?")),
    CubeCraft(Regex("(\\w*\\.)?cubecraft\\.net(:25565)?")),
}