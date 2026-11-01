package dev.zoenetic.outsider.survival.vitals

import dev.zoenetic.outsider.survival.units.Conductance
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Insulation
import dev.zoenetic.outsider.survival.vitals.BodyTemperature.Companion.rate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContactTests {

    private val warmAir = Contact(Heat(25.0), Conductance(1.0))

    private fun water(ground: Double) = Contact(
        ConductiveMedium.WATER.temperature(air = Heat(25.0), ground = Heat(ground)),
        ConductiveMedium.WATER.conductance,
    )

    @Test
    fun `air alone is its own environment`() {
        assertEquals(25.0, listOf(warmAir).environment().celsius, 1e-9)
    }

    @Test
    fun `the environment is the conductance-weighted mean of what you touch`() {
        val contacts = listOf(
            Contact(Heat(20.0), Conductance(1.0)),
            Contact(Heat(8.0), Conductance(3.0)),
        )
        assertEquals((20.0 + 3.0 * 8.0) / 4.0, contacts.environment().celsius, 1e-9)
        assertEquals(4.0, contacts.conductance().value, 1e-9)
    }

    private fun List<Contact>.rateFor(body: Heat): Double =
        rate(body, environment(), conductance(), Insulation.NONE, MET(1.0))

    @Test
    fun `cold water chills you on a warm day`() {
        val swimming = listOf(warmAir, water(ground = 12.0))
        val midway = (25.0 + 12.0) / 2.0
        assertTrue(swimming.environment().celsius < midway, "the river should outweigh the air")
        assertTrue(swimming.rateFor(NORMAL_BODY_TEMPERATURE) < 0.0)
        assertTrue(listOf(warmAir).rateFor(NORMAL_BODY_TEMPERATURE) >= 0.0)
    }

    @Test
    fun `a hot spring warms a cold body even in freezing air`() {
        val freezingAir = Contact(Heat(-10.0), Conductance(1.0))
        val bath = listOf(freezingAir, water(ground = 38.0))
        assertTrue(bath.rateFor(Heat(34.0)) > 0.0)
        assertTrue(listOf(freezingAir).rateFor(Heat(34.0)) < 0.0)
    }

    @Test
    fun `liquid water is never below freezing`() {
        assertEquals(0.0, water(ground = -15.0).temperature.celsius, 1e-9)
    }

    @Test
    fun `water ignores the air and follows the ground`() {
        assertEquals(12.0, water(ground = 12.0).temperature.celsius, 1e-9)
    }

    @Test
    fun `rain is at air temperature`() {
        assertEquals(
            7.0,
            ConductiveMedium.RAIN.temperature(air = Heat(7.0), ground = Heat(15.0)).celsius,
            1e-9,
        )
    }

    @Test
    fun `snow and ice never warm above freezing`() {
        for (surface in listOf(ConductiveSurface.SNOW, ConductiveSurface.ICE)) {
            val t = surface.temperature(air = Heat(20.0), ground = Heat(10.0))
            assertEquals(0.0, t.celsius, 1e-9, "$surface")
        }
    }

    @Test
    fun `magma warms you on a cold night`() {
        val night = Contact(Heat(-5.0), Conductance(1.0))
        val magma = ConductiveSurface.MAGMA
        val onMagma = listOf(
            night,
            Contact(magma.temperature(air = Heat(-5.0), ground = Heat(-5.0)), magma.conductance),
        )
        assertTrue(onMagma.environment() > night.temperature)
    }
}
