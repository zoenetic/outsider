// Builds animated ember (lit_log) textures from our own campfire burn textures.
//
// For each fuel level N, reads campfire_fuel_N.png and writes campfire_ember_N.png: a vertical
// strip of FRAMES frames in which the charred pixels carry drifting orange/yellow glow spots,
// plus a .mcmeta that animates it the way vanilla's lit log does (interpolated, 20 ticks/frame).
//
// Only our textures are used as input, so the output is original. Re-run after changing the
// burn textures; output is deterministic.

@file:Import("lib/common.main.kts")

import java.awt.image.BufferedImage
import java.io.File

val ROOT: File = __FILE__.canonicalFile.parentFile.parentFile
val TEXTURES = ROOT.resolve("survival/common/src/main/resources/assets/outsider_survival/textures/block")
val LEVELS = 0..15
val FRAMES = 4
val FRAMETIME = 20

// glow ramp, from a faint ember to a bright spot
val RAMP = listOf(intArrayOf(110, 25, 5), intArrayOf(235, 105, 20), intArrayOf(255, 215, 90))

fun ramp(t: Double): IntArray {
    val scaled = clamp(t) * (RAMP.size - 1)
    val i = minOf(scaled.toInt(), RAMP.size - 2)
    return lerp(RAMP[i], RAMP[i + 1], scaled - i)
}

/** Bark is warm (red well above blue); char is grey-blue (blue at or above red). */
fun charredness(r: Int, g: Int, b: Int): Double = clamp((10 - (r - b)) / 30.0)

/** Darker char reads as a crack, where embers show most. */
fun crackDepth(r: Int, g: Int, b: Int): Double {
    val lum = 0.3 * r + 0.59 * g + 0.11 * b
    return clamp((90 - lum) / 70, 0.3, 1.0)
}

/** The same spot positions at every fuel level, so embers don't jump when the level drops. */
fun spot(frame: Int, x: Int, y: Int): Double = clamp((PyRandom("$frame:$x:$y").random() - 0.55) / 0.45)

fun emberFrame(base: BufferedImage, frame: Int): BufferedImage = base.copy().also { out ->
    for (y in 0 until base.height) {
        for (x in 0 until base.width) {
            val (r, g, b, a) = base.rgba(x, y)
            if (a == 0) continue
            val glow = charredness(r, g, b) * crackDepth(r, g, b) * spot(frame, x, y)
            if (glow <= 0) continue
            val (gr, gg, gb) = ramp(glow)
            val mix = clamp(glow * 1.4)
            out.set(x, y, intArrayOf(
                pyRound(r + (gr - r) * mix),
                pyRound(g + (gg - g) * mix),
                pyRound(b + (gb - b) * mix),
                a,
            ))
        }
    }
}

for (level in LEVELS) {
    val base = readImage(TEXTURES.resolve("campfire_fuel_$level.png"))
    val strip = newImage(base.width, base.height * FRAMES)
    for (frame in 0 until FRAMES) {
        val pixels = emberFrame(base, frame).getRGB(0, 0, base.width, base.height, null, 0, base.width)
        strip.setRGB(0, frame * base.height, base.width, base.height, pixels, 0, base.width)
    }
    val target = TEXTURES.resolve("campfire_ember_$level.png")
    strip.save(target)
    val meta = mapOf("animation" to mapOf("interpolate" to true, "frametime" to FRAMETIME))
    target.resolveSibling(target.name + ".mcmeta").writeText(pyJson(meta, indent = "  "))
    println("wrote ${target.name} ($FRAMES frames)")
}
