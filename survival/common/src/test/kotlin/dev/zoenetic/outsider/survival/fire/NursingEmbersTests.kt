package dev.zoenetic.outsider.survival.fire

import dev.zoenetic.outsider.survival.fire.LightingFireState.NursingEmbers
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Time
import kotlin.test.Test
import kotlin.test.assertEquals

class NursingEmbersTests {

    private val recordedAt = Time(1_000)

    private fun nursing(held: Boolean, blown: Long = 0) = NursingEmbers(
        diesAt = recordedAt + Duration(100),
        blown = Duration(blown),
        blownRequired = Duration(20),
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
}
