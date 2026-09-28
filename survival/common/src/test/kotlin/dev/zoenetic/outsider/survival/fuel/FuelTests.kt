package dev.zoenetic.outsider.survival.fuel

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelTests {

    @Test
    fun `adding past the maximum clamps instead of throwing`() {
        assertEquals(Fuel.MAX, Fuel(12) + Fuel(8))
    }

    @Test
    fun `adding within range is plain addition`() {
        assertEquals(Fuel(7), Fuel(3) + Fuel(4))
    }
}
