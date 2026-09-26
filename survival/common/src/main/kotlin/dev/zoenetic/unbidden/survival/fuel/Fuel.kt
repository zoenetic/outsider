package dev.zoenetic.unbidden.survival.fuel

public data class Fuel(public val level: Int) {
    init {
        require(level in 0..31) { "fuel level out of range: $level" }
    }

    public operator fun plus(other: Fuel): Fuel =
        Fuel((level + other.level).coerceAtMost(MAX.level))

    public operator fun compareTo(other: Fuel): Int = level.compareTo(other.level)

    public fun coerceAtMost(maximum: Fuel): Fuel {
        return if (this.level > maximum.level) maximum else this
    }

    public companion object {
        public val EMPTY: Fuel = Fuel(0)
        public val MAX: Fuel = Fuel(15)
    }
}
