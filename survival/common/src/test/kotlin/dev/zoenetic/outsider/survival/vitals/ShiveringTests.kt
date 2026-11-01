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
    fun `full shivering from the point it saturates down to where it starts failing`() {
        for (celsius in listOf(SHIVER_FULL.celsius, 34.5, SHIVER_FADING.celsius)) {
            assertEquals(1.0, intensity(celsius), 1e-9, "at $celsius")
        }
    }

    @Test
    fun `half way through either ramp is half strength`() {
        assertEquals(0.5, intensity((SHIVER_ONSET.celsius + SHIVER_FULL.celsius) / 2), 1e-9)
        assertEquals(0.5, intensity((SHIVER_FADING.celsius + SHIVER_CEASES.celsius) / 2), 1e-9)
    }

    @Test
    fun `rises while cooling to full strength, then falls`() {
        val cooling = generateSequence(SHIVER_ONSET.celsius) { it - 0.1 }
            .takeWhile { it > SHIVER_CEASES.celsius }
            .map(::intensity)
            .toList()
        val firstFull = cooling.indexOfFirst { it >= 1.0 - 1e-9 }
        val lastFull = cooling.indexOfLast { it >= 1.0 - 1e-9 }
        assertTrue(cooling.take(firstFull + 1).zipWithNext().all { (a, b) -> b >= a })
        assertTrue(cooling.drop(lastFull).zipWithNext().all { (a, b) -> b <= a })
    }
}
