package dev.zoenetic.outsider.survival.fire

import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Time
import kotlin.math.log2

public data class EmberConditions(
    val ambient: Heat,
    val blowingRise: Heat,
    val halfLife: Duration,
    val dies: Heat,
    val catches: Heat,
) {
    public fun diesAt(heat: Heat, now: Time): Time = when {
        heat <= dies -> now
        ambient >= dies -> Time.NEVER
        else -> now + halfLife * log2((heat - ambient) / (dies - ambient))
    }
}
