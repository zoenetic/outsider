// TODO(zoe): placeholder art. Redraw the interim stages (0–2) by hand, and make the end state
//  (stage 3) more fiery, then put them in art/items.png, slice them, and drop this script from
//  the textures:items task.
//
// Builds the smouldering tinder textures from our own tinder textures.
//
// The lit tinder is the unlit one with a burning core. That core (the pixels that differ between
// tinder_lit.png and tinder_unlit.png) is where an ember sits, so each stage lights part of it,
// growing from its centre: tinder_ember_0.png is a faint red spot, tinder_ember_3.png is nearly
// alight. Only our textures are used as input, so the output is original. Re-run after editing
// the tinder textures; output is deterministic.

@file:Import("lib/common.main.kts")

import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.hypot
import kotlin.math.pow

val ROOT: File = __FILE__.canonicalFile.parentFile.parentFile
val TEXTURES = ROOT.resolve("survival/common/src/main/resources/assets/outsider_survival/textures/item")

// how much of the core each stage lights (fraction of its radius) and how hot it burns
val STAGES = listOf(0.4 to 0.35, 0.6 to 0.55, 0.85 to 0.8, 1.0 to 1.0)

val unlit = readImage(TEXTURES.resolve("tinder_unlit.png"))
val lit = readImage(TEXTURES.resolve("tinder_lit.png"))

val core: List<Pair<Int, Int>> = (0 until unlit.height).flatMap { y ->
    (0 until unlit.width).map { x -> x to y }
}.filter { (x, y) -> !unlit.rgba(x, y).contentEquals(lit.rgba(x, y)) }

val centreX = core.map { it.first }.average()
val centreY = core.map { it.second }.average()
val radius = core.maxOf { (x, y) -> hypot(x - centreX, y - centreY) }

fun luminance(c: IntArray): Double = 0.3 * c[0] + 0.59 * c[1] + 0.11 * c[2]

// The lit tinder's own fire colours, coolest first, so every stage is drawn in its palette and
// the last one burns like it.
val FIRE: List<IntArray> = core.map { (x, y) -> lit.rgba(x, y) }
    .filter { it[3] > 0 && it[0] >= 150 && it[0] >= it[1] && it[1] >= it[2] }
    .distinctBy { it.toList() }
    .sortedBy(::luminance)

// below this, a pixel the ember has reached only singes
val GLOWS_FROM = 0.18
val SINGE = 0.85

// The same flicker per pixel at every stage, so the glow grows rather than reshuffling.
fun speck(x: Int, y: Int): Double = 0.55 + 0.45 * PyRandom("$x:$y").random()

fun stage(reach: Double, heat: Double): BufferedImage = unlit.copy().also { out ->
    for ((x, y) in core) {
        val distance = hypot(x - centreX, y - centreY) / radius
        if (distance > reach) continue
        val glow = heat * (1 - distance / reach).pow(0.5) * speck(x, y)
        val (r, g, b, a) = unlit.rgba(x, y)
        if (glow < GLOWS_FROM) {
            out.set(x, y, intArrayOf(pyRound(r * SINGE), pyRound(g * SINGE), pyRound(b * SINGE), a))
            continue
        }
        val shade = ((glow - GLOWS_FROM) / (1 - GLOWS_FROM) * FIRE.size).toInt()
        val (fr, fg, fb) = FIRE[shade.coerceIn(0, FIRE.size - 1)]
        out.set(x, y, intArrayOf(fr, fg, fb, a))
    }
}

for ((index, stage) in STAGES.withIndex()) {
    val (reach, heat) = stage
    stage(reach, heat).save(TEXTURES.resolve("tinder_ember_$index.png"))
    println("tinder_ember_$index.png")
}
