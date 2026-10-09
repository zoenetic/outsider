package dev.zoenetic.outsider.survival.fuel

import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Time

@JvmInline
public value class Burnout(public val time: Time) {

    public fun fuelAt(now: Time, burnRate: Duration): Fuel {
        val ticks = time - now
        val fuel =
            Math.ceilDiv(ticks.value, burnRate.value).coerceIn(0, Fuel.MAX.level.toLong()).toInt()
        return Fuel(fuel)
    }

    public fun nextDropAt(now: Time, burnRate: Duration): Time? {
        val fuel = fuelAt(now, burnRate)
        if (fuel.level == 0) return null
        return time - burnRate * (fuel.level - 1)
    }

    public fun isOut(now: Time): Boolean = now >= time

    public fun refuel(added: Fuel, now: Time, burnRate: Duration): Burnout {
        val from = if (time > now) time else now
        val extended = from + burnRate * added.level
        val cap = now + burnRate * Fuel.MAX.level
        return Burnout(if (extended < cap) extended else cap)
    }

    public companion object {
        public val NEVER: Burnout = Burnout(Time(Long.MAX_VALUE))

        public fun forFuel(stored: Time?, now: Time, fuel: Fuel, burnRate: Duration): Burnout {
            require(fuel.level in 1..Fuel.MAX.level) {
                "fuel must be 1..${Fuel.MAX.level}, got ${fuel.level}"
            }
            val full = now + burnRate * fuel.level
            stored ?: return Burnout(full)
            val slack = full - stored
            return if (slack >= Duration(0) && slack < burnRate) Burnout(stored) else Burnout(full)
        }
    }
}
