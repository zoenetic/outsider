package dev.zoenetic.outsider.survival.tools.drill.handdrill

import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat

public data class DrillingConditions(
    val ambient: Heat,
    val risePerTick: Heat,
    val halfLife: Duration,
    val ignition: Heat,
)
