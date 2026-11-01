package dev.zoenetic.outsider.survival.emission

import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.units.Power
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeatTableTests {

    private val max = Power(16.0)
    private val embers = HeatTable.embers(max)

    @Test
    fun `a full fire gives its full heat`() {
        assertEquals(max, embers[Fuel.MAX.level])
    }

    @Test
    fun `a spent fire gives none`() {
        assertEquals(Power.ZERO, embers[0])
    }

    @Test
    fun `the last embers still give a good share of the heat`() {
        assertTrue(embers[1] >= max * HeatTable.EMBER_FRACTION)
    }

    @Test
    fun `heat falls steadily as the fuel burns down`() {
        val levels = (1..Fuel.MAX.level).map { embers[it] }
        assertTrue(levels.zipWithNext().all { (a, b) -> b > a })
    }

    @Test
    fun `a constant table ignores the fuel level`() {
        val constant = HeatTable.constant(Power(4.0))
        assertTrue((0..Fuel.MAX.level).all { constant[it] == Power(4.0) })
    }
}
