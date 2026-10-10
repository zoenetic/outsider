// Builds burn textures: every pixel fades through a set's keyframe colours, each change over its
// own window of the burn, so pixels char one by one rather than all at once.
//
// Each set in SETS gives its keyframes (first = fresh), where each pixel's windows come from, and
// which frames to write:
// - painted: the windows are hand-painted. Schedule images show every pixel in its fresh colour
//   until a hand-picked step, then in the next keyframe's colour; each switch becomes a fade over
//   the `fade` steps leading up to it, so a pixel arrives at the same step as painted.
// - topDown: only pixels that differ between the two keyframes change; their windows start
//   top row first (ties shuffled by a fixed seed, so a row doesn't char left to right) and overlap.
//
//     kotlin scripts/make_burn_textures.main.kts            # every set
//     kotlin scripts/make_burn_textures.main.kts torch      # just the named sets
//
// Re-run make_ember_textures after changing the campfire. Output is deterministic.

@file:Import("lib/common.main.kts")

import java.awt.image.BufferedImage
import java.io.File
import java.util.Base64
import kotlin.system.exitProcess

val ROOT: File = __FILE__.canonicalFile.parentFile.parentFile
val TEXTURES = ROOT.resolve("survival/common/src/main/resources/assets/outsider_survival/textures/block")
val CAMPFIRE = ROOT.resolve("blockbench/campfire")
val TORCH = ROOT.resolve("blockbench/torch")

/** One change of colour, over [start, start + length) in a set's own time units. */
class Window(val start: Double, val length: Double) {
    fun progress(time: Double): Double =
        if (length == 0.0) (if (time >= start) 1.0 else 0.0) else clamp((time - start) / length)
}

/** Per pixel (row by row), one window per keyframe change; null where the pixel stays put. */
typealias Schedule = List<List<Window?>>

/** A texture to write: the burn [time] to render at, and what to do with the image. */
class Frame(val time: Double, val write: (BufferedImage) -> Unit)

class BurnSet(
    val name: String,
    val keyframes: () -> List<BufferedImage>,
    val schedule: (List<BufferedImage>) -> Schedule,
    val frames: List<Frame>,
)

/** Windows from schedule images, one per step; time = step index. */
fun painted(steps: () -> List<BufferedImage>, fade: Int): (List<BufferedImage>) -> Schedule = { keyframes ->
    val sequences = steps().map { it.pixels() }
    val colours = keyframes.map { it.pixels() }
    colours[0].indices.map { p ->
        val sequence = sequences.map { it[p] }
        var done: Int? = 0
        (1 until colours.size).map { k ->
            val previous = done ?: return@map null
            done = when {
                colours[k][p].contentEquals(colours[k - 1][p]) -> previous
                else -> sequence.indices.firstOrNull {
                    it >= (if (k == 1) 1 else previous) && sequence[it].contentEquals(colours[k][p])
                }
            }
            val arrives = done ?: return@map null
            if (k > 1 && arrives == previous) return@map null
            val start = maxOf(if (k == 1) 0 else previous, arrives - fade)
            Window(start.toDouble(), (arrives - start).toDouble())
        }
    }
}

/** Windows of length [fade] starting top row first; time runs 0 (fresh) to 1 (spent). */
fun topDown(fade: Double): (List<BufferedImage>) -> Schedule = { (fresh, spent) ->
    val changed = (0 until fresh.height).flatMap { y ->
        (0 until fresh.width).filter { x -> fresh.getRGB(x, y) != spent.getRGB(x, y) }.map { x -> x to y }
    }
    val jitter = changed.associateWith { (x, y) -> PyRandom("$x:$y").random() }
    val ordered = changed.sortedWith(compareBy({ it.second }, { jitter.getValue(it) }))
    val last = maxOf(ordered.size - 1, 1)
    val starts = ordered.withIndex().associate { (i, p) -> p to (1 - fade) * i / last }
    (0 until fresh.height).flatMap { y ->
        (0 until fresh.width).map { x -> listOf(starts[x to y]?.let { Window(it, fade) }) }
    }
}

fun render(keyframes: List<BufferedImage>, schedule: Schedule, time: Double): BufferedImage {
    val colours = keyframes.map { it.pixels() }
    val (width, height) = keyframes[0].width to keyframes[0].height
    return newImage(width, height).also { image ->
        for ((p, windows) in schedule.withIndex()) {
            var colour = colours[0][p]
            for ((change, window) in windows.withIndex()) {
                if (window != null) colour = lerp(colour, colours[change + 1][p], window.progress(time))
            }
            image.set(p % width, p / width, colour)
        }
    }
}

// --- sets ----------------------------------------------------------------------------------------

fun campfire(name: String) = readImage(CAMPFIRE.resolve(name))

fun embeddedTexture(bbmodel: File): BufferedImage {
    val source = parseJson(bbmodel.readText()).obj().getValue("textures").list()[0].obj()["source"] as String
    return readImage(Base64.getDecoder().decode(source.substringAfter(",")))
}

val TORCH_HEAD = 7 to 6 // top-left of the 2x2 head, which is also the top face
val TORCH_FLAME = listOf(
    listOf(intArrayOf(255, 246, 178, 255), intArrayOf(255, 222, 104, 255)),
    listOf(intArrayOf(255, 196, 64, 255), intArrayOf(238, 142, 34, 255)),
)

fun lit(torch: BufferedImage): BufferedImage = torch.copy().also { lit ->
    for ((dy, row) in TORCH_FLAME.withIndex()) {
        for ((dx, colour) in row.withIndex()) lit.set(TORCH_HEAD.first + dx, TORCH_HEAD.second + dy, colour)
    }
}

fun save(name: String): (BufferedImage) -> Unit = { image ->
    image.save(TEXTURES.resolve(name))
    println("wrote $name")
}

val SETS = listOf(
    // Logs go fresh -> half burnt -> burnt; campfire_fuel_15 is step 00.
    BurnSet(
        "campfire",
        keyframes = {
            listOf("burn_steps/campfire_log_burn_00.png", "campfire_log_half_burnt.png", "campfire_log_burnt.png")
                .map(::campfire)
        },
        schedule = painted({ (0..15).map { campfire("burn_steps/campfire_log_burn_%02d.png".format(it)) } }, fade = 3),
        frames = (0..15).map { step -> Frame(step.toDouble(), save("campfire_fuel_${15 - step}.png")) },
    ),
    // Vanilla torch layout (sides [7, 6, 9, 16], top [7, 6, 9, 8]); torch_*_7 is fresh, _0 spent.
    // Lit stages are the unlit ones with a flame on the head.
    BurnSet(
        "torch",
        keyframes = { listOf(embeddedTexture(TORCH.resolve("fresh_torch.bbmodel")), embeddedTexture(TORCH.resolve("dead_torch.bbmodel"))) },
        schedule = topDown(fade = 0.35),
        frames = (0..7).map { stage ->
            Frame((7 - stage) / 7.0) { unlit ->
                save("torch_unlit_$stage.png")(unlit)
                save("torch_lit_$stage.png")(lit(unlit))
            }
        },
    ),
)

val unknown = args.toSet() - SETS.map { it.name }.toSet()
if (unknown.isNotEmpty()) {
    System.err.println("unknown set ${unknown.first()}; known: ${SETS.joinToString { it.name }}")
    exitProcess(1)
}
for (set in SETS.filter { args.isEmpty() || it.name in args }) {
    val keyframes = set.keyframes()
    check(keyframes.all { it.width == keyframes[0].width && it.height == keyframes[0].height }) {
        "${set.name}: keyframes must be the same size"
    }
    val schedule = set.schedule(keyframes)
    for (frame in set.frames) frame.write(render(keyframes, schedule, frame.time))
}
