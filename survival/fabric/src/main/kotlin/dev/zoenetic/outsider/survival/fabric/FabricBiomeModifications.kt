package dev.zoenetic.outsider.survival.fabric

import dev.zoenetic.outsider.survival.Survival
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.level.levelgen.GenerationStep

private val LOOSE_STONE_GROUPS = listOf(
    "has_loose_temperate_stones" to listOf(
        "loose_temperate_stones_scatter",
        "loose_temperate_stones_near_plants",
        "loose_temperate_stones_underwater_scatter",
        "loose_temperate_stones_underwater_near_plants",
    ),
    "has_loose_mountain_stones" to listOf(
        "loose_mountain_stones_scatter",
        "loose_mountain_stones_near_plants",
    ),
    "has_loose_peak_stones" to listOf(
        "loose_peak_stones_scatter",
        "loose_peak_stones_near_plants",
    ),
    "has_loose_desert_stones" to listOf(
        "loose_desert_stones_scatter",
        "loose_desert_stones_near_plants",
    ),
    "has_loose_badlands_stones" to listOf(
        "loose_badlands_stones_scatter",
        "loose_badlands_stones_near_plants",
    ),
    "has_loose_beach_stones" to listOf(
        "loose_beach_stones_scatter",
        "loose_beach_stones_near_plants",
    ),
    "has_loose_ocean_stones" to listOf(
        "loose_ocean_stones_underwater_scatter",
        "loose_ocean_stones_underwater_near_plants",
    ),
    "has_loose_warm_ocean_stones" to listOf(
        "loose_warm_ocean_stones_underwater_scatter",
        "loose_warm_ocean_stones_underwater_near_plants",
    ),
)

public object FabricBiomeModifications {
    public fun init() {
        for ((biomes, features) in LOOSE_STONE_GROUPS) {
            val selector = BiomeSelectors.tag(TagKey.create(Registries.BIOME, id(biomes)))
            for (feature in features) {
                BiomeModifications.addFeature(
                    selector,
                    GenerationStep.Decoration.VEGETAL_DECORATION,
                    ResourceKey.create(Registries.PLACED_FEATURE, id(feature)),
                )
            }
        }
    }

    private fun id(path: String): Identifier =
        Identifier.fromNamespaceAndPath(Survival.NAMESPACE, path)
}
