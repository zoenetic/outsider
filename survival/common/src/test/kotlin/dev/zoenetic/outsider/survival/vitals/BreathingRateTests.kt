package dev.zoenetic.outsider.survival.vitals

import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import net.minecraft.SharedConstants
import kotlin.math.pow
import kotlin.test.*

class BreathingRateTests {

    private val resting = RESTING_BREATHING_RATE
    private val max = MAX_BREATHING_RATE
    private val normal = NORMAL_BODY_TEMPERATURE

    private val oneSecond = Duration(SharedConstants.TICKS_PER_SECOND.toLong())
    private val forever = Duration(10_000_000L)

    private fun seconds(s: Double) = Duration((s * SharedConstants.TICKS_PER_SECOND).toLong())

    private fun settled(core: Heat = normal, met: Double = 1.0, from: Double = resting): Double =
        BreathingRate(from).getNew(core, MET(met), forever).value

    private fun visibility(air: Double): Float = BreathingRate(resting).visibility(Heat(air))

    @Test
    fun `resting breathing rate at normal temperature and one MET`() {
        assertEquals(resting, settled(), 1e-9)
    }

    @Test
    fun `max MET reaches max breathing rate`() {
        assertEquals(max, settled(met = MET_MAX.value), 1e-9)
    }

    @Test
    fun `breathing rate rises convexly, slower than linear at half capacity`() {
        val half = 1.0 + (MET_MAX.value - 1.0) / 2.0
        val rate = settled(met = half)
        val linear = resting + (max - resting) / 2.0
        assertTrue(
            rate > resting && rate < linear,
            "rate $rate should sit between $resting and $linear"
        )
        assertEquals(resting + (max - resting) * 0.5.pow(BREATHING_RATE_CURVE), rate, 1e-9)
    }

    @Test
    fun `capacity is clamped at both ends before the curve`() {
        assertEquals(resting, settled(met = 0.9), 1e-9)
        assertEquals(max, settled(met = MET_MAX.value * 2), 1e-9)
    }

    @Test
    fun `fever adds a fixed number of breaths per degree`() {
        assertEquals(
            resting + 2.0 * BREATHS_PER_DEGREE_OF_FEVER,
            settled(core = normal + Heat(2.0)),
            1e-9
        )
    }

    @Test
    fun `breathing is unaffected down to where shivering ceases`() {
        assertEquals(resting, settled(core = SHIVER_CEASES), 1e-9)
    }

    @Test
    fun `hypothermia slows breathing linearly down to apnoea`() {
        val midway = (SHIVER_CEASES + APNOEA_TEMPERATURE) / 2.0
        assertEquals(resting / 2.0, settled(core = midway), 1e-9)
        assertEquals(0.0, settled(core = APNOEA_TEMPERATURE), 1e-9)
        assertEquals(0.0, settled(core = APNOEA_TEMPERATURE - Heat(5.0)), 1e-9)
    }

    @Test
    fun `never exceeds max breathing rate`() {
        assertEquals(max, settled(core = normal + Heat(10.0), met = MET_MAX.value), 1e-9)
    }

    @Test
    fun `one rising half-life closes half the gap to the target`() {
        val after = BreathingRate(resting)
            .getNew(normal, MET(MET_MAX.value), seconds(BREATHING_RATE_RISES_AT))
        assertEquals(resting + (max - resting) / 2.0, after.value, 1e-9)
    }

    @Test
    fun `one falling half-life closes half the gap to the target`() {
        val after = BreathingRate(max)
            .getNew(normal, MET(1.0), seconds(BREATHING_RATE_FALLS_AT))
        assertEquals(max - (max - resting) / 2.0, after.value, 1e-9)
    }

    @Test
    fun `breathing rate rises faster than it falls`() {
        val gap = max - resting
        val up = BreathingRate(resting).getNew(normal, MET(MET_MAX.value), oneSecond)
        val down = BreathingRate(max).getNew(normal, MET(1.0), oneSecond)
        val closedUp = (up.value - resting) / gap
        val closedDown = (max - down.value) / gap
        assertTrue(closedUp > closedDown, "up $closedUp should exceed down $closedDown")
    }

    @Test
    fun `no breath at zero rate`() {
        assertNull(BreathingRate(0.0).toBreath())
    }

    @Test
    fun `breath interval is a minute of ticks divided by the rate`() {
        val breath = assertNotNull(BreathingRate(resting).toBreath())
        assertEquals((SharedConstants.TICKS_PER_MINUTE / resting).toLong(), breath.interval.value)
    }

    @Test
    fun `breathing is silent at rest`() {
        assertEquals(0F, BreathingRate(resting).volume())
    }

    @Test
    fun `breathing volume ramps from the audible threshold to full`() {
        assertEquals(0F, BreathingRate(BREATHING_AUDIBLE_THRESHOLD).volume())
        val mid = (BREATHING_AUDIBLE_THRESHOLD + BREATHING_FULL_VOLUME_THRESHOLD) / 2.0
        assertEquals(0.5F, BreathingRate(mid).volume(), 1e-6F)
        assertEquals(1F, BreathingRate(BREATHING_FULL_VOLUME_THRESHOLD).volume())
        assertEquals(1F, BreathingRate(max).volume())
    }

    @Test
    fun `breath is invisible at and above the visible threshold`() {
        assertEquals(0F, visibility(BREATHING_VISIBLE_THRESHOLD))
        assertEquals(0F, visibility(BREATHING_VISIBLE_THRESHOLD + 10.0))
    }

    @Test
    fun `breath visibility ramps up as the air gets colder`() {
        val mid = (BREATHING_VISIBLE_THRESHOLD + BREATHING_FULL_VISIBILITY_THRESHOLD) / 2.0
        assertEquals(0.5F, visibility(mid), 1e-6F)
        assertEquals(1F, visibility(BREATHING_FULL_VISIBILITY_THRESHOLD))
        assertEquals(1F, visibility(BREATHING_FULL_VISIBILITY_THRESHOLD - 20.0))
    }

    @Test
    fun `breath visibility never increases with temperature`() {
        val temps = (-20..20).map { it.toDouble() }
        val vis = temps.map { visibility(it) }
        assertEquals(vis.sortedDescending(), vis, "visibility should fall as air warms")
    }
}
