package dev.zoenetic.outsider.survival.units

@JvmInline
public value class Power(public val value: Double) : Comparable<Power> {
    public operator fun times(d: Double): Power = Power(value * d)
    override fun compareTo(other: Power): Int = compareValues(value, other.value)
    override fun toString(): String = "${value}P"

    public companion object {
        public val ZERO: Power = Power(0.0)
    }
}
