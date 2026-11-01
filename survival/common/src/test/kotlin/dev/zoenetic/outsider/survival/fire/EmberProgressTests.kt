package dev.zoenetic.outsider.survival.fire

import dev.zoenetic.outsider.survival.fire.LightingFireState.NursingEmbers
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Time
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmberProgressTests {

    private fun ember(heat: Double) = NursingEmbers(
        heat = Heat(heat),
        diesAt = Time(1_100),
        blown = Duration.NONE,
        blownRequired = Duration(20),
        breathUntil = Time(1_000),
        nextBreathAt = Time(1_000),
        held = false,
        timestamp = Time(1_000),
    )

    @Test
    fun `an ember about to die has made no progress`() {
        assertEquals(0F, emberProgress(ember(250.0)))
    }

    @Test
    fun `an ember hot enough to catch the tinder is all the way there`() {
        assertEquals(1F, emberProgress(ember(600.0)))
    }

    @Test
    fun `progress is clamped outside the dying and catching heats`() {
        assertEquals(0F, emberProgress(ember(100.0)))
        assertEquals(1F, emberProgress(ember(900.0)))
    }

    @Test
    fun `progress rises with heat between them`() {
        val heats = (250..600 step 25).map { emberProgress(ember(it.toDouble())) }
        for ((cooler, hotter) in heats.zipWithNext()) assertTrue(hotter > cooler, "$heats")
        assertEquals(0.5F, emberProgress(ember(425.0)), 1e-6F)
    }
}
