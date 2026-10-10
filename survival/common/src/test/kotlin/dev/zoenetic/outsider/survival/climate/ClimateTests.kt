package dev.zoenetic.outsider.survival.climate

import dev.zoenetic.outsider.survival.units.*
import net.minecraft.SharedConstants
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClimateTests {

    private val dry = Humidity(0.0)
    private val humid = Humidity(1.0)

    @Test
    fun `temperature from noise is monotonic`() {
        assertTrue(temperatureFromNoise(-1.0) < temperatureFromNoise(-0.5))
        assertTrue(temperatureFromNoise(-0.5) < temperatureFromNoise(0.0))
        assertTrue(temperatureFromNoise(0.0) < temperatureFromNoise(0.5))
        assertTrue(temperatureFromNoise(0.5) < temperatureFromNoise(1.0))
    }

    @Test
    fun `values are clamped at pole and equator`() {
        assertEquals(temperatureFromNoise(-1.0), temperatureFromNoise(-2.0))
        assertEquals(temperatureFromNoise(-2.0), temperatureFromNoise(-1e6))
        assertEquals(temperatureFromNoise(1.0), temperatureFromNoise(2.0))
        assertEquals(temperatureFromNoise(2.0), temperatureFromNoise(1e6))
    }

    @Test
    fun `noise 0 is around 45 degree latitude`() {
        val atZero = temperatureFromNoise(0.0).celsius
        assertTrue(atZero > 10, "expected $atZero to be greater than 10")
        assertTrue(atZero < 14, "expected $atZero to be less than 14")
    }

    @Test
    fun `the whole noise range stays between pole and equator`() {
        for (noise in listOf(-1.0, -0.5, 0.0, 0.5, 1.0)) {
            val t = temperatureFromNoise(noise).celsius
            assertTrue(t >= POLE_C, "noise $noise gave $t, below $POLE_C")
            assertTrue(t <= EQUATOR_C, "noise $noise gave $t, above $EQUATOR_C")
        }
    }

    @Test
    fun `hot noise really is hot`() {
        assertTrue(
            temperatureFromNoise(0.8).celsius > 25.0,
            "a hot climate should read hot: ${temperatureFromNoise(0.8).celsius}",
        )
    }

    @Test
    fun `adjusting for altitude has no effect at or below sea level`() {
        val t = Heat(0.0)
        assertEquals(0.0, t.adjustForAltitude(Altitude(0)).celsius)
        assertEquals(0.0, t.adjustForAltitude(Altitude(-100)).celsius)
    }

    @Test
    fun `a null altitude leaves the temperature alone`() {
        assertEquals(5.0, Heat(5.0).adjustForAltitude(null).celsius)
    }

    @Test
    fun `adjusting for altitude is monotonic above sea level`() {
        val t = Heat(0.0)
        val top = t.adjustForAltitude(Altitude(300)).celsius
        val middle = t.adjustForAltitude(Altitude(150)).celsius
        val bottom = t.adjustForAltitude(Altitude(1)).celsius
        assertTrue(top < middle, "expected $top to be less than $middle")
        assertTrue(middle < bottom, "expected $middle to be less than $bottom")
    }

    @Test
    fun `a big spike in altitude gives a big drop in temperature`() {
        val adjusted = Heat(0.0).adjustForAltitude(Altitude(300)).celsius
        assertTrue(adjusted > -30.0, "expected $adjusted to be greater than -30")
        assertTrue(adjusted < -10.0, "expected $adjusted to be less than -10")
    }

    private val dayTicks = SharedConstants.TICKS_PER_GAME_DAY.toLong()
    private val mean = Heat(10.0)
    private val warmest = Time(WARMEST_TICK)

    private fun Heat.atTime(t: Long, sky: Sky, humidity: Humidity) =
        adjustForTimeOfDay(Time(t), sky, humidity).celsius

    @Test
    fun `the warmest tick is exactly the dry swing above the mean in dry air`() {
        assertEquals(
            mean.celsius + DIURNAL_SWING_DRY,
            mean.adjustForTimeOfDay(warmest, Sky(1.0), dry).celsius,
            1e-9,
        )
    }

    @Test
    fun `half a day after the warmest tick is exactly the dry swing below the mean`() {
        assertEquals(
            mean.celsius - DIURNAL_SWING_DRY,
            mean.atTime(WARMEST_TICK + dayTicks / 2, Sky(1.0), dry),
            1e-9,
        )
    }

    @Test
    fun `time of day adjustment repeats every game day`() {
        val sky = Sky(0.5)
        for (time in listOf(0L, 3000L, WARMEST_TICK, 17500L, 23999L)) {
            val today = mean.atTime(time, sky, dry)
            assertEquals(today, mean.atTime(time + dayTicks, sky, dry), 1e-9, "tick $time")
            assertEquals(today, mean.atTime(time + 7 * dayTicks, sky, dry), 1e-9, "tick $time")
        }
    }

    @Test
    fun `evenly spaced samples across a day average to the mean`() {
        val average = (0 until 24)
            .map { mean.atTime(it * dayTicks / 24, Sky(0.5), dry) }
            .average()
        assertEquals(mean.celsius, average, 1e-9)
    }

    @Test
    fun `the coldest moment is before dawn, not midnight`() {
        val sky = Sky(0.5)
        val sunrise = mean.atTime(0L, sky, dry)
        val noon = mean.atTime(6000L, sky, dry)
        val midnight = mean.atTime(18000L, sky, dry)
        val beforeDawn = mean.atTime(21000L, sky, dry)
        assertTrue(beforeDawn < midnight, "before dawn $beforeDawn vs midnight $midnight")
        assertTrue(beforeDawn < sunrise, "before dawn $beforeDawn vs sunrise $sunrise")
        assertTrue(sunrise < noon, "sunrise $sunrise vs noon $noon")
    }

    @Test
    fun `sky openness scales the swing`() {
        for (sky in listOf(0.25, 0.5, 0.75)) {
            assertEquals(
                mean.celsius + DIURNAL_SWING_DRY * sky,
                mean.atTime(WARMEST_TICK, Sky(sky), dry),
                1e-9,
                "sky $sky",
            )
        }
    }

    @Test
    fun `with no sky there is no diurnal swing`() {
        for (time in listOf(0L, 6000L, WARMEST_TICK, 18000L, 21000L)) {
            assertEquals(mean.celsius, mean.atTime(time, Sky(0.0), dry), 1e-9, "tick $time")
        }
    }

    @Test
    fun `dry air gives the dry swing and humid air the humid swing`() {
        assertEquals(
            mean.celsius + DIURNAL_SWING_DRY,
            mean.atTime(WARMEST_TICK, Sky(1.0), dry),
            1e-9,
        )
        assertEquals(
            mean.celsius + DIURNAL_SWING_HUMID,
            mean.atTime(WARMEST_TICK, Sky(1.0), humid),
            1e-9,
        )
    }

    @Test
    fun `the swing shrinks as humidity rises`() {
        val swings = listOf(0.0, 0.25, 0.5, 0.75, 1.0)
            .map { h -> mean.atTime(WARMEST_TICK, Sky(1.0), Humidity(h)) - mean.celsius }
        for (i in 1 until swings.size) {
            assertTrue(swings[i] < swings[i - 1], "step $i (${swings[i]}) vs ${swings[i - 1]}")
        }
    }

    @Test
    fun `humidity narrows the swing but does not move the mean`() {
        for (h in listOf(0.0, 0.5, 1.0)) {
            val average = (0 until 24)
                .map { mean.atTime(it * dayTicks / 24, Sky(1.0), Humidity(h)) }
                .average()
            assertEquals(mean.celsius, average, 1e-9, "humidity $h")
        }
    }

    @Test
    fun `the diurnal swing alone cannot flip a hot climate cold`() {
        val hot = temperatureFromNoise(0.8)
        for (time in 0 until dayTicks step 500) {
            val t = hot.atTime(time, Sky(1.0), dry)
            assertTrue(t > 0.0, "hot climate read $t at tick $time")
        }
    }
}
