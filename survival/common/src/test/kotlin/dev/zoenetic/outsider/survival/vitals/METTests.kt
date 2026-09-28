package dev.zoenetic.outsider.survival.vitals

import java.util.ArrayDeque
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class METTests {

    private val max = MET(16.0)

    @Test
    fun `one MET is zero capacity`() {
        assertEquals(0.0, MET(1.0).capacity(max), 1e-9)
    }

    @Test
    fun `max MET is full capacity`() {
        assertEquals(1.0, max.capacity(max), 1e-9)
    }

    @Test
    fun `capacity is linear between one and max`() {
        val midpoint = MET(1.0 + (max.value - 1.0) / 2.0)
        assertEquals(0.5, midpoint.capacity(max), 1e-9)
        val quarter = MET(1.0 + (max.value - 1.0) / 4.0)
        assertEquals(0.25, quarter.capacity(max), 1e-9)
    }

    @Test
    fun `below one MET is negative capacity, above max exceeds one`() {
        assertTrue(MET(0.9).capacity(max) < 0.0)
        assertTrue(MET(20.0).capacity(max) > 1.0)
    }

    @Test
    fun `average of an empty accumulator is resting`() {
        assertEquals(1.0, ArrayDeque<MET>().average().value, 1e-9)
    }

    @Test
    fun `average of an accumulator is the arithmetic mean`() {
        val deque = ArrayDeque(listOf(MET(1.0), MET(4.0), MET(7.0)))
        assertEquals(4.0, deque.average().value, 1e-9)
    }
}
