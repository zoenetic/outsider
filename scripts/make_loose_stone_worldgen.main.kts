// Writes the loose stone worldgen data: one group of features per set of biomes.
//
// Each group in GROUPS gets a configured feature (the weighted mix of stone types and counts), placed
// features for land (scatter + near plants) and/or underwater, a biome tag
// (#outsider_survival:has_loose_<group>_stones) and a NeoForge biome modifier. Fabric attaches the
// features in code, from the table in FabricBiomeModifications.kt; the script warns if a group is
// missing there. Also writes the block tags the placements test against.
//
// Output is plain JSON in src/main/resources, so hand edits work too, but the next run overwrites
// them: change the tables here instead. Only files whose content changes are rewritten.
//
//     kotlin scripts/make_loose_stone_worldgen.main.kts           # write
//     kotlin scripts/make_loose_stone_worldgen.main.kts --check   # list files that would change

@file:Import("lib/common.main.kts")

import java.io.File

val ROOT: File = __FILE__.canonicalFile.parentFile.parentFile
val NS = "outsider_survival"
val DATA = ROOT.resolve("survival/common/src/main/resources/data/$NS")
val MODIFIERS = ROOT.resolve("survival/neoforge/src/main/resources/data/$NS/neoforge/biome_modifier")
val FABRIC = ROOT.resolve("survival/fabric/src/main/kotlin/dev/zoenetic/outsider/survival/fabric/FabricBiomeModifications.kt")

// Stones per cluster -> weight, shared by every type in a mix.
val COUNTS = listOf(1 to 3, 2 to 5, 3 to 3, 4 to 1)

fun optional(tag: String) = mapOf("id" to tag, "required" to false)

val SOIL = "minecraft:substrate_overworld"
val ROCKY = "$NS:loose_stone_rocky_ground"
val DESERT = "$NS:loose_stone_desert_ground"
val BADLANDS = "$NS:loose_stone_badlands_ground"
val BEACH = "$NS:loose_stone_beach_ground"
val GREEN = "$NS:loose_stone_companions"
val ARID = "$NS:loose_stone_arid_companions"
val UNDERWATER = "$NS:loose_stone_underwater_companions"

val BLOCK_TAGS = mapOf(
    "loose_stone_rocky_ground" to listOf("#minecraft:substrate_overworld", "#minecraft:base_stone_overworld", "minecraft:gravel"),
    "loose_stone_desert_ground" to listOf("minecraft:sand", "minecraft:sandstone"),
    "loose_stone_badlands_ground" to listOf(
        "minecraft:red_sand", "minecraft:red_sandstone", "#minecraft:terracotta", "#minecraft:substrate_overworld",
    ),
    "loose_stone_beach_ground" to listOf("minecraft:sand", "minecraft:gravel", "#minecraft:base_stone_overworld"),
    "loose_stone_arid_companions" to listOf(
        "minecraft:dead_bush", "minecraft:cactus", "minecraft:short_dry_grass", "minecraft:tall_dry_grass",
    ),
)

/** Attempts per chunk: the even scatter, and the extra attempts next to companion plants. */
data class Attempts(val scatter: Int, val nearPlants: Int)

data class Group(
    val name: String,
    val mix: List<Pair<String, Int>>, // stone type to relative weight
    val biomes: List<Any>, // biome tag values
    val ground: String? = null, // block tag the land stones sit on
    val land: Attempts? = null,
    val water: Attempts? = null, // near seagrass/kelp instead of plants
    val companions: String = GREEN,
)

val TEMPERATE_MIX = listOf("stone" to 27, "andesite" to 2, "diorite" to 2, "granite" to 2)

val GROUPS = listOf(
    Group(
        "temperate", TEMPERATE_MIX,
        listOf(
            "#minecraft:is_forest", "#minecraft:is_taiga", "#minecraft:is_jungle", "#minecraft:is_savanna",
            "minecraft:plains", "minecraft:sunflower_plains", "minecraft:meadow", "minecraft:cherry_grove",
            "minecraft:swamp", "minecraft:mangrove_swamp", "minecraft:snowy_plains",
            optional("#c:is_plains"), optional("#c:is_swamp"),
        ),
        ground = SOIL, land = Attempts(2, 4), water = Attempts(4, 8),
    ),
    Group(
        "mountain", listOf("stone" to 9, "andesite" to 4, "diorite" to 4, "granite" to 4),
        listOf("#minecraft:is_hill", "minecraft:stony_shore", optional("#c:is_windswept"), optional("#c:is_stony_shores")),
        ground = ROCKY, land = Attempts(2, 4),
    ),
    Group(
        "peak", listOf("stone" to 9, "calcite" to 2),
        listOf("minecraft:stony_peaks", "minecraft:jagged_peaks"),
        ground = ROCKY, land = Attempts(2, 4),
    ),
    Group(
        "desert", listOf("sandstone" to 18, "stone" to 1),
        listOf("minecraft:desert", optional("#c:is_desert")),
        ground = DESERT, land = Attempts(1, 2), companions = ARID,
    ),
    Group(
        "badlands", listOf("red_sandstone" to 17, "sandstone" to 3),
        listOf("#minecraft:is_badlands", optional("#c:is_badlands")),
        ground = BADLANDS, land = Attempts(1, 2), companions = ARID,
    ),
    Group(
        "beach",
        listOf("stone" to 1) + listOf("granite", "diorite", "andesite", "calcite", "sandstone", "red_sandstone").map { it to 2 },
        listOf("#minecraft:is_beach", optional("#c:is_beach")),
        ground = BEACH, land = Attempts(1, 2),
    ),
    Group(
        "ocean", TEMPERATE_MIX,
        listOf(
            "#minecraft:is_river", "minecraft:ocean", "minecraft:cold_ocean", "minecraft:frozen_ocean",
            "minecraft:deep_ocean", "minecraft:deep_cold_ocean", "minecraft:deep_frozen_ocean",
            optional("#c:is_river"),
        ),
        water = Attempts(2, 4),
    ),
    Group(
        "warm_ocean", listOf("sandstone" to 18, "stone" to 1),
        listOf("minecraft:warm_ocean", "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean"),
        water = Attempts(2, 4),
    ),
)

val AIR = mapOf("type" to "minecraft:matching_blocks", "blocks" to "minecraft:air")
val WATER = mapOf("type" to "minecraft:matching_fluids", "fluids" to "minecraft:water")

fun stones(mix: List<Pair<String, Int>>, waterlogged: Boolean) = mapOf(
    "type" to "minecraft:simple_block",
    "config" to mapOf(
        "to_place" to mapOf(
            "type" to "minecraft:weighted_state_provider",
            "entries" to mix.flatMap { (stone, share) ->
                COUNTS.map { (count, weight) ->
                    mapOf(
                        "data" to mapOf(
                            "Name" to "$NS:loose_$stone",
                            "Properties" to mapOf("stones" to "$count", "waterlogged" to "$waterlogged"),
                        ),
                        "weight" to weight * share,
                    )
                }
            },
        ),
    ),
)

fun on(tag: String) = mapOf("type" to "minecraft:matching_block_tag", "tag" to tag, "offset" to listOf(0, -1, 0))

fun nextTo(tag: String) = mapOf(
    "type" to "minecraft:any_of",
    "predicates" to listOf(listOf(1, 0, 0), listOf(-1, 0, 0), listOf(0, 0, 1), listOf(0, 0, -1)).map {
        mapOf("type" to "minecraft:matching_block_tag", "tag" to tag, "offset" to it)
    },
)

fun placed(feature: String, count: Int, heightmap: String, predicates: List<Any>) = mapOf(
    "feature" to "$NS:$feature",
    "placement" to listOf(
        mapOf("type" to "minecraft:count", "count" to count),
        mapOf("type" to "minecraft:in_square"),
        mapOf("type" to "minecraft:heightmap", "heightmap" to heightmap),
        mapOf(
            "type" to "minecraft:block_predicate_filter",
            "predicate" to mapOf("type" to "minecraft:all_of", "predicates" to predicates),
        ),
        mapOf("type" to "minecraft:biome"),
    ),
)

/** A group's features, biome tag and biome modifier, as (file, JSON). */
fun groupFiles(group: Group): List<Pair<File, Any>> {
    val g = "loose_${group.name}_stones"
    val files = mutableListOf<Pair<File, Any>>()
    val features = mutableListOf<String>()
    group.land?.let { (scatter, near) ->
        val ground = on(group.ground!!)
        files += DATA.resolve("worldgen/configured_feature/$g.json") to stones(group.mix, false)
        files += DATA.resolve("worldgen/placed_feature/${g}_scatter.json") to
            placed(g, scatter, "WORLD_SURFACE", listOf(AIR, ground))
        files += DATA.resolve("worldgen/placed_feature/${g}_near_plants.json") to
            placed(g, near, "WORLD_SURFACE", listOf(nextTo(group.companions), AIR, ground))
        features += listOf("${g}_scatter", "${g}_near_plants")
    }
    group.water?.let { (scatter, near) ->
        files += DATA.resolve("worldgen/configured_feature/${g}_waterlogged.json") to stones(group.mix, true)
        files += DATA.resolve("worldgen/placed_feature/${g}_underwater_scatter.json") to
            placed("${g}_waterlogged", scatter, "OCEAN_FLOOR", listOf(WATER))
        files += DATA.resolve("worldgen/placed_feature/${g}_underwater_near_plants.json") to
            placed("${g}_waterlogged", near, "OCEAN_FLOOR", listOf(nextTo(UNDERWATER), WATER))
        features += listOf("${g}_underwater_scatter", "${g}_underwater_near_plants")
    }
    files += DATA.resolve("tags/worldgen/biome/has_$g.json") to mapOf("values" to group.biomes)
    files += MODIFIERS.resolve("$g.json") to mapOf(
        "type" to "neoforge:add_features",
        "biomes" to "#$NS:has_$g",
        "features" to features.map { "$NS:$it" },
        "step" to "vegetal_decoration",
    )
    val fabric = FABRIC.readText()
    val missing = (listOf("has_$g") + features).filter { "\"$it\"" !in fabric }
    if (missing.isNotEmpty()) System.err.println("warning: FabricBiomeModifications.kt is missing ${missing.joinToString()}")
    return files
}

val check = "--check" in args
val files = BLOCK_TAGS.map { (name, values) -> DATA.resolve("tags/block/$name.json") to mapOf("values" to values) } +
    GROUPS.flatMap(::groupFiles)
for ((file, json) in files) {
    val text = pyJson(json)
    if (file.exists() && file.readText() == text) continue
    println("${if (check) "would write" else "wrote"} ${file.relativeTo(ROOT)}")
    if (!check) {
        file.parentFile.mkdirs()
        file.writeText(text)
    }
}
