package dev.zoenetic.outsider.survival.emission

import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.units.Power

public class HeatTable private constructor(private val values: DoubleArray) {

    public operator fun get(level: Int): Power = Power(values[level])

    override fun equals(other: Any?): Boolean =
        other is HeatTable && values.contentEquals(other.values)
    override fun hashCode(): Int = values.contentHashCode()
    override fun toString(): String =
        (0..15).joinToString(prefix = "HeatTable[", postfix = "]") { "${values[it]}" }

    public companion object {
        internal const val EMBER_FRACTION: Double = 0.4

        public fun from(f: (Int) -> Power): HeatTable =
            HeatTable(DoubleArray(Fuel.MAX.level + 1) { f(it).value })

        public fun constant(power: Power): HeatTable = from { power }

        public fun embers(max: Power): HeatTable = from { level ->
            if (level == 0) {
                Power.ZERO
            } else {
                max * (EMBER_FRACTION + (1.0 - EMBER_FRACTION) * level / Fuel.MAX.level)
            }
        }
    }
}
