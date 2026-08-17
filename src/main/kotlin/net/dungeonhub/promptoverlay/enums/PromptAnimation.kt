package net.dungeonhub.promptoverlay.enums

/** Entrance animations available to [net.dungeonhub.promptoverlay.feature.OverlayFeature]. */
enum class PromptAnimation {
    /** Original smooth drop from the top of the screen. */
    FlyIn,
    /** Drops in with a small, playful overshoot before settling. */
    Bounce,
    /** Shows the prompt immediately. */
    Appear
}
