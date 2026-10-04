package net.dungeonhub.promptoverlay.api

import net.minecraft.network.chat.Component

data class PromptSource @JvmOverloads constructor(
    val namespace: String,
    val displayName: Component? = null,
) {
    companion object {
        private val VALID_NAMESPACE = Regex("[a-z][a-z0-9_-]{1,63}")

        @JvmStatic fun isValid(namespace: String): Boolean = VALID_NAMESPACE.matches(namespace)
    }
}
