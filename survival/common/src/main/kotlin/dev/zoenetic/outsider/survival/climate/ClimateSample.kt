package dev.zoenetic.outsider.survival.climate

import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Humidity
import dev.zoenetic.outsider.survival.units.Wind

public data class ClimateSample(
    val humidity: Humidity,
    val temperature: Heat,
    val wind: Wind,
)