package dev.zoenetic.outsider.survival.fire

import dev.zoenetic.outsider.survival.fire.LightingFireState.Drilling
import dev.zoenetic.outsider.survival.fire.LightingFireState.NursingEmbers
import dev.zoenetic.outsider.survival.fire.LightingFireState.Phase
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Time
import dev.zoenetic.outsider.survival.vitals.BLOWING
import dev.zoenetic.outsider.survival.vitals.HAND_DRILLING
import net.minecraft.world.phys.Vec3
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LightingFireExertionTests {

    private fun drilling(held: Boolean) = Drilling(
        dust = Duration(35),
        dustRequired = Duration(160),
        heat = Heat(212.5),
        held = held,
        timestamp = Time(9_000),
    )

    private fun nursing(held: Boolean) = NursingEmbers(
        heat = Heat(450.0),
        diesAt = Time(9_040),
        blown = Duration(7),
        blownRequired = Duration(20),
        breathUntil = Time(9_060),
        nextBreathAt = Time(8_990),
        held = held,
        timestamp = Time(9_000),
    )

    private fun exertion(phase: Phase) = LightingFireState(Vec3.ZERO, phase).exertion()

    @Test
    fun `drilling is vigorous exertion while held`() {
        assertEquals(HAND_DRILLING, exertion(drilling(held = true)))
    }

    @Test
    fun `blowing on the ember is light exertion while held`() {
        assertEquals(BLOWING, exertion(nursing(held = true)))
    }

    @Test
    fun `resting in any phase is no exertion of its own`() {
        assertNull(exertion(drilling(held = false)))
        assertNull(exertion(nursing(held = false)))
    }
}
