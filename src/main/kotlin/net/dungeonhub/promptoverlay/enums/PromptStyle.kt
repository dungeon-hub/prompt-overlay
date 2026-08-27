package net.dungeonhub.promptoverlay.enums

enum class PromptStyle(val supportsWrappedProgress: Boolean = false) {
    Default(true),
    Rounded(true),
    CornerAccents,
    DoubleBorder(true),
    SideRails;
}