package dev.zoenetic.outsider.survival.vitals

import dev.zoenetic.outsider.survival.units.Conductance
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Insulation
import dev.zoenetic.outsider.survival.vitals.BodyTemperature.Companion.rate
import dev.zoenetic.outsider.survival.vitals.BodyTemperature.Companion.step
import dev.zoenetic.outsider.survival.vitals.BodyTemperature.Companion.sweating
import dev.zoenetic.outsider.survival.vitals.BodyTemperature.Companion.tissueResistance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BodyTemperatureTests {

    private val calm = Conductance(1.0)
    private val resting = MET(1.0)
    private val sprinting = MET(14.0)
    private val leather = Insulation(2.0)

    private val oneSecond = Duration(20L)
    private val anHour = 3600

    private class Body(
        val environment: Heat,
        val conductance: Conductance,
        val insulation: Insulation,
        val exertion: MET,
        var heat: Heat,
    ) {
        fun live(seconds: Int): Body = apply {
            repeat(seconds) { tick() }
        }

        fun secondsUntil(limit: Int, reached: (Heat) -> Boolean): Int? {
            for (second in 1..limit) {
                tick()
                if (reached(heat)) return second
            }
            return null
        }

        // In game, Exertion adds the body's own shivering each tick; mirror that here.
        private fun tick() {
            val shivering = (SHIVERING.value - 1.0) * shiverIntensity(heat)
            val met = MET(exertion.value + shivering)
            heat = step(heat, environment, conductance, insulation, met, Duration(20L))
        }
    }

    private fun body(
        environment: Double,
        conductance: Conductance = calm,
        insulation: Insulation = Insulation.NONE,
        exertion: MET = resting,
        from: Double = NORMAL_BODY_TEMPERATURE.celsius,
    ) = Body(Heat(environment), conductance, insulation, exertion, Heat(from))

    @Test
    fun `no time passing changes nothing`() {
        val t = Heat(35.0)
        assertEquals(t, step(t, Heat(-20.0), calm, Insulation.NONE, resting, Duration.NONE))
    }

    @Test
    fun `two one-second steps equal one two-second step`() {
        val t = Heat(36.0)
        val once = step(t, Heat(-5.0), calm, Insulation.NONE, resting, Duration(40L))
        val first = step(t, Heat(-5.0), calm, Insulation.NONE, resting, oneSecond)
        val twice = step(first, Heat(-5.0), calm, Insulation.NONE, resting, oneSecond)
        assertEquals(once.celsius, twice.celsius, 1e-12)
    }

    @Test
    fun `catching up after a long absence is capped`() {
        val t = Heat(36.0)
        val threeDays = Duration(24_000L * 3)
        assertEquals(
            step(t, Heat(-30.0), calm, Insulation.NONE, resting, MAX_CATCH_UP),
            step(t, Heat(-30.0), calm, Insulation.NONE, resting, threeDays),
        )
    }

    @Test
    fun `a body colder than its surroundings warms, and a hotter one cools`() {
        assertTrue(rate(Heat(33.0), Heat(30.0), calm, Insulation.NONE, resting) > 0.0)
        assertTrue(rate(Heat(37.0), Heat(-10.0), calm, Insulation.NONE, resting) < 0.0)
    }

    @Test
    fun `the body holds normal temperature across a mild band without shivering or sweat`() {
        for (environment in listOf(20.0, 25.0, 30.0)) {
            val settled = body(environment).live(anHour).heat
            assertTrue(settled > SHIVER_ONSET, "at $environment settled at $settled")
            assertTrue(settled < Heat(37.5), "at $environment settled at $settled")
        }
    }

    @Test
    fun `in mild cold shivering holds the line`() {
        val settled = body(5.0).live(anHour).heat
        assertTrue(shiverIntensity(settled) > 0.0, "expected shivering at $settled")
        assertTrue(settled > Heat(35.5), "5C without clothing settled at $settled")
    }

    @Test
    fun `in hard cold the body falls past the point shivering stops, within a night`() {
        val seconds = body(-10.0).secondsUntil(anHour) { it <= SHIVER_CEASES }
        assertTrue(seconds != null && seconds in 4 * 60..12 * 60, "reached 32C after $seconds s")
    }

    @Test
    fun `once shivering fails there is no resting point`() {
        val failing = body(-10.0).live(anHour).heat
        assertTrue(failing < Heat(28.0), "an hour at -10C without clothing ended at $failing")
    }

    @Test
    fun `leather lets the body hold in hard cold`() {
        val settled = body(-10.0, insulation = leather).live(anHour).heat
        assertTrue(settled > Heat(35.5), "-10C in leather settled at $settled")
    }

    @Test
    fun `insulation slows the fall`() {
        val naked = body(-20.0).live(120).heat
        val wrapped = body(-20.0, insulation = leather).live(120).heat
        assertTrue(wrapped > naked, "wrapped $wrapped should be warmer than naked $naked")
    }

    @Test
    fun `cold water is dangerous within minutes, not seconds`() {
        val air = Contact(Heat(15.0), calm)
        val water = Contact(Heat(5.0), ConductiveMedium.WATER.conductance)
        val contacts = listOf(air, water)
        val swimming = body(contacts.environment().celsius, contacts.conductance())
        val seconds = swimming.secondsUntil(anHour) { it <= Heat(34.0) }
        assertTrue(seconds != null && seconds in 2 * 60..10 * 60, "reached 34C after $seconds s")
    }

    @Test
    fun `sprinting keeps you warm enough to stop shivering at freezing`() {
        val settled = body(0.0, exertion = sprinting).live(anHour).heat
        assertTrue(settled >= SHIVER_ONSET, "sprinting at 0C settled at $settled")
    }

    @Test
    fun `sprinting in temperate weather never overheats`() {
        val settled = body(25.0, exertion = sprinting).live(anHour).heat
        assertTrue(settled < Heat(38.5), "sprinting at 25C settled at $settled")
    }

    @Test
    fun `standing in desert heat, sweat holds the body`() {
        val settled = body(35.0).live(anHour).heat
        assertTrue(settled < Heat(38.0), "standing at 35C settled at $settled")
    }

    @Test
    fun `sprinting in desert heat overheats within minutes`() {
        val seconds = body(35.0, exertion = sprinting).secondsUntil(anHour) { it >= Heat(39.0) }
        assertTrue(seconds != null && seconds in 30..5 * 60, "reached 39C after $seconds s")
    }

    @Test
    fun `extreme heat overwhelms sweating`() {
        val settled = body(55.0).live(anHour).heat
        assertTrue(settled > Heat(40.0), "an hour at 55C ended at $settled")
    }

    @Test
    fun `a fire brings a chilled body back within minutes`() {
        val seconds = body(30.0, from = 33.0).secondsUntil(anHour) { it >= SHIVER_ONSET }
        assertTrue(seconds != null && seconds < 5 * 60, "back to 36.5C after $seconds s")
    }

    @Test
    fun `blood vessels constrict in the cold and dilate in the heat`() {
        assertEquals(TISSUE_RESISTANCE_CONSTRICTED, tissueResistance(Heat(35.0)))
        assertEquals(TISSUE_RESISTANCE_DILATED, tissueResistance(Heat(38.0)))
        val between = tissueResistance(NORMAL_BODY_TEMPERATURE)
        assertTrue(between < TISSUE_RESISTANCE_CONSTRICTED && between > TISSUE_RESISTANCE_DILATED)
    }

    @Test
    fun `sweating starts above normal and saturates`() {
        assertEquals(0.0, sweating(NORMAL_BODY_TEMPERATURE))
        assertEquals(1.0, sweating(SWEAT_FULL))
        assertEquals(1.0, sweating(Heat(41.0)))
    }
}
