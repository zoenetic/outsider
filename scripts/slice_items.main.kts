// Slices art/items.png into the mod's item textures.
//
// The sheet is a grid of 16x16 cells read left to right, top to bottom. SHEET lists the texture
// name for each cell in that order; null leaves a cell unused. A name listed for several cells is
// an animation: its cells become frames, stacked top to bottom in sheet order, for a .png.mcmeta
// to animate. Output goes to textures/item/<name>.png. Re-run after editing the sheet; existing
// textures are overwritten.
//

@file:Import("lib/common.main.kts")

import java.awt.image.BufferedImage
import java.io.File
import kotlin.system.exitProcess

val root: File = __FILE__.canonicalFile.parentFile.parentFile
val sheetPath = root.resolve("art/items.png")
val textures = root.resolve(
    "survival/common/src/main/resources/assets/outsider_survival/textures/item",
)
val cellSize = 16

val items: List<String?> = listOf(
    "loose_diorite",
    "loose_andesite",
    "loose_granite",
    "loose_stone",
    "loose_deepslate",
    "loose_tuff",
    "loose_sandstone",
    "loose_red_sandstone",
    "loose_basalt",
    "loose_blackstone",
    "loose_calcite",
    "loose_end_stone",
    null,
    null,
    null,
    null,
    "hand_drill",
    "hand_drill_hearth_board",
    "hand_drill_hearth_board_ember",
    "hand_drill_hearth_board_ember",
    "hand_drill_hearth_board_ember",
    "hand_drill_hearth_board_ember",
    "tinder_unlit",
    "tinder_lit",
    "tinder_lit",
    "tinder_lit",
    "tinder_lit",
)

val sheet = readImage(sheetPath)
val columns = sheet.width / cellSize
val cells: Map<String, List<Int>> = items.withIndex()
    .filter { it.value != null }
    .groupBy({ it.value!! }, { it.index })

fun cell(name: String, index: Int): BufferedImage {
    val (x, y) = index % columns * cellSize to index / columns * cellSize
    val cell = sheet.getSubimage(x, y, cellSize, cellSize).copy()
    if (cell.pixels().all { it[3] == 0 }) {
        System.err.println("$name: cell $index is empty")
        exitProcess(1)
    }
    return cell
}

for ((name, indices) in cells) {
    val frames = indices.map { cell(name, it) }
    val texture =
        frames.singleOrNull() ?: newImage(cellSize, cellSize * frames.size).also { strip ->
            val graphics = strip.createGraphics()
            frames.forEachIndexed { frame, image ->
                graphics.drawImage(image, 0, frame * cellSize, null)
            }
            graphics.dispose()
        }
    texture.save(textures.resolve("$name.png"))
    println("$name.png")
}
