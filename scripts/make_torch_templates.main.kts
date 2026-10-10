// Builds the dead torch geometry templates from the Blockbench model.
//
// Input: blockbench/torch/dead_torch.bbmodel (a standing torch, texture slot 0 -> #torch).
// Output: template_torch_unlit_dead.json and template_wall_torch_unlit_dead.json in the mod's
// block models.
//
// The wall variant moves the standing geometry onto the wall and tilts it the way vanilla's wall
// torch does (base on the wall at x 0, raised 3.5, leaning 22.5 degrees out), so the stub keeps its
// base where a full wall torch's base would be. Re-run after editing the Blockbench model.

@file:Import("lib/common.main.kts")

import java.io.File

val ROOT: File = __FILE__.canonicalFile.parentFile.parentFile
val SOURCE = ROOT.resolve("blockbench/torch/dead_torch.bbmodel")
val OUT = ROOT.resolve("survival/common/src/main/resources/assets/outsider_survival/models/block")

val WALL_OFFSET = listOf(-8.0, 3.5, 0.0)
val WALL_ROTATION = mapOf("origin" to listOf(0.0, 3.5, 8.0), "axis" to "z", "angle" to -22.5)

/** Whole numbers as integers, like the hand-written models. */
fun tidy(value: Any?): Any? = when (value) {
    is Double -> if (value == Math.floor(value)) value.toLong() else value
    is List<*> -> value.map(::tidy)
    is Map<*, *> -> value.entries.associate { (k, v) -> k to tidy(v) }
    else -> value
}

fun element(cube: Map<String, Any?>): MutableMap<String, Any?> {
    val faces = LinkedHashMap<String, Any?>()
    for ((direction, face) in cube.getValue("faces").obj()) {
        if (face.obj()["texture"] == null) continue
        faces[direction] = linkedMapOf<String, Any?>("uv" to face.obj()["uv"], "texture" to "#torch").apply {
            if ("cullface" in face.obj()) put("cullface", face.obj()["cullface"])
        }
    }
    return linkedMapOf("from" to cube["from"], "to" to cube["to"], "faces" to faces)
}

fun standing(cubes: List<Map<String, Any?>>): MutableMap<String, Any?> = linkedMapOf(
    "ambientocclusion" to false,
    "textures" to linkedMapOf("particle" to "#torch"),
    "elements" to cubes.mapTo(mutableListOf()) { element(it) },
)

fun onWall(model: Map<String, Any?>): Map<String, Any?> {
    val wall = deepCopy(model)
    for (e in wall.getValue("elements").list().map { it.obj() }) {
        e["from"] = e["from"].list().zip(WALL_OFFSET) { v, d -> v.num() + d }
        e["to"] = e["to"].list().zip(WALL_OFFSET) { v, d -> v.num() + d }
        e["rotation"] = deepCopy(WALL_ROTATION)
        for (face in e.getValue("faces").obj().values) face.obj().remove("cullface")
    }
    return wall
}

val cubes = parseJson(SOURCE.readText()).obj().getValue("elements").list().map { it.obj() }
    .filter { it["export"] as Boolean? ?: true }
val model = standing(cubes)
for ((name, contents) in listOf(
    "template_torch_unlit_dead.json" to model,
    "template_wall_torch_unlit_dead.json" to onWall(model),
)) {
    OUT.resolve(name).writeText(pyJson(tidy(contents), indent = "\t"))
    println("wrote $name")
}
