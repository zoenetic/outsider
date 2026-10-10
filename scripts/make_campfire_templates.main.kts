// Builds the campfire geometry templates from one base model.
//
// Input: blockbench/campfire/template_campfire_base.json (full-size logs, fire planes,
// top/bottom/ember_* slots).
// Output: template_campfire_{lit,unlit}_{stage}.json in the mod's block models, one per burn stage.
//
// Stages follow the Blockbench files: a tier "sinks" (loses height, settles into the tier below)
// once it is mostly charred. Fire size is tied to the stage. Unlit variants drop the fire planes
// and show plain log faces where the lit ones show embers. Re-run after editing the base.

@file:Import("lib/common.main.kts")

import java.io.File

val ROOT: File = __FILE__.canonicalFile.parentFile.parentFile
val BASE = ROOT.resolve("blockbench/campfire/template_campfire_base.json")
val OUT = ROOT.resolve("survival/common/src/main/resources/assets/outsider_survival/models/block")

/** Bottom logs y-range, top logs y-range, fire scale; per Blockbench on_4 / on_1 / on_0. */
data class Stage(val bottom: IntRange, val top: IntRange, val fireScale: Double)

val STAGES = linkedMapOf(
    "full" to Stage(0..4, 3..7, 1.0),
    "top_sunk" to Stage(0..4, 3..6, 0.5),
    "sunk" to Stage(0..3, 2..5, 0.25),
)
val SIDES = setOf("north", "south", "east", "west")
val FIRE_HALF_WIDTH = 7.2
val FIRE_BASE = 1.0
val FIRE_HEIGHT = 16.0

fun isFire(element: Map<String, Any?>): Boolean =
    element.getValue("faces").obj().values.map { it.obj()["texture"] }.toSet() == setOf("#fire")

fun tier(element: Map<String, Any?>): String = if (element["from"].list()[1] == 0.0) "bottom" else "top"

fun resizeLog(element: MutableMap<String, Any?>, yRange: IntRange) {
    val (low, high) = yRange.first.toDouble() to yRange.last.toDouble()
    element["from"].list()[1] = low
    element["to"].list()[1] = high
    for ((direction, face) in element.getValue("faces").obj()) {
        if (direction !in SIDES) continue
        val (u0, v0, u1) = face.obj()["uv"].list().map { it.num() }
        face.obj()["uv"] = mutableListOf(u0, v0, u1, v0 + (high - low))
    }
}

fun scaleFire(element: MutableMap<String, Any?>, scale: Double) {
    val (lo, hi) = 8 - FIRE_HALF_WIDTH * scale to 8 + FIRE_HALF_WIDTH * scale
    val top = FIRE_BASE + FIRE_HEIGHT * scale
    val from = element["from"].list()
    val to = element["to"].list()
    val runsAlongX = from[2] == to[2]
    val axis = if (runsAlongX) 0 else 2
    from[axis] = lo
    to[axis] = hi
    from[1] = FIRE_BASE
    to[1] = top
}

/** Whole numbers as integers, others to 2 decimals, like the hand-written models. */
fun tidy(value: Any?): Any? = when (value) {
    is Double -> if (value == Math.floor(value)) value.toLong() else pyRound(value, 2)
    is List<*> -> value.map(::tidy)
    is Map<*, *> -> value.entries.associate { (k, v) -> k to tidy(v) }
    else -> value
}

fun build(base: Map<String, Any?>, stage: Stage, lit: Boolean): Any? {
    val model = deepCopy(base).toMutableMap()
    val elements = mutableListOf<Any?>()
    for (element in model.getValue("elements").list().map { it.obj() }) {
        if (isFire(element)) {
            if (lit) {
                scaleFire(element, stage.fireScale)
                elements += element
            }
            continue
        }
        resizeLog(element, if (tier(element) == "bottom") stage.bottom else stage.top)
        if (!lit) {
            for (face in element.getValue("faces").obj().values) {
                face.obj()["texture"] = (face.obj()["texture"] as String).replace("#ember_", "#")
            }
        }
        elements += element
    }
    model["elements"] = elements
    if (!lit) model["textures"]?.obj()?.remove("fire")
    return tidy(model)
}

val base = parseJson(BASE.readText()).obj()
for ((name, stage) in STAGES) {
    for (lit in listOf(true, false)) {
        val file = "template_campfire_${if (lit) "lit" else "unlit"}_$name.json"
        OUT.resolve(file).writeText(pyJson(build(base, stage, lit), indent = "\t"))
        println("wrote $file")
    }
}
