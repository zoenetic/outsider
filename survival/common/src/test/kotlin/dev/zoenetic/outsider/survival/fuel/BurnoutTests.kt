package dev.zoenetic.outsider.survival.fuel

import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Time
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class BurnoutTests {

    private val max = Fuel(15)
    private val rate = Duration(200L)
    private val now = Time(1000L)

    private fun forFuel(current: Time?, fuel: Int) =
        Burnout.forFuel(current, now, Fuel(fuel), max, rate)

    @Test
    fun `no existing burnout starts a full burn for the fuel level`() {
        assertEquals(Time(1000L + 7 * 200L), forFuel(null, 7).at)
    }

    @Test
    fun `a burnout that agrees with the fuel level keeps its part-burned remainder`() {
        val partBurned = Time(now.value + 1300L)
        assertEquals(
            partBurned, forFuel(partBurned, 7).at,
            "recomputing would round the remainder up to a full level and gift 100 ticks"
        )
    }

    @Test
    fun `a burnout exactly at the top of its level is kept`() {
        val atTop = Time(now.value + 7 * 200L)
        assertEquals(atTop, forFuel(atTop, 7).at)
    }

    @Test
    fun `a burnout exactly at the bottom of its level is recomputed`() {
        val atBottom = Time(now.value + 6 * 200L)
        assertEquals(Time(now.value + 7 * 200L), forFuel(atBottom, 7).at)
    }

    @Test
    fun `a burnout claiming more time than the fuel level allows is recomputed`() {
        val tooLong = Time(now.value + 99_999L)
        assertEquals(Time(now.value + 7 * 200L), forFuel(tooLong, 7).at)
    }

    @Test
    fun `a burnout already in the past is recomputed`() {
        assertEquals(Time(now.value + 7 * 200L), forFuel(Time(0L), 7).at)
    }

    @Test
    fun `a fresh burnout reads back as the fuel level it was built from`() {
        for (level in 1..max.level) {
            val burnout = forFuel(null, level)
            assertEquals(
                Fuel(level), burnout.fuelAt(now, max, rate),
                "round trip failed at fuel $level"
            )
        }
    }

    @Test
    fun `a preserved burnout still reads back as the same fuel level`() {
        for (level in 1..max.level) {
            val partBurned = Time(now.value + (level - 1) * rate.value + 1L)
            val burnout = forFuel(partBurned, level)
            assertEquals(partBurned, burnout.at, "should have been kept at fuel $level")
            assertEquals(Fuel(level), burnout.fuelAt(now, max, rate))
        }
    }

    @Test
    fun `fuel outside one to max is rejected`() {
        assertFailsWith<IllegalArgumentException> { forFuel(null, 0) }
        assertFailsWith<IllegalArgumentException> { forFuel(null, max.level + 1) }
    }

    @Test
    fun `the next drop from a full burn is one burn rate away`() {
        val burnout = forFuel(null, max.level)
        assertEquals(Time(now.value + rate.value), burnout.nextDropAt(now, max, rate))
    }

    @Test
    fun `an exhausted burnout has no next drop`() {
        assertNull(Burnout(Time(0L)).nextDropAt(now, max, rate))
    }
}
