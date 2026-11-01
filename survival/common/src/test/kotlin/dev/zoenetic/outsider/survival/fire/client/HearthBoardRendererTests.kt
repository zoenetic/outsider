package dev.zoenetic.outsider.survival.fire.client

import dev.zoenetic.outsider.survival.fire.LightingFireState.NursingEmbers
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Time
import kotlin.test.Test
import kotlin.test.assertEquals

class HearthBoardRendererTests {

    private val diesAt = Time(1_000)

    private fun nursing(held: Boolean) = NursingEmbers(
        heat = Heat(450.0),
        diesAt = diesAt,
        blown = Duration.NONE,
        blownRequired = Duration(20),
        breathUntil = Time(9_060),
        nextBreathAt = Time(8_990),
        held = held,
        timestamp = Time(960),
    )

    private fun glowWithTicksLeft(ticks: Long, held: Boolean = false): Int =
        HearthBoardRenderer.emberGlow(nursing(held), Time(diesAt.value - ticks))

    @Test
    fun `blowing keeps the ember at full glow, even as it dies`() {
        for (left in listOf(30L, 20L, 10L, 1L, 0L)) assertEquals(15, glowWithTicksLeft(left, held = true))
    }

    @Test
    fun `a fresh ember glows fully until its last second`() {
        assertEquals(15, glowWithTicksLeft(30))
        assertEquals(15, glowWithTicksLeft(20))
    }

    @Test
    fun `an ember left alone fades over its last second`() {
        assertEquals(8, glowWithTicksLeft(10))
        assertEquals(1, glowWithTicksLeft(1))
        assertEquals(0, glowWithTicksLeft(0))
    }

    @Test
    fun `the fade never brightens as the ember dies`() {
        val glows = (20L downTo 0L).map { glowWithTicksLeft(it) }
        for ((earlier, later) in glows.zipWithNext()) assert(later <= earlier) { "$glows" }
    }

    @Test
    fun `a dead ember stays dark`() {
        assertEquals(0, glowWithTicksLeft(-5))
    }
}
