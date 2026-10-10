package dev.zoenetic.outsider.survival.vitals

import dev.zoenetic.outsider.survival.units.Heat
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

internal val SHIVER_PEAK: Heat = Heat(34.0)
internal val SHIVER_ONSET: Heat = Heat(36.5)
internal val SHIVER_CEASES: Heat = Heat(32.0)

public fun shiverIntensity(temperature: Heat): Double {
    if (temperature >= SHIVER_ONSET || temperature <= SHIVER_CEASES) return 0.0
    val halfWidth =
        if (temperature > SHIVER_PEAK) SHIVER_ONSET - SHIVER_PEAK else SHIVER_PEAK - SHIVER_CEASES
    val distance = abs(temperature.celsius - SHIVER_PEAK.celsius) / halfWidth.celsius
    return 0.5 * (1.0 + cos(PI * distance))
}
