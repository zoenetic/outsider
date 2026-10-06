package dev.zoenetic.outsider.survival.stone.loose

import dev.zoenetic.outsider.survival.stone.loose.LooseStoneArrangements.Box
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneArrangements.forCount
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LooseStoneArrangementsTests {
    private val home = listOf(
        Box(4, 10, 6, 12, 1),
        Box(3, 3, 6, 7, 2),
        Box(11, 11, 14, 13, 2),
        Box(9, 5, 11, 8, 1),
    )

    // Row = shape A..D, column = slot (the home of shape A..D).
    private val grid = listOf(
        listOf(
            Box(4, 10, 6, 12, 1),
            Box(4, 4, 6, 6, 1),
            Box(12, 11, 14, 13, 1),
            Box(9, 6, 11, 8, 1),
        ),
        listOf(
            Box(4, 9, 7, 13, 2),
            Box(3, 3, 6, 7, 2),
            Box(11, 10, 14, 14, 2),
            Box(9, 5, 12, 9, 2),
        ),
        listOf(
            Box(4, 10, 7, 12, 2),
            Box(3, 4, 6, 6, 2),
            Box(11, 11, 14, 13, 2),
            Box(9, 6, 12, 8, 2),
        ),
        listOf(
            Box(4, 10, 6, 13, 1),
            Box(4, 4, 6, 7, 1),
            Box(12, 11, 14, 14, 1),
            Box(9, 5, 11, 8, 1),
        ),
    )

    private fun overlaps(a: Box, b: Box): Boolean =
        a.minX < b.maxX && b.minX < a.maxX && a.minZ < b.maxZ && b.minZ < a.maxZ

    @Test
    fun `each count has every placement of that many shapes`() {
        assertEquals(listOf(16, 72, 96, 24), (1..4).map { forCount(it).size })
    }

    @Test
    fun `every arrangement has as many stones as its count`() {
        for (count in 1..4) {
            assertTrue(forCount(count).all { it.size == count }, "count $count")
        }
    }

    @Test
    fun `a single stone covers every shape in every slot, shape by shape`() {
        val singles = forCount(1).map { it.single() }
        assertEquals(grid.flatten(), singles)
    }

    @Test
    fun `stones in one arrangement never overlap`() {
        for (count in 2..4) {
            for ((index, arrangement) in forCount(count).withIndex()) {
                for (i in arrangement.indices) {
                    for (j in i + 1 until arrangement.size) {
                        assertTrue(
                            !overlaps(arrangement[i], arrangement[j]),
                            "count $count, arrangement $index: ${arrangement[i]} and ${arrangement[j]}",
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `no arrangement repeats`() {
        for (count in 1..4) {
            val arrangements = forCount(count)
            assertEquals(arrangements.size, arrangements.toSet().size, "count $count")
        }
    }

    @Test
    fun `the first full arrangement is the hand-made layout`() {
        assertEquals(home, forCount(4).first())
    }

    // Pinned so any change to the enumeration order fails here: the variant list order in the
    // blockstate and the outline lookup both depend on it.
    @Test
    fun `the enumeration order is stable`() {
        fun at(shape: Int, slot: Int) = grid[shape][slot]
        assertEquals(listOf(at(0, 0), at(2, 2)), forCount(2)[13])
        assertEquals(listOf(at(2, 3), at(3, 2)), forCount(2)[71])
        assertEquals(listOf(at(0, 0), at(2, 2), at(3, 1)), forCount(3)[50])
        assertEquals(listOf(at(0, 3), at(1, 2), at(2, 1), at(3, 0)), forCount(4)[23])
    }

    @Test
    fun `counts outside one to four are refused`() {
        assertFailsWith<IllegalArgumentException> { forCount(0) }
        assertFailsWith<IllegalArgumentException> { forCount(5) }
    }

    @Test
    fun `a box at the block edge is allowed`() {
        val _ = Box(0, 0, 16, 16, 1)
    }
}
