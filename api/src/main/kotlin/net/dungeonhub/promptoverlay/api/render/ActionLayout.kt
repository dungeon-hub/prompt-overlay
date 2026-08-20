package net.dungeonhub.promptoverlay.api.render

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor

internal object ActionLayout {
    private const val ACTION_SPACING = 20
    private const val LINE_SPACING = 4
    private const val TEXT_COLOR = -1

    fun height(lineCount: Int): Int {
        return lineCount * (Minecraft.getInstance().font.lineHeight + LINE_SPACING)
    }

    fun width(font: Font, actions: List<String>): Int = rows(actions).maxOf { row ->
        row.sumOf(font::width) + ACTION_SPACING * (row.size - 1)
    }

    fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, actions: List<String>) {
        val font = Minecraft.getInstance().font

        rows(actions).forEachIndexed { rowIndex, row ->
            val rowWidth = row.sumOf(font::width) + ACTION_SPACING * (row.size - 1)
            var actionX = x + (width - rowWidth) / 2
            val actionY = y + rowIndex * (font.lineHeight + LINE_SPACING)

            row.forEach { text ->
                graphics.text(font, text, actionX, actionY, TEXT_COLOR)
                actionX += font.width(text) + ACTION_SPACING
            }
        }
    }

    private fun rows(actions: List<String>) = actions.chunked(2)
}
