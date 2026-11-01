package dev.zoenetic.outsider.survival.vitals

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.units.Heat
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.util.Mth
import kotlin.math.PI
import kotlin.math.cos

internal val SHIVER_ONSET: Heat = Heat(36.5)
internal val SHIVER_FULL: Heat = Heat(35.5)
internal val SHIVER_FADING: Heat = Heat(33.5)
internal val SHIVER_CEASES: Heat = Heat(32.0)

internal fun shiverIntensity(heat: Heat): Double = when {
    heat >= SHIVER_ONSET || heat <= SHIVER_CEASES -> 0.0
    heat > SHIVER_FULL -> easeInOut(heat, SHIVER_ONSET, SHIVER_FULL)
    heat >= SHIVER_FADING -> 1.0
    else -> easeInOut(heat, SHIVER_CEASES, SHIVER_FADING)
}

private fun easeInOut(heat: Heat, from: Heat, to: Heat): Double {
    val progress = Mth.inverseLerp(heat.celsius, from.celsius, to.celsius)
    return 0.5 * (1.0 - cos(PI * progress))
}

public fun shiverStrength(entity: LivingEntity): Float {
    if (entity !is Player) return 0F
    val vitals = Survival.platform.vitals.get(entity) ?: return 0F
    return shiverIntensity(vitals.bodyTemperature.heat).toFloat()
}
