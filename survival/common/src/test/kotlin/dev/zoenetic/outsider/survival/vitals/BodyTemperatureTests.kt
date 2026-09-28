package dev.zoenetic.outsider.survival.vitals

import dev.zoenetic.outsider.survival.units.*
import dev.zoenetic.outsider.survival.vitals.BodyTemperature.Companion.approach
import dev.zoenetic.outsider.survival.vitals.BodyTemperature.Companion.halfLife
import dev.zoenetic.outsider.survival.vitals.BodyTemperature.Companion.target
import net.minecraft.world.phys.Vec3
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BodyTemperatureTests {

    private fun h(v: Double) = Heat(v)
    private fun d(v: Long) = Duration(v)

    private fun assertEquals(expected: Heat, actual: Heat, absoluteTolerance: Double) {
        assertEquals(expected.celsius, actual.celsius, absoluteTolerance)
    }

    @Test
    fun `approach returns current exactly for 0 elapsed ticks`() {
        val low = h(30.0)
        assertEquals(low, approach(low, Heat(28.0), d(0), BODY_COOLS_AT))
        val high = h(44.0)
        assertEquals(high, approach(high, Heat(46.0), d(0), BODY_WARMS_AT))
    }

    @Test
    fun `one half-life closes exactly half the gap`() {
        val oneSecond = 1.0
        assertEquals(
            28.5, approach(
                h(30.0), h(27.0), d(20), oneSecond
            ).celsius, 1e-9
        )
        assertEquals(
            45.5, approach(
                h(44.0), h(47.0), d(20), oneSecond
            ).celsius, 1e-9
        )
    }

    @Test
    fun `approach never overshoots when warming`() {
        val current = NORMAL_BODY_TEMPERATURE
        for (gap in 1..10) {
            val target = current + h(gap.toDouble())
            for (elapsed in listOf(1L, 20L, 100_000L)) {
                val new = approach(current, target, d(elapsed), BODY_WARMS_AT)
                assertTrue(
                    new in current..target,
                    "gap $gap, elapsed $elapsed: got $new"
                )
            }
        }
    }

    @Test
    fun `approach never overshoots when cooling`() {
        val current = NORMAL_BODY_TEMPERATURE
        for (gap in 1..10) {
            val target = current - h(gap.toDouble())
            for (elapsed in listOf(1L, 20L, 100_000L)) {
                val new = approach(current, target, d(elapsed), BODY_COOLS_AT)
                assertTrue(
                    new in target..current,
                    "gap $gap, elapsed $elapsed: got $new"
                )
            }
        }
    }

    @Test
    fun `approach composes, 40 ticks equals 2 x 20 ticks`() {
        val target = NORMAL_BODY_TEMPERATURE + h(3.0)
        val afterForty = approach(NORMAL_BODY_TEMPERATURE, target, d(40), BODY_WARMS_AT)
        val afterFirstTwenty = approach(NORMAL_BODY_TEMPERATURE, target, d(20), BODY_WARMS_AT)
        val afterSecondTwenty = approach(afterFirstTwenty, target, d(20), BODY_WARMS_AT)
        assertEquals(afterForty, afterSecondTwenty, 1e-9)
    }

    @Test
    fun `one tick at a time equals one step of twenty ticks`() {
        val target = NORMAL_BODY_TEMPERATURE - h(8.0)
        var stepwise = NORMAL_BODY_TEMPERATURE
        repeat(20) { stepwise = approach(stepwise, target, d(1), BODY_COOLS_AT) }
        val oneGo = approach(NORMAL_BODY_TEMPERATURE, target, d(20), BODY_COOLS_AT)
        assertEquals(oneGo, stepwise, 1e-9)
    }

    @Test
    fun `a single tick actually moves the body temperature`() {
        val target = NORMAL_BODY_TEMPERATURE - h(20.0)
        val after = approach(NORMAL_BODY_TEMPERATURE, target, d(1), BODY_COOLS_AT)
        assertTrue(
            after < NORMAL_BODY_TEMPERATURE,
            "one tick in the cold should cool the body, got $after"
        )
    }

    @Test
    fun `if current == target, approach returns current`() {
        val current = h(24.0)
        assertEquals(current, approach(current, current, d(1), BODY_WARMS_AT))
        assertEquals(current, approach(current, current, d(1), BODY_COOLS_AT))
    }

    @Test
    fun `half life is the baseline with no medium, surface or wind`() {
        assertEquals(BODY_WARMS_AT, halfLife(isWarming = true))
        assertEquals(BODY_COOLS_AT, halfLife(isWarming = false))
    }

    @Test
    fun `warming and cooling use different baselines`() {
        assertNotEquals(halfLife(isWarming = true), halfLife(isWarming = false))
    }

    @Test
    fun `a conductive medium divides the baseline by its conductance`() {
        val water = ConductiveMedium.WATER.conductance
        assertEquals(BODY_COOLS_AT / water.value, halfLife(isWarming = false, medium = water))
        assertEquals(BODY_WARMS_AT / water.value, halfLife(isWarming = true, medium = water))
    }

    @Test
    fun `a conductive surface divides the baseline by its conductance`() {
        val metal = ConductiveSurface.METAL.conductance
        assertEquals(BODY_COOLS_AT / metal.value, halfLife(isWarming = false, surface = metal))
    }

    @Test
    fun `medium and surface conductance values multiply`() {
        val water = ConductiveMedium.WATER.conductance
        val metal = ConductiveSurface.METAL.conductance
        assertEquals(
            BODY_COOLS_AT / (water.value * metal.value),
            halfLife(isWarming = false, medium = water, surface = metal),
            1e-9,
        )
    }

    @Test
    fun `lava is effectively instantaneous`() {
        val lava = ConductiveMedium.LAVA.conductance
        assertTrue(halfLife(isWarming = true, medium = lava) < 1.0)
    }

    @Test
    fun `target is normal anywhere inside the comfort band`() {
        for (ambient in listOf(COMFORT_LOW, h(22.0), h(25.0), h(28.0), COMFORT_HIGH)) {
            assertEquals(NORMAL_BODY_TEMPERATURE, target(ambient), "ambient $ambient")
        }
    }

    @Test
    fun `target is continuous at both band edges`() {
        val epsilon = 1e-6
        assertEquals(
            target(COMFORT_LOW),
            target(h(COMFORT_LOW.celsius - epsilon)),
            1e-3
        )
        assertEquals(
            target(COMFORT_HIGH),
            target(h(COMFORT_HIGH.celsius + epsilon)),
            1e-3
        )
    }

    @Test
    fun `target moves with ambient outside the band, but by less than ambient does`() {
        val coldDrop = NORMAL_BODY_TEMPERATURE - target(h(0.0))
        assertTrue(coldDrop > h(0.0) && coldDrop < COMFORT_LOW, "cold drop $coldDrop")
        val heatRise = target(h(60.0)) - NORMAL_BODY_TEMPERATURE
        assertTrue(heatRise > h(0.0) && heatRise < h(60.0) - COMFORT_HIGH, "heat rise $heatRise")
    }

    @Test
    fun `cold leaks through to the core more than heat does`() {
        val coldDrop = NORMAL_BODY_TEMPERATURE - target(h(COMFORT_LOW.celsius - 10.0))
        val heatRise = target(h(COMFORT_HIGH.celsius + 10.0)) - NORMAL_BODY_TEMPERATURE
        assertTrue(coldDrop > heatRise)
    }

    @Test
    fun `calm wind has unit conductance and leaves the half life unchanged`() {
        assertEquals(1.0, CALM.conductance.value, 1e-9)
        assertEquals(
            BODY_COOLS_AT,
            halfLife(isWarming = false, wind = CALM.conductance),
            1e-9
        )
    }

    @Test
    fun `wind conductance grows linearly with speed`() {
        val fiveMetresPerSecond = Wind(Vec3(5.0, 0.0, 0.0))
        assertEquals(1.0 + CHILL * 5.0, fiveMetresPerSecond.conductance.value, 1e-9)
        assertEquals(
            BODY_COOLS_AT / (1.0 + CHILL * 5.0),
            halfLife(isWarming = false, wind = fiveMetresPerSecond.conductance),
            1e-9
        )
    }

    @Test
    fun `wind conductance depends on speed, not direction`() {
        val east = Wind(Vec3(3.0, 0.0, 0.0))
        val north = Wind(Vec3(0.0, 0.0, -3.0))
        val diagonal = Wind(Vec3(3.0 / sqrt(2.0), 0.0, 3.0 / sqrt(2.0)))
        assertEquals(east.conductance.value, north.conductance.value, 1e-9)
        assertEquals(east.conductance.value, diagonal.conductance.value, 1e-9)
    }

    @Test
    fun `wind multiplies with medium and surface`() {
        val water = ConductiveMedium.WATER.conductance
        val metal = ConductiveSurface.METAL.conductance
        val wind = Wind(Vec3(5.0, 0.0, 0.0)).conductance
        assertEquals(
            BODY_COOLS_AT / (water.value * metal.value * wind.value),
            halfLife(
                isWarming = false,
                medium = water,
                surface = metal,
                wind = wind
            ),
            1e-9,
        )
    }

    @Test
    fun `no insulation leaves target and half life unchanged`() {
        assertEquals(target(h(0.0)), target(h(0.0), Insulation.NONE))
        assertEquals(
            halfLife(isWarming = false),
            halfLife(isWarming = false, insulation = Insulation.NONE)
        )
    }

    @Test
    fun `insulation divides the cold drop of the target`() {
        val naked = NORMAL_BODY_TEMPERATURE - target(h(0.0))
        val wrapped = NORMAL_BODY_TEMPERATURE - target(h(0.0), Insulation(2.0))
        assertEquals(naked / 2.0, wrapped, 1e-9)
    }

    @Test
    fun `insulation multiplies the heat rise of the target`() {
        val naked = target(h(45.0)) - NORMAL_BODY_TEMPERATURE
        val wrapped = target(h(45.0), Insulation(2.0)) - NORMAL_BODY_TEMPERATURE
        assertEquals(naked * 2.0, wrapped, 1e-9)
    }

    @Test
    fun `insulation does nothing inside the comfort band`() {
        assertEquals(NORMAL_BODY_TEMPERATURE, target(h(25.0), Insulation(3.0)))
    }

    @Test
    fun `insulation lengthens the half life in both directions`() {
        assertEquals(
            BODY_COOLS_AT * 2.0,
            halfLife(isWarming = false, insulation = Insulation(2.0)),
            1e-9
        )
        assertEquals(
            BODY_WARMS_AT * 2.0,
            halfLife(isWarming = true, insulation = Insulation(2.0)),
            1e-9
        )
    }

    @Test
    fun `insulation and wind pull the half life in opposite directions`() {
        val wind = Wind(Vec3(5.0, 0.0, 0.0)).conductance
        assertEquals(
            BODY_COOLS_AT * 2.0 / wind.value,
            halfLife(isWarming = false, wind = wind, insulation = Insulation(2.0)),
            1e-9,
        )
    }
}
