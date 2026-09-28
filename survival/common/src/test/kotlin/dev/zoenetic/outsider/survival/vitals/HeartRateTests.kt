package dev.zoenetic.outsider.survival.vitals

import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import net.minecraft.SharedConstants
import kotlin.test.*

class HeartRateTests {

    private val resting = RESTING_HEART_RATE.value
    private val max = MAX_HEART_RATE.value
    private val normal = NORMAL_BODY_TEMPERATURE

    private val oneSecond = Duration(SharedConstants.TICKS_PER_SECOND.toLong())
    private val forever = Duration(10_000_000L)

    private fun seconds(s: Double) = Duration((s * SharedConstants.TICKS_PER_SECOND).toLong())

    private fun settled(core: Heat = normal, met: Double = 1.0, from: Double = resting): Double =
        HeartRate(BPM(from)).getNew(core, MET(met), forever).bpm.value

    @Test
    fun `resting heart rate at normal temperature and one MET`() {
        assertEquals(resting, settled(), 1e-9)
    }

    @Test
    fun `max MET at normal temperature reaches max heart rate`() {
        assertEquals(max, settled(met = MET_MAX.value), 1e-9)
    }

    @Test
    fun `exertion claims a linear fraction of the reserve`() {
        val half = 1.0 + (MET_MAX.value - 1.0) / 2.0
        assertEquals(resting + (max - resting) / 2.0, settled(met = half), 1e-9)
    }

    @Test
    fun `sleeping is slightly below resting`() {
        val asleep = settled(met = 0.9)
        assertTrue(asleep < resting && asleep > resting - 5.0, "asleep $asleep")
    }

    @Test
    fun `fever adds beats and keeps adding with temperature`() {
        val mild = settled(core = normal + Heat(1.0))
        val high = settled(core = normal + Heat(3.0))
        assertTrue(mild > resting, "mild $mild")
        assertTrue(high > mild, "high $high should exceed mild $mild")
    }

    @Test
    fun `heart rate is unaffected down to where shivering ceases`() {
        assertEquals(resting, settled(core = SHIVER_CEASES), 1e-9)
    }

    @Test
    fun `hypothermia scales the heart rate linearly down to asystole`() {
        val midway = (SHIVER_CEASES + ASYSTOLE_TEMPERATURE) / 2.0
        assertEquals(resting / 2.0, settled(core = midway), 1e-9)
        assertEquals(0.0, settled(core = ASYSTOLE_TEMPERATURE), 1e-9)
        assertEquals(0.0, settled(core = ASYSTOLE_TEMPERATURE - Heat(5.0)), 1e-9)
    }

    @Test
    fun `chill scales the exertion contribution too`() {
        val midway = (SHIVER_CEASES + ASYSTOLE_TEMPERATURE) / 2.0
        assertEquals(max / 2.0, settled(core = midway, met = MET_MAX.value), 1e-9)
    }

    @Test
    fun `never exceeds max heart rate`() {
        assertEquals(max, settled(core = ARRHYTHMIA_TEMPERATURE, met = MET_MAX.value), 1e-9)
        assertEquals(max, settled(met = MET_MAX.value * 2), 1e-9)
    }

    @Test
    fun `zero elapsed leaves the heart rate unchanged`() {
        val before = HeartRate(BPM(resting))
        val after = before.getNew(normal, MET(MET_MAX.value), Duration(0L))
        assertEquals(before.bpm.value, after.bpm.value, 1e-9)
    }

    @Test
    fun `one rising half-life closes half the gap to the target`() {
        val after = HeartRate(BPM(resting))
            .getNew(normal, MET(MET_MAX.value), seconds(HEART_RATE_RISES_AT))
        assertEquals(resting + (max - resting) / 2.0, after.bpm.value, 1e-9)
    }

    @Test
    fun `one falling half-life closes half the gap to the target`() {
        val after = HeartRate(BPM(max))
            .getNew(normal, MET(1.0), seconds(HEART_RATE_FALLS_AT))
        assertEquals(max - (max - resting) / 2.0, after.bpm.value, 1e-9)
    }

    @Test
    fun `heart rate rises faster than it falls`() {
        val gap = max - resting
        val up = HeartRate(BPM(resting)).getNew(normal, MET(MET_MAX.value), oneSecond)
        val down = HeartRate(BPM(max)).getNew(normal, MET(1.0), oneSecond)
        val closedUp = (up.bpm.value - resting) / gap
        val closedDown = (max - down.bpm.value) / gap
        assertTrue(closedUp > closedDown, "up $closedUp should exceed down $closedDown")
    }

    @Test
    fun `no heartbeat at zero bpm`() {
        assertNull(HeartRate(BPM(0.0)).toHeartbeat())
    }

    @Test
    fun `heartbeat interval is a minute of ticks divided by bpm`() {
        val beat = assertNotNull(HeartRate(BPM(resting)).toHeartbeat())
        assertEquals((SharedConstants.TICKS_PER_MINUTE / resting).toLong(), beat.interval.value)
    }

    @Test
    fun `heartbeat is silent in the normal band`() {
        for (bpm in listOf(LOW_BPM_AUDIBLE_THRESHOLD, resting, HIGH_BPM_AUDIBLE_THRESHOLD)) {
            assertEquals(0F, HeartRate(BPM(bpm)).volume(), "at $bpm")
        }
    }

    @Test
    fun `heartbeat volume ramps to full on both sides`() {
        val lowMid = (LOW_BPM_AUDIBLE_THRESHOLD + LOW_BPM_FULL_VOLUME_THRESHOLD) / 2.0
        val highMid = (HIGH_BPM_AUDIBLE_THRESHOLD + HIGH_BPM_FULL_VOLUME_THRESHOLD) / 2.0
        assertEquals(0.5F, HeartRate(BPM(lowMid)).volume(), 1e-6F)
        assertEquals(0.5F, HeartRate(BPM(highMid)).volume(), 1e-6F)
        assertEquals(1F, HeartRate(BPM(LOW_BPM_FULL_VOLUME_THRESHOLD)).volume())
        assertEquals(1F, HeartRate(BPM(HIGH_BPM_FULL_VOLUME_THRESHOLD)).volume())
        assertEquals(1F, HeartRate(BPM(LOW_BPM_FULL_VOLUME_THRESHOLD - 20.0)).volume())
        assertEquals(1F, HeartRate(BPM(HIGH_BPM_FULL_VOLUME_THRESHOLD + 20.0)).volume())
    }
}
