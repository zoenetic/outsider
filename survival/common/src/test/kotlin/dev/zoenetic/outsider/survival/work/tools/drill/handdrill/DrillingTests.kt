package dev.zoenetic.outsider.survival.work.tools.drill.handdrill

import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Time
import dev.zoenetic.outsider.survival.work.tools.drill.handdrill.LightingFireState.Drilling
import net.minecraft.world.InteractionHand
import kotlin.test.Test
import kotlin.test.assertEquals

class DrillingTests {

    private val conditions = DrillingConditions(
        ambient = Heat(20.0),
        risePerTick = Heat(4.0),
        halfLife = Duration(40),
        ignition = Heat(400.0),
    )

    private val recordedAt = Time(1_000)

    private fun drilling(held: Boolean, dust: Long = 0, heat: Double = 20.0) = Drilling(
        InteractionHand.MAIN_HAND,
        Duration(dust),
        Duration(200),
        Heat(heat),
        held,
        recordedAt,
    )

    private fun later(ticks: Long) = recordedAt + Duration(ticks)

    @Test
    fun `held dust grows by one for every tick since the press`() {
        assertEquals(Duration(65), drilling(held = true, dust = 50).dustAt(later(15)))
    }

    @Test
    fun `resting dust stays banked`() {
        assertEquals(Duration(50), drilling(held = false, dust = 50).dustAt(later(4_000)))
    }

    @Test
    fun `held heat rises at the conditions' rate`() {
        assertEquals(Heat(100.0), drilling(held = true).heatAt(later(20), conditions))
    }

    @Test
    fun `held heat stops at the ignition point`() {
        assertEquals(
            Heat(400.0),
            drilling(held = true, heat = 300.0).heatAt(later(100), conditions),
        )
    }

    @Test
    fun `resting heat closes half the gap to ambient every half-life`() {
        val resting = drilling(held = false, heat = 400.0)
        assertEquals(210.0, resting.heatAt(later(40), conditions).celsius, 1e-9)
        assertEquals(115.0, resting.heatAt(later(80), conditions).celsius, 1e-9)
    }

    @Test
    fun `resting heat at ambient stays at ambient`() {
        assertEquals(Heat(20.0), drilling(held = false).heatAt(later(1_000), conditions))
    }

    @Test
    fun `resting heat below ambient warms towards it`() {
        assertEquals(10.0, drilling(held = false, heat = 0.0).heatAt(later(40), conditions).celsius)
    }

    @Test
    fun `a press after a rest climbs from the cooled heat`() {
        val rested = drilling(held = false, heat = 400.0)
        val pressedAt = later(40)
        val pressed = rested.copy(
            heat = rested.heatAt(pressedAt, conditions),
            held = true,
            timestamp = pressedAt,
        )
        assertEquals(250.0, pressed.heatAt(pressedAt + Duration(10), conditions).celsius, 1e-9)
    }
}
