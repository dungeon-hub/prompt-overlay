package net.dungeonhub.promptoverlay.api

import java.util.UUID

interface PromptHandle {
    val id: UUID
    val source: PromptSource
    fun state(): PromptLifecycleState
    fun update(update: PromptUpdate): Boolean
    fun cancel(): Boolean
}
