package net.dungeonhub.promptoverlay.api

import net.dungeonhub.promptoverlay.api.render.Overlay

data class PromptRequest @JvmOverloads constructor(
    val overlay: Overlay,
    val source: PromptSource,
    val expiresAtEpochMillis: Long? = null,
    val displayDurationMillis: Long? = null,
    val listener: PromptLifecycleListener? = null,
)
