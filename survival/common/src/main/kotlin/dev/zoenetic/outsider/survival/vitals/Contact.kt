package dev.zoenetic.outsider.survival.vitals

import dev.zoenetic.outsider.survival.Survival.NAMESPACE
import dev.zoenetic.outsider.survival.conditions.PlayerConditions
import dev.zoenetic.outsider.survival.units.Conductance
import dev.zoenetic.outsider.survival.units.Heat
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

public val FREEZING: Heat = Heat(0.0)
public val MAGMA_TEMPERATURE: Heat = Heat(50.0)

public data class Contact(val temperature: Heat, val conductance: Conductance)

public fun List<Contact>.conductance(): Conductance =
    Conductance(sumOf { it.conductance.value })

public fun List<Contact>.environment(): Heat =
    Heat(sumOf { it.temperature.celsius * it.conductance.value } / conductance().value)

public enum class ConductiveMedium(public val conductance: Conductance) {
    RAIN(Conductance(1.5)),
    WATER(Conductance(3.0)),
    POWDER_SNOW(Conductance(8.0)),
    ;

    public fun temperature(air: Heat, ground: Heat): Heat = when (this) {
        RAIN -> air
        WATER -> ground.coerceAtLeast(FREEZING.celsius)
        POWDER_SNOW -> ground.coerceAtMost(FREEZING)
    }
}

public fun Player.getMediumIn(): ConductiveMedium? {
    if (this.isInWater) return ConductiveMedium.WATER
    if (this.isInPowderSnow) return ConductiveMedium.POWDER_SNOW
    if (this.isInWaterOrRain) return ConductiveMedium.RAIN
    return null
}

public val METAL: TagKey<Block> = TagKey.create(
    Registries.BLOCK,
    Identifier.fromNamespaceAndPath(NAMESPACE, "metal"),
)

public enum class ConductiveSurface(public val conductance: Conductance) {
    SNOW(Conductance(0.3)),
    STONE(Conductance(0.2)),
    METAL(Conductance(0.4)),
    ICE(Conductance(0.4)),
    MAGMA(Conductance(0.5)),
    ;

    public fun temperature(air: Heat, ground: Heat): Heat = when (this) {
        SNOW, ICE -> ground.coerceAtMost(FREEZING)
        STONE -> ground
        METAL -> air
        MAGMA -> MAGMA_TEMPERATURE
    }
}

public fun Player.getSurfaceOn(): ConductiveSurface? {
    val on = blockStateOn
    if (on.`is`(Blocks.MAGMA_BLOCK)) return ConductiveSurface.MAGMA
    if (on.`is`(BlockTags.SNOW)) return ConductiveSurface.SNOW
    if (on.`is`(BlockTags.BASE_STONE_OVERWORLD)) return ConductiveSurface.STONE
    if (on.`is`(METAL)) return ConductiveSurface.METAL
    if (on.`is`(BlockTags.ICE)) return ConductiveSurface.ICE
    return null
}

public fun Player.getContacts(conditions: PlayerConditions): List<Contact> {
    val air = conditions.ambient
    val ground = conditions.ground
    return listOfNotNull(
        Contact(conditions.feelsLike(), conditions.wind.conductance),
        getMediumIn()?.let { Contact(it.temperature(air, ground), it.conductance) },
        getSurfaceOn()?.let { Contact(it.temperature(air, ground), it.conductance) },
    )
}
