package dev.zoenetic.outsider.survival.units

@JvmInline
public value class Humidity(public val value: Double) {

    public operator fun plus(o: Humidity): Humidity = Humidity(value + o.value)
    public operator fun minus(o: Humidity): Humidity = Humidity(value - o.value)
    public operator fun times(k: Double): Humidity = Humidity(value * k)
    public operator fun div(k: Double): Humidity = Humidity(value / k)
    public operator fun unaryMinus(): Humidity = Humidity(-value)
    public operator fun compareTo(other: Humidity): Int = value.compareTo(other.value)

    public companion object {
        public fun fromNoise(n: Double): Humidity = Humidity((n.coerceIn(-1.0, 1.0) + 1.0) / 2.0)
    }
}
