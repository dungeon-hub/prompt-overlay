package net.dungeonhub.promptoverlay.server

sealed interface Server {
    data object UnknownServer : Server
}