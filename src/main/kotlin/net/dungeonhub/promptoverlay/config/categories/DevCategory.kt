package net.dungeonhub.promptoverlay.config.categories

import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import net.dungeonhub.promptoverlay.config.Config

object DevCategory : CategoryKt("dev") {
    override val name: TranslatableValue
        get() = Literal("Developer")

    override val hidden: Boolean
        get() = !Config.developer

    var extendedDebug by boolean("extended_debug", false) {
        name = Literal("Extended Debug output")
        description = Literal("This increases the amount of information sent to you ingame.")
    }

    var catchAllPrompts by boolean("catch_all", false) {
        name = Literal("Catch All")
        description = Literal("Experimental. Use at your own risk.\nThis catches all click events in messages and makes them into a default prompt.")
    }

    var catchAllIgnoreMessages by stringsWithId("catch_all_ignore") {
        name = Literal("Ignore Messages")
        description = Literal("These chat messages will be ignored for the Catch All prompts. Each line is their own filter.")
    }
}