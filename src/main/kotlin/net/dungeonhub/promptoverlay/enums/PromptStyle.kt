package net.dungeonhub.promptoverlay.enums

enum class PromptStyle(val supportsWrappedProgress: Boolean) {
    Default(true),
    Rounded(true),
    CornerAccents,
    DoubleBorder(true),
    SideRails;

    constructor(): this(false)
}