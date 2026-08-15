package net.dungeonhub.promptoverlay.enums

/** Optional glow treatments drawn around the prompt frame. */
enum class GlowStyle {
    None,
    /** A restrained, constant glow in the prompt's selected color. */
    Soft,
    Strong,
    /** A brighter glow whose intensity slowly breathes. */
    Pulse
}
