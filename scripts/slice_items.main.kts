// Slices art/items.png into the mod's item textures.
//
// The sheet is a grid of 16x16 cells read left to right, top to bottom. SHEET lists the texture
// name for each cell in that order; null leaves a cell unused. Output goes to
// textures/item/<name>.png. Re-run after editing the sheet; existing textures are overwritten.

@file:Import("lib/common.main.kts")

import java.io.File
import kotlin.system.exitProcess

val ROOT: File = __FILE__.canonicalFile.parentFile.parentFile
val SHEET_PATH = ROOT.resolve("art/items.png")
val TEXTURES = ROOT.resolve("survival/common/src/main/resources/assets/outsider_survival/textures/item")
val CELL = 16

val SHEET: List<String?> = listOf(
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
)

val sheet = readImage(SHEET_PATH)
val columns = sheet.width / CELL
for ((index, name) in SHEET.withIndex()) {
    if (name == null) continue
    val (x, y) = index % columns * CELL to index / columns * CELL
    val cell = sheet.getSubimage(x, y, CELL, CELL).copy()
    if (cell.pixels().all { it[3] == 0 }) {
        System.err.println("$name: cell $index is empty")
        exitProcess(1)
    }
    cell.save(TEXTURES.resolve("$name.png"))
    println("$name.png")
}
