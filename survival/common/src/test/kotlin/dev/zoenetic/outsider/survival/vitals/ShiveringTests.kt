package dev.zoenetic.outsider.survival.vitals

import dev.zoenetic.outsider.survival.units.Heat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShiveringTests {

    private fun intensity(celsius: Double) = shiverIntensity(Heat(celsius))

    @Test
    fun `no shivering at or above onset`() {
        assertEquals(0.0, intensity(SHIVER_ONSET.celsius))
        assertEquals(0.0, intensity(37.0))
    }

    @Test
    fun `no shivering at or below the point it ceases`() {
        assertEquals(0.0, intensity(SHIVER_CEASES.celsius))
        assertEquals(0.0, intensity(30.0))
    }

    @Test
    fun `full shivering at the peak`() {
        assertEquals(1.0, intensity(SHIVER_PEAK.celsius), 1e-9)
    }

    @Test
    fun `half way to either edge is half strength`() {
        val peak = SHIVER_PEAK.celsius
        assertEquals(0.5, intensity((peak + SHIVER_ONSET.celsius) / 2), 1e-9)
        assertEquals(0.5, intensity((peak + SHIVER_CEASES.celsius) / 2), 1e-9)
    }

    @Test
    fun `rises while cooling towards the peak, then falls`() {
        val cooling = generateSequence(SHIVER_ONSET.celsius) { it - 0.1 }
            .takeWhile { it > SHIVER_CEASES.celsius }
            .map(::intensity)
            .toList()
        val peakIndex = cooling.indexOf(cooling.max())
        assertTrue(cooling.take(peakIndex + 1).zipWithNext().all { (a, b) -> b >= a })
        assertTrue(cooling.drop(peakIndex).zipWithNext().all { (a, b) -> b <= a })
    }
}
