package dev.zoenetic.outsider.survival.units

import kotlin.math.roundToLong

@JvmInline
public value class Duration(public val value: Long) {
    public operator fun plus(o: Duration): Duration = Duration(this.value + o.value)
    public operator fun minus(o: Duration): Duration = Duration(this.value - o.value)
    public operator fun times(k: Int): Duration = Duration(this.value * k)
    public operator fun times(k: Double): Duration = Duration((this.value * k).roundToLong())
    public operator fun compareTo(o: Duration): Int = value.compareTo(o.value)
}