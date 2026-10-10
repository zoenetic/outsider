package dev.zoenetic.outsider.survival.emission

import dev.zoenetic.outsider.survival.units.Heat

public class HeatTable private constructor(private val values: DoubleArray) {

    public operator fun get(level: Int): Heat = Heat(values[level])

    internal fun with(level: Int, heat: Heat): HeatTable =
        HeatTable(values.copyOf().also { it[level] = heat.celsius })

    internal fun map(f: (Heat) -> Heat): HeatTable =
        HeatTable(DoubleArray(16) { f(this[it]).celsius })

    public fun toDoubleArray(): DoubleArray = values.copyOf()

    override fun equals(other: Any?): Boolean =
        other is HeatTable && values.contentEquals(other.values)
    override fun hashCode(): Int = values.contentHashCode()
    override fun toString(): String = (0..15).joinToString(prefix = "HeatTable[", postfix = "]")

    public companion object {
        public fun from(f: (Int) -> Heat): HeatTable = HeatTable(DoubleArray(16) { f(it).celsius })

        public fun of(vararg celsius: Double): HeatTable {
            require(celsius.size == 16) { "need 16 values, got ${celsius.size}" }
            return from { Heat(celsius[it]) }
        }
    }
}
