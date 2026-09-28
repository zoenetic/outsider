package dev.zoenetic.outsider.survival.fuel

import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Time

@JvmInline
public value class Burnout(public val at: Time) {

    public fun fuelAt(now: Time, max: Fuel, burnRate: Duration): Fuel {
        val ticks = at - now
        val fuel =
            Math.ceilDiv(ticks.value, burnRate.value).coerceIn(0, max.level.toLong()).toInt()
        return Fuel(fuel)
    }

    public fun nextDropAt(now: Time, max: Fuel, burnRate: Duration): Time? {
        val fuel = fuelAt(now, max, burnRate)
        if (fuel.level == 0) return null
        return at - burnRate * (fuel.level - 1)
    }

    public fun isOut(now: Time): Boolean = now >= at

    public fun refuel(added: Fuel, now: Time, max: Fuel, burnRate: Duration): Burnout {
        val from = if (at > now) at else now
        val extended = from + burnRate * added.level
        val cap = now + burnRate * max.level
        return Burnout(if (extended < cap) extended else cap)
    }

    public companion object {
        public val NEVER: Burnout = Burnout(Time(Long.MAX_VALUE))

        public fun forFuel(current: Time?, now: Time, fuel: Fuel, max: Fuel, burnRate: Duration): Burnout {
            require(fuel.level in 1..max.level) { "fuel must be 1..${max.level}, got ${fuel.level}" }
            val full = now + burnRate * fuel.level
            current ?: return Burnout(full)
            val slack = full - current
            return if (slack >= Duration(0) && slack < burnRate) Burnout(current) else Burnout(full)
        }
    }
}