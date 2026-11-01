package dev.zoenetic.outsider.survival.fire

import dev.zoenetic.outsider.survival.fire.LightingFireState.Drilling
import dev.zoenetic.outsider.survival.tools.drill.handdrill.DrillingConditions
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Time
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DrillingTests {

    private val conditions = DrillingConditions(
        ambient = Heat(20.0),
        drillingRise = Heat(480.0),
        halfLife = Duration(40),
        ignition = Heat(400.0),
    )

    private val recordedAt = Time(1_000)

    private fun drilling(held: Boolean, dust: Long = 0, heat: Double = 20.0) = Drilling(
        dust = Duration(dust),
        dustRequired = Duration(200),
        heat = Heat(heat),
        held = held,
        timestamp = recordedAt,
    )

    private fun later(ticks: Long) = recordedAt + Duration(ticks)

    @Test
    fun `held dust grows by one for every tick since the press`() {
        assertEquals(Duration(65), drilling(held = true, dust = 50).dustAt(later(15)))
    }

    @Test
    fun `resting dust stays banked`() {
        assertEquals(Duration(50), drilling(held = false, dust = 50).dustAt(later(4_000)))
    }

    @Test
    fun `held heat closes half the gap to its drilling equilibrium every half-life`() {
        assertEquals(260.0, drilling(held = true).heatAt(later(40), conditions).celsius, 1e-9)
    }

    @Test
    fun `held heat levels off at ambient plus the drilling rise`() {
        val settled = drilling(held = true).heatAt(later(10_000), conditions)
        assertEquals(500.0, settled.celsius, 1e-6)
    }

    @Test
    fun `steady drilling from cold reaches ignition in a few seconds`() {
        val start = drilling(held = true)
        assertTrue(start.heatAt(later(90), conditions) < conditions.ignition)
        assertTrue(start.heatAt(later(91), conditions) >= conditions.ignition)
    }

    @Test
    fun `colder air makes the char take longer to reach ignition`() {
        val cold = conditions.copy(ambient = Heat(-20.0))
        val start = drilling(held = true, heat = -20.0)
        assertTrue(start.heatAt(later(91), cold) < cold.ignition)
    }

    @Test
    fun `air too cold for the drilling rise never reaches ignition`() {
        val bitter = conditions.copy(ambient = Heat(-90.0))
        val start = drilling(held = true, heat = -90.0)
        assertTrue(start.heatAt(later(100_000), bitter) < bitter.ignition)
    }

    @Test
    fun `resting heat closes half the gap to ambient every half-life`() {
        val resting = drilling(held = false, heat = 400.0)
        assertEquals(210.0, resting.heatAt(later(40), conditions).celsius, 1e-9)
        assertEquals(115.0, resting.heatAt(later(80), conditions).celsius, 1e-9)
    }

    @Test
    fun `resting heat at ambient stays at ambient`() {
        assertEquals(Heat(20.0), drilling(held = false).heatAt(later(1_000), conditions))
    }

    @Test
    fun `resting heat below ambient warms towards it`() {
        assertEquals(10.0, drilling(held = false, heat = 0.0).heatAt(later(40), conditions).celsius)
    }

    @Test
    fun `a press after a rest climbs from the cooled heat`() {
        val rested = drilling(held = false, heat = 400.0)
        val pressedAt = later(40)
        val pressed = rested.copy(
            heat = rested.heatAt(pressedAt, conditions),
            held = true,
            timestamp = pressedAt,
        )
        val expected = 500.0 - 290.0 * 0.5.pow(10.0 / 40.0)
        assertEquals(expected, pressed.heatAt(pressedAt + Duration(10), conditions).celsius, 1e-9)
    }
}
