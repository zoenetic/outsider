package dev.zoenetic.outsider.survival.fire

import dev.zoenetic.outsider.survival.fire.LightingFireState.NursingEmbers
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Time
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NursingEmbersTests {

    private val recordedAt = Time(1_000)

    private fun conditions(ambient: Double = 20.0) = EmberConditions(
        ambient = Heat(ambient),
        blowingRise = Heat(800.0),
        halfLife = Duration(40),
        dies = Heat(250.0),
        catches = Heat(600.0),
    )

    private fun nursing(held: Boolean, blown: Long = 0, heat: Double = 400.0) = NursingEmbers(
        heat = Heat(heat),
        diesAt = recordedAt + Duration(100),
        blown = Duration(blown),
        blownRequired = Duration(20),
        breathUntil = Time(9_060),
        nextBreathAt = Time(8_990),
        held = held,
        timestamp = recordedAt,
    )

    @Test
    fun `held blowing grows by one for every tick since the press`() {
        assertEquals(Duration(12), nursing(held = true, blown = 5).blownAt(recordedAt + Duration(7)))
    }

    @Test
    fun `resting blowing stays where it was`() {
        assertEquals(Duration(5), nursing(held = false, blown = 5).blownAt(recordedAt + Duration(50)))
    }

    @Test
    fun `blowing heats the ember halfway to its equilibrium each half-life`() {
        // ambient 20 + rise 800 = 820; from 400 that is 420 short, so 210 short after one half-life.
        val heat = nursing(held = true).heatAt(recordedAt + Duration(40), conditions())
        assertEquals(610.0, heat.celsius, 1e-9)
    }

    @Test
    fun `a resting ember cools halfway to the air each half-life`() {
        val heat = nursing(held = false).heatAt(recordedAt + Duration(40), conditions())
        assertEquals(210.0, heat.celsius, 1e-9)
    }

    @Test
    fun `the heat at the moment of recording is the recorded heat`() {
        assertEquals(400.0, nursing(held = true).heatAt(recordedAt, conditions()).celsius, 1e-9)
        assertEquals(400.0, nursing(held = false).heatAt(recordedAt, conditions()).celsius, 1e-9)
    }

    @Test
    fun `a resting ember crosses the dying heat on the tick of diesAt`() {
        // diesAt is a whole tick, so the crossing lies within a tick either side of it.
        for (ambient in listOf(-30.0, -10.0, 20.0, 50.0)) {
            val conditions = conditions(ambient)
            val diesAt = conditions.diesAt(Heat(400.0), recordedAt)
            val ember = nursing(held = false)
            val before = ember.heatAt(diesAt - Duration(1), conditions)
            val after = ember.heatAt(diesAt + Duration(1), conditions)
            assertTrue(before > Heat(250.0) && after < Heat(250.0), "at $ambient °C air")
        }
    }

    @Test
    fun `an ember dies sooner in colder air`() {
        val cold = conditions(-10.0).diesAt(Heat(400.0), recordedAt)
        val mild = conditions(20.0).diesAt(Heat(400.0), recordedAt)
        val hot = conditions(50.0).diesAt(Heat(400.0), recordedAt)
        assertTrue(cold < mild && mild < hot, "cold $cold, mild $mild, hot $hot")
    }

    @Test
    fun `an ember at or below the dying heat dies at once`() {
        assertEquals(recordedAt, conditions().diesAt(Heat(250.0), recordedAt))
        assertEquals(recordedAt, conditions().diesAt(Heat(100.0), recordedAt))
    }

    @Test
    fun `an ember never dies at rest in air hotter than the dying heat`() {
        assertEquals(Time.NEVER, conditions(ambient = 300.0).diesAt(Heat(400.0), recordedAt))
    }

    @Test
    fun `blowing from a fresh ember takes longer to catch than the blowing required`() {
        // The tinder needs both, so heat, not blownRequired, decides at the default numbers.
        val ember = nursing(held = true)
        val caught = (1..200).first {
            ember.heatAt(recordedAt + Duration(it.toLong()), conditions()) >= Heat(600.0)
        }
        assertTrue(caught > 20, "caught after $caught ticks")
    }

    @Test
    fun `a fresh ember holds its heat until its recorded time`() {
        // A fresh ember is recorded a grace period ahead, so it smoulders without cooling till then.
        val ember = nursing(held = false)
        for (early in listOf(60L, 30L, 1L)) {
            assertEquals(400.0, ember.heatAt(recordedAt - Duration(early), conditions()).celsius, 1e-9)
        }
        assertTrue(ember.heatAt(recordedAt + Duration(1), conditions()) < Heat(400.0))
    }

    @Test
    fun `an ember settling later dies later by exactly the delay`() {
        val now = conditions().diesAt(Heat(400.0), recordedAt)
        val later = conditions().diesAt(Heat(400.0), recordedAt + Duration(60))
        assertEquals(Duration(60), later - now)
    }
}
