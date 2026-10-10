package dev.zoenetic.outsider.survival.vitals

import dev.zoenetic.outsider.survival.Survival.NAMESPACE
import dev.zoenetic.outsider.survival.units.Conductance
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

public enum class ConductiveMedium(public val conductance: Conductance) {
    RAIN(Conductance(5.0)),
    WATER(Conductance(25.0)),
    POWDER_SNOW(Conductance(50.0)),
    LAVA(Conductance(1000.0)),
}

public fun Player.getConductanceOfMediumIn(): Conductance? {
    if (this.isInLava) return ConductiveMedium.LAVA.conductance
    if (this.isInWater) return ConductiveMedium.WATER.conductance
    if (this.isInPowderSnow) return ConductiveMedium.POWDER_SNOW.conductance
    if (this.isInWaterOrRain) return ConductiveMedium.RAIN.conductance
    return null
}

public val METAL: TagKey<Block> = TagKey.create(
    Registries.BLOCK,
    Identifier.fromNamespaceAndPath(NAMESPACE, "metal"),
)

public enum class ConductiveSurface(public val conductance: Conductance) {
    SNOW(Conductance(1.2)),
    STONE(Conductance(1.5)),
    METAL(Conductance(2.0)),
    ICE(Conductance(2.5)),
    MAGMA(Conductance(3.0)),
}

public fun Player.getConductanceOfSurfaceOn(): Conductance? {
    val on = blockStateOn
    if (on.`is`(Blocks.MAGMA_BLOCK)) return ConductiveSurface.MAGMA.conductance
    if (on.`is`(BlockTags.SNOW)) return ConductiveSurface.SNOW.conductance
    if (on.`is`(BlockTags.BASE_STONE_OVERWORLD)) return ConductiveSurface.STONE.conductance
    if (on.`is`(METAL)) return ConductiveSurface.METAL.conductance
    if (on.`is`(BlockTags.ICE)) return ConductiveSurface.ICE.conductance
    return null
}
