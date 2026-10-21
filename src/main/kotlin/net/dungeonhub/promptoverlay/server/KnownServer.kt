package net.dungeonhub.promptoverlay.server

enum class KnownServer(val regex: Regex) : Server {
    Hypixel(Regex("(\\w*\\.)?hypixel\\.net")),
    CubeCraft(Regex("(\\w*\\.)?cubecraft\\.net")),
}