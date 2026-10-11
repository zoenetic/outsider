package dev.zoenetic.outsider.survival.vitals

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.units.Heat
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

internal val SHIVER_PEAK: Heat = Heat(34.0)
internal val SHIVER_ONSET: Heat = Heat(36.5)
internal val SHIVER_CEASES: Heat = Heat(32.0)

internal fun shiverIntensity(heat: Heat): Double {
    if (heat >= SHIVER_ONSET || heat <= SHIVER_CEASES) return 0.0
    val halfWidth = if (heat > SHIVER_PEAK) {
        SHIVER_ONSET - SHIVER_PEAK
    } else {
        SHIVER_PEAK - SHIVER_CEASES
    }
    val distance = abs(heat.celsius - SHIVER_PEAK.celsius) / halfWidth.celsius
    return (0.5 * (1.0 + cos(PI * distance)))
}

public fun shiverStrength(entity: LivingEntity): Float {
    if (entity !is Player) return 0F
    val vitals = Survival.platform.vitals.get(entity) ?: return 0F
    return shiverIntensity(vitals.bodyTemperature.heat).toFloat()
}
