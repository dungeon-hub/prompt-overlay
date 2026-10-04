package net.dungeonhub.promptoverlay.api

import net.dungeonhub.promptoverlay.api.render.Overlay

/** A replacement update. Builders distinguish an unchanged value from clearing it. */
class PromptUpdate private constructor(
    val overlay: Overlay?,
    val expirationChanged: Boolean,
    val expiresAtEpochMillis: Long?,
    val displayDurationChanged: Boolean,
    val displayDurationMillis: Long?,
    val restartDisplayTimer: Boolean,
) {
    class Builder {
        private var overlay: Overlay? = null
        private var expirationChanged = false
        private var expiration: Long? = null
        private var durationChanged = false
        private var duration: Long? = null
        private var restart = false

        fun overlay(value: Overlay) = apply { overlay = value }
        fun expiresAtEpochMillis(value: Long?) = apply { expirationChanged = true; expiration = value }
        fun displayDurationMillis(value: Long?) = apply { durationChanged = true; duration = value }
        fun restartDisplayTimer(value: Boolean = true) = apply { restart = value }
        fun build() = PromptUpdate(overlay, expirationChanged, expiration, durationChanged, duration, restart)
    }

    companion object { @JvmStatic fun builder() = Builder() }
}
